package com.thizthizzydizzy.treefeller.platform.bukkit.core;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import com.thizthizzydizzy.treefeller.core.connector.world.IWorldConnector;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.connector.BukkitConnector;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.connector.BukkitPlayerConnector;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.connector.BukkitWorldConnector;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.event.BukkitEvents;
import com.thizthizzydizzy.treefeller.core.test.TreeFellerTestSequences;
import java.util.HashMap;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
public abstract class TreeFellerBukkit extends JavaPlugin{
    public abstract com.thizthizzydizzy.treefeller.platform.bukkit.connector.BukkitNbtConnector createNbtConnector(String version);
    public abstract void registerCompatibilities();
    public BukkitConnector connector;
    private com.thizthizzydizzy.treefeller.platform.bukkit.core.connector.BukkitControlAudience.Renderer controls;
    @Override
    public void onEnable(){
        TreeFellerCore.initialize(connector = new BukkitConnector(this));
        registerCompatibilities();
        com.thizthizzydizzy.treefeller.core.compatibility.TreeFellerCompatibility.reload();
        controls=new com.thizthizzydizzy.treefeller.platform.bukkit.core.connector.BukkitControlAudience.Renderer(this);
        getServer().getPluginManager().registerEvents(controls,this);
        BukkitEvents events=new BukkitEvents(this);
        getServer().getPluginManager().registerEvents(events, this);
        events.registerFallingDrops();
        if(getServer().getPluginManager().isPluginEnabled("mcautotester")){
            TreeFellerTestSequences.register(new com.thizthizzydizzy.treefeller.platform.bukkit.core.connector.BukkitTestPlatform());
        }
    }
    
    @Override
    public void onDisable(){com.thizthizzydizzy.treefeller.core.compatibility.TreeFellerCompatibility.close();com.thizthizzydizzy.treefeller.core.debug.TreeFellerDebugger.clearAll();}
    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender,org.bukkit.command.Command command,String label,String[] args){
        return com.thizthizzydizzy.treefeller.core.control.TreeFellerCommands.execute(audience(sender),args);
    }
    private com.thizthizzydizzy.treefeller.core.control.ControlAudience audience(org.bukkit.command.CommandSender sender){
        return new com.thizthizzydizzy.treefeller.platform.bukkit.core.connector.BukkitControlAudience(this,sender,controls);
    }
    @Override
    public java.util.List<String> onTabComplete(org.bukkit.command.CommandSender sender,org.bukkit.command.Command command,String alias,String[] args){
        return com.thizthizzydizzy.treefeller.core.control.TreeFellerCommands.complete(audience(sender),args);
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
