package com.thizthizzydizzy.treefeller.config;
import com.thizthizzydizzy.treefeller.config.hjson.SerializedComment;
public class DetectionConfiguration{
    @SerializedComment("The maximum distance that TreeFeller may scan.\nBlocks will not be checked outside this range")
    public int scan_distance = 256;
    
    @SerializedComment("The maximum distance from the trunk that leaves will be searched for.\nBlocks will not be checked outside this range")
    public int leaf_detect_range = 6;
    
    @SerializedComment("Include player-placed (persistent=true) leaves in the tree")
    public boolean detect_persistent_leaves = false;
    
    @SerializedComment("Allow leaves to be detected diagonally")
    public boolean diagonal_leaves = false;
    
    @SerializedComment("Ignore leaf data (distance) when detecting leaves")
    public boolean ignore_leaf_data = false;
    
    @SerializedComment("Decorations that should be considered part of the tree")
    public DecorationConfiguration[] decorations;
    
    @SerializedComment("The maximum distance to check for a tree trunk when breaking roots")
    public int root_distance = 6;
}
