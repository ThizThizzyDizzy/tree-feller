package com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.griefprevention;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.compatibility.BukkitPluginCompatibility;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.TreeFellerBukkit;
import com.thizthizzydizzy.treefeller.core.compatibility.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
public class GriefPreventionCompat extends BukkitPluginCompatibility{
    public GriefPreventionCompat(TreeFellerBukkit plugin){super(plugin);}
    @Override
    public String getPluginName(){
        return "GriefPrevention";
    }
    @Override
    public boolean test(Player player, Block block) {
        return me.ryanhamshire.GriefPrevention.GriefPrevention.instance.allowBreak(player, block, block.getLocation())==null;
    }
    @Override public Runnable testRegion(com.thizthizzydizzy.treefeller.core.connector.world.IWorldConnector world,long min,long max,java.util.UUID owner){
        me.ryanhamshire.GriefPrevention.DataStore store=me.ryanhamshire.GriefPrevention.GriefPrevention.instance.dataStore;
        me.ryanhamshire.GriefPrevention.CreateClaimResult result=store.createClaim(
            ((com.thizthizzydizzy.treefeller.platform.bukkit.core.connector.BukkitWorldConnector)world).getWorld(),
            com.thizthizzydizzy.treefeller.core.connector.world.BlockPos.getX(min),com.thizthizzydizzy.treefeller.core.connector.world.BlockPos.getX(max),
            com.thizthizzydizzy.treefeller.core.connector.world.BlockPos.getY(min),com.thizthizzydizzy.treefeller.core.connector.world.BlockPos.getY(max),
            com.thizthizzydizzy.treefeller.core.connector.world.BlockPos.getZ(min),com.thizthizzydizzy.treefeller.core.connector.world.BlockPos.getZ(max),owner,null,null,null);
        if(!result.succeeded)throw new IllegalStateException("Could not create test claim");
        return () -> store.deleteClaim(result.claim);
    }
}
