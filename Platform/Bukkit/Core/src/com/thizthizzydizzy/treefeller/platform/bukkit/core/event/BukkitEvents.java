package com.thizthizzydizzy.treefeller.platform.bukkit.core.event;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
import com.thizthizzydizzy.treefeller.core.event.TreeFellerEvents;
import com.thizthizzydizzy.treefeller.core.event.player.PlayerBreakBlockEvent;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.TreeFellerBukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
public class BukkitEvents implements Listener{
    private final TreeFellerBukkit plugin;
    public BukkitEvents(TreeFellerBukkit plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFallingBlock(org.bukkit.event.entity.EntityChangeBlockEvent event){
        com.thizthizzydizzy.treefeller.platform.bukkit.core.connector.BukkitFallingBlock falling=
                com.thizthizzydizzy.treefeller.platform.bukkit.core.connector.BukkitFallingBlock.get(event.getEntity());
        if(falling==null||event.getTo()==org.bukkit.Material.AIR)return;
        boolean refused=event.isCancelled();
        event.setCancelled(true);
        Block block=event.getBlock();
        if(refused)falling.failed();else falling.impact(BlockPos.toPos(block.getX(),block.getY(),block.getZ()));
    }
    /** EntityDropItemEvent is absent from the old Bukkit compile API. */
    public void registerFallingDrops(){
        try{
            Class<? extends org.bukkit.event.Event> type=Class.forName("org.bukkit.event.entity.EntityDropItemEvent").asSubclass(org.bukkit.event.Event.class);
            plugin.getServer().getPluginManager().registerEvent(type,this,EventPriority.HIGHEST,(listener,event) -> {
                try{
                    org.bukkit.entity.Entity entity=(org.bukkit.entity.Entity)type.getMethod("getEntity").invoke(event);
                    com.thizthizzydizzy.treefeller.platform.bukkit.core.connector.BukkitFallingBlock falling=
                            com.thizthizzydizzy.treefeller.platform.bukkit.core.connector.BukkitFallingBlock.get(entity);
                    if(falling!=null){((org.bukkit.event.Cancellable)event).setCancelled(true);falling.failed();}
                }catch(ReflectiveOperationException ex){throw new org.bukkit.event.EventException(ex);}
            },plugin);
        }catch(ClassNotFoundException ex){/* Legacy entity removal is observed through the Core lifecycle. */}
    }

    @EventHandler
    public void onQuit(org.bukkit.event.player.PlayerQuitEvent event){
        com.thizthizzydizzy.treefeller.core.debug.TreeFellerDebugger.clear(plugin.getPlayerConnector(event.getPlayer()));
    }
    @EventHandler
    public void onWorldChange(org.bukkit.event.player.PlayerChangedWorldEvent event){
        com.thizthizzydizzy.treefeller.core.debug.TreeFellerDebugger.clear(plugin.getPlayerConnector(event.getPlayer()));
    }
    // Felling observes the original event's cancellation without modifying it.
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event){
        if(event.getClass().getName().contains("Fake"))return;
        Block block = event.getBlock();
        TreeFellerEvents.fireEvent(new PlayerBreakBlockEvent(plugin.getPlayerConnector(event.getPlayer()), plugin.getWorldConnector(event.getBlock().getWorld()), BlockPos.toPos(block.getX(), block.getY(), block.getZ())));
    }
}
