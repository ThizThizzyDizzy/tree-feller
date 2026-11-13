package com.thizthizzydizzy.treefeller.platform.bukkit.connector;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform.ExplosionObservation;
import com.thizthizzydizzy.treefeller.core.test.TestItem;
import java.util.List;
import java.util.Set;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/** Native API hooks selected once through the Bukkit version matcher. */
public interface BukkitApiConnector {
    void registerOutputListeners(Listener listener, List<Entity> markers,
            List<ExplosionObservation> explosions);
    boolean invulnerable(Entity entity);
    boolean gravity(Entity entity);
    boolean marker(Entity entity);
    Set<String> markerTags(Entity entity);
    Enchantment enchantment(String name);
    boolean extendedReach();
    ItemStack reachItem(TestItem fixture);
    ItemStack canBreakItem(TestItem fixture);
    void unbreakable(ItemMeta meta);
    void customModelData(ItemMeta meta, Integer value);
    Runnable preventItemPickup(Player player);
    void disableAI(LivingEntity entity);
    String biome(World world, int x, int y, int z);
    void biome(World world, int x, int y, int z, String biome);
    boolean gameRule(World world, boolean mobs, Boolean value);
}
