package com.thizthizzydizzy.treefeller.platform.bukkit.connector;
import com.thizthizzydizzy.treefeller.core.nbt.NbtAccess;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
/** Only native parsing, serialization, and application belong in version modules. */
public interface BukkitNbtConnector extends NbtAccess{
    byte[] block(Block block);
    void block(Block block,byte[] data);
    String itemDataKey();
    byte[] item(ItemStack item);
    ItemStack item(ItemStack item,byte[] data);
}
