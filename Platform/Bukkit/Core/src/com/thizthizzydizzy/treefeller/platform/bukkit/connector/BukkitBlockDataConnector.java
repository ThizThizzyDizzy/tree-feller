package com.thizthizzydizzy.treefeller.platform.bukkit.connector;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockAxis;
import org.bukkit.block.Block;
public interface BukkitBlockDataConnector{
    public BlockAxis getBlockAxis(Block block);
    public int getLeafDistance(Block block);
}
