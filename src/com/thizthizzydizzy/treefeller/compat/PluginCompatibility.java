package com.thizthizzydizzy.treefeller.compat;
import com.thizthizzydizzy.simplegui.ItemBuilder;
import com.thizthizzydizzy.treefeller.Modifier;
import com.thizthizzydizzy.treefeller.OptionBoolean;
import com.thizthizzydizzy.treefeller.Tool;
import com.thizthizzydizzy.treefeller.Tree;
import com.thizthizzydizzy.treefeller.TreeFeller;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
public abstract class PluginCompatibility{
    /**
     * This is the option that enables or disables this compatibility.
     */
    public OptionBoolean enabled;
    public PluginCompatibility(){
        enabled = new OptionBoolean("Compatibility "+getCompatibilityName(), true, false, false, defaultEnabled(), defaultEnabled()){
            @Override
            public String getDesc(boolean ingame){
                return "Toggle compatibility with "+PluginCompatibility.this.getFriendlyName();
            }
            @Override
            public String getFriendlyName(){
                return "Compatibility: "+PluginCompatibility.this.getFriendlyName();
            }
            @Override
            public ItemBuilder getConfigurationDisplayItem(Boolean value){
                return PluginCompatibility.this.getConfigurationDisplayItem();
            }
        };
    }
    public void init(TreeFeller treeFeller){}
    /**
     * The name of the compatibility used in the config.
     * This should be the same as the plugin name, except in special cases (such as multiple plugins with the same name)
    */
    public String getCompatibilityName(){
        return getPluginName();
    }
    public abstract String getPluginName();
    /**
     * Called when a block is removed, but not broken, such as when a tree falls over
     * @param player the player that caused the block to be removed
     * @param block the block that was removed
     */
    public void removeBlock(Player player, Block block){}
    /**
     * Called when a block is broken by a player as part of felling a tree
     * @param tree the tree that was cut down
     * @param tool the tool that was used
     * @param player the player that felled the tree
     * @param axe the item that was used to fell the tree
     * @param block the block that was broken
     * @param modifiers a list of modifiers for multiplying item/exp drops
     */
    public void breakBlock(Tree tree, Tool tool, Player player, ItemStack axe, Block block, List<Modifier> modifiers){}
    @Deprecated
    /**
     * Called when a block is added, such as when trees land from the NATURAL fell behavior
     * @param player the player who caused this to happen
     * @param block the block that was added
     * @deprecated use addBlock(Player, Block, BlockState) instead
     */
    public void addBlock(Player player, Block block){}
    /**
     * Called when a block is added, such as when trees land from the NATURAL fell behavior
     * @param player the player who caused this to happen
     * @param block the block that was added
     * @param was the BlockState before the block was placed
     */
    public void addBlock(Player player, Block block, BlockState was){
        addBlock(player, block);
    }
    /**
     * Called whenever TreeFeller drops an item
     * @param player the player who dropped the item, if any
     * @param item the item that was dropped
     */
    public void dropItem(Player player, Item item){}
    /**
     * Tests if the player can break a certain block
     * @param player the player
     * @param block the block to test
     * @return true if the player can break the given block
     */
    public boolean test(Player player, Block block){
        return true;
    }
    /**
     * Called when a tree is successfully felled, but before the felling happens
     * @param block the block that was broken
     * @param player the player that felled the tree
     * @param axe the item that was used to fell the tree
     * @param tool the tool that was used
     * @param tree the tree that was felled
     * @param blocks the blocks of the tree, stored by distance from the first block
     */
    public void fellTree(Block block, Player player, ItemStack axe, Tool tool, Tree tree, HashMap<Integer, ArrayList<Block>> blocks){}
    public boolean defaultEnabled(){
        return true;
    }
    public Block test(Player player, Iterable<Block> blocks){
        for(Block b : blocks){
            if(!test(player, b))return b;
        }
        return null;
    }
    public String getFriendlyName(){
        return getCompatibilityName();
    }
    /**
     * If the compatibility is enabled or not.
     * You may wish to override this method and return <code>true</code> for custom compatibilities
     * @return true if this compatibility should be run
     */
    public boolean isEnabled(){
        return enabled.isTrue();
    }
    //not abstract as to not break external compatibilities
    public ItemBuilder getConfigurationDisplayItem(){
        return new ItemBuilder(Material.JIGSAW);
    }
    public void reload(){}
    public Plugin getPlugin(){
        return Bukkit.getPluginManager().getPlugin(getPluginName());
    }
    public boolean isInstalled() {
        return getPlugin()!=null&&getPlugin().isEnabled();
    }
}
