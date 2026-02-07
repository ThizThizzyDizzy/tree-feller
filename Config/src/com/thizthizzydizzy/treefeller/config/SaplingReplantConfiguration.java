package com.thizthizzydizzy.treefeller.config;
import com.thizthizzydizzy.treefeller.config.hjson.SerializedComment;
public class SaplingReplantConfiguration{
    public boolean enable = false;
    
    @SerializedComment("Use saplings dropped from the tree leaves to replant trees")
    public boolean use_tree_saplings = true;
    
    @SerializedComment("Use saplings from the player inventory to replant trees")
    public boolean use_inventory_saplings = false;
    
    @SerializedComment("Spawn saplings to replant if not enough are available")
    public boolean spawn_saplings = false;
    
    @SerializedComment("The maximum time, in game ticks, to wait for saplings before giving up")
    public int sapling_timeout = 50;
    
    @SerializedComment("The blocks that saplings may be planted on")
    public String[] grasses;
}
