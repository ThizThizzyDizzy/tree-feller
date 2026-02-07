package com.thizthizzydizzy.treefeller.config;
import com.thizthizzydizzy.treefeller.config.hjson.SerializedComment;
import java.util.HashMap;
public class TriggerConfiguration {
    @SerializedComment("Whether TreeFeller should be toggled on by default (per-player toggle)")
    public boolean default_enabled;
    
    @SerializedComment("This range defines specific enchantments and levels that are required for or prevent felling")
    public HashMap<String, Range> tool_enchantments;
    
    @SerializedComment("This range defines how much durability a tool must have to fell trees")
    public Range tool_durability;
    
    @SerializedComment("This range defines how much durability, as a percentage, a tool must have to fell trees")
    public Range tool_durability_percent;
    
    @SerializedComment("Lore required on a tool to allow tree felling")
    public String[] required_lore;
    
    @SerializedComment("Custom name required on a tool to allow tree felling")
    public String required_name;
    
    @SerializedComment("Permissions required to fell trees")
    public String[] required_permissions;
    
    @SerializedComment("The time of day when tree felling is allowed, in ticks (0-24000)")
    public Range required_day_time;
    
    @SerializedComment("The moon phases where tree felling is allowed, (0-7, where 0 is a full moon, and 7 is a waxing gibbous)")
    public Range required_moon_phase;
    
    @SerializedComment("Food level required to fell trees")
    public Range required_food;
    
    @SerializedComment("Health required to fell trees")
    public Range required_health;
    
    @SerializedComment("CustomModelData required on a tool to fell trees")
    public int required_custom_model_data;
    
    @SerializedComment("Allow felling trees in adventure mode")
    public boolean enable_adventure_mode = false;
    
    @SerializedComment("Allow felling trees in survival mode")
    public boolean enable_survival_mode = true;
    
    @SerializedComment("Allow felling trees in creative mode")
    public boolean enable_creative_mode = false;
    
    @SerializedComment("Allow felling trees while sneaking")
    public boolean enable_with_sneaking = false;
    
    @SerializedComment("Allow felling trees while not sneaking")
    public boolean enable_without_sneaking = true;
    
    @SerializedComment("Dimensions where tree felling is allowed")
    public String[] allowed_dimensions;
    
    @SerializedComment("Invert allowed_dimensions to allow tree felling in all worlds except those specified")
    public boolean dimension_blacklist = false;
    
    @SerializedComment("Biomes where tree felling is allowed")
    public String[] allowed_biomes;
    
    @SerializedComment("Invert allowed_biomes to allow tree felling in all worlds except those specified")
    public boolean biome_blacklist = false;
    
    @SerializedComment("Minimum time, in ticks, between felling trees. (per player)")
    public int cooldown;
}
