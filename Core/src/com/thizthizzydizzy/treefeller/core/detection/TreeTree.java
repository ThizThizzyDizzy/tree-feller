package com.thizthizzydizzy.treefeller.core.detection;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
import com.thizthizzydizzy.treefeller.lib.it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import com.thizthizzydizzy.treefeller.lib.it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import com.thizthizzydizzy.treefeller.lib.it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.EnumMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
public class TreeTree{
    public TreeNode root;
    public final Long2ObjectOpenHashMap<TreeNode> nodeMap = new Long2ObjectOpenHashMap<>();
    private final Int2ObjectOpenHashMap<EnumMap<TreeNodeType, ReferenceArrayList<ReferenceArrayList<TreeNode>>>> sections = new Int2ObjectOpenHashMap<>();
    private final boolean secondary;
    public TreeTree(long rootPos, boolean secondary){
        root = new TreeNode(rootPos, null);
        this.secondary = secondary;
    }
    void addNode(TreeNode node){
        registerNode(node);
        if(node.parent!=null){
            node.parent.addChild(node);
        }
    }
    private void registerNode(TreeNode node){
        node.secondary = secondary;
        if(nodeMap.containsKey(node.pos))
            throw new IllegalStateException("Tried to register the same node twice! "+BlockPos.getX(node.pos)+" "+BlockPos.getY(node.pos)+" "+BlockPos.getZ(node.pos));
        nodeMap.put(node.pos, node);
        if(node.type!=null){
            ReferenceArrayList<ReferenceArrayList<TreeNode>> distanceIndexes = sections.computeIfAbsent(node.sectionId, section -> new EnumMap<>(TreeNodeType.class))
                .computeIfAbsent(node.type, type -> new ReferenceArrayList<>());
            ReferenceArrayList<TreeNode> distanceIndex = node.distance<distanceIndexes.size()?distanceIndexes.get(node.distance):null;
            while(node.distance>=distanceIndexes.size()){
                distanceIndex = new ReferenceArrayList<>();
                distanceIndexes.add(distanceIndex);
            }
            distanceIndex.add(node);
        }
    }
    private void unregisterNode(TreeNode node){
        if(!nodeMap.containsKey(node.pos))
            throw new IllegalStateException("Tried to unregister a node that is not registered! "+BlockPos.getX(node.pos)+" "+BlockPos.getY(node.pos)+" "+BlockPos.getZ(node.pos));
        nodeMap.remove(node.pos);
        if(node.type!=null){
            EnumMap<TreeNodeType, ReferenceArrayList<ReferenceArrayList<TreeNode>>> typeMap = sections.get(node.sectionId);
            if(typeMap==null)return;
            ReferenceArrayList<ReferenceArrayList<TreeNode>> distanceIndexes = typeMap.get(node.type);
            if(distanceIndexes==null)return;
            ReferenceArrayList<TreeNode> distanceIndex = node.distance<distanceIndexes.size()?distanceIndexes.get(node.distance):null;
            if(distanceIndex==null)return;
            distanceIndex.remove(node);
        }
    }
    public TreeNode findAnyNode(Predicate<TreeNode> filter){
        return nodeMap.values().stream().filter(filter).findAny().orElse(null);
    }
    int getMinimumScanDepth(TreeNodeType type){
        int minDepth = -1;
        for(int id : sections.keySet()){
            int sectionDepth = getScanDepth(type, id);
            if(minDepth==-1||sectionDepth<minDepth)minDepth = sectionDepth;
        }
        return minDepth;
    }
    int getScanDepth(TreeNodeType type, int sectionId){
        if(sectionId==-1)return getMinimumScanDepth(type);
        EnumMap<TreeNodeType, ReferenceArrayList<ReferenceArrayList<TreeNode>>> section = sections.computeIfAbsent(sectionId, (s) -> new EnumMap<>(TreeNodeType.class));
        ReferenceArrayList<ReferenceArrayList<TreeNode>> depthMap = section.computeIfAbsent(type, (t) -> new ReferenceArrayList<>());
        int sectionDepth = 0;
        for(int depth = 0; depth<depthMap.size(); depth++){
            if(!depthMap.get(depth).stream().anyMatch((node) -> node.type==type))
                continue;
            sectionDepth = depth;
        }
        return sectionDepth;
    }
    public ReferenceArrayList<TreeNode> getNodes(TreeNodeType type, int sectionId, int depth){
        ReferenceArrayList<TreeNode> allNodes = new ReferenceArrayList<>();
        for(int sid : sections.keySet()){
            if(sectionId==-1||sid==sectionId){
                EnumMap<TreeNodeType, ReferenceArrayList<ReferenceArrayList<TreeNode>>> section = sections.get(sid);
                ReferenceArrayList<ReferenceArrayList<TreeNode>> depthMap = section.computeIfAbsent(type, (t) -> new ReferenceArrayList<>());
                for(int d = 0; d<depthMap.size(); d++){
                    if(depth==-1||d==depth){
                        allNodes.addAll(depthMap.get(d));
                    }
                }
            }
        }
        return allNodes;
    }
    public void rebase(TreeNode newRoot){
        if(newRoot==null)
            throw new IllegalArgumentException("Cannot rebase around a null root!");
        if(root==newRoot)return;
        newRoot.parent = null;
        newRoot.distance = 1;
        if(newRoot.children!=null)newRoot.children.clear();
        nodeMap.clear();
        sections.clear();
        registerNode(root = newRoot);
    }
    public boolean isEmpty(){
        return nodeMap.isEmpty();
    }
    public boolean contains(long pos){
        return nodeMap.containsKey(pos);
    }
    public int nextSectionId(){
        int maxId = 0;
        for(int i : sections.keySet())if(i>maxId)maxId = i;
        return maxId+1;
    }
    public int trim(TreeNode node){
        if(!nodeMap.containsKey(node.pos))return 0;
        reclassify(node, n -> {
            n.type = TreeNodeType.NONE;
        });
        int n = 1;
        if(node.children!=null){
            for(TreeNode child : node.children){
                n += trim(child);
            }
        }
        return n;
    }
    public void reclassify(TreeNode node, Consumer<TreeNode> mutator){
        unregisterNode(node);
        mutator.accept(node);
        registerNode(node);
    }
    public int count(TreeNodeType type){
        int count = 0;
        for(int sid : sections.keySet()){
            EnumMap<TreeNodeType, ReferenceArrayList<ReferenceArrayList<TreeNode>>> section = sections.get(sid);
            ReferenceArrayList<ReferenceArrayList<TreeNode>> depthMap = section.computeIfAbsent(type, (t) -> new ReferenceArrayList<>());
            for(int d = 0; d<depthMap.size(); d++){
                count += depthMap.get(d).size();
            }
        }
        return count;
    }
    public void walk(Predicate<TreeNode> visit){
        root.walk(visit);
    }
}
