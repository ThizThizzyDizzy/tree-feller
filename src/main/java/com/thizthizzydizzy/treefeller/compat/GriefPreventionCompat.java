package com.thizthizzydizzy.treefeller.compat;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
public class GriefPreventionCompat extends InternalCompatibility{
    @Override
    public String getPluginName(){
        return "GriefPrevention";
    }
    @Override
    public boolean test(Player player, Block block) {
        return me.ryanhamshire.GriefPrevention.GriefPrevention.instance.allowBreak(player, block, block.getLocation())==null;
    }
}