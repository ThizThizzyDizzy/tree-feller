package com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.jobsreborn;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.compatibility.BukkitPluginCompatibility;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.TreeFellerBukkit;
import com.thizthizzydizzy.treefeller.core.compatibility.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
public class JobsRebornCompat extends BukkitPluginCompatibility{
    public JobsRebornCompat(TreeFellerBukkit plugin){super(plugin);}
    @Override
    public String getPluginName(){
        return "Jobs";
    }
    @Override
    public String getFriendlyName(){
        return "Jobs Reborn";
    }
    @Override
    public double breakBlock(CompatibilityContext context, Player player, ItemStack axe, Block block){
        JobsRebornBlockActions.breakBlock(player, block);

        return 1;
    }
}
