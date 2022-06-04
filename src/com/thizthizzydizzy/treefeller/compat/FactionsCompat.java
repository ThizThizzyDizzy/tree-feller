package com.thizthizzydizzy.treefeller.compat;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class FactionsCompat extends InternalCompatibility{
    @Override
    public String getPluginName(){
        return "Factions";
    }
    @Override
    public boolean test(Player player, Block block){
        return com.massivecraft.factions.listeners.FactionsBlockListener.playerCanBuildDestroyBlock(player, block.getLocation(), "destroy", true);
    }
}