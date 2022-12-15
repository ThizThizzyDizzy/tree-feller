package com.thizthizzydizzy.treefeller.compat;

import com.thizthizzydizzy.treefeller.*;
import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.system.preset.struct.BlockPreset;
import nl.aurorion.blockregen.system.regeneration.struct.RegenerationProcess;
import nl.aurorion.blockregen.system.region.struct.RegenerationRegion;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class BlockRegenCompat extends InternalCompatibility{
	@Override
	public String getPluginName(){
		return "BlockRegen";
	}
	
	@Override
	public void breakBlock( Tree tree, Tool tool, Player player, ItemStack axe, Block block, List<Modifier> modifiers ){
		BlockRegen brPlugin = nl.aurorion.blockregen.BlockRegen.getInstance( );
		
		boolean useRegions = brPlugin.getConfig( ).getBoolean( "Use-Regions", false );
		RegenerationRegion region = brPlugin.getRegionManager( ).getRegion( block.getLocation( ) );
		
		BlockPreset preset;
		
		if( useRegions && region != null ){
			preset = brPlugin.getPresetManager( ).getPreset( block, region );
		} else {
			preset = brPlugin.getPresetManager( ).getPreset( block );
		}
		RegenerationProcess proc = brPlugin.getRegenerationManager( ).createProcess( block, preset );
		
		if( proc != null ){
			long tl = preset == null ? 1000 : preset.getDelay().getInt() * 1000L;
			BukkitTask task = Bukkit.getScheduler().runTaskLaterAsynchronously(brPlugin, proc, tl / 50L);
		}
	}
	
	@Override
	public boolean defaultEnabled(){
		return false;
	}
}
