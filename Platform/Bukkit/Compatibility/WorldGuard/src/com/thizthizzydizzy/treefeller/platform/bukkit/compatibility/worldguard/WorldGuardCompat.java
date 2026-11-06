package com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.worldguard;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.compatibility.BukkitPluginCompatibility;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.TreeFellerBukkit;
import com.thizthizzydizzy.treefeller.core.compatibility.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
public class WorldGuardCompat extends BukkitPluginCompatibility{
    public WorldGuardCompat(TreeFellerBukkit plugin){super(plugin);}
    @Override
    public String getPluginName(){
        return "WorldGuard";
    }
    @Override
    public boolean test(Player player, Block block){
        return WorldGuardRegionQuery.test(player, block);
    }

    @Override public Runnable testRegion(com.thizthizzydizzy.treefeller.core.connector.world.IWorldConnector world,long min,long max,java.util.UUID owner){
        org.bukkit.World nativeWorld=((com.thizthizzydizzy.treefeller.platform.bukkit.core.connector.BukkitWorldConnector)world).getWorld();
        com.sk89q.worldguard.protection.managers.RegionManager manager=com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer().get(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(nativeWorld));
        com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion region=new com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion("treefeller_"+java.util.UUID.randomUUID().toString().replace("-",""),
            com.sk89q.worldedit.math.BlockVector3.at(com.thizthizzydizzy.treefeller.core.connector.world.BlockPos.getX(min),com.thizthizzydizzy.treefeller.core.connector.world.BlockPos.getY(min),com.thizthizzydizzy.treefeller.core.connector.world.BlockPos.getZ(min)),
            com.sk89q.worldedit.math.BlockVector3.at(com.thizthizzydizzy.treefeller.core.connector.world.BlockPos.getX(max),com.thizthizzydizzy.treefeller.core.connector.world.BlockPos.getY(max),com.thizthizzydizzy.treefeller.core.connector.world.BlockPos.getZ(max)));
        region.getOwners().addPlayer(owner);manager.addRegion(region);
        return () -> manager.removeRegion(region.getId());
    }
}
