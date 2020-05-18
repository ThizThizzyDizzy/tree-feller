package com.thizthizzydizzy.treefeller.compat;
import com.thizthizzydizzy.treefeller.OptionBoolean;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
public abstract class PluginCompatibility{
    private final OptionBoolean enabled;
    public PluginCompatibility(){
        enabled = new OptionBoolean("Compatibility "+getPluginName(), true, false, false, true, true){
            @Override
            public String getDesc(){
                return "Toggle compatibility with "+PluginCompatibility.this.getFriendlyName();
            }
        };
    }
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
    public String getFriendlyName(){
        return getPluginName();
    }
    public boolean isEnabled(){
        return enabled.isTrue();
    }
}