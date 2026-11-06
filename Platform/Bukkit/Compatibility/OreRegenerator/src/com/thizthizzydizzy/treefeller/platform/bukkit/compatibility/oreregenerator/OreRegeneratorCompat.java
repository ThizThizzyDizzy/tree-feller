package com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.oreregenerator;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.compatibility.BukkitPluginCompatibility;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.TreeFellerBukkit;
import com.thizthizzydizzy.treefeller.core.compatibility.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
public class OreRegeneratorCompat extends BukkitPluginCompatibility{
    public OreRegeneratorCompat(TreeFellerBukkit plugin){super(plugin);}

    @Override
    public String getPluginName(){
        return "OreRegenerator";
    }
    @Override
    public double breakBlock(CompatibilityContext context, Player player, ItemStack axe, Block block){
        dev.mrshawn.oreregenerator.api.utils.RegenUtils.doRegen(block.getLocation(), block.getType(), block.getBlockData(), TreeFellerCompatibility.settings(context).oreregenerator_regen_delay);

        return 1;
    }

}
