package com.thizthizzydizzy.treefeller.core.breaking;
import com.thizthizzydizzy.treefeller.core.connector.item.IItemConnector;
import com.thizthizzydizzy.treefeller.core.detection.TreeNode;
import java.util.List;
import java.util.Map;
public class BreakingResult{
    public final List<TreeNode> nodes;
    public final Map<Long, List<IItemConnector>> drops;
    public BreakingResult(List<TreeNode> nodes, Map<Long, List<IItemConnector>> drops){
        this.nodes = nodes;
        this.drops = drops;
    }
}
