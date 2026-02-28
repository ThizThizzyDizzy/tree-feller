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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event){
        if(event.getClass().getName().contains("Fake"))return;
        Block block = event.getBlock();
        TreeFellerEvents.fireEvent(new PlayerBreakBlockEvent(plugin.getPlayerConnector(event.getPlayer()), plugin.getWorldConnector(event.getBlock().getWorld()), new BlockPos(block.getX(), block.getY(), block.getZ())));
    }
}
