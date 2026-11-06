package com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.lands;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.compatibility.BukkitPluginCompatibility;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.TreeFellerBukkit;
import com.thizthizzydizzy.treefeller.core.compatibility.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
public class LandsCompat extends BukkitPluginCompatibility{
    public LandsCompat(TreeFellerBukkit plugin){super(plugin);}
    private me.angeschossen.lands.api.LandsIntegration integration;
    @Override
    public String getPluginName(){
        return "Lands";
    }
    @Override
    public void initialize(){
        integration = me.angeschossen.lands.api.LandsIntegration.of(plugin);
    }

    @Override
    public boolean test(Player player, Block block) {
        me.angeschossen.lands.api.land.Area area = integration.getArea(block.getLocation());
        return area==null||area.hasRoleFlag(integration.getLandPlayer(player.getUniqueId()), me.angeschossen.lands.api.flags.type.Flags.BLOCK_BREAK, block.getType(), false);
    }
}
