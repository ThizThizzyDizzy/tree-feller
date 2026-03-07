package com.thizthizzydizzy.treefeller.platform.bukkit.core.connector;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IBlockDefinition;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockAxis;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
import com.thizthizzydizzy.treefeller.core.connector.world.IWorldConnector;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.definition.BukkitBlockDefinition;
import org.bukkit.World;
public class BukkitWorldConnector implements IWorldConnector{
    private final World world;
    public BukkitWorldConnector(World world){
        this.world = world;
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
        return null; //TODO calculate block axis for pillar blocks prior to bukkit having API for that
    }
    @Override
    public int getLeafDistance(long pos){
        // doesn't exist before 1.13
        //TODO return actual values in 1.13
        return -1;
    }
}
