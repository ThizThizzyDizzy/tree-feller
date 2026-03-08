package com.thizthizzydizzy.treefeller.core.detection.tree;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
import com.thizthizzydizzy.treefeller.lib.it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import com.thizthizzydizzy.treefeller.lib.it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import com.thizthizzydizzy.treefeller.lib.it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.EnumMap;
import java.util.function.Predicate;
public class TreeTree{
    public TreeNode root;
    public final Long2ObjectOpenHashMap<TreeNode> nodeMap = new Long2ObjectOpenHashMap<>();
    private final Int2ObjectOpenHashMap<EnumMap<TreeNodeType, ReferenceArrayList<ReferenceArrayList<TreeNode>>>> sections = new Int2ObjectOpenHashMap<>();
    public TreeTree(long rootPos){
        root = new TreeNode(rootPos, null);
    }
    public void addNode(TreeNode node){
        registerNode(node);
        if(node.parent!=null){
            node.parent.addChild(node);
        }
    }
    private void registerNode(TreeNode node){
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
    public TreeNode findAnyNode(Predicate<TreeNode> filter){
        return nodeMap.values().stream().filter(filter).findAny().orElse(null);
    }
    public int getMinimumScanDepth(TreeNodeType type){
        int minDepth = -1;
        for(int id : sections.keySet()){
            int sectionDepth = getScanDepth(type, id);
            if(minDepth==-1||sectionDepth<minDepth)minDepth = sectionDepth;
        }
        return minDepth;
    }
    public int getScanDepth(TreeNodeType type, int sectionId){
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
}
