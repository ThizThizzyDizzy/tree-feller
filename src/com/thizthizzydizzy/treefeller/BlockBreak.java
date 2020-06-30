package com.thizthizzydizzy.treefeller;
import com.thizthizzydizzy.treefeller.TreeFeller.FallingTreeBlock;
import java.util.Iterator;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.entity.EntityChangeBlockEvent;
public class BlockBreak implements Listener{
    private final TreeFeller plugin;
    public BlockBreak(TreeFeller plugin){
        this.plugin = plugin;
    }
    @EventHandler
    public void onItemDrop(ItemSpawnEvent event){
        if(TreeFeller.watching){
            TreeFeller.watchedDrops.add(event.getEntity().getItemStack());
            event.setCancelled(true);
        }else{
            if(event.isCancelled())return;
            if(!plugin.saplings.isEmpty()){
                for(Iterator<Sapling> it = plugin.saplings.iterator(); it.hasNext();){
                    Sapling sapling = it.next();
                    if(sapling.isDead()){
                        it.remove();
                    }else{
                        if(!sapling.autofill)return;
                        ItemStack stack = event.getEntity().getItemStack();
                        if(sapling.getMaterial()==stack.getType()&&stack.getAmount()>=1){
                            if(sapling.place()){
                                stack.setAmount(stack.getAmount()-1);
                                it.remove();
                            }
                        }
                    }
                }
            }
        }
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