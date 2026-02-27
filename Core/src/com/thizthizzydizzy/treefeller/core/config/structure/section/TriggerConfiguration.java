package com.thizthizzydizzy.treefeller.core.config.structure.section;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
import com.thizthizzydizzy.treefeller.core.config.structure.general.Range;
import java.util.HashMap;
public class TriggerConfiguration{
    @ConfigComment("Whether TreeFeller should be toggled on by default (per-player toggle)")
    public boolean default_enabled;

    @ConfigComment("This range defines specific enchantments and levels that are required for or prevent felling")
    public HashMap<String, Range> tool_enchantments;

    @ConfigComment("This range defines how much durability a tool must have to fell trees")
    public Range tool_durability;

    @ConfigComment("This range defines how much durability, as a percentage, a tool must have to fell trees")
    public Range tool_durability_percent;

    @ConfigComment("Lore required on a tool to allow tree felling")
    public String[] required_lore;

    @ConfigComment("Custom name required on a tool to allow tree felling")
    public String required_name;

    @ConfigComment("Permissions required to fell trees")
    public String[] required_permissions;

    @ConfigComment("The time of day when tree felling is allowed, in ticks (0-24000)")
    public Range required_day_time;

    @ConfigComment("The moon phases where tree felling is allowed, (0-7, where 0 is a full moon, and 7 is a waxing gibbous)")
    public Range required_moon_phase;

    @ConfigComment("Food level required to fell trees")
    public Range required_food;

    @ConfigComment("Health required to fell trees")
    public Range required_health;

    @ConfigComment("CustomModelData required on a tool to fell trees")
    public int required_custom_model_data;

    @ConfigComment("Allow felling trees in adventure mode")
    public boolean enable_adventure_mode = false;

    @ConfigComment("Allow felling trees in survival mode")
    public boolean enable_survival_mode = true;

    @ConfigComment("Allow felling trees in creative mode")
    public boolean enable_creative_mode = false;

    @ConfigComment("Allow felling trees while sneaking")
    public boolean enable_with_sneaking = false;

    @ConfigComment("Allow felling trees while not sneaking")
    public boolean enable_without_sneaking = true;

    @ConfigComment("Dimensions where tree felling is allowed")
    public String[] allowed_dimensions;

    @ConfigComment("Invert allowed_dimensions to allow tree felling in all worlds except those specified")
    public boolean dimension_blacklist = false;

    @ConfigComment("Biomes where tree felling is allowed")
    public String[] allowed_biomes;

    @ConfigComment("Invert allowed_biomes to allow tree felling in all worlds except those specified")
    public boolean biome_blacklist = false;

    @ConfigComment("Minimum time, in ticks, between felling trees. (per player)")
    public int cooldown;
}
