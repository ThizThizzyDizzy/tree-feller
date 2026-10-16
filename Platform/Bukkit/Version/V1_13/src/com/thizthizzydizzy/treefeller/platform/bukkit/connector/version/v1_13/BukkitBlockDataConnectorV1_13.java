package com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_13;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockAxis;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.BukkitBlockDataConnector;
import java.util.Collection;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
public class BukkitBlockDataConnectorV1_13 implements BukkitBlockDataConnector{
    @Override public java.util.function.Consumer<Location> prepareParticle(com.thizthizzydizzy.treefeller.core.config.structure.section.result.EffectConfiguration e){
        String name=e.particle.toUpperCase(java.util.Locale.ROOT);
        org.bukkit.Particle particle;
        try{particle=org.bukkit.Particle.valueOf(name);}
        catch(IllegalArgumentException ex){
            // Bukkit renamed these particles in 1.20.5; keep v1 configurations usable.
            switch(name){
                case "REDSTONE": name="DUST"; break;
                case "ITEM_CRACK": name="ITEM"; break;
                case "BLOCK_CRACK": case "BLOCK_DUST": name="BLOCK"; break;
                case "EXPLOSION_NORMAL": name="POOF"; break;
                case "EXPLOSION_LARGE": name="EXPLOSION"; break;
                case "EXPLOSION_HUGE": name="EXPLOSION_EMITTER"; break;
                case "SMOKE_NORMAL": name="SMOKE"; break;
                case "SMOKE_LARGE": name="LARGE_SMOKE"; break;
                case "FIREWORKS_SPARK": name="FIREWORK"; break;
                case "WATER_BUBBLE": name="BUBBLE"; break;
                case "WATER_SPLASH": name="SPLASH"; break;
                case "WATER_WAKE": name="FISHING"; break;
                case "VILLAGER_HAPPY": name="HAPPY_VILLAGER"; break;
                case "VILLAGER_ANGRY": name="ANGRY_VILLAGER"; break;
                case "ENCHANTMENT_TABLE": name="ENCHANT"; break;
                case "CRIT_MAGIC": name="ENCHANTED_HIT"; break;
                case "SPELL": name="EFFECT"; break;
                case "SPELL_INSTANT": name="INSTANT_EFFECT"; break;
                case "SPELL_WITCH": name="WITCH"; break;
                case "TOWN_AURA": name="MYCELIUM"; break;
                case "SNOWBALL": case "SNOW_SHOVEL": name="ITEM_SNOWBALL"; break;
                case "SLIME": name="ITEM_SLIME"; break;
                case "DRIP_WATER": name="DRIPPING_WATER"; break;
                case "DRIP_LAVA": name="DRIPPING_LAVA"; break;
                case "WATER_DROP": name="RAIN"; break;
                default: throw ex;
            }
            particle=org.bukkit.Particle.valueOf(name);
        }
        Object extra=null;
        Class<?> data=particle.getDataType();
        if(data==org.bukkit.Particle.DustOptions.class){
            if(e.size<=0)throw new IllegalArgumentException("Dust size must be positive");
            extra=new org.bukkit.Particle.DustOptions(org.bukkit.Color.fromRGB((int)e.r,(int)e.g,(int)e.b),e.size);
        }else if(data==ItemStack.class){
            if(e.item==null)throw new IllegalArgumentException("Particle item is required");
            Material material=((com.thizthizzydizzy.treefeller.platform.bukkit.core.definition.BukkitItemDefinition)e.item).material;
            if(material==null||material==Material.AIR)throw new IllegalArgumentException("Invalid particle item: "+e.item);
            extra=((com.thizthizzydizzy.treefeller.platform.bukkit.core.connector.BukkitItemConnector)new com.thizthizzydizzy.treefeller.platform.bukkit.core.connector.BukkitItemConnector(new ItemStack(material)).convert(e.item)).getStack();
        }else if(data==BlockData.class){
            if(e.block==null)throw new IllegalArgumentException("Particle block is required");
            extra=org.bukkit.Bukkit.createBlockData(e.block.toLowerCase(java.util.Locale.ROOT));
        }else if(data!=Void.class)throw new IllegalArgumentException("Unsupported particle data: "+particle);
        final org.bukkit.Particle selected=particle;
        final Object payload=extra;
        return location -> location.getWorld().spawnParticle(selected,location.clone().add(e.x,e.y,e.z),e.count,e.dx,e.dy,e.dz,e.speed,payload);
    }
    private static final class Snapshot implements com.thizthizzydizzy.treefeller.core.connector.world.BlockSnapshot{
        final BlockData data;
        Snapshot(BlockData data){this.data=data.clone();}
        @Override public BlockAxis axis(){return data instanceof Orientable?BlockAxis.valueOf(((Orientable)data).getAxis().name()):null;}
        @Override public Snapshot withAxis(BlockAxis axis){
            BlockData copy=data.clone();
            if(axis!=null&&copy instanceof Orientable)((Orientable)copy).setAxis(org.bukkit.Axis.valueOf(axis.name()));
            return new Snapshot(copy);
        }
    }
    @Override public com.thizthizzydizzy.treefeller.core.connector.world.BlockSnapshot snapshot(Block block){return new Snapshot(block.getBlockData());}
    @Override public void place(Block block,com.thizthizzydizzy.treefeller.core.connector.world.BlockSnapshot data){block.setBlockData(((Snapshot)data).data,false);}
    @Override public org.bukkit.entity.FallingBlock spawn(Location location,com.thizthizzydizzy.treefeller.core.connector.world.BlockSnapshot data){
        org.bukkit.entity.FallingBlock entity=location.getWorld().spawnFallingBlock(location,((Snapshot)data).data);
        entity.setHurtEntities(false); // Core applies the configured damage once on impact.
        return entity;
    }
    @Override public boolean passable(Block block){if(block.getType().name().equals("POWDER_SNOW"))return false;
        try{return (Boolean)Block.class.getMethod("isPassable").invoke(block);}
        catch(NoSuchMethodException ex){return !block.getType().isSolid();}
        catch(ReflectiveOperationException ex){throw new IllegalStateException(ex);}}

