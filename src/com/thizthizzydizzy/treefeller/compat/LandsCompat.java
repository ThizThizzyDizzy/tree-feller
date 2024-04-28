package com.thizthizzydizzy.treefeller.compat;
import com.thizthizzydizzy.treefeller.TreeFeller;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
public class LandsCompat extends InternalCompatibility{
    private me.angeschossen.lands.api.LandsIntegration integration;
    @Override
    public String getPluginName(){
        return "Lands";
    }
    @Override
    public void init(TreeFeller treeFeller){
        integration = me.angeschossen.lands.api.LandsIntegration.of(treeFeller);
    }
    
    @Override
    public boolean test(Player player, Block block) {
        me.angeschossen.lands.api.land.Area area = integration.getArea(block.getLocation());
        return area==null||area.hasRoleFlag(integration.getLandPlayer(player.getUniqueId()), me.angeschossen.lands.api.flags.type.Flags.BLOCK_BREAK, block.getType(), false);
    }
}