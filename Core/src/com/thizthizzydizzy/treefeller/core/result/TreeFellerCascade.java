package com.thizthizzydizzy.treefeller.core.result;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.config.structure.*;
import com.thizthizzydizzy.treefeller.core.config.structure.section.DetectionConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IBlockDefinition;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import com.thizthizzydizzy.treefeller.core.connector.world.*;
import com.thizthizzydizzy.treefeller.core.cutting.TreeFellerCutting;
import com.thizthizzydizzy.treefeller.core.detection.*;
import com.thizthizzydizzy.treefeller.core.trigger.TreeFellerTrigger;
import com.thizthizzydizzy.treefeller.core.debug.*;
import java.util.*;
/** Server-thread cascade work. No platform state or mutable global "cascading" flag. */
public final class TreeFellerCascade{
    private static final int[][] DIRECT = {{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}};
    private static final ArrayDeque<Search> pending = new ArrayDeque<>();
    private static boolean scheduled;
    private static final Map<TreeFellerConfiguration, Integer> active = new IdentityHashMap<>();
    private TreeFellerCascade(){}
    public static final class Chain{
        final Set<Long> claimed = new HashSet<>();
    }
    /** Capture external leaf boundaries before the selected blocks disappear. */
    public static Runnable enqueue(IWorldConnector world, IPlayerConnector player, DetectionResult detected,
            List<TreeNode> nodes, Collection<Long> sourceTrunks, Chain chain){
        DebuggerContext context=TreeFellerDebugger.begin(DebugPhase.CASCADE,player,world);
        context.values("Cascade settings",TreeFellerConfiguration.getCombinedResultConfiguration(detected.tree,detected.tool).cascade);
        if(chain==null)chain = new Chain();
        chain.claimed.addAll(sourceTrunks);
        for(TreeNode node : nodes)chain.claimed.add(node.pos);
        boolean enabled = Boolean.TRUE.equals(TreeFellerConfiguration.getCombinedResultConfiguration(detected.tree, detected.tool).cascade.enable);
        TreeFellerConfiguration owner = TreeFellerCore.config;
        active.merge(owner, 1, Integer::sum);
        Runnable complete = new Runnable(){
            private boolean done;
            @Override public void run(){
                if(done)return;
                done = true;
                int remaining = active.getOrDefault(owner, 1)-1;
                if(remaining<=0)active.remove(owner);else active.put(owner, remaining);
            }
        };
        if(!enabled){context.summary("Cascade disabled");return complete;}
        context.user("Searching for connected trees");
        context.mark(DebugLevel.USER,detected.detected.root.pos,OverlayMark.CASCADE);
        List<TreeConfiguration> targets = new ArrayList<>();
        if(detected.tree.cascade_trees==null)targets.add(detected.tree);
        else for(int index : detected.tree.cascade_trees){
            if(index>=0&&index<TreeFellerCore.config.trees.length&&!targets.contains(TreeFellerCore.config.trees[index]))
                targets.add(TreeFellerCore.config.trees[index]);
        }
        if(targets.isEmpty()){context.summary("No cascade target species");return complete;}
        Set<Long> boundaries = new LinkedHashSet<>();
        for(TreeNode node : nodes)for(int[] dir : DIRECT){
            long pos = BlockPos.getRelative(node.pos, dir[0], dir[1], dir[2]);
            if(!chain.claimed.contains(pos))boundaries.add(pos);
        }
        for(long pos : boundaries){
            // Air/decorations cannot connect trees. Queue only intact external leaves.
            if(targets.stream().anyMatch(tree -> matches(world, pos, tree.leaves))){
                pending.add(new Search(world, player, targets, pos, chain));context.decision(DebugLevel.DETAIL,pos,OverlayMark.CASCADE,"Queued external leaf boundary");
            }else context.trace("No cascade leaves at "+DebuggerContext.position(pos));
        }
        schedule();
        return complete;
    }
    private static void schedule(){
        if(scheduled||pending.isEmpty())return;
        scheduled = true;
        TreeFellerCore.connector.schedule(1, TreeFellerCascade::tick);
    }
    private static void tick(){
        scheduled = false;
        int checks = 0, felled = 0;
        int checkLimit = Math.max(1, TreeFellerCore.config.global.result.cascade.cascade_check_limit);
        int fellLimit = Math.max(1, TreeFellerCore.config.global.result.cascade.parallel_cascade_limit);
        while(!pending.isEmpty()&&checks<checkLimit&&felled<fellLimit&&active.getOrDefault(TreeFellerCore.config, 0)<fellLimit){
            Search search = pending.peek();
            if(search.config!=TreeFellerCore.config){pending.remove();continue;}
            if(search.candidates==null){search.scan();checks++;}
            if(search.candidates.isEmpty()){pending.remove();continue;}
            search.context.summary("Cascade budgets: checks="+checks+"/"+checkLimit+", felled="+felled+"/"+fellLimit+", active="+active.getOrDefault(TreeFellerCore.config,0));
            long pos = search.candidates.remove();
            if(search.chain.claimed.contains(pos))continue;
            DetectionResult detected = TreeFellerTrigger.detect(search.player, search.world, pos, false, search.targets, true);
            if(detected!=null&&TreeFellerCutting.cut(search.world, search.player, detected, search.chain)){felled++;search.context.decision(DebugLevel.USER,pos,OverlayMark.CASCADE,"Felled connected tree");}
            else search.context.decision(DebugLevel.DETAIL,pos,OverlayMark.REJECTED,"Cascade tree detection or cutting refused");
        }
        schedule();
    }
    private static boolean matches(IWorldConnector world, long pos, IBlockDefinition[] blocks){
        if(blocks!=null)for(IBlockDefinition block : blocks)if(world.matches(pos, block))return true;
        return false;
    }
    private static final class Search{
        final DebuggerContext context;
        final IWorldConnector world;
        final IPlayerConnector player;
        final TreeFellerConfiguration config = TreeFellerCore.config;
        final List<TreeConfiguration> targets;
        final long start;
        final Chain chain;
        ArrayDeque<Long> candidates;
        Search(IWorldConnector world, IPlayerConnector player, List<TreeConfiguration> targets, long start, Chain chain){
            context=TreeFellerDebugger.begin(DebugPhase.CASCADE,player,world);
            this.world=world;this.player=player;this.targets=targets;this.start=start;this.chain=chain;
        }
        void scan(){
            Set<Long> trunks = new LinkedHashSet<>();
            for(TreeConfiguration tree : targets){
                DetectionConfiguration detection = TreeFellerConfiguration.getCombinedDetectionConfiguration(tree);
                ArrayDeque<Long> leaves = new ArrayDeque<>();
                Map<Long,Integer> depths = new HashMap<>();
                if(!matches(world,start,tree.leaves))continue;
                leaves.add(start);depths.put(start,0);
                while(!leaves.isEmpty()){
                    long leaf = leaves.remove();
                    context.decision(DebugLevel.TRACE,leaf,OverlayMark.CANDIDATE,"Cascade search depth="+depths.get(leaf));
                    int depth = depths.get(leaf);
                    for(int[] dir : DIRECT){
                        long pos = BlockPos.getRelative(leaf,dir[0],dir[1],dir[2]);
                        if(!chain.claimed.contains(pos)&&matches(world,pos,tree.trunk))trunks.add(pos);
                    }
                    if(depth>=detection.leaf_detect_range||depths.size()>=64)continue;
                    for(int dx=-1;dx<=1;dx++)for(int dy=-1;dy<=1;dy++)for(int dz=-1;dz<=1;dz++){
                        int distance = Math.abs(dx)+Math.abs(dy)+Math.abs(dz);
                        if(distance==0||(!detection.diagonal_leaves&&distance!=1))continue;
                        long pos = BlockPos.getRelative(leaf,dx,dy,dz);
                        if(depths.containsKey(pos)||chain.claimed.contains(pos)||!matches(world,pos,tree.leaves))continue;
                        int previous = world.getLeafDistance(leaf), next = world.getLeafDistance(pos);
                        // v1 walks toward the neighboring trunk, reversing ordinary leaf detection.
                        if(detection.block_data_rules.use_leaf_distance&&previous!=-1&&next!=-1&&next>=previous)continue;
                        if(depths.size()>=64)break;
                        depths.put(pos,depth+1);leaves.add(pos);
                    }
                }
            }
            candidates = new ArrayDeque<>(trunks);
            context.summary("Cascade found "+trunks.size()+" trunk candidates");
            for(long pos : trunks)context.mark(DebugLevel.DETAIL,pos,OverlayMark.CASCADE);
        }
    }
}
