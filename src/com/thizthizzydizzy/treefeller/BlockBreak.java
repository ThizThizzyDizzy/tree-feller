package com.thizthizzydizzy.treefeller;
import com.thizthizzydizzy.treefeller.TreeFeller.FallingTreeBlock;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
public class BlockBreak implements Listener{
    private final TreeFeller plugin;
    public BlockBreak(TreeFeller plugin){
        this.plugin = plugin;
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        if(event.isCancelled())return;
        plugin.fellTree(event);
    }
    @EventHandler
    public void onBlockLand(EntityChangeBlockEvent event){
        if(event.getEntityType()==EntityType.FALLING_BLOCK){
            FallingTreeBlock falling = null;
            for(FallingTreeBlock b : plugin.fallingBlocks){
                if(b.entity.getUniqueId().equals(event.getEntity().getUniqueId())){
                    falling = b;
                    break;
                }
            }
            if(falling!=null)falling.land(event);
        }
    }
}