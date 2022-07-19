package com.thizthizzydizzy.treefeller.compat;
import com.thizthizzydizzy.treefeller.TreeFeller;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
public class LandsCompat extends InternalCompatibility{
    private me.angeschossen.lands.api.integration.LandsIntegration integration;
    @Override
    public String getPluginName(){
        return "Lands";
    }
    @Override
    public void init(TreeFeller treeFeller){
        integration = new me.angeschossen.lands.api.integration.LandsIntegration(treeFeller);
    }
    
    @Override
    public boolean test(Player player, Block block) {
        me.angeschossen.lands.api.land.Area area = integration.getAreaByLoc(block.getLocation());
        return area==null||area.hasFlag(player, me.angeschossen.lands.api.flags.Flags.BLOCK_BREAK, false);
    }
}