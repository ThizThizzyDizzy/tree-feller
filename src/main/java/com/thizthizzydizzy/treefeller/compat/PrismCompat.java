package com.thizthizzydizzy.treefeller.compat;

import com.thizthizzydizzy.treefeller.*;
import me.botsko.prism.Prism;
import me.botsko.prism.actionlibs.ActionFactory;
import me.botsko.prism.listeners.PrismBlockEvents;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class PrismCompat extends InternalCompatibility {
    public static PrismBlockEvents prismBlockEvents;
    private Prism prismApi;

    @Override
    public String getPluginName() {
        return "Prism";
    }

    @Override
    public void init(TreeFeller treeFeller) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Prism");
        if (plugin != null & plugin.isEnabled()) {
            this.prismApi = (Prism) plugin;
            prismBlockEvents = new PrismBlockEvents(this.prismApi);
        }
    }

    @Override
    public void addBlock(Player player, Block block, BlockState was) {
        prismBlockEvents.onBlockPlace(new BlockPlaceEvent(block, was, block, new ItemStack(block.getType(), 1), player, true, player.getHandRaised()));
    }

    @Override
    public void breakBlock(Tree tree, Tool tool, Player player, ItemStack axe, Block block, List<Modifier> modifiers) {
        prismBlockEvents.onBlockBreak(new BlockBreakEvent(block, player));
    }

    @Override
    public void dropItem(Player player, Item item) {
        ActionFactory.createItemStack("item-drop", item.getItemStack(), item.getItemStack().getAmount(), -1,
                item.getItemStack().getEnchantments(), item.getLocation(), "Environment");
    }

    @Override
    public void placeSapling(Sapling sapling, Player player) {
        Block o = sapling.block.getWorld().getBlockAt(sapling.block.getLocation());
        PrismCompat.prismBlockEvents.onBlockPlace(new BlockPlaceEvent(
                sapling.block,
                o.getState(),
                o.getRelative(BlockFace.DOWN),
                new ItemStack(sapling.block.getType(), 1), player, true, EquipmentSlot.HAND));
    }
}