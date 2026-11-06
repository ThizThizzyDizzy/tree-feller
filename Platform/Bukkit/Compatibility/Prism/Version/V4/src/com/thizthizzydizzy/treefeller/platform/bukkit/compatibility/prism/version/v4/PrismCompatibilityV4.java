package com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.prism.version.v4;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.compatibility.BukkitPluginCompatibility;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.TreeFellerBukkit;
import com.thizthizzydizzy.treefeller.core.compatibility.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
public class PrismCompatibilityV4 extends BukkitPluginCompatibility{
    public PrismCompatibilityV4(TreeFellerBukkit plugin){super(plugin);}
    @Override
    public String getPluginName(){
        return "prism";
    }
    @Override
    public String getPluginPackageFilter(){
        return "org.prism_mc.prism";
    }
    @Override
    public String getCompatibilityName(){
        return "Prism 4";
    }
    @Override
    public void initialize(){
        PrismActivityLogger.init(getPlugin());
    }
    @Override
    public void addBlock(Player player, Block block, BlockState was){
        PrismActivityLogger.addBlock(player, block, was);
    }
    @Override
    public double breakBlock(CompatibilityContext context, Player player, ItemStack axe, Block block){
        PrismActivityLogger.breakBlock(player, block);

        return 1;
    }
    @Override
    public void dropItem(Player player, Item item){
        PrismActivityLogger.dropItem(player, item);
    }
}