    @Override
    public boolean supportsTestMaterial(String name){
        Material material = Material.matchMaterial(name);
        return material!=null&&material.isBlock();
    }
    @Override
    public void setTestBlock(Block block, String name, BlockAxis axis, int leafDistance){
        Material material = Material.matchMaterial(name);
        if(material==null)throw new IllegalArgumentException("Unavailable material: "+name);
        BlockData data = material.createBlockData();
        if(axis!=null&&data instanceof Orientable)((Orientable)data).setAxis(org.bukkit.Axis.valueOf(axis.name()));
        if(data instanceof Leaves){
            ((Leaves)data).setPersistent(false);
            ((Leaves)data).setDistance(leafDistance);
        }
        block.setBlockData(data, false);
    }
    @Override
    public String testBlockState(Block block){return block.getBlockData().getAsString();}
    @Override
    public boolean isTestMaterial(Block block, String name){return block.getType()==Material.matchMaterial(name);}
    @Override
    public Collection<ItemStack> getDrops(Block block, ItemStack tool, Player player){
        return block.getDrops(tool);
    }
    @Override
    public void restoreBlockChange(Player player, Location location){
        player.sendBlockChange(location, location.getBlock().getBlockData());
    }
    @Override
    public void sendBlockChange(Player player, Location location, Material material){
        // The material/byte overload initializes expensive legacy conversion tables.
        player.sendBlockChange(location, material.createBlockData());
    }
    @Override
    public BlockAxis getBlockAxis(Block block){
        BukkitBlockDataConnectorV1_13 c;
        BlockData data = block.getBlockData();
        if(data instanceof Orientable){
            Orientable orientable = (Orientable)data;
            switch(orientable.getAxis()){
                case X: return BlockAxis.X;
                case Y: return BlockAxis.Y;
                case Z: return BlockAxis.Z;
                default: throw new AssertionError(orientable.getAxis().name());
            }
        }
        return null;
    }
    @Override public Boolean getPersistent(Block block){
        BlockData data=block.getBlockData();
        return data instanceof Leaves?((Leaves)data).isPersistent():null;
    }
    @Override public void setTestPersistent(Block block, boolean persistent){
        Leaves data=(Leaves)block.getBlockData();
        data.setPersistent(persistent);
        block.setBlockData(data,false);
    }
    @Override
    public int getLeafDistance(Block block){
        BlockData data = block.getBlockData();
        if(data instanceof Leaves){
            Leaves leaves = (Leaves)data;
            return leaves.getDistance();
        }
        return -1;
    }
}
