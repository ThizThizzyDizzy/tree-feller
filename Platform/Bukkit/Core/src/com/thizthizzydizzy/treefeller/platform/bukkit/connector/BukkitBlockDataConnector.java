package com.thizthizzydizzy.treefeller.platform.bukkit.connector;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockAxis;
import java.util.Collection;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
public interface BukkitBlockDataConnector{
    public Collection<ItemStack> getDrops(Block block, ItemStack tool, Player player);
    public BlockAxis getBlockAxis(Block block);
    public int getLeafDistance(Block block);
    public void sendBlockChange(Player player, Location location, Material material);
}
