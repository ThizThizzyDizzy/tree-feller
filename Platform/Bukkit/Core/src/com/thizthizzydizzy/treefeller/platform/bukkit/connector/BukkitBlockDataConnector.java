package com.thizthizzydizzy.treefeller.platform.bukkit.connector;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockAxis;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
public interface BukkitBlockDataConnector{
    public BlockAxis getBlockAxis(Block block);
    public int getLeafDistance(Block block);
    public void sendBlockChange(Player player, Location location, Material material);
}
