package com.thizthizzydizzy.treefeller.core.detection.tree;
import com.thizthizzydizzy.treefeller.lib.it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import com.thizthizzydizzy.treefeller.lib.it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
public class TreeTree{
    public TreeNode root;
    private final Long2ObjectOpenHashMap<TreeNode> nodeMap = new Long2ObjectOpenHashMap<>();
    private final Int2ObjectOpenHashMap<List<TreeNode>> sections = new Int2ObjectOpenHashMap<>();
    public TreeTree(long rootPos){
        root = new TreeNode(rootPos, null, 0);
        registerNode(root);
    }
    public void addNode(TreeNode node){
        registerNode(node);
        if(node.parent!=null){
            node.parent.addChild(node);
        }
    }
    private void registerNode(TreeNode node){
        nodeMap.put(node.pos, node);
        sections.computeIfAbsent(node.sectionId, k -> new ArrayList<>()).add(node);
    }
}
