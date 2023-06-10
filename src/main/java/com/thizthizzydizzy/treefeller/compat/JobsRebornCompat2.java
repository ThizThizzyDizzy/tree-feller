package com.thizthizzydizzy.treefeller.compat;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
/**
 * This is required to prevent a crash during initialization... Don't ask
 * @author ThizThizzyDizzy
 */
public class JobsRebornCompat2{
    public static void breakBlock(Player player, Block block){
        if(player==null)return;
        com.gamingmesh.jobs.container.JobsPlayer jp = com.gamingmesh.jobs.Jobs.getPlayerManager().getJobsPlayer(player);
        if(jp!=null)com.gamingmesh.jobs.Jobs.action(jp, new com.gamingmesh.jobs.actions.BlockActionInfo(block, com.gamingmesh.jobs.container.ActionType.BREAK));
    }
}