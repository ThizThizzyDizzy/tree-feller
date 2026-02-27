package com.thizthizzydizzy.treefeller.platform.bukkit.core.connector;
import com.thizthizzydizzy.treefeller.core.connector.IWorldConnector;
import org.bukkit.World;
public class BukkitWorldConnector implements IWorldConnector{
    private final World world;
    public BukkitWorldConnector(World world){
        this.world = world;
    }
}
