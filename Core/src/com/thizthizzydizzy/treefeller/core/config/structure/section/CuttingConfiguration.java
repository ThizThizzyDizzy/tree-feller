package com.thizthizzydizzy.treefeller.core.config.structure.section;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
import com.thizthizzydizzy.treefeller.core.config.globaldefault.ConfigGlobalDefaultBoolean;
import com.thizthizzydizzy.treefeller.core.config.globaldefault.ConfigGlobalDefaultInteger;
public class CuttingConfiguration{
    // Filters that decide which tree blocks are cut
    @ConfigComment("Leave the tree stump (any blocks below the one that was cut)")
    @ConfigGlobalDefaultBoolean(false)
    public Boolean leave_stump;

    @ConfigComment("Allow trees to be partially cut down, either from insufficient tool durability, or from other settings")
    @ConfigGlobalDefaultBoolean(false)
    public Boolean allow_partial;

    @ConfigComment("Allow a tool to fully cut down a tree, even with insufficient durability")
    @ConfigGlobalDefaultBoolean(true)
    public Boolean allow_partial_tool;

    @ConfigComment("Prevent felling a tree if doing so would break the tool")
    @ConfigGlobalDefaultBoolean(false)
    public Boolean prevent_breakage;

    @ConfigComment("The maximum number of trunk blocks that can be cut down at once")
    public Integer partial_trunk_limit;

    @ConfigComment("The maximum distance from the trunk that leaves will be broken")
    @ConfigGlobalDefaultInteger(6)
    public Integer leaf_break_range;

    // Behavior
    @ConfigComment("Cut down the tree with an animation, rather than all at once")
    @ConfigGlobalDefaultBoolean(false)
    public Boolean cutting_animation;

    @ConfigComment("Animation delay, in game ticks")
    @ConfigGlobalDefaultInteger(1)
    public Float animation_delay;
}
