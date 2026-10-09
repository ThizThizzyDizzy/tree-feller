package com.thizthizzydizzy.treefeller.core.config.structure.section;
import com.thizthizzydizzy.treefeller.core.config.legacy.LegacyBukkitOption;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
import com.thizthizzydizzy.treefeller.core.config.globaldefault.ConfigGlobalDefaultBoolean;
import com.thizthizzydizzy.treefeller.core.config.structure.general.DualList;
import com.thizthizzydizzy.treefeller.core.config.structure.general.IntegerRange;
import com.thizthizzydizzy.treefeller.core.config.structure.general.Range;
public class TriggerConfiguration{
    // PLAYER REQUIREMENTS
    
    @ConfigComment("Whether TreeFeller should be toggled on by default (per-player toggle)")
    @ConfigGlobalDefaultBoolean(true)
    @LegacyBukkitOption(value = "default-enabled",
            scopes = {LegacyBukkitOption.Scope.GLOBAL})
    public Boolean default_enabled;

    @ConfigComment("Minimum time, in ticks, between felling trees. (per player)")
    @LegacyBukkitOption(value = "cooldown",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE})
    public Integer cooldown;
    
    @ConfigComment("Permissions required to fell trees")
    @LegacyBukkitOption(value = "required-permissions",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE},
            suffix = ".whitelist")
    public DualList<String> permissions;

    @ConfigComment("Food level required to fell trees")
    @LegacyBukkitOption(value = "min-food",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE},
            conversion = LegacyBukkitOption.Conversion.INTEGER,
            suffix = ".min")
    @LegacyBukkitOption(value = "max-food",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE},
            conversion = LegacyBukkitOption.Conversion.INTEGER,
            suffix = ".max")
    public Range food;

    @ConfigComment("Saturation level required to fell trees")
    public Range saturation;

    @ConfigComment("Health required to fell trees")
    @LegacyBukkitOption(value = "min-health",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE},
            suffix = ".min")
    @LegacyBukkitOption(value = "max-health",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE},
            suffix = ".max")
    public Range health;

    @ConfigComment("Allow felling trees in adventure mode")
    @ConfigGlobalDefaultBoolean(false)
    @LegacyBukkitOption(value = "enable-adventure",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE})
    public Boolean adventure_mode;

    @ConfigComment("Allow felling trees in survival mode")
    @ConfigGlobalDefaultBoolean(true)
    @LegacyBukkitOption(value = "enable-survival",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE})
    public Boolean survival_mode;

    @ConfigComment("Allow felling trees in creative mode")
    @ConfigGlobalDefaultBoolean(false)
    @LegacyBukkitOption(value = "enable-creative",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE})
    public Boolean creative_mode;

    @ConfigComment("Allow felling trees while sneaking")
    @ConfigGlobalDefaultBoolean(false)
    @LegacyBukkitOption(value = "with-sneak",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE})
    public Boolean with_sneaking;

    @ConfigComment("Allow felling trees while not sneaking")
    @ConfigGlobalDefaultBoolean(true)
    @LegacyBukkitOption(value = "without-sneak",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE})
    public Boolean without_sneaking;

    // WORLD REQUIREMENTS
    
    @ConfigComment("The time of day when tree felling is allowed, in ticks (0-24000)")
    @LegacyBukkitOption(value = "min-time",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE},
            suffix = ".min")
    @LegacyBukkitOption(value = "max-time",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE},
            suffix = ".max")
    public IntegerRange day_time;

    @ConfigComment("The moon phases where tree felling is allowed, (0-7, where 0 is a full moon, and 7 is a waxing gibbous)")
    @LegacyBukkitOption(value = "min-phase",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE},
            suffix = ".min")
    @LegacyBukkitOption(value = "max-phase",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE},
            suffix = ".max")
    public IntegerRange moon_phase;

    @ConfigComment("Dimensions where tree felling is allowed")
    public DualList<String> dimensions;

    @ConfigComment("Biomes where tree felling is allowed")
    @LegacyBukkitOption(value = "biomes",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE},
            conversion = LegacyBukkitOption.Conversion.BIOMES)
    @LegacyBukkitOption(value = "biome-blacklist",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE},
            conversion = LegacyBukkitOption.Conversion.BIOMES)
    public DualList<String> biomes;
}
