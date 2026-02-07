package com.thizthizzydizzy.treefeller.config;
import com.thizthizzydizzy.treefeller.config.hjson.SerializedComment;
import java.util.HashMap;
public class ResultConfiguration{
    @SerializedComment("Replant saplings after a tree is felled")
    public SaplingReplantConfiguration sapling_replant = new SaplingReplantConfiguration();
    
    @SerializedComment("Blocks that can be overridden if a falling block lands on them")
    public String[] overrideable_blocks;
    
    @SerializedComment("Reduce damage taken for tools with Unbreaking")
    public boolean respect_unbreaking = true;
    
    @SerializedComment("Deal no damage to unbreakable tools")
    public boolean respect_unbreakable = true;
    
    @SerializedComment("Damage dealt to the tool per block in the tree trunk")
    public float trunk_damage_mult = 1;
    
    @SerializedComment("Damage dealt to the tool per block in the tree leaves")
    public float leaves_damage_mult = 0;
    
    @SerializedComment("Damage dealt to the tool per block in the tree decorations")
    public float decorations_damage_mult = 0;
    
    @SerializedComment("Consume stacked tools one at a time. (The entire stack will be treated as one tool)\nWARNING: Using stacked tools is not recommended!")
    public boolean stacked_tools = false;
    
    @SerializedComment("Apply the tool's Fortune to drops from the trunk")
    public boolean trunk_fortune = true;
    
    @SerializedComment("Apply the tool's Fortune to drops from the leaves")
    public boolean leaves_fortune = true;
    
    @SerializedComment("Apply the tool's Fortune to drops from the decorations")
    public boolean decoration_fortune = false;
    
    @SerializedComment("Apply the tool's Silk Touch to drops from the trunk")
    public boolean trunk_silk_touch = true;
    
    @SerializedComment("Apply the tool's Silk Touch to drops from the leaves")
    public boolean leaves_silk_touch = true;
    
    @SerializedComment("Apply the tool's Silk Touch to drops from the decorations")
    public boolean decoration_silk_touch = false;
    
    @SerializedComment("Rotate falling pillar blocks (axis=x/y/z) as they fall")
    public boolean rotate_logs = true;
    
    @SerializedComment("Convert block drops into other items when felling")
    public HashMap<String, String> drop_conversions;

    @SerializedComment("Place dropped items directly in the player inventory")
    public boolean drop_to_inventory = false;
    
    @SerializedComment("Chance of trunk dropping items. Values higher than 1 will multiply drops")
    public float trunk_drop_chance = 1;
    
    @SerializedComment("Chance of leaves dropping items. Values higher than 1 will multiply drops")
    public float leaves_drop_chance = 1;
    
    @SerializedComment("Chance of decorations dropping items. Values higher than 1 will multiply drops")
    public float decorations_drop_chance = 1;
    
    @SerializedComment("Effects to apply to falling trees")
    public EffectConfiguration[] effects;
    
    @SerializedComment("Base food consumed per tree felled")
    public float consumed_food_base = 0;
    
    @SerializedComment("Food consumed per trunk block felled")
    public float consumed_food_trunk = 0;
    
    @SerializedComment("Food consumed per leaf block felled")
    public float consumed_food_leaves = 0;
    
    @SerializedComment("Food consumed per decoration block felled")
    public float consumed_food_decoration = 0;
    
    @SerializedComment("Base health consumed per tree felled")
    public float consumed_health_base = 0;
    
    @SerializedComment("Health consumed per trunk block felled")
    public float consumed_health_trunk = 0;
    
    @SerializedComment("Health consumed per leaf block felled")
    public float consumed_health_leaves = 0;
    
    @SerializedComment("Health consumed per decoration block felled")
    public float consumed_health_decoration = 0;
    
    @SerializedComment("Cascade tree felling to other trees connected through leaves")
    public CascadeConfiguration cascade;
}
