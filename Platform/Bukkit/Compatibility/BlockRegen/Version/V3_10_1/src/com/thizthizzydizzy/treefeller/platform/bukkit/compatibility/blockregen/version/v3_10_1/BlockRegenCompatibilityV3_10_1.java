package com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.blockregen.version.v3_10_1;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.compatibility.BukkitPluginCompatibility;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.TreeFellerBukkit;
import com.thizthizzydizzy.treefeller.core.compatibility.*;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public class BlockRegenCompatibilityV3_10_1 extends BukkitPluginCompatibility{
    public BlockRegenCompatibilityV3_10_1(TreeFellerBukkit plugin){super(plugin);}
    @Override protected String requiredClass(){return "nl.aurorion.blockregen.BlockRegen";}
    @Override
    public String getPluginName(){
        return "BlockRegen";
    }

    @Override
    public double breakBlock(CompatibilityContext context, Player player, ItemStack axe, Block block){
        nl.aurorion.blockregen.BlockRegen brPlugin = nl.aurorion.blockregen.BlockRegen.getInstance();

        boolean useRegions = brPlugin.getConfig().getBoolean("Use-Regions", false);
        nl.aurorion.blockregen.system.region.struct.RegenerationRegion region = brPlugin.getRegionManager().getRegion(block.getLocation());

        nl.aurorion.blockregen.system.preset.struct.BlockPreset preset;

        if(useRegions&&region!=null){
            preset = brPlugin.getPresetManager().getPreset(block, region);
        }else{
            preset = brPlugin.getPresetManager().getPreset(block);
        }
        nl.aurorion.blockregen.system.regeneration.struct.RegenerationProcess proc = brPlugin.getRegenerationManager().createProcess(block, preset);

        if(proc!=null){
            long tl = preset==null?1000:preset.getDelay().getInt()*1000L;
            BukkitTask task = Bukkit.getScheduler().runTaskLaterAsynchronously(brPlugin, proc, tl/50L);
        }

        return 1;
    }

}

