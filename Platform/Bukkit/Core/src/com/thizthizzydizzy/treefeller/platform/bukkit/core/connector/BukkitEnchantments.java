package com.thizthizzydizzy.treefeller.platform.bukkit.core.connector;
import org.bukkit.enchantments.Enchantment;
/** Translate Minecraft enchantment keys through the selected Bukkit API. */
public final class BukkitEnchantments {
    private BukkitEnchantments() {}
    public static Enchantment find(String name) {
        return name == null ? null : BukkitConnector.api.enchantment(name);
    }
}
