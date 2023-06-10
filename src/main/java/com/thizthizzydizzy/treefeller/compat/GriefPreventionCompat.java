package com.thizthizzydizzy.treefeller.compat;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class GriefPreventionCompat extends InternalCompatibility {
    @Override
    public String getPluginName() {
        return "GriefPrevention";
    }

    @Override
    public boolean test(Player player, Block block) {
        return GriefPrevention.instance.allowBreak(player, block,
                block.getLocation()) == null;
    }
}