package com.thizthizzydizzy.treefeller.core.config.structure.section;
import com.thizthizzydizzy.treefeller.core.config.structure.section.detection.DecorationConfiguration;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
public class DetectionConfiguration{
    @ConfigComment("The maximum distance that TreeFeller may scan.\nBlocks will not be checked outside this range")
    public int scan_distance = 256;
    
    @ConfigComment("The maximum distance from the trunk that leaves will be searched for.\nBlocks will not be checked outside this range")
    public int leaf_detect_range = 6;
    
    @ConfigComment("Include player-placed (persistent=true) leaves in the tree")
    public boolean detect_persistent_leaves = false;
    
    @ConfigComment("Allow leaves to be detected diagonally")
    public boolean diagonal_leaves = false;
    
    @ConfigComment("Ignore leaf data (distance) when detecting leaves")
    public boolean ignore_leaf_data = false;
    
    @ConfigComment("Decorations that should be considered part of the tree")
    public DecorationConfiguration[] decorations;
    
    @ConfigComment("The maximum distance to check for a tree trunk when breaking roots")
    public int root_distance = 6;
}
