package com.thizthizzydizzy.treefeller.core.config.structure.section;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
import com.thizthizzydizzy.treefeller.core.config.globaldefault.ConfigGlobalDefaultBoolean;
import com.thizthizzydizzy.treefeller.core.config.globaldefault.ConfigGlobalDefaultFloat;
import com.thizthizzydizzy.treefeller.core.config.globaldefault.ConfigGlobalDefaultInteger;
public class CuttingConfiguration{
    // Filters that decide which tree blocks are cut
    @ConfigComment("Leave the tree stump (any blocks below the one that was cut)")
    @ConfigGlobalDefaultBoolean(false)
    public Boolean leave_stump;

    @ConfigComment("The maximum distance from the trunk that leaves will be broken")
    @ConfigGlobalDefaultInteger(6)
    public Integer leaf_break_range;

    @ConfigComment("The maximum number of trunk blocks that can be cut down at once. This will cause trees to be partially cut if exceeded.")
    public Integer partial_trunk_limit;

    // Tool durability checks
    @ConfigComment("Damage dealt to the tool per block in the tree trunk")
    @ConfigGlobalDefaultFloat(1)
    public Float trunk_damage_mult;

    @ConfigComment("Damage dealt to the tool per block in the tree leaves")
    public Float leaves_damage_mult;

    @ConfigComment("Damage dealt to the tool per block in the tree decorations")
    public Float decorations_damage_mult;

    @ConfigComment("Reduce damage taken for tools with Unbreaking")
    @ConfigGlobalDefaultBoolean(true)
    public Boolean respect_unbreaking;

    @ConfigComment("Deal no damage to unbreakable tools")
    @ConfigGlobalDefaultBoolean(true)
    public Boolean respect_unbreakable;

    @ConfigComment("Consume stacked tools one at a time. (The entire stack will be treated as one tool)\nWARNING: Using stacked tools is not recommended!")
    @ConfigGlobalDefaultBoolean(false)
    public Boolean stacked_tools;

    @ConfigComment("Prevent felling a tree if doing so would break the tool")
    @ConfigGlobalDefaultBoolean(false)
    public Boolean prevent_breakage;

    @ConfigComment("Allow trees to be partially cut down if the tool has insufficient durability")
    @ConfigGlobalDefaultBoolean(false)
    public Boolean allow_partial;

    @ConfigComment("Allow a tool to fully cut down a tree, even with insufficient durability")
    @ConfigGlobalDefaultBoolean(true)
    public Boolean allow_partial_tool;

    // Behavior
    @ConfigComment("Cut down the tree with an animation, rather than all at once")
    @ConfigGlobalDefaultBoolean(false)
    public Boolean cutting_animation;

    @ConfigComment("Animation delay, in game ticks")
    @ConfigGlobalDefaultInteger(1)
    public Integer animation_delay;
}
