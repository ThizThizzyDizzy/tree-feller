package com.thizthizzydizzy.treefeller.platform.bukkit.core.connector;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IBlockDefinition;
import com.thizthizzydizzy.treefeller.core.connector.item.IItemConnector;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockAxis;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
import com.thizthizzydizzy.treefeller.core.nbt.NbtBlockSnapshot;
import com.thizthizzydizzy.treefeller.core.connector.world.IWorldConnector;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.definition.BukkitBlockDefinition;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
public class BukkitWorldConnector implements IWorldConnector{
    private final World world;
    public World getWorld(){return world;}
    @Override public java.util.function.LongConsumer prepareEffect(com.thizthizzydizzy.treefeller.core.config.structure.section.result.EffectConfiguration effect){
        java.util.function.Consumer<org.bukkit.Location> playback=BukkitEffectPlayback.prepare(effect);
        return pos -> playback.accept(block(pos).getLocation().add(.5,.5,.5));
    }
    @Override public String material(long pos){return block(pos).getType().name();}
    @Override public com.thizthizzydizzy.treefeller.core.connector.world.BlockSnapshot snapshot(long pos){return new NbtBlockSnapshot(BukkitConnector.blockData.snapshot(block(pos)),BukkitConnector.nbt.block(block(pos)));}
    @Override public void setBlock(long pos,com.thizthizzydizzy.treefeller.core.connector.world.BlockSnapshot data){
        NbtBlockSnapshot snapshot=data instanceof NbtBlockSnapshot?(NbtBlockSnapshot)data:null;
        BukkitConnector.blockData.place(block(pos),snapshot==null?data:snapshot.state);
        if(snapshot!=null&&snapshot.hasNbt())BukkitConnector.nbt.block(block(pos),snapshot.at(BukkitConnector.nbt.block(block(pos))));
    }
    @Override public boolean isAir(long pos){return block(pos).getType().name().endsWith("AIR");}
    @Override public boolean isPassable(long pos){return BukkitConnector.blockData.passable(block(pos));}
    @Override public int minHeight(){
        try{return (Integer)World.class.getMethod("getMinHeight").invoke(world);}
        catch(NoSuchMethodException ex){return 0;}
        catch(ReflectiveOperationException ex){throw new IllegalStateException(ex);}
    }
    @Override public int maxHeight(){return world.getMaxHeight();}
    @Override public com.thizthizzydizzy.treefeller.core.connector.world.FallingBlockHandle spawnFallingBlock(long pos,
            com.thizthizzydizzy.treefeller.core.connector.world.BlockSnapshot data,
            com.thizthizzydizzy.treefeller.core.breaking.FallVector velocity,
            java.util.function.BiConsumer<Long,Float> impact,java.util.function.LongConsumer failed){
        org.bukkit.entity.FallingBlock entity=BukkitConnector.blockData.spawn(
                block(pos).getLocation().add(.5,.5,.5),data instanceof NbtBlockSnapshot?((NbtBlockSnapshot)data).state:data);
        entity.setDropItem(false);
        entity.setVelocity(new org.bukkit.util.Vector(velocity.x,velocity.y,velocity.z));
        return new BukkitFallingBlock(entity,impact,failed);
    }

    public BukkitWorldConnector(World world){
        this.world = world;
    }
    private Block block(long pos){
        return world.getBlockAt(BlockPos.getX(pos), BlockPos.getY(pos), BlockPos.getZ(pos));
    }
    @Override
    public Collection<IItemConnector> getDrops(long pos, IItemConnector tool, IPlayerConnector player){
        List<IItemConnector> drops = new ArrayList<>();
        for(ItemStack stack : BukkitConnector.blockData.getDrops(block(pos), ((BukkitItemConnector)tool).getStack(), ((BukkitPlayerConnector)player).getPlayer()))
            drops.add(new BukkitItemConnector(stack));
        return drops;
    }
    @Override
    public void removeBlock(long pos){
        block(pos).setType(Material.AIR, false);
    }
    @Override
    public void setBlock(long pos, IBlockDefinition definition){
        BukkitBlockDefinition target=(BukkitBlockDefinition)definition;
        Material material=target.material;
        if(material!=null&&material!=block(pos).getType())block(pos).setType(material,false);
        if(target.appliesNbt(BukkitConnector.nbt))BukkitConnector.nbt.block(block(pos),target.mergeBlockNbt(BukkitConnector.nbt,BukkitConnector.nbt.block(block(pos))));
    }
    @Override
    public void dropItem(long pos, IItemConnector item){
        world.dropItemNaturally(block(pos).getLocation().add(0.5, 0.5, 0.5), ((BukkitItemConnector)item).getStack());
    }
    @Override
    public float getDayTime(){
        return world.getTime();
    }
    @Override
    public float getMoonPhase(){
        return (world.getFullTime()/24000)%8;
    }
    @Override
    public String getDimension(){
        return world.getName();
    }
    @Override
    public String getBiome(long pos){
        return world.getBiome(BlockPos.getX(pos), BlockPos.getZ(pos)).toString();
    }
    @Override
    public boolean matches(long pos, IBlockDefinition definition){
        if(definition instanceof BukkitBlockDefinition){
            BukkitBlockDefinition block = (BukkitBlockDefinition)definition;
            return block.matches(world.getBlockAt(BlockPos.getX(pos), BlockPos.getY(pos), BlockPos.getZ(pos)));
        }
        throw new AssertionError("Unsupported block definition: "+definition.getClass().getName());
    }
    @Override
    public BlockAxis getBlockAxis(long pos){
        return BukkitConnector.blockData.getBlockAxis(world.getBlockAt(BlockPos.getX(pos), BlockPos.getY(pos), BlockPos.getZ(pos)));
    }
    @Override
    public int getLeafDistance(long pos){
        return BukkitConnector.blockData.getLeafDistance(world.getBlockAt(BlockPos.getX(pos), BlockPos.getY(pos), BlockPos.getZ(pos)));
    }
}
