package com.thizthizzydizzy.treefeller;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
public class JobsRebornCompat extends InternalCompatibility{
    @Override
    public String getPluginName(){
        return "Jobs";
    }
    @Override
    public void breakBlock(Player player, Block block){
        if(player==null)return;
        com.gamingmesh.jobs.container.JobsPlayer jp = com.gamingmesh.jobs.Jobs.getPlayerManager().getJobsPlayer(player);
        if(jp!=null)com.gamingmesh.jobs.Jobs.action(jp, new com.gamingmesh.jobs.actions.BlockActionInfo(block, com.gamingmesh.jobs.container.ActionType.BREAK));
    }
}