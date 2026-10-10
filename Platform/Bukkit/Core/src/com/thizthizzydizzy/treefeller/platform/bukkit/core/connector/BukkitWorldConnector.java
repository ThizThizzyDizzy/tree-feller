package com.thizthizzydizzy.treefeller.platform.bukkit.core.connector;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IBlockDefinition;
import com.thizthizzydizzy.treefeller.core.connector.item.IItemConnector;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockAxis;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
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
        Material material = ((BukkitBlockDefinition)definition).material;
        if(material!=null)block(pos).setType(material, false);
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
