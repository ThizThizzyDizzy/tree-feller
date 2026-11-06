package com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.prism.version.v3;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.compatibility.BukkitPluginCompatibility;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.TreeFellerBukkit;
import com.thizthizzydizzy.treefeller.core.compatibility.*;
import network.darkhelmet.prism.Prism;
import network.darkhelmet.prism.actionlibs.ActionFactory;
import network.darkhelmet.prism.listeners.PrismBlockEvents;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
public class PrismCompatibilityV3 extends BukkitPluginCompatibility{
    public PrismCompatibilityV3(TreeFellerBukkit plugin){super(plugin);}
    private Prism prism;
    private PrismBlockEvents prismBlockEvents;
    @Override
    public String getPluginName(){
        return "Prism";
    }
    @Override
    public String getPluginPackageFilter(){
        return "network.darkhelmet.prism";
    }
    @Override
    public String getCompatibilityName(){
        return "Prism 3";
    }
    @Override
    public void initialize(){
        prism = (Prism)getPlugin();
        prismBlockEvents = new PrismBlockEvents(prism);
    }
    @Override
    public void addBlock(Player player, Block block, BlockState was){
        prismBlockEvents.onBlockPlace(new BlockPlaceEvent(block, was, block, new ItemStack(block.getType(), 1), player, true));
    }
    @Override
    public double breakBlock(CompatibilityContext context, Player player, ItemStack axe, Block block){
        prismBlockEvents.onBlockBreak(new BlockBreakEvent(block, player));

        return 1;
    }
    @Override
    public void dropItem(Player player, Item item){
        ActionFactory.createItemStack("item-drop", item.getItemStack(), item.getItemStack().getAmount(), -1, item.getItemStack().getEnchantments(), item.getLocation(), "Environment");
    }
}
