package com.thizthizzydizzy.treefeller.platform.bukkit.core.connector;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
import com.thizthizzydizzy.treefeller.core.connector.world.IWorldConnector;
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
    public String getBiome(BlockPos pos){
        return world.getBiome(pos.x, pos.z).toString();
    }
}
