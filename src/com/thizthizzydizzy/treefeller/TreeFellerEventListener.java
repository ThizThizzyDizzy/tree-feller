package com.thizthizzydizzy.treefeller;
import com.thizthizzydizzy.treefeller.menu.MenuTreeConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
public class TreeFellerEventListener implements Listener{
    private final TreeFeller plugin;
    public TreeFellerEventListener(TreeFeller plugin){
        this.plugin = plugin;
    }
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event){
        Entity damager = event.getDamager();
        if(event.getEntity().getType()==EntityType.DROPPED_ITEM&&damager.getType()==EntityType.FALLING_BLOCK&&damager.getScoreboardTags().contains("tree_feller"))event.setCancelled(true);
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
            if(falling!=null)falling.land(plugin, event);
        }
    }
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event){
        TreeFeller.detectingTrees.remove(event.getPlayer());
    }
    @EventHandler
    public void onRightClick(PlayerInteractEvent event){
        if(event.getAction()==Action.RIGHT_CLICK_BLOCK){
            if(TreeFeller.detectingTrees.containsKey(event.getPlayer())){
                event.setCancelled(true);
                Tree tree = TreeFeller.detect(event.getClickedBlock(), event.getPlayer());
                if(tree!=null){
                    TreeFeller.trees.add(tree);
                    new MenuTreeConfiguration(TreeFeller.detectingTrees.get(event.getPlayer()), plugin, event.getPlayer(), tree).openInventory();
                }else{
                    TreeFeller.detectingTrees.get(event.getPlayer()).openInventory();
                }
                TreeFeller.detectingTrees.remove(event.getPlayer());
            }
        }
    }
}