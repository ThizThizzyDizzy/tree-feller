package com.thizthizzydizzy.treefeller.platform.bukkit.core.connector;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IItemDefinition;
import com.thizthizzydizzy.treefeller.core.connector.item.IItemConnector;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import com.thizthizzydizzy.treefeller.core.connector.player.PlayerGameMode;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockAxis;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
import com.thizthizzydizzy.treefeller.core.connector.world.IWorldConnector;
import com.thizthizzydizzy.treefeller.core.nbt.NbtCompound;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
import com.thizthizzydizzy.treefeller.core.test.TestItem;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.Config;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.ConfigValue;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.TreeFellerBukkit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.LongPredicate;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

/** Bukkit API translation only; fixture construction lives in Core. */
public final class BukkitTestPlatform implements ITestPlatform{
    @Override public Runnable operator(String name,boolean enabled){
        Player target=player(name);boolean previous=target.isOp();target.setOp(enabled);return () -> target.setOp(previous);
    }
    @Override public UUID playerId(String name){return player(name).getUniqueId();}
    @Override public BreakCapture captureBreaks(String name,LongPredicate cancelled){
        List<Long> positions=new ArrayList<>();
        Listener listener=new Listener(){};
        Bukkit.getPluginManager().registerEvent(BlockBreakEvent.class,listener,EventPriority.HIGH,(ignored,event) -> {
            BlockBreakEvent broken=(BlockBreakEvent)event;
            if(!broken.getPlayer().getName().equals(name))return;
            Block block=broken.getBlock();
            long pos=BlockPos.toPos(block.getX(),block.getY(),block.getZ());
            positions.add(pos);
            if(cancelled.test(pos))broken.setCancelled(true);
        },Bukkit.getPluginManager().getPlugin("treefeller"));
        return new BreakCapture(){
            @Override public List<Long> positions(){return new ArrayList<>(positions);}
            @Override public void close(){HandlerList.unregisterAll(listener);}
        };
    }
    private Player player(String name){
        Player player = Bukkit.getPlayerExact(name);
        if(player==null)throw new IllegalArgumentException("Test player is offline: "+name);
        return player;
    }
    @Override public List<String> controlEntries(String name){
        List<String> entries=new ArrayList<>();
        for(ItemStack item:player(name).getOpenInventory().getTopInventory().getContents())
            entries.add(item==null||!item.hasItemMeta()?"":item.getItemMeta().getDisplayName());
        return entries;
    }
    @Override public void clickControl(String name,int slot){
        Bukkit.getPluginManager().callEvent(new InventoryClickEvent(player(name).getOpenInventory(),
                SlotType.CONTAINER,slot,
                ClickType.LEFT,InventoryAction.PICKUP_ALL));
    }
    @Override public void submitControlText(String name,String text){
        Bukkit.getPluginManager().callEvent(new AsyncPlayerChatEvent(false,player(name),text,new HashSet<>(Bukkit.getOnlinePlayers())));
    }
    @Override public void closeControl(String name){player(name).closeInventory();}
    @Override public ItemCapture captureItems(String name){
        World world=player(name).getWorld();
        List<ItemObservation> items=new ArrayList<>();
        Listener listener=new Listener(){};
        Bukkit.getPluginManager().registerEvent(ItemSpawnEvent.class,listener,EventPriority.MONITOR,(ignored,event) -> {
            Item item=((ItemSpawnEvent)event).getEntity();
            if(item.getWorld()!=world)return;
            Location p=item.getLocation();ItemStack stack=item.getItemStack();
            items.add(new ItemObservation(p.getX(),p.getY(),p.getZ(),stack.getType().name().toLowerCase(Locale.ROOT),stack.getAmount()));
        },Bukkit.getPluginManager().getPlugin("treefeller"),true);
        return new ItemCapture(){
            @Override public List<ItemObservation> items(){return new ArrayList<>(items);}
            @Override public void close(){HandlerList.unregisterAll(listener);}
        };
    }
    @Override public TestOutputRecorder recordOutput(String name){return new BukkitTestOutputRecorder(player(name));}
    @Override
    public ITestWorld world(String player){
        return new TestWorld(player(player).getWorld());
    }
    @Override
    public void preparePlayer(String name, boolean survival){
        Player player = player(name);
        player.setGameMode(survival?GameMode.SURVIVAL:GameMode.CREATIVE);
        player.getInventory().clear();
        player.setItemInHand(new ItemStack(Material.IRON_AXE));
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setFallDistance(0);
    }
    @Override
    public void setPlayerGameMode(String name, PlayerGameMode mode){
        player(name).setGameMode(GameMode.valueOf(mode.name()));
    }
    @Override
    public void setPlayerSneaking(String name, boolean sneaking){
        player(name).setSneaking(sneaking);
    }
    @Override
    public Runnable permissions(String name, Map<String, Boolean> values){
        PermissionAttachment attachment = player(name).addAttachment(
                JavaPlugin.getPlugin(TreeFellerBukkit.class));
        values.forEach(attachment::setPermission);
        return attachment::remove;
    }
    @Override public boolean supportsMaterial(String material){return Material.matchMaterial(material)!=null;}
    @Override public boolean supportsExtendedReach(){return BukkitConnector.api.extendedReach();}
    @Override public boolean usesItemComponents(){return BukkitConnector.nbt.itemDataKey().equals("components");}
    @Override
    public void equipItem(String name,String definition){
        TestItem fixture=new TestItem(definition);
        Config properties=fixture.components;
        ItemStack stack=new ItemStack(Material.matchMaterial(fixture.material));
        ItemMeta meta=stack.getItemMeta();
        if(properties.hasPath("attribute_modifiers")){
            stack=BukkitConnector.api.reachItem(fixture);
        }else if(properties.hasPath("can_break")){
            stack=BukkitConnector.api.canBreakItem(fixture);
        }else{
                if(properties.hasPath("custom_name"))meta.setDisplayName(properties.getString("custom_name"));
                if(properties.hasPath("lore"))meta.setLore(properties.getStringList("lore"));
                if(properties.hasPath("unbreakable"))BukkitConnector.api.unbreakable(meta);
                if(properties.hasPath("enchantments"))for(Entry<String,ConfigValue> entry:properties.getConfig("enchantments").root().entrySet()){
                    Enchantment enchantment=BukkitEnchantments.find(entry.getKey());
                    if(enchantment==null)throw new IllegalArgumentException("Unknown fixture enchantment "+entry.getKey());
                    meta.addEnchant(enchantment,((Number)entry.getValue().unwrapped()).intValue(),true);
                }
                if(properties.hasPath("tooltip_display.hidden_components"))for(String hidden:properties.getStringList("tooltip_display.hidden_components")){
                    if(!hidden.equals("minecraft:enchantments"))throw new IllegalArgumentException("Unknown fixture tooltip component "+hidden);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
                stack.setItemMeta(meta);
                if(properties.hasPath("damage"))stack.setDurability((short)properties.getInt("damage"));
                if(fixture.customData!=null){
                    NbtCompound serialized=NbtCompound.read(BukkitConnector.nbt.item(stack));
                    NbtCompound data=NbtCompound.read(BukkitConnector.nbt.parse(fixture.customData));
                    String key=BukkitConnector.nbt.itemDataKey();
                    if(key.equals("components"))data=NbtCompound.read(BukkitConnector.nbt.parse("{}" )).withChild("minecraft:custom_data",data);
                    stack=BukkitConnector.nbt.item(stack,serialized.withChild(key,serialized.child(key).merge(data)).bytes());
                }
            }
        player(name).setItemInHand(stack);
    }
    @Override public void setCustomModelData(String name,Integer value){
        ItemStack item=player(name).getItemInHand();
        ItemMeta meta=item.getItemMeta();
        BukkitConnector.api.customModelData(meta,value);
        item.setItemMeta(meta);player(name).setItemInHand(item);
    }
    @Override public Runnable preventItemPickup(String name){return BukkitConnector.api.preventItemPickup(player(name));}
    @Override
    public void setToolCount(String name, int count){
        ItemStack stack = player(name).getItemInHand();
        stack.setAmount(count);
        player(name).setItemInHand(stack);
    }
    @Override
    public void setPlayerLevels(String name, int food, float health, float saturation){
        Player player = player(name);
        player.setFoodLevel(food);
        player.setHealth(health);
        player.setSaturation(saturation);
        player.setExhaustion(0);
    }
    @Override
    public IItemConnector tool(String name){
        return new BukkitItemConnector(player(name).getItemInHand());
    }
    @Override
    public IPlayerConnector playerState(String name){
        return JavaPlugin.getPlugin(TreeFellerBukkit.class).getPlayerConnector(player(name));
    }
    @Override
    public int inventoryItemCount(String name, String material){
        int count = 0;
        Material type = Material.valueOf(material.toUpperCase(Locale.ROOT));
        for(ItemStack stack : player(name).getInventory().getContents())
            if(stack!=null&&stack.getType()==type)count += stack.getAmount();
        return count;
    }
    @Override
    public void setInventoryItem(String name, int slot, String material, int count){
        player(name).getInventory().setItem(slot, new ItemStack(Material.valueOf(material.toUpperCase(Locale.ROOT)), count));
    }
    @Override
    public void prepareCamera(String name){
        preparePlayer(name, false);
        player(name).setFlying(true);
    }
    @Override
    public void showTestStatus(String name, String message){
        player(name).sendMessage("\u00a7e[TreeFeller test] \u00a7f"+message);
    }
    private static final class TestWorld implements ITestWorld{
        private final World world;
        TestWorld(World world){this.world = world;}
        @Override public List<FallingObservation> fallingBlocks(){
            List<FallingObservation> result=new ArrayList<>();
            for(Entity entity : world.getEntities())if(entity instanceof FallingBlock){
                Location p=entity.getLocation();Vector v=entity.getVelocity();
                result.add(new FallingObservation(p.getX(),p.getY(),p.getZ(),v.getX(),v.getY(),v.getZ(),
                        ((FallingBlock)entity).getMaterial().name().toLowerCase(Locale.ROOT)));
            }
            return result;
        }
        @Override public void removeFallingBlocks(){
            for(Entity entity : world.getEntities())if(entity instanceof FallingBlock){
                BukkitFallingBlock tracked=BukkitFallingBlock.get(entity);
                if(tracked==null)entity.remove();else tracked.remove();
            }
        }
        @Override public TestTarget spawnTarget(double x,double y,double z){
            LivingEntity target=(LivingEntity)world.spawnEntity(new Location(world,x,y,z),EntityType.VILLAGER);
            BukkitConnector.api.disableAI(target);
            target.setHealth(20);
            return new TestTarget(){
                @Override public float health(){return (float)target.getHealth();}
                @Override public void remove(){target.remove();}
            };
        }
        @Override public int fireCount(int minX,int minY,int minZ,int maxX,int maxY,int maxZ){
            int count=0;
            for(int x=minX;x<=maxX;x++)for(int y=minY;y<=maxY;y++)for(int z=minZ;z<=maxZ;z++)
                if(world.getBlockAt(x,y,z).getType()==Material.FIRE)count++;
            return count;
        }
        @Override public IWorldConnector state(){return new BukkitWorldConnector(world);}
        @Override public int droppedItemCount(IItemDefinition definition,int minX,int minY,int minZ,int maxX,int maxY,int maxZ){
            int count=0;
            for(Entity entity:world.getEntities()){
                Location location=entity.getLocation();
                if(location.getX()<minX||location.getX()>maxX||location.getY()<minY||location.getY()>maxY||location.getZ()<minZ||location.getZ()>maxZ)continue;
                if(entity instanceof Item){
                    ItemStack stack=((Item)entity).getItemStack();
                    if(new BukkitItemConnector(stack).matches(definition))count+=stack.getAmount();
                }
            }
            return count;
        }
        @Override public int maxHeight(){return world.getMaxHeight();}
        @Override public boolean supports(String material){return BukkitConnector.blockData.supportsTestMaterial(material);}
        @Override public void setBlock(int x, int y, int z, String material, BlockAxis axis, int leafDistance){
            BukkitConnector.blockData.setTestBlock(world.getBlockAt(x, y, z), material, axis, leafDistance);
        }
        @Override public void setPersistent(int x, int y, int z, boolean persistent){
            BukkitConnector.blockData.setTestPersistent(world.getBlockAt(x,y,z),persistent);
        }
        @Override public String blockState(int x, int y, int z){return BukkitConnector.blockData.testBlockState(world.getBlockAt(x, y, z));}
        @Override public boolean isMaterial(int x, int y, int z, String material){
            return BukkitConnector.blockData.isTestMaterial(world.getBlockAt(x, y, z), material);
        }
        @Override public int droppedItemCount(String material, int minX, int minY, int minZ, int maxX, int maxY, int maxZ){
            int count = 0;
            for(Entity entity : world.getEntities()){
                Location location = entity.getLocation();
                if(location.getX()<minX||location.getX()>maxX||location.getY()<minY||location.getY()>maxY
                        ||location.getZ()<minZ||location.getZ()>maxZ)continue;
                if(entity instanceof Item){
                    ItemStack stack = ((Item)entity).getItemStack();
                    if(stack.getType()==Material.valueOf(material.toUpperCase(Locale.ROOT)))count += stack.getAmount();
                }
            }
            return count;
        }
        @Override public void removeDroppedItems(int minX, int minY, int minZ, int maxX, int maxY, int maxZ){
            for(Entity entity : world.getEntities()){
                Location location = entity.getLocation();
                if(entity instanceof Item&&location.getX()>=minX&&location.getX()<=maxX
                        &&location.getY()>=minY&&location.getY()<=maxY&&location.getZ()>=minZ&&location.getZ()<=maxZ)entity.remove();
            }
        }
        @Override public void loadChunk(int x, int z){world.loadChunk(x, z);}
        @Override public long fullTime(){return world.getFullTime();}
        @Override public void setFullTime(long ticks){world.setFullTime(ticks);}
        @Override public boolean daylightCycle(){return BukkitConnector.api.gameRule(world,false,null);}
        @Override public void setDaylightCycle(boolean enabled){BukkitConnector.api.gameRule(world,false,enabled);}
        @Override public String dimension(){return world.getName();}
        @Override public String biome(int x,int y,int z){return BukkitConnector.api.biome(world,x,y,z);}
        @Override public void setBiome(int x,int y,int z,String biome){BukkitConnector.api.biome(world,x,y,z,biome);}
        @Override public boolean mobSpawning(){return BukkitConnector.api.gameRule(world,true,null);}
        @Override public void setMobSpawning(boolean enabled){BukkitConnector.api.gameRule(world,true,enabled);}
        @Override public void removeMobs(int minX,int minZ,int maxX,int maxZ){
            for(Entity entity:world.getEntities()){
                Location p=entity.getLocation();
                if(entity instanceof LivingEntity&&!(entity instanceof Player)
                        &&p.getX()>=minX&&p.getX()<=maxX&&p.getZ()>=minZ&&p.getZ()<=maxZ)entity.remove();
            }
        }
        @Override public void daylight(){world.setTime(6000); world.setStorm(false); world.setThundering(false);}
    }
}
