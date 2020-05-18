package com.thizthizzydizzy.treefeller.compat;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
public class JobsRebornCompat extends InternalCompatibility{
    @Override
    public String getPluginName(){
        return "Jobs";
    }
    @Override
    public String getFriendlyName(){
        return "Jobs Reborn";
    }
    @Override
    public void breakBlock(Player player, Block block){
        JobsRebornCompat2.breakBlock(player, block);
    }
}