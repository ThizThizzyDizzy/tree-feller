package com.thizthizzydizzy.treefeller.core.config.structure.section.breaking;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
public class FellBehavior{
    @ConfigComment("Attempt to lay the tree down naturally")
    public boolean natural = false;
    
    @ConfigComment("Fall as falling block entities, rather than breaking in-place")
    public boolean fall_as_entities = false;
    
    @ConfigComment("The damage falling blocks should deal, per block fallen")
    public float fall_damage_amount = 0;
    
    @ConfigComment("The maximum damage falling blocks can deal")
    public float fall_damage_maximum = 40;
}
