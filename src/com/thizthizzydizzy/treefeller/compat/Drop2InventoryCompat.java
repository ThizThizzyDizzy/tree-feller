package com.thizthizzydizzy.treefeller.compat;
import com.thizthizzydizzy.treefeller.Modifier;
import com.thizthizzydizzy.treefeller.Tool;
import com.thizthizzydizzy.treefeller.Tree;
import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
public class Drop2InventoryCompat extends InternalCompatibility{
    @Override
    public String getPluginName(){
        return "Drop2Inventory";
    }
    @Override
    public void breakBlock(Tree tree, Tool tool, Player player, ItemStack axe, Block block, List<Modifier> modifiers){
        de.jeff_media.drop2inventory.Drop2InventoryAPI.registerFutureDrop(player, block);
    }
    @Override
    public void dropItem(Player player, Item item){
        de.jeff_media.drop2inventory.Drop2InventoryAPI.registerFutureDrop(player, item.getLocation().getBlock());
    }
}