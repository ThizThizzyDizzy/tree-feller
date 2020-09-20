package com.thizthizzydizzy.treefeller.compat;
import com.thizthizzydizzy.simplegui.ItemBuilder;
import com.thizthizzydizzy.treefeller.Modifier;
import com.thizthizzydizzy.treefeller.OptionBoolean;
import com.thizthizzydizzy.treefeller.Tool;
import com.thizthizzydizzy.treefeller.Tree;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
public abstract class PluginCompatibility{
    private final OptionBoolean enabled;
    public PluginCompatibility(){
        enabled = new OptionBoolean("Compatibility "+getPluginName(), true, false, false, true, true){
            @Override
            public String getDesc(){
                return "Toggle compatibility with "+PluginCompatibility.this.getFriendlyName();
            }
            @Override
            public String getFriendlyName(){
                return "Compatibility: "+PluginCompatibility.this.getFriendlyName();
            }
            @Override
            public ItemBuilder getConfigurationDisplayItem(){
                return PluginCompatibility.this.getConfigurationDisplayItem();
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
    public void breakBlock(Tree tree, Tool tool, Player player, ItemStack axe, Block block, List<Modifier> modifiers){}
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
    //not abstract as to not break external compatibilities
    public ItemBuilder getConfigurationDisplayItem(){
        return new ItemBuilder(Material.JIGSAW);
    }
}