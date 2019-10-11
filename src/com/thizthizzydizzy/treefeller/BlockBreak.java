package com.thizthizzydizzy.treefeller;
import com.thizthizzydizzy.treefeller.TreeFeller.Sapling;
import java.util.Iterator;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.util.Vector;
public class BlockBreak implements Listener{
    private final TreeFeller plugin;
    public BlockBreak(TreeFeller plugin){
        this.plugin = plugin;
    }
    @EventHandler
    public void onItemDrop(ItemSpawnEvent event){
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
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        plugin.fellTree(event);
    }
    @EventHandler
    public void onBlockLand(EntityChangeBlockEvent event){
        if(event.getEntityType()==EntityType.FALLING_BLOCK){
            if(event.getTo()!=Material.AIR&&plugin.fallingBlocks.contains(event.getEntity().getUniqueId())){
                if(event.getBlock().getRelative(0, -1, 0).isPassable()){
                    event.setCancelled(true);
                    plugin.fallingBlocks.remove(event.getEntity().getUniqueId());
                    FallingBlock falling = event.getBlock().getWorld().spawnFallingBlock(event.getBlock().getLocation().add(.5,.5,.5), event.getBlockData());
                    plugin.fallingBlocks.add(falling.getUniqueId());
                    falling.setVelocity(new Vector(0, event.getEntity().getVelocity().getY(), 0));
                    falling.setHurtEntities(((FallingBlock)event.getEntity()).canHurtEntities());
                    for(String s : event.getEntity().getScoreboardTags()){
                        falling.addScoreboardTag(s);
                    }
                }else{
                    if(event.getEntity().getScoreboardTags().contains("TreeFeller_Break")){
                        event.setCancelled(true);
                        ItemStack stack = new ItemStack(event.getTo());
                        if(event.getEntity().getScoreboardTags().contains("TreeFeller_Convert")){
                            if(stack.getType().name().contains("_WOOD")){
                                stack.setType(Material.matchMaterial(stack.getType().name().replace("_WOOD", "_LOG")));
                            }
                        }
                        event.getBlock().getWorld().dropItemNaturally(event.getEntity().getLocation(), stack);
                    }
                    plugin.fallingBlocks.remove(event.getEntity().getUniqueId());
                }
            }
        }
    }
}