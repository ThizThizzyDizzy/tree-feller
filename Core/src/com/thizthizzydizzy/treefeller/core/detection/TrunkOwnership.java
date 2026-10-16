package com.thizthizzydizzy.treefeller.core.detection;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
import com.thizthizzydizzy.treefeller.core.debug.DebuggerContext;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.DetectionConfiguration;
import com.thizthizzydizzy.treefeller.core.connector.world.IWorldConnector;
import java.util.*;
import java.util.function.LongConsumer;
import java.util.function.IntPredicate;
/** Dense log cores and nearest-core ownership, using ordinary neighbor flood fills. */
final class TrunkOwnership{
    private TrunkOwnership(){}
    static List<TreeTree> split(DebuggerContext context, TreeTree tree, IWorldConnector world, TreeConfiguration definition, DetectionConfiguration config, IntPredicate validSize){
        int sectionId=0;
        // Leaf-connected sections already form a deliberate composite tree. Keep
        // that scan intact rather than splitting a section away from its support.
        if(tree.getNodes(TreeNodeType.TRUNK,-1,-1).stream().anyMatch(n -> n.sectionId!=0))return Collections.singletonList(tree);
        java.util.function.BiConsumer<Long,LongConsumer> neighbors=(pos,visit) -> TreeScanner.forEachTrunkNeighbor(pos,n -> {
            if(TreeScanner.connectsTrunk(context,world,tree,pos,n,definition.trunk,config))visit.accept(n);
        });
        List<TreeNode> trunks=tree.getNodes(TreeNodeType.TRUNK,sectionId,-1);
        if(trunks.isEmpty())return Collections.singletonList(tree);
        Map<Long,TreeNode> logs=new HashMap<>();
        TreeNode start=null;
        for(TreeNode node:trunks){
            logs.put(node.pos,node);
            if(node.parent==null||node.parent.sectionId!=sectionId||node.parent.type!=TreeNodeType.TRUNK)start=node;
        }
        if(start==null)return Collections.singletonList(tree);
        // A solid 2x2x2 cube is the smallest dense core. Thin branches and roots
        // may connect cores, but cannot seed a separate tree themselves.
        Set<Long> cores=new HashSet<>();
        for(long pos:logs.keySet()){
            boolean solid=true;
            for(int x=0;x<2;x++)for(int y=0;y<2;y++)for(int z=0;z<2;z++)
                solid&=logs.containsKey(BlockPos.getRelative(pos,x,y,z));
            if(solid)for(int x=0;x<2;x++)for(int y=0;y<2;y++)for(int z=0;z<2;z++)
                cores.add(BlockPos.getRelative(pos,x,y,z));
        }
        // Stable source order makes equidistant bridge ownership independent of the cut.
        List<Long> ordered=new ArrayList<>(cores);
        ordered.sort(Long::compare);
        Map<Long,Integer> owners=new HashMap<>();
        ArrayDeque<Long> wave=new ArrayDeque<>();
        List<Integer> bottoms=new ArrayList<>();
        for(long pos:ordered){
            if(!cores.remove(pos))continue;
            int owner=bottoms.size(), bottom=BlockPos.getY(pos);
            ArrayDeque<Long> queue=new ArrayDeque<>();
            queue.add(pos);
            while(!queue.isEmpty()){
                long next=queue.remove();
                bottom=Math.min(bottom,BlockPos.getY(next));
                owners.put(next,owner);
                wave.add(next);
                neighbors.accept(next,n -> {if(cores.remove(n))queue.add(n);});
            }
            bottoms.add(bottom);
        }
        int bases=bottoms.size();
        if(bases<2)return Collections.singletonList(tree);
        while(!wave.isEmpty()){
            long pos=wave.remove();
            int owner=owners.get(pos);
            neighbors.accept(pos,n -> {
                if(logs.containsKey(n)&&!owners.containsKey(n)){
                    owners.put(n,owner);
                    wave.add(n);
                }
            });
        }
        int[] groups=new int[bases];
        for(int i=0;i<bases;i++)groups[i]=i;
        // A connection entering the bottom cube of either core is a root, low
        // fork, or continuation of a bent trunk, rather than a canopy bridge.
        // Compare each core at its own elevation; there is no common ground plane.
        for(long pos:logs.keySet()){
            int owner=owners.get(pos);
            neighbors.accept(pos,n -> {
                Integer other=owners.get(n);
                if(other==null||other==owner)return;
                int joinY=Math.max(BlockPos.getY(pos),BlockPos.getY(n));
                if(joinY<=bottoms.get(owner)+1||joinY<=bottoms.get(other)+1)
                    groups[group(groups,owner)]=group(groups,other);
            });
        }
        int selected=group(groups,owners.get(start.pos));
        if(java.util.stream.IntStream.range(0,bases).allMatch(i -> group(groups,i)==selected))return Collections.singletonList(tree);
        Map<Integer,Integer> sizes=new HashMap<>();
        for(int owner:owners.values())sizes.merge(group(groups,owner),1,Integer::sum);
        // Do not leave an intact core that is too small/large to qualify on its own.
        if(sizes.values().stream().anyMatch(size -> !validSize.test(size)))return Collections.singletonList(tree);
        final long selectedPos=start.pos;
        Map<Integer,TreeTree> forest=new TreeMap<>();
        // Lowest packed log position orders both the sources and equidistant claims.
        List<Long> positions=new ArrayList<>(logs.keySet());
        positions.sort(Long::compare);
        for(long pos:positions){
            int owner=group(groups,owners.get(pos));
            forest.computeIfAbsent(owner,g -> new TreeTree(g==selected?selectedPos:pos,tree.isSecondary()));
        }
        for(TreeTree part:forest.values()){
            TreeNode root=part.root;
            root.setType(TreeNodeType.TRUNK);
            part.addNode(root);
            Set<Long> visited=new HashSet<>();
            ArrayDeque<TreeNode> queue=new ArrayDeque<>();
            visited.add(root.pos);
            queue.add(root);
            int owner=group(groups,owners.get(root.pos));
            while(!queue.isEmpty()){
                TreeNode node=queue.remove();
                neighbors.accept(node.pos,n -> {
                    if(logs.containsKey(n)&&group(groups,owners.get(n))==owner&&visited.add(n)){
                        TreeNode child=new TreeNode(n,node);
                        child.setType(TreeNodeType.TRUNK);
                        part.addNode(child);
                        queue.add(child);
                    }
                });
            }
            if(visited.size()!=sizes.get(owner))return Collections.singletonList(tree);
        }
        List<TreeTree> result=new ArrayList<>(forest.values());
        result.sort(Comparator.comparingLong(t -> t.nodeMap.keySet().longStream().min().getAsLong()));
        context.summary("Partitioned connected logs into "+result.size()+" trees");
        return result;
    }
    private static int group(int[] groups,int id){
        while(groups[id]!=id){groups[id]=groups[groups[id]];id=groups[id];}
        return id;
    }
}
