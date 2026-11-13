package com.thizthizzydizzy.treefeller.platform.bukkit.core;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.compatibility.TreeFellerCompatibility;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import com.thizthizzydizzy.treefeller.core.connector.world.IWorldConnector;
import com.thizthizzydizzy.treefeller.core.control.ControlAudience;
import com.thizthizzydizzy.treefeller.core.control.TreeFellerCommands;
import com.thizthizzydizzy.treefeller.core.debug.TreeFellerDebugger;
import com.thizthizzydizzy.treefeller.core.test.TreeFellerTestSequences;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.BukkitApiConnector;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.BukkitNbtConnector;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.BukkitPacketConnector;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.connector.BukkitConnector;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.connector.BukkitControlAudience;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.connector.BukkitControlAudience.Renderer;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.connector.BukkitPlayerConnector;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.connector.BukkitTestPlatform;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.connector.BukkitWorldConnector;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.event.BukkitEvents;
import java.util.HashMap;
import java.util.List;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class TreeFellerBukkit extends JavaPlugin{
    public abstract BukkitNbtConnector createNbtConnector(String version);
    public abstract BukkitPacketConnector createPacketConnector(String version);
    public abstract BukkitApiConnector createApiConnector(String version);
    public abstract void registerCompatibilities();
    public BukkitConnector connector;
    private Renderer controls;
    @Override
    public void onEnable(){
        TreeFellerCore.initialize(connector = new BukkitConnector(this));
        registerCompatibilities();
        TreeFellerCompatibility.reload();
        controls=new Renderer(this);
        getServer().getPluginManager().registerEvents(controls,this);
        BukkitEvents events=new BukkitEvents(this);
        getServer().getPluginManager().registerEvents(events, this);
        events.registerFallingDrops();
        if(getServer().getPluginManager().isPluginEnabled("mcautotester")){
            TreeFellerTestSequences.register(new BukkitTestPlatform());
        }
    }
    
    @Override
    public void onDisable(){TreeFellerCompatibility.close();TreeFellerDebugger.clearAll();}
    @Override
    public boolean onCommand(CommandSender sender,Command command,String label,String[] args){
        return TreeFellerCommands.execute(audience(sender),args);
    }
    private ControlAudience audience(CommandSender sender){
        return new BukkitControlAudience(this,sender,controls);
    }
    @Override
    public List<String> onTabComplete(CommandSender sender,Command command,String alias,String[] args){
        return TreeFellerCommands.complete(audience(sender),args);
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
