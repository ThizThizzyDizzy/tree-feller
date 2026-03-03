package com.thizthizzydizzy.treefeller.core.detection.tree;
import java.util.ArrayList;
import java.util.List;
public class TreeNode{
    public TreeNodeType type = TreeNodeType.NONE;
    public final long pos;
    public TreeNode parent;
    public List<TreeNode> children = null;
    public int sectionId;
    public int distance;
    public TreeNode(long pos, TreeNode parent, int sectionId){
        this.pos = pos;
        this.parent = parent;
        this.sectionId = sectionId;
    }
    public void setType(TreeNodeType type){
        distance = parent==null?0:(type==parent.type?parent.distance+1:1);
    }
    public void addChild(TreeNode child){
        if(children==null){
            children = new ArrayList<>(4);
        }
        children.add(child);
    }
    public void removeChild(TreeNode child){
        if(children!=null)children.remove(child);
    }
}
