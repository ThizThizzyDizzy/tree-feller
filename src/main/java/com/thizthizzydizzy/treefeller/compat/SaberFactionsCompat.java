package com.thizthizzydizzy.treefeller.compat;

import com.massivecraft.factions.listeners.FactionsBlockListener;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class SaberFactionsCompat extends InternalCompatibility {
    @Override
    public String getPluginName() {
        return "SaberFactions";
    }

    @Override
    public boolean test(Player player, Block block) {
        return FactionsBlockListener.playerCanBuildDestroyBlock(player,
                block.getLocation(), "destroy", true);
    }
}