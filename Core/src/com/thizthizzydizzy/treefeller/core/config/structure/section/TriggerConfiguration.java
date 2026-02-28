package com.thizthizzydizzy.treefeller.core.config.structure.section;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
import com.thizthizzydizzy.treefeller.core.config.structure.general.DualList;
import com.thizthizzydizzy.treefeller.core.config.structure.general.Range;
public class TriggerConfiguration{
    // PLAYER REQUIREMENTS
    
    @ConfigComment("Whether TreeFeller should be toggled on by default (per-player toggle)")
    public boolean default_enabled;

    @ConfigComment("Minimum time, in ticks, between felling trees. (per player)")
    public int cooldown;
    
    @ConfigComment("Permissions required to fell trees")
    public DualList<String> permissions;

    @ConfigComment("Food level required to fell trees")
    public Range food;

    @ConfigComment("Saturation level required to fell trees")
    public Range saturation;

    @ConfigComment("Health required to fell trees")
    public Range health;

    @ConfigComment("Allow felling trees in adventure mode")
    public boolean adventure_mode = false;

    @ConfigComment("Allow felling trees in survival mode")
    public boolean survival_mode = true;

    @ConfigComment("Allow felling trees in creative mode")
    public boolean creative_mode = false;

    @ConfigComment("Allow felling trees while sneaking")
    public boolean with_sneaking = false;

    @ConfigComment("Allow felling trees while not sneaking")
    public boolean without_sneaking = true;

    // WORLD REQUIREMENTS
    
    @ConfigComment("The time of day when tree felling is allowed, in ticks (0-24000)")
    public Range day_time;

    @ConfigComment("The moon phases where tree felling is allowed, (0-7, where 0 is a full moon, and 7 is a waxing gibbous)")
    public Range moon_phase;

    @ConfigComment("Dimensions where tree felling is allowed")
    public DualList<String> dimensions;

    @ConfigComment("Biomes where tree felling is allowed")
    public DualList<String> biomes;
}
