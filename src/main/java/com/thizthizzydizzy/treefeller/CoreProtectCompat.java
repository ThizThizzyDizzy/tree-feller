package com.thizthizzydizzy.treefeller;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class CoreProtectCompat extends InternalCompatibility {
    @Override
    public String getPluginName() {
        return "CoreProtect";
    }

    @Override
    public void addBlock(Player player, Block block) {
        net.coreprotect.CoreProtect.getInstance().getAPI().logPlacement(player == null ? null : player.getName(), block.getLocation(), block.getType(), block.getBlockData());
    }

    @Override
    public void removeBlock(Player player, Block block) {
        net.coreprotect.CoreProtect.getInstance().getAPI().logRemoval(player == null ? null : player.getName(), block.getLocation(), block.getType(), block.getBlockData());
    }

    @Override
    public void breakBlock(Player player, Block block) {
        net.coreprotect.CoreProtect.getInstance().getAPI().logRemoval(player == null ? null : player.getName(), block.getLocation(), block.getType(), block.getBlockData());
    }
}