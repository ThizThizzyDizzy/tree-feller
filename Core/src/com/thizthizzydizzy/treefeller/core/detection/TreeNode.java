package com.thizthizzydizzy.treefeller.core.detection;
import com.thizthizzydizzy.treefeller.core.config.structure.section.detection.DecorationConfiguration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
public class TreeNode{
    public TreeNodeType type = TreeNodeType.NONE;
    public final long pos;
    public TreeNode parent;
    public List<TreeNode> children = null;
    public int sectionId;
    public int distance;
    public int extendedDistance;
    public boolean secondary;
    public DecorationConfiguration decoration;
    public TreeNode(long pos, TreeNode parent){
        this(pos, parent, parent==null?0:parent.sectionId);
    }
    public TreeNode(long pos, TreeNode parent, int sectionId){
        this.pos = pos;
        this.parent = parent;
        this.sectionId = sectionId;
    }
    public void setType(TreeNodeType type){
        this.type = type;
        distance = parent!=null&&type==parent.type?parent.distance+1:1;
        if(type==TreeNodeType.EXTENDED_LEAVES)extendedDistance = (parent.type==TreeNodeType.EXTENDED_LEAVES?parent.extendedDistance:parent.distance)+1;
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
    void walk(Predicate<TreeNode> visit){
        if(!visit.test(this))return;
        for(TreeNode child : children){
            child.walk(visit);
        }
    }
}
