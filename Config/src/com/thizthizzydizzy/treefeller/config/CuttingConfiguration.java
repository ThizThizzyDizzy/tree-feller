package com.thizthizzydizzy.treefeller.config;
import com.thizthizzydizzy.treefeller.config.hjson.SerializedComment;
public class CuttingConfiguration {
    @SerializedComment("Leave the tree stump (any blocks below the one that was cut)")
    public boolean leave_stump = false;
    
    @SerializedComment("The maximum taxicab distance from the trunk that leaves will be broken")
    public int leaf_break_range = 6;
    
    @SerializedComment("The maximum number of trunk blocks that can be cut down at once")
    public int partial_trunk_limit = -1;
    
    @SerializedComment("Allow trees to be partially cut down, either from insufficient tool durability, or from other settings")
    public boolean allow_partial = false;
    
    @SerializedComment("Cut down the tree with an animation, rather than all at once")
    public boolean cutting_animation = false;
    
    @SerializedComment("Animation delay, in game ticks")
    public float animation_delay = 1;
}
