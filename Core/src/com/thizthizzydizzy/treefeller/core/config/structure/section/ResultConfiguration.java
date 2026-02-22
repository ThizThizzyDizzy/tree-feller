package com.thizthizzydizzy.treefeller.core.config.structure.section;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
import com.thizthizzydizzy.treefeller.core.config.structure.section.result.CascadeConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.result.EffectConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.result.SaplingReplantConfiguration;
import java.util.HashMap;
public class ResultConfiguration{
    @ConfigComment("Replant saplings after a tree is felled")
    public SaplingReplantConfiguration sapling_replant = new SaplingReplantConfiguration();
    
    @ConfigComment("Blocks that can be overridden if a falling block lands on them")
    public String[] overrideable_blocks;
    
    @ConfigComment("Reduce damage taken for tools with Unbreaking")
    public boolean respect_unbreaking = true;
    
    @ConfigComment("Deal no damage to unbreakable tools")
    public boolean respect_unbreakable = true;
    
    @ConfigComment("Damage dealt to the tool per block in the tree trunk")
    public float trunk_damage_mult = 1;
    
    @ConfigComment("Damage dealt to the tool per block in the tree leaves")
    public float leaves_damage_mult = 0;
    
    @ConfigComment("Damage dealt to the tool per block in the tree decorations")
    public float decorations_damage_mult = 0;
    
    @ConfigComment("Consume stacked tools one at a time. (The entire stack will be treated as one tool)\nWARNING: Using stacked tools is not recommended!")
    public boolean stacked_tools = false;
    
    @ConfigComment("Apply the tool's Fortune to drops from the trunk")
    public boolean trunk_fortune = true;
    
    @ConfigComment("Apply the tool's Fortune to drops from the leaves")
    public boolean leaves_fortune = true;
    
    @ConfigComment("Apply the tool's Fortune to drops from the decorations")
    public boolean decoration_fortune = false;
    
    @ConfigComment("Apply the tool's Silk Touch to drops from the trunk")
    public boolean trunk_silk_touch = true;
    
    @ConfigComment("Apply the tool's Silk Touch to drops from the leaves")
    public boolean leaves_silk_touch = true;
    
    @ConfigComment("Apply the tool's Silk Touch to drops from the decorations")
    public boolean decoration_silk_touch = false;
    
    @ConfigComment("Rotate falling pillar blocks (axis=x/y/z) as they fall")
    public boolean rotate_logs = true;
    
    @ConfigComment("Convert block drops into other items when felling")
    public HashMap<String, String> drop_conversions;

    @ConfigComment("Place dropped items directly in the player inventory")
    public boolean drop_to_inventory = false;
    
    @ConfigComment("Chance of trunk dropping items. Values higher than 1 will multiply drops")
    public float trunk_drop_chance = 1;
    
    @ConfigComment("Chance of leaves dropping items. Values higher than 1 will multiply drops")
    public float leaves_drop_chance = 1;
    
    @ConfigComment("Chance of decorations dropping items. Values higher than 1 will multiply drops")
    public float decorations_drop_chance = 1;
    
    @ConfigComment("Effects to apply to falling trees")
    public EffectConfiguration[] effects;
    
    @ConfigComment("Base food consumed per tree felled")
    public float consumed_food_base = 0;
    
    @ConfigComment("Food consumed per trunk block felled")
    public float consumed_food_trunk = 0;
    
    @ConfigComment("Food consumed per leaf block felled")
    public float consumed_food_leaves = 0;
    
    @ConfigComment("Food consumed per decoration block felled")
    public float consumed_food_decoration = 0;
    
    @ConfigComment("Base health consumed per tree felled")
    public float consumed_health_base = 0;
    
    @ConfigComment("Health consumed per trunk block felled")
    public float consumed_health_trunk = 0;
    
    @ConfigComment("Health consumed per leaf block felled")
    public float consumed_health_leaves = 0;
    
    @ConfigComment("Health consumed per decoration block felled")
    public float consumed_health_decoration = 0;
    
    @ConfigComment("Cascade tree felling to other trees connected through leaves")
    public CascadeConfiguration cascade = new CascadeConfiguration();
}
