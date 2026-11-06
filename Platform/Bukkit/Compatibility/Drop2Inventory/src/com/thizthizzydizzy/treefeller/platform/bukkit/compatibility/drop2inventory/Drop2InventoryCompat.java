package com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.drop2inventory;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.compatibility.BukkitPluginCompatibility;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.TreeFellerBukkit;
import com.thizthizzydizzy.treefeller.core.compatibility.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
public class Drop2InventoryCompat extends BukkitPluginCompatibility{
    public Drop2InventoryCompat(TreeFellerBukkit plugin){super(plugin);}
    @Override
    public String getPluginName(){
        return "Drop2Inventory";
    }
    @Override
    public double breakBlock(CompatibilityContext context, Player player, ItemStack axe, Block block){
        de.jeff_media.drop2inventory.Drop2InventoryAPI.registerFutureDrop(player, block);

        return 1;
    }
    @Override
    public void dropItem(Player player, Item item){
        de.jeff_media.drop2inventory.Drop2InventoryAPI.registerFutureDrop(player, item.getLocation().getBlock());
    }
}
