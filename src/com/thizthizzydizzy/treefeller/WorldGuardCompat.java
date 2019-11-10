package com.thizthizzydizzy.treefeller;
import java.util.ArrayList;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
public class WorldGuardCompat{
    public static Block test(Player player, ArrayList<Block>... blockses){
        com.sk89q.worldguard.LocalPlayer lp = com.sk89q.worldguard.bukkit.WorldGuardPlugin.inst().wrapPlayer(player);
        boolean canBypass = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(lp, lp.getWorld());
        if(!canBypass){
            com.sk89q.worldguard.protection.regions.RegionContainer container = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer();
            com.sk89q.worldguard.protection.regions.RegionQuery query = container.createQuery();
            for(ArrayList<Block> blocks : blockses){
                for(Block b : blocks){
                    com.sk89q.worldedit.util.Location loc = new com.sk89q.worldedit.util.Location(lp.getWorld(), b.getX(), b.getY(), b.getZ());
                    if(!query.testState(loc, lp, com.sk89q.worldguard.protection.flags.Flags.BLOCK_BREAK)){
                        return b;
                    }
                }
            }
        }
        return null;
    }
}