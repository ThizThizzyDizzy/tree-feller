package com.thizthizzydizzy.treefeller.core.config.structure.section;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
import com.thizthizzydizzy.treefeller.core.config.ConfigGlobalDefaultBoolean;
import com.thizthizzydizzy.treefeller.core.config.ConfigGlobalDefaultFloat;
import com.thizthizzydizzy.treefeller.core.config.ConfigGlobalDefaultNewInstance;
import com.thizthizzydizzy.treefeller.core.config.structure.section.result.CascadeConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.result.EffectConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.result.SaplingReplantConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IBlockDefinition;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IItemDefinition;
import java.util.HashMap;
public class ResultConfiguration{
    @ConfigComment("Replant saplings after a tree is felled")
    @ConfigGlobalDefaultNewInstance
    public SaplingReplantConfiguration sapling_replant;
    
    @ConfigComment("Blocks that can be overridden if a falling block lands on them")
    public IBlockDefinition[] overrideable_blocks;
    
    @ConfigComment("Reduce damage taken for tools with Unbreaking")
    @ConfigGlobalDefaultBoolean(true)
    public Boolean respect_unbreaking;
    
    @ConfigComment("Deal no damage to unbreakable tools")
    @ConfigGlobalDefaultBoolean(true)
    public Boolean respect_unbreakable;
    
    @ConfigComment("Damage dealt to the tool per block in the tree trunk")
    @ConfigGlobalDefaultFloat(1)
    public Float trunk_damage_mult;
    
    @ConfigComment("Damage dealt to the tool per block in the tree leaves")
    @ConfigGlobalDefaultFloat(0)
    public Float leaves_damage_mult;
    
    @ConfigComment("Damage dealt to the tool per block in the tree decorations")
    @ConfigGlobalDefaultFloat(0)
    public Float decorations_damage_mult;
    
    @ConfigComment("Consume stacked tools one at a time. (The entire stack will be treated as one tool)\nWARNING: Using stacked tools is not recommended!")
    @ConfigGlobalDefaultBoolean(false)
    public Boolean stacked_tools;
    
    @ConfigComment("Apply the tool's Fortune to drops from the trunk")
    @ConfigGlobalDefaultBoolean(true)
    public Boolean trunk_fortune;
    
    @ConfigComment("Apply the tool's Fortune to drops from the leaves")
    @ConfigGlobalDefaultBoolean(true)
    public Boolean leaves_fortune;
    
    @ConfigComment("Apply the tool's Fortune to drops from the decorations")
    @ConfigGlobalDefaultBoolean(false)
    public Boolean decoration_fortune;
    
    @ConfigComment("Apply the tool's Silk Touch to drops from the trunk")
    @ConfigGlobalDefaultBoolean(true)
    public Boolean trunk_silk_touch;
    
    @ConfigComment("Apply the tool's Silk Touch to drops from the leaves")
    @ConfigGlobalDefaultBoolean(true)
    public Boolean leaves_silk_touch;
    
    @ConfigComment("Apply the tool's Silk Touch to drops from the decorations")
    @ConfigGlobalDefaultBoolean(false)
    public Boolean decoration_silk_touch;
    
    @ConfigComment("Rotate falling pillar blocks (axis=x/y/z) as they fall")
    @ConfigGlobalDefaultBoolean(true)
    public Boolean rotate_logs;
    
    @ConfigComment("Convert block drops into other items when felling")
    public HashMap<IItemDefinition, IItemDefinition> drop_conversions;

    @ConfigComment("Place dropped items directly in the player inventory")
    @ConfigGlobalDefaultBoolean(false)
    public Boolean drop_to_inventory;
    
    @ConfigComment("Chance of trunk dropping items. Values higher than 1 will multiply drops")
    @ConfigGlobalDefaultFloat(1)
    public Float trunk_drop_chance;
    
    @ConfigComment("Chance of leaves dropping items. Values higher than 1 will multiply drops")
    @ConfigGlobalDefaultFloat(1)
    public Float leaves_drop_chance;
    
    @ConfigComment("Chance of decorations dropping items. Values higher than 1 will multiply drops")
    @ConfigGlobalDefaultFloat(1)
    public Float decorations_drop_chance;
    
    @ConfigComment("Effects to apply to falling trees")
    public EffectConfiguration[] effects;
    
    @ConfigComment("Base food consumed per tree felled")
    @ConfigGlobalDefaultFloat(0)
    public Float consumed_food_base;
    
    @ConfigComment("Food consumed per trunk block felled")
    @ConfigGlobalDefaultFloat(0)
    public Float consumed_food_trunk;
    
    @ConfigComment("Food consumed per leaf block felled")
    @ConfigGlobalDefaultFloat(0)
    public Float consumed_food_leaves;
    
    @ConfigComment("Food consumed per decoration block felled")
    @ConfigGlobalDefaultFloat(0)
    public Float consumed_food_decoration;
    
    @ConfigComment("Base health consumed per tree felled")
    @ConfigGlobalDefaultFloat(0)
    public Float consumed_health_base;
    
    @ConfigComment("Health consumed per trunk block felled")
    @ConfigGlobalDefaultFloat(0)
    public Float consumed_health_trunk;
    
    @ConfigComment("Health consumed per leaf block felled")
    @ConfigGlobalDefaultFloat(0)
    public Float consumed_health_leaves;
    
    @ConfigComment("Health consumed per decoration block felled")
    @ConfigGlobalDefaultFloat(0)
    public Float consumed_health_decoration;
    
    @ConfigComment("Cascade tree felling to other trees connected through leaves")
    @ConfigGlobalDefaultNewInstance
    public CascadeConfiguration cascade;
}
