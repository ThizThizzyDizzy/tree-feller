package com.thizthizzydizzy.treefeller.core.config.structure.section;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
public class CuttingConfiguration {
    @ConfigComment("Leave the tree stump (any blocks below the one that was cut)")
    public boolean leave_stump = false;
    
    @ConfigComment("The maximum taxicab distance from the trunk that leaves will be broken")
    public int leaf_break_range = 6;
    
    @ConfigComment("The maximum number of trunk blocks that can be cut down at once")
    public int partial_trunk_limit = -1;
    
    @ConfigComment("Allow trees to be partially cut down, either from insufficient tool durability, or from other settings")
    public boolean allow_partial = false;
    
    @ConfigComment("Cut down the tree with an animation, rather than all at once")
    public boolean cutting_animation = false;
    
    @ConfigComment("Animation delay, in game ticks")
    public float animation_delay = 1;
}
