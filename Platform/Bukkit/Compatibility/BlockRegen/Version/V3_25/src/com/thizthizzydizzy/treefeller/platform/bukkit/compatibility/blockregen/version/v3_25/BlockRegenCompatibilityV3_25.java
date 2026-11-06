package com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.blockregen.version.v3_25;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.compatibility.BukkitPluginCompatibility;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.TreeFellerBukkit;
import com.thizthizzydizzy.treefeller.core.compatibility.*;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BlockRegenCompatibilityV3_25 extends BukkitPluginCompatibility{
    public BlockRegenCompatibilityV3_25(TreeFellerBukkit plugin){super(plugin);}
    @Override protected String requiredClass(){return "nl.aurorion.blockregen.BlockRegenPlugin";}
    @Override
    public String getPluginName(){
        return "BlockRegen";
    }
    @Override
    public String getCompatibilityName(){
        return "blockregen-v3.25+";
    }

    @Override
    public double breakBlock(CompatibilityContext context, Player player, ItemStack axe, Block block){
        nl.aurorion.blockregen.BlockRegenPlugin brPlugin = nl.aurorion.blockregen.BlockRegenPlugin.getInstance();

        boolean useRegions = brPlugin.getConfig().getBoolean("Use-Regions", false);
        nl.aurorion.blockregen.region.struct.RegenerationArea area = useRegions?brPlugin.getRegionManager().getArea(block):null;

        if(area==null)return 1;

        nl.aurorion.blockregen.preset.BlockPreset preset = brPlugin.getPresetManager().getPreset(block, area);

        if(preset==null)return 1;

        nl.aurorion.blockregen.regeneration.struct.RegenerationProcess proc = brPlugin.getRegenerationManager().createProcess(block, preset, area);

        proc.start();

        return 1;
    }

}

