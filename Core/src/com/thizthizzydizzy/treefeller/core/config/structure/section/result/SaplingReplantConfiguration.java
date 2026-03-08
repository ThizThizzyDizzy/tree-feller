package com.thizthizzydizzy.treefeller.core.config.structure.section.result;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
import com.thizthizzydizzy.treefeller.core.config.globaldefault.ConfigGlobalDefaultBoolean;
import com.thizthizzydizzy.treefeller.core.config.globaldefault.ConfigGlobalDefaultInteger;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IBlockDefinition;
public class SaplingReplantConfiguration{
    @ConfigGlobalDefaultBoolean(false)
    public Boolean enable;
    
    @ConfigComment("Use saplings dropped from the tree leaves to replant trees")
    @ConfigGlobalDefaultBoolean(true)
    public Boolean use_tree_saplings;
    
    @ConfigComment("Use saplings from the player inventory to replant trees")
    @ConfigGlobalDefaultBoolean(false)
    public Boolean use_inventory_saplings;
    
    @ConfigComment("Spawn saplings to replant if not enough are available")
    @ConfigGlobalDefaultBoolean(false)
    public Boolean spawn_saplings;
    
    @ConfigComment("The maximum time, in game ticks, to wait for saplings before giving up")
    @ConfigGlobalDefaultInteger(50)
    public Integer sapling_timeout;
    
    @ConfigComment("The blocks that saplings may be planted on")
    public IBlockDefinition[] grasses;
}
