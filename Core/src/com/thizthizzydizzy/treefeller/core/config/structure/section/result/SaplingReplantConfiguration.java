package com.thizthizzydizzy.treefeller.core.config.structure.section.result;
import com.thizthizzydizzy.treefeller.core.config.legacy.LegacyBukkitOption;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
import com.thizthizzydizzy.treefeller.core.config.globaldefault.ConfigGlobalDefaultBoolean;
import com.thizthizzydizzy.treefeller.core.config.globaldefault.ConfigGlobalDefaultInteger;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IBlockDefinition;
public class SaplingReplantConfiguration{
    @ConfigGlobalDefaultBoolean(false)
    @LegacyBukkitOption(value = "replant-saplings",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE})
    public Boolean enable;
    
    @ConfigComment("Use saplings dropped from the tree leaves to replant trees")
    @ConfigGlobalDefaultBoolean(true)
    @LegacyBukkitOption(value = "use-tree-saplings",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE})
    public Boolean use_tree_saplings;
    
    @ConfigComment("Use saplings from the player inventory to replant trees")
    @ConfigGlobalDefaultBoolean(false)
    @LegacyBukkitOption(value = "use-inventory-saplings",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE})
    public Boolean use_inventory_saplings;
    
    @ConfigComment("Spawn saplings to replant if not enough are available")
    @ConfigGlobalDefaultBoolean(false)
    @LegacyBukkitOption(value = "spawn-saplings",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE},
            conversion = LegacyBukkitOption.Conversion.SPAWN)
    public Boolean spawn_saplings;
    
    @ConfigComment("The maximum time, in game ticks, to wait for saplings before giving up")
    @ConfigGlobalDefaultInteger(50)
    @LegacyBukkitOption(value = "sapling-timeout",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE})
    public Integer sapling_timeout;
    
    @ConfigComment("The blocks that saplings may be planted on")
    @LegacyBukkitOption(value = "grass",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TREE})
    public IBlockDefinition[] grasses;
}
