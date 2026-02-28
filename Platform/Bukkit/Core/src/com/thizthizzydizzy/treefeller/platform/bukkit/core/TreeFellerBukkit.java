package com.thizthizzydizzy.treefeller.platform.bukkit.core;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import com.thizthizzydizzy.treefeller.core.connector.world.IWorldConnector;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.connector.BukkitConnector;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.connector.BukkitPlayerConnector;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.connector.BukkitWorldConnector;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.event.BukkitEvents;
import java.util.HashMap;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
public class TreeFellerBukkit extends JavaPlugin{
    public BukkitConnector connector;
    @Override
    public void onEnable(){
        TreeFellerCore.initialize(connector = new BukkitConnector(this));
        getServer().getPluginManager().registerEvents(new BukkitEvents(this), this);
    }
    
    private final HashMap<Player, BukkitPlayerConnector> playerConnectors = new HashMap<>();
    public IPlayerConnector getPlayerConnector(Player player){
        if(player==null)return null;
        return playerConnectors.computeIfAbsent(player, BukkitPlayerConnector::new);
    }
    private final HashMap<World, BukkitWorldConnector> worldConnectors = new HashMap<>();
    public IWorldConnector getWorldConnector(World world){
        if(world==null)return null;
        return worldConnectors.computeIfAbsent(world, BukkitWorldConnector::new);
    }
}