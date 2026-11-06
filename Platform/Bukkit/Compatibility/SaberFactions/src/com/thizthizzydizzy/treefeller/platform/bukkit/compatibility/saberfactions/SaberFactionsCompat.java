package com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.saberfactions;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.compatibility.BukkitPluginCompatibility;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.TreeFellerBukkit;
import com.thizthizzydizzy.treefeller.core.compatibility.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class SaberFactionsCompat extends BukkitPluginCompatibility{
    public SaberFactionsCompat(TreeFellerBukkit plugin){super(plugin);}
    @Override
    public String getPluginName(){
        return "SaberFactions";
    }
    @Override
    public boolean test(Player player, Block block){
        return com.massivecraft.factions.listeners.FactionsBlockListener.playerCanBuildDestroyBlock(player, block.getLocation(), "destroy", true);
    }
}
