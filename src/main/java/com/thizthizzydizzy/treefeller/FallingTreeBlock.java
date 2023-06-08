package com.thizthizzydizzy.treefeller;
import com.thizthizzydizzy.vanillify.Vanillify;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
public class FallingTreeBlock{
    private final DetectedTree detectedTree;
    public FallingBlock entity;
    private final Tool tool;
    private final Tree tree;
    private final ItemStack axe;
    private final boolean doBreak;
    private final Player player;
    private final RotationData rot;
    private final boolean dropItems;
    private final List<Modifier> modifiers;
    public FallingTreeBlock(DetectedTree detectedTree, FallingBlock entity, Tool tool, Tree tree, ItemStack axe, boolean doBreak, Player player, RotationData rot, boolean dropItems, List<Modifier> modifiers){
        this.detectedTree = detectedTree;
        this.entity = entity;
        this.tool = tool;
        this.tree = tree;
        this.axe = axe;
        this.doBreak = doBreak;
        this.player = player;
        this.rot = rot;
        this.dropItems = dropItems;
        this.modifiers = modifiers;
    }
    public void land(TreeFeller plugin, EntityChangeBlockEvent event){
        if(event.getTo()==Material.AIR)return;
        Block on = event.getBlock().getRelative(0, -1, 0);
        if(on.isPassable()&&!on.getType().getKey().getKey().equals("powder_snow")){
            event.setCancelled(true);
            FallingBlock falling = event.getBlock().getWorld().spawnFallingBlock(event.getBlock().getLocation().add(.5,.5,.5), event.getBlockData());
            entity = falling;
            falling.setVelocity(new Vector(0, event.getEntity().getVelocity().getY(), 0));
            falling.setHurtEntities(((FallingBlock)event.getEntity()).canHurtEntities());
            Vanillify.modifyEntityNBT(falling, "FallHurtAmount", Vanillify.getEntityNBTFloat(entity, "FallHurtAmount"));
            Vanillify.modifyEntityNBT(falling, "FallHurtMax", Vanillify.getEntityNBTFloat(entity, "FallHurtMax"));
            for(String s : event.getEntity().getScoreboardTags()){
                falling.addScoreboardTag(s);
            }
        }else{
            int[] xp = new int[]{0};
            if(!dropItems){
                event.setCancelled(true);
                plugin.fallingBlocks.remove(this);
                return;
            }
            ArrayList<ItemStack> drops = plugin.getDrops(event.getTo(), tool, tree, axe, event.getBlock(), xp, modifiers);
            if(doBreak){
                event.setCancelled(true);
                for(ItemStack drop : drops){
                    plugin.dropItem(detectedTree, player, event.getBlock().getWorld().dropItemNaturally(event.getEntity().getLocation(), drop));
                }
                plugin.dropExp(event.getBlock().getWorld(), event.getEntity().getLocation(), xp[0]);
            }
            if(player!=null){
                event.setCancelled(true);
                for(ItemStack drop : drops){
                    for(ItemStack stack : player.getInventory().addItem(drop).values())plugin.dropItem(detectedTree, player, event.getBlock().getWorld().dropItemNaturally(event.getEntity().getLocation(), stack));
                }
                player.setTotalExperience(player.getTotalExperience()+xp[0]);
            }
            plugin.fallingBlocks.remove(this);
            if(event.isCancelled())return;
            if(rot!=null){
                Axis axis = rot.axis;
                double xDiff = Math.abs(rot.x-event.getEntity().getLocation().getX());
                double yDiff = Math.abs(rot.y-event.getEntity().getLocation().getY());
                double zDiff = Math.abs(rot.z-event.getEntity().getLocation().getZ());
                Axis newAxis = Axis.Y;
                if(Math.max(Math.max(xDiff, yDiff), zDiff)==xDiff)newAxis = Axis.X;
                if(Math.max(Math.max(xDiff, yDiff), zDiff)==zDiff)newAxis = Axis.Z;
                if(newAxis==Axis.X){
                    switch(axis){
                        case X:
                            axis = Axis.Y;
                            break;
                        case Y:
                            axis = Axis.X;
                            break;
                        case Z:
                            break;
                    }
                }
                if(newAxis==Axis.Z){
                    switch(axis){
                        case X:
                            break;
                        case Y:
                            axis = Axis.Z;
                            break;
                        case Z:
                            axis = Axis.X;
                            break;
                    }
                }
                Orientable data = (Orientable)event.getBlockData();
                data.setAxis(axis);
                event.setCancelled(true);
                event.getBlock().setType(event.getTo());
                event.getBlock().setBlockData(data);
            }
        }
    }
}