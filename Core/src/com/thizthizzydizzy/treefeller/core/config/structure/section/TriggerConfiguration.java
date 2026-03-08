package com.thizthizzydizzy.treefeller.core.config.structure.section;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
import com.thizthizzydizzy.treefeller.core.config.globaldefault.ConfigGlobalDefaultBoolean;
import com.thizthizzydizzy.treefeller.core.config.structure.general.DualList;
import com.thizthizzydizzy.treefeller.core.config.structure.general.Range;
public class TriggerConfiguration{
    // PLAYER REQUIREMENTS
    
    @ConfigComment("Whether TreeFeller should be toggled on by default (per-player toggle)")
    @ConfigGlobalDefaultBoolean(true)
    public Boolean default_enabled;

    @ConfigComment("Minimum time, in ticks, between felling trees. (per player)")
    public Integer cooldown;
    
    @ConfigComment("Permissions required to fell trees")
    public DualList<String> permissions;

    @ConfigComment("Food level required to fell trees")
    public Range food;

    @ConfigComment("Saturation level required to fell trees")
    public Range saturation;

    @ConfigComment("Health required to fell trees")
    public Range health;

    @ConfigComment("Allow felling trees in adventure mode")
    @ConfigGlobalDefaultBoolean(false)
    public Boolean adventure_mode;

    @ConfigComment("Allow felling trees in survival mode")
    @ConfigGlobalDefaultBoolean(true)
    public Boolean survival_mode;

    @ConfigComment("Allow felling trees in creative mode")
    @ConfigGlobalDefaultBoolean(false)
    public Boolean creative_mode;

    @ConfigComment("Allow felling trees while sneaking")
    @ConfigGlobalDefaultBoolean(false)
    public Boolean with_sneaking;

    @ConfigComment("Allow felling trees while not sneaking")
    @ConfigGlobalDefaultBoolean(true)
    public Boolean without_sneaking;

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
