package com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_8;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform.ExplosionObservation;
import com.thizthizzydizzy.treefeller.core.test.TestItem;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.BukkitApiConnector;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.BukkitNbtConnector;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.connector.BukkitConnector;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftLivingEntity;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

/** Direct API integration for 1_8. */
public final class BukkitApiConnectorV1_8 implements BukkitApiConnector {
    @Override
    public Enchantment enchantment(String name) {
        if (name.equalsIgnoreCase("unbreaking"))
            name = "DURABILITY";
        if (name.equalsIgnoreCase("fortune"))
            name = "LOOT_BONUS_BLOCKS";
        return Enchantment.getByName(name.toUpperCase(Locale.ROOT));
    }
    @Override
    public boolean extendedReach() {
        return false;
    }
    @Override
    public ItemStack reachItem(TestItem fixture) {
        throw new UnsupportedOperationException("This Bukkit version has no block interaction range attribute");
    }
    @Override
    public ItemStack canBreakItem(TestItem fixture) {
        ItemStack stack = new ItemStack(Material.matchMaterial(fixture.material));
        BukkitNbtConnector nbt = BukkitConnector.nbt;
        return nbt.item(stack, fixture.legacyCanDestroy(nbt, nbt.item(stack)));
    }
    @Override
    public void unbreakable(ItemMeta meta) {
        throw new UnsupportedOperationException("This Bukkit version has no unbreakable item metadata API");
    }
    @Override
    public void customModelData(ItemMeta meta, Integer value) {
        throw new UnsupportedOperationException("This Bukkit version has no custom model data API");
    }
    @Override
    public Runnable preventItemPickup(Player player) {
        Listener listener = new Listener() {};
        Bukkit.getPluginManager().registerEvent(
                PlayerPickupItemEvent.class, listener, EventPriority.HIGHEST, (ignored, event) -> {
                    PlayerPickupItemEvent pickup = (PlayerPickupItemEvent) event;
                    if (pickup.getPlayer().equals(player))
                        pickup.setCancelled(true);
                }, Bukkit.getPluginManager().getPlugin("treefeller"));
        return () -> HandlerList.unregisterAll(listener);
    }
    @Override
    public void disableAI(LivingEntity entity) {
        ((CraftLivingEntity) entity).getHandle().getDataWatcher().watch(15, (byte) 1);
    }
    @Override
    public String biome(World world, int x, int y, int z) {
        return world.getBiome(x, z).name();
    }
    @Override
    public void biome(World world, int x, int y, int z, String biome) {
        world.setBiome(x, z, Biome.valueOf(biome));
    }
    @Override
    public boolean gameRule(World world, boolean mobs, Boolean value) {
        String rule = mobs ? "doMobSpawning" : "doDaylightCycle";
        return value == null ? Boolean.parseBoolean(world.getGameRuleValue(rule))
                             : world.setGameRuleValue(rule, value.toString());
    }
    @Override
    public boolean invulnerable(Entity entity) {
        return false;
    }
    @Override
    public boolean gravity(Entity entity) {
        return true;
    }
    @Override
    public boolean marker(Entity entity) {
        return false;
    }
    @Override
    public Set<String> markerTags(Entity entity) {
        return Collections.emptySet();
    }
    @Override
    public void registerOutputListeners(
            Listener listener, List<Entity> markers, List<ExplosionObservation> explosions) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("treefeller");
        Bukkit.getPluginManager().registerEvent(
                CreatureSpawnEvent.class, listener, EventPriority.MONITOR, (ignored, event) -> {
                    Entity entity = ((CreatureSpawnEvent) event).getEntity();
                    if (entity instanceof ArmorStand || entity.getType().name().equals("AREA_EFFECT_CLOUD"))
                        markers.add(entity);
                }, plugin);
    }
}
