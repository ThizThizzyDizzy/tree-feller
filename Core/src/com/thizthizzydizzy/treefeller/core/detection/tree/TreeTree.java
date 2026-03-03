package com.thizthizzydizzy.treefeller.core.detection.tree;
import com.thizthizzydizzy.treefeller.lib.it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import com.thizthizzydizzy.treefeller.lib.it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import com.thizthizzydizzy.treefeller.lib.it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.EnumMap;
import java.util.function.Predicate;
public class TreeTree{
    public TreeNode root;
    private final Long2ObjectOpenHashMap<TreeNode> nodeMap = new Long2ObjectOpenHashMap<>();
    private final Int2ObjectOpenHashMap<EnumMap<TreeNodeType, ReferenceArrayList<ReferenceArrayList<TreeNode>>>> sections = new Int2ObjectOpenHashMap<>();
    public TreeTree(long rootPos){
        root = new TreeNode(rootPos, null, 0);
    }
    public void addNode(TreeNode node){
        registerNode(node);
        if(node.parent!=null){
            node.parent.addChild(node);
        }
    }
    private void registerNode(TreeNode node){
        nodeMap.put(node.pos, node);
        if(node.type!=null){
            ReferenceArrayList<ReferenceArrayList<TreeNode>> distanceIndexes = sections.computeIfAbsent(node.sectionId, section -> new EnumMap<>(TreeNodeType.class))
                .computeIfAbsent(node.type, type -> new ReferenceArrayList<>());
            ReferenceArrayList<TreeNode> distanceIndex = null;
            if(node.distance>distanceIndexes.size()){
                distanceIndex = new ReferenceArrayList<>();
                distanceIndexes.add(distanceIndex);
            }else
                distanceIndex = distanceIndexes.get(node.distance);
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
        EnumMap<TreeNodeType, ReferenceArrayList<ReferenceArrayList<TreeNode>>> section = sections.get(sectionId);
        ReferenceArrayList<ReferenceArrayList<TreeNode>> depthMap = section.get(type);
        int sectionDepth = 0;
        for(int depth = 0; depth<depthMap.size(); depth++){
            if(!depthMap.get(depth).stream().anyMatch((node) -> node.type==type))
                continue;
            if(depth>sectionDepth)sectionDepth = depth;
        }
        return sectionDepth;
    }
    public void rebase(TreeNode newRoot){
        if(root==newRoot)return;
        newRoot.parent = null;
        newRoot.distance = 0;
        newRoot.children.clear();
        nodeMap.clear();
        sections.clear();
        registerNode(root = newRoot);
    }
    public boolean isEmpty(){
        return nodeMap.isEmpty();
    }
}
