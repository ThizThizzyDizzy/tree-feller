package com.thizthizzydizzy.treefeller.platform.bukkit.connector;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockAxis;
import java.util.Collection;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
public interface BukkitBlockDataConnector{
    java.util.function.Consumer<Location> prepareParticle(com.thizthizzydizzy.treefeller.core.config.structure.section.result.EffectConfiguration effect);
    com.thizthizzydizzy.treefeller.core.connector.world.BlockSnapshot snapshot(Block block);
    void place(Block block, com.thizthizzydizzy.treefeller.core.connector.world.BlockSnapshot data);
    org.bukkit.entity.FallingBlock spawn(Location location, com.thizthizzydizzy.treefeller.core.connector.world.BlockSnapshot data);
    boolean passable(Block block);
    public boolean supportsTestMaterial(String material);
    public void setTestBlock(Block block, String material, BlockAxis axis, int leafDistance);
    public String testBlockState(Block block);
    public boolean isTestMaterial(Block block, String material);
    public Collection<ItemStack> getDrops(Block block, ItemStack tool, Player player);
    public BlockAxis getBlockAxis(Block block);
    public int getLeafDistance(Block block);
    /** Null when the block has no persistent state. */
    Boolean getPersistent(Block block);
    void setTestPersistent(Block block, boolean persistent);
    public void restoreBlockChange(Player player, Location location);
    public void sendBlockChange(Player player, Location location, Material material);
}
