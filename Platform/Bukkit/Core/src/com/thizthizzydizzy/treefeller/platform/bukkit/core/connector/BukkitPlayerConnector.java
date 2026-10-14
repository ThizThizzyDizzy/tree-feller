package com.thizthizzydizzy.treefeller.platform.bukkit.core.connector;
import com.thizthizzydizzy.treefeller.core.connector.item.IItemConnector;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import com.thizthizzydizzy.treefeller.core.connector.player.PlayerGameMode;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
import com.thizthizzydizzy.treefeller.core.detection.TreeNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
public class BukkitPlayerConnector implements IPlayerConnector{
    private final Player player;
    public BukkitPlayerConnector(Player player){
        this.player = player;
    }
    @Override
    public com.thizthizzydizzy.treefeller.core.breaking.FallVector position(){
        Location l=player.getLocation();
        return new com.thizthizzydizzy.treefeller.core.breaking.FallVector(l.getX(),l.getY(),l.getZ());
    }
    @Override
    public int inventorySize(){return player.getInventory().getSize();}
    @Override
    public IItemConnector inventoryItem(int slot){
        ItemStack stack=player.getInventory().getItem(slot);
        return stack==null?null:new BukkitItemConnector(stack.clone());
    }
    @Override
    public void setInventoryItem(int slot,IItemConnector item){
        player.getInventory().setItem(slot,item.getCount()==0?null:((BukkitItemConnector)item).getStack());
    }
    public Player getPlayer(){
        return player;
    }
    @Override
    public void setTool(IItemConnector item){
        player.setItemInHand(((BukkitItemConnector)item).getStack());
    }
    @Override
    public Collection<IItemConnector> giveItems(IItemConnector item){
        List<IItemConnector> overflow = new ArrayList<>();
        for(ItemStack stack : player.getInventory().addItem(((BukkitItemConnector)item).getStack()).values())
            overflow.add(new BukkitItemConnector(stack));
        return overflow;
    }
    @Override
    public void setFoodLevel(int food){
        player.setFoodLevel(food);
    }
    @Override
    public void setSaturationLevel(float saturation){
        player.setSaturation(saturation);
    }
    @Override
    public float getExhaustion(){
        return player.getExhaustion();
    }
    @Override
    public void setExhaustion(float exhaustion){
        player.setExhaustion(exhaustion);
    }
    @Override
    public void setHealth(float health){
        player.setHealth(health);
    }
    @Override
    public float getMaxHealth(){
        return (float)player.getMaxHealth();
    }
    @Override
    public void damage(float damage){
        player.damage(damage);
    }
    @Override
    public boolean hasPermission(String permission){
        return player.hasPermission(permission);
    }
    @Override
    public float getFoodLevel(){
        return player.getFoodLevel();
    }
    @Override
    public float getSaturationLevel(){
        return player.getSaturation();
    }
    @Override
    public float getHealth(){
        return (float)player.getHealth();
    }
    @Override
    public PlayerGameMode getGameMode(){
        switch(player.getGameMode()){
            case ADVENTURE:
                return PlayerGameMode.ADVENTURE;
            case SURVIVAL:
                return PlayerGameMode.SURVIVAL;
            case CREATIVE:
                return PlayerGameMode.CREATIVE;
            case SPECTATOR:
                return PlayerGameMode.SPECTATOR;
            default:
                return PlayerGameMode.UNKNOWN;
        }
    }
    @Override
    public boolean isSneaking(){
        return player.isSneaking();
    }
    @Override
    public IItemConnector getTool(){
        return new BukkitItemConnector(player.getItemInHand());
    }
    @Override
    public void sendMessage(String message){
        player.sendMessage(message);
    }
    @Override
    public String getPlayerName(){
        return player.getName();
    }
    @Override
    public void debugDisplayTreeNode(TreeNode node){
        Material m = Material.BEDROCK;
        switch(node.type){
            case ROOTS:
                m = Material.COAL_BLOCK;
                break;
            case TRUNK:
                m = node.secondary?Material.REDSTONE_BLOCK:Material.GOLD_BLOCK;
                if(!node.secondary&&node.sectionId>0)m = Material.GOLD_ORE;
                break;
            case LEAVES:
                m = node.secondary?Material.STONE:Material.IRON_BLOCK;
                if(node.extendedDistance>0)m = Material.EMERALD_BLOCK;
                if(!node.secondary&&node.sectionId>0){
                    m = Material.IRON_ORE;
                    if(node.extendedDistance>0)m = Material.EMERALD_ORE;
                }
                break;
            case EXTENDED_LEAVES:
                m = Material.OBSIDIAN;
                break;
            case DECORATION:
                m = Material.DIAMOND_BLOCK;
                if(!node.secondary&&node.sectionId>0)m = Material.DIAMOND_ORE;
                break;
            case NONE:
                return;
        }
        BukkitConnector.blockData.sendBlockChange(player, new Location(player.getWorld(), BlockPos.getX(node.pos), BlockPos.getY(node.pos), BlockPos.getZ(node.pos)), m);
    }
}
