package com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.towny;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.compatibility.BukkitPluginCompatibility;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.TreeFellerBukkit;
import com.thizthizzydizzy.treefeller.core.compatibility.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
public class TownyCompat extends BukkitPluginCompatibility{
    public TownyCompat(TreeFellerBukkit plugin){super(plugin);}
    @Override
    public String getPluginName(){
        return "Towny";
    }
    @Override
    public boolean test(Player player, Block block){
        return com.palmergames.bukkit.towny.event.executors.TownyActionEventExecutor.canDestroy(player, block.getLocation(), block.getType());
    }
}
