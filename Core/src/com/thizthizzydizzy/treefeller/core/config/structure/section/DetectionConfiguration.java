package com.thizthizzydizzy.treefeller.core.config.structure.section;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
import com.thizthizzydizzy.treefeller.core.config.structure.section.detection.DecorationConfiguration;
public class DetectionConfiguration{
    @ConfigComment("The maximum distance to check for a tree trunk when breaking roots")
    public int root_distance = 6;
    
    @ConfigComment("The maximum distance between disconnected trunk sections, connected by leaves")
    public int disconnected_trunk_distance = 0;
    
    @ConfigComment("The maximum distance from the trunk that leaves will be searched for")
    public int leaf_detect_range = 6;
    
    @ConfigComment("Include player-placed (persistent=true) leaves in the tree")
    public boolean detect_persistent_leaves = false;
    
    @ConfigComment("Allow leaves to be detected diagonally")
    public boolean diagonal_leaves = false;
    
    @ConfigComment("Ignore leaf data (distance) when detecting leaves")
    public boolean ignore_leaf_data = false;
    
    @ConfigComment("Decorations that should be considered part of the tree")
    public DecorationConfiguration[] decorations;
}
