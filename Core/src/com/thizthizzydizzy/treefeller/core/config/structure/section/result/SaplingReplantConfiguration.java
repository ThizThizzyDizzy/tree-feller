package com.thizthizzydizzy.treefeller.core.config.structure.section.result;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IBlockDefinition;
public class SaplingReplantConfiguration{
    public boolean enable = false;
    
    @ConfigComment("Use saplings dropped from the tree leaves to replant trees")
    public boolean use_tree_saplings = true;
    
    @ConfigComment("Use saplings from the player inventory to replant trees")
    public boolean use_inventory_saplings = false;
    
    @ConfigComment("Spawn saplings to replant if not enough are available")
    public boolean spawn_saplings = false;
    
    @ConfigComment("The maximum time, in game ticks, to wait for saplings before giving up")
    public int sapling_timeout = 50;
    
    @ConfigComment("The blocks that saplings may be planted on")
    public IBlockDefinition[] grasses = new IBlockDefinition[1];
}
