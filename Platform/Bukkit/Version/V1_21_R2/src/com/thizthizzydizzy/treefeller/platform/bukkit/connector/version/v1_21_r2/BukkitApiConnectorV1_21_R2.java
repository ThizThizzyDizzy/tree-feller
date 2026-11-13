package com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_21_r2;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform.ExplosionObservation;
import com.thizthizzydizzy.treefeller.core.test.TestItem;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.BukkitApiConnector;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.block.Biome;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

/** Direct API integration for 1_21_R2. */
public final class BukkitApiConnectorV1_21_R2 implements BukkitApiConnector {
    @Override
    public Enchantment enchantment(String name) {
        Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(name.toLowerCase(Locale.ROOT)));
        return enchantment != null ? enchantment : Enchantment.getByName(name.toUpperCase(Locale.ROOT));
    }
    @Override
    public boolean extendedReach() {
        return true;
    }
    @Override
    public ItemStack reachItem(TestItem fixture) {
        ItemStack stack = new ItemStack(Material.matchMaterial(fixture.material));
        ItemMeta meta = stack.getItemMeta();
        for (TestItem.AttributeModifier entry : fixture.attributeModifiers())
            meta.addAttributeModifier(Attribute.BLOCK_INTERACTION_RANGE,
                    new AttributeModifier(UUID.nameUUIDFromBytes(entry.id.getBytes(StandardCharsets.UTF_8)), entry.id,
                            entry.amount, Operation.ADD_NUMBER, EquipmentSlot.HAND));
        stack.setItemMeta(meta);
        return stack;
    }
    @Override
    public ItemStack canBreakItem(TestItem fixture) {
        String blocks = fixture.canBreakBlocks();
        return Bukkit.getItemFactory().createItemStack(
                fixture.material + "[can_break={predicates:[{blocks:" + blocks + "}]}]");
    }
    @Override
    public void unbreakable(ItemMeta meta) {
        meta.setUnbreakable(true);
    }
    @Override
    public void customModelData(ItemMeta meta, Integer value) {
        meta.setCustomModelData(value);
    }
    @Override
    public Runnable preventItemPickup(Player player) {
        Listener listener = new Listener() {};
        Bukkit.getPluginManager().registerEvent(
                EntityPickupItemEvent.class, listener, EventPriority.HIGHEST, (ignored, event) -> {
                    EntityPickupItemEvent pickup = (EntityPickupItemEvent) event;
                    if (pickup.getEntity().equals(player))
                        pickup.setCancelled(true);
                }, Bukkit.getPluginManager().getPlugin("treefeller"));
        return () -> HandlerList.unregisterAll(listener);
    }
    @Override
    public void disableAI(LivingEntity entity) {
        entity.setAI(false);
    }
    @Override
    public String biome(World world, int x, int y, int z) {
        return world.getBiome(x, y, z).name();
    }
    @Override
    public void biome(World world, int x, int y, int z, String biome) {
        world.setBiome(x, y, z, Biome.valueOf(biome));
    }
    @Override
    public boolean gameRule(World world, boolean mobs, Boolean value) {
        GameRule<Boolean> rule = mobs ? GameRule.DO_MOB_SPAWNING : GameRule.DO_DAYLIGHT_CYCLE;
        return value == null ? world.getGameRuleValue(rule) : world.setGameRule(rule, value);
    }
    @Override
    public boolean invulnerable(Entity entity) {
        return entity.isInvulnerable();
    }
    @Override
    public boolean gravity(Entity entity) {
        return entity.hasGravity();
    }
    @Override
    public boolean marker(Entity entity) {
        return entity instanceof ArmorStand && ((ArmorStand) entity).isMarker();
    }
    @Override
    public Set<String> markerTags(Entity entity) {
        return entity.getScoreboardTags();
    }
    @Override
    public void registerOutputListeners(
            Listener listener, List<Entity> markers, List<ExplosionObservation> explosions) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("treefeller");
        Bukkit.getPluginManager().registerEvent(
                EntitySpawnEvent.class, listener, EventPriority.MONITOR, (ignored, event) -> {
                    Entity entity = ((EntitySpawnEvent) event).getEntity();
                    if (entity instanceof ArmorStand || entity.getType().name().equals("AREA_EFFECT_CLOUD"))
                        markers.add(entity);
                }, plugin);
        Bukkit.getPluginManager().registerEvent(
                BlockExplodeEvent.class, listener, EventPriority.MONITOR, (ignored, event) -> {
                    BlockExplodeEvent explosion = (BlockExplodeEvent) event;
                    Location p = explosion.getBlock().getLocation();
                    explosions.add(
                            new ExplosionObservation(p.getX(), p.getY(), p.getZ(), explosion.blockList().size()));
                }, plugin);
    }
}
