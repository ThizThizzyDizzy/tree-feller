package com.thizthizzydizzy.treefeller.compat;

import com.palmergames.bukkit.towny.event.executors.TownyActionEventExecutor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class TownyCompat extends InternalCompatibility {
    @Override
    public String getPluginName() {
        return "Towny";
    }

    @Override
    public boolean test(Player player, Block block) {
        return TownyActionEventExecutor.canDestroy(player,
                block.getLocation(), block.getType());
    }
}