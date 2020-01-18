package com.thizthizzydizzy.treefeller.compat;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
public abstract class PluginCompatibility{
    public abstract String getPluginName();
    /**
     * Called when a block is removed, but not broken, such as when a tree falls over
     */
    public void removeBlock(Player player, Block block){}
    /**
     * Called when a block is broken by a player
     */
    public void breakBlock(Player player, Block block){}
    public void addBlock(Player player, Block block){}
    public boolean test(Player player, Block block){
        return true;
    }
    public Block test(Player player, Iterable<Block> blocks){
        for(Block b : blocks){
            if(!test(player, b))return b;
        }
        return null;
    }
}