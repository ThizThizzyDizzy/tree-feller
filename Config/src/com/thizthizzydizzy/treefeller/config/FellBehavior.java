package com.thizthizzydizzy.treefeller.config;
import com.thizthizzydizzy.treefeller.config.hjson.SerializedComment;
public class FellBehavior{
    @SerializedComment("Attempt to lay the tree down naturally")
    public boolean natural = false;
    
    @SerializedComment("Fall as falling block entities, rather than breaking in-place")
    public boolean fall_as_entities = false;
    
    @SerializedComment("The damage falling blocks should deal, per block fallen")
    public float fall_damage_amount = 0;
    
    @SerializedComment("The maximum damage falling blocks can deal")
    public float fall_damage_maximum = 40;
}
