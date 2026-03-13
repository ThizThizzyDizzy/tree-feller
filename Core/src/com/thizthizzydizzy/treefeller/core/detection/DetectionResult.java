package com.thizthizzydizzy.treefeller.core.detection;
import com.thizthizzydizzy.treefeller.core.config.structure.ToolConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeConfiguration;
public class DetectionResult{
    public final TreeTree detected;
    public final TreeConfiguration tree;
    public final ToolConfiguration tool;
    public DetectionResult(TreeTree detected, TreeConfiguration tree, ToolConfiguration tool){
        this.detected = detected;
        this.tree = tree;
        this.tool = tool;
    }
}
