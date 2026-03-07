package com.thizthizzydizzy.treefeller.core.config.structure.section.breaking;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
import com.thizthizzydizzy.treefeller.core.config.ConfigGlobalDefaultBoolean;
import com.thizthizzydizzy.treefeller.core.config.ConfigGlobalDefaultFloat;
public class FellBehavior{
    @ConfigComment("Attempt to lay the tree down naturally")
    @ConfigGlobalDefaultBoolean(false)
    public Boolean natural;
    
    @ConfigComment("Fall as falling block entities, rather than breaking in-place")
    @ConfigGlobalDefaultBoolean(false)
    public Boolean fall_as_entities;
    
    @ConfigComment("The damage falling blocks should deal, per block fallen")
    @ConfigGlobalDefaultFloat(0)
    public Float fall_damage_amount;
    
    @ConfigComment("The maximum damage falling blocks can deal")
    @ConfigGlobalDefaultFloat(40)
    public Float fall_damage_maximum;
}
