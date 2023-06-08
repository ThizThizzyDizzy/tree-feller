package com.thizthizzydizzy.treefeller.compat;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
public class WorldGuardCompat extends InternalCompatibility{
    @Override
    public String getPluginName(){
        return "WorldGuard";
    }
    @Override
    public boolean test(Player player, Block block){
        return WorldGuardCompat2.test(player, block);
    }
    @Override
    public Block test(Player player, Iterable<Block> blocks){
        return WorldGuardCompat2.test(player, blocks);
    }
}