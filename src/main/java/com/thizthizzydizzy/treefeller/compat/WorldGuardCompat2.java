package com.thizthizzydizzy.treefeller.compat;
import java.util.ArrayList;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
/**
 * This is required to prevent a crash during initialization... Don't ask
 * @author ThizThizzyDizzy
 */
public class WorldGuardCompat2{
    public static boolean test(Player player, Block block){
        ArrayList<Block> blocks = new ArrayList<>();
        blocks.add(block);
        return test(player, blocks)==null;
    }
    public static Block test(Player player, Iterable<Block> blocks){
        com.sk89q.worldguard.LocalPlayer lp = com.sk89q.worldguard.bukkit.WorldGuardPlugin.inst().wrapPlayer(player);
        boolean canBypass = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(lp, lp.getWorld());
        if(!canBypass){
            com.sk89q.worldguard.protection.regions.RegionContainer container = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer();
            if(container==null){//RegionContainer is null. I don't know what this means, probably that there's no regions?
                return null;//all is good, no regions(?)
            }
            com.sk89q.worldguard.protection.regions.RegionQuery query = container.createQuery();
            for(Block b : blocks){
                com.sk89q.worldedit.util.Location loc = new com.sk89q.worldedit.util.Location(lp.getWorld(), b.getX(), b.getY(), b.getZ());
                if(query.queryState(loc, lp, com.sk89q.worldguard.protection.flags.Flags.BLOCK_BREAK)==com.sk89q.worldguard.protection.flags.StateFlag.State.ALLOW){
                    continue;
                }
                if(query.queryState(loc, lp, com.sk89q.worldguard.protection.flags.Flags.BLOCK_BREAK)==com.sk89q.worldguard.protection.flags.StateFlag.State.DENY)return b;
                if(query.queryState(loc, lp, com.sk89q.worldguard.protection.flags.Flags.BUILD)==com.sk89q.worldguard.protection.flags.StateFlag.State.ALLOW){
                    continue;
                }
                return b;
            }
        }
        return null;
    }
}