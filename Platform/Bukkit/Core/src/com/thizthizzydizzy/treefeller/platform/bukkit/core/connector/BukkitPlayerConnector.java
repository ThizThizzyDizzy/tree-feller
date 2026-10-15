package com.thizthizzydizzy.treefeller.platform.bukkit.core.connector;
import com.thizthizzydizzy.treefeller.core.connector.item.IItemConnector;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import com.thizthizzydizzy.treefeller.core.connector.player.PlayerGameMode;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
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
    public void debugOverlay(com.thizthizzydizzy.treefeller.core.connector.world.IWorldConnector world,long pos,
            com.thizthizzydizzy.treefeller.core.debug.OverlayMark mark,String material){
        org.bukkit.World target=((BukkitWorldConnector)world).getWorld();
        if(player.getWorld()!=target)return;
        Material selected;
        switch(mark){
            case ROOT: selected=Material.COAL_BLOCK;break;
            case TRUNK: selected=Material.GOLD_BLOCK;break;
            case SECONDARY_TRUNK: selected=Material.REDSTONE_BLOCK;break;
            case SECTION_TRUNK: selected=Material.GOLD_ORE;break;
            case LEAVES: selected=Material.IRON_BLOCK;break;
            case SECONDARY_LEAVES: selected=Material.STONE;break;
            case SECTION_LEAVES: selected=Material.IRON_ORE;break;
            case OWNED_LEAVES: selected=Material.EMERALD_BLOCK;break;
            case SECTION_OWNED_LEAVES: selected=Material.EMERALD_ORE;break;
            case EXTENDED_LEAVES: selected=Material.OBSIDIAN;break;
            case DECORATION: selected=Material.DIAMOND_BLOCK;break;
            case SECTION_DECORATION: selected=Material.DIAMOND_ORE;break;
            case CANDIDATE: selected=Material.GLASS;break;
            case REJECTED: selected=Material.matchMaterial("RED_STAINED_GLASS");if(selected==null)selected=Material.GLASS;break;
            case ACCEPTED: case SELECTED: selected=Material.EMERALD_BLOCK;break;
            case RETAINED: selected=Material.COAL_BLOCK;break;
            case REMOVED: selected=Material.AIR;break;
            case CONVERTED: selected=Material.LAPIS_BLOCK;break;
            case LANDING: selected=Material.GOLD_BLOCK;break;
            case DROP: selected=Material.LAPIS_ORE;break;
            case REPLANT: selected=Material.EMERALD_ORE;break;
            case EFFECT: selected=Material.GLOWSTONE;break;
            case CASCADE: selected=Material.DIAMOND_BLOCK;break;
            default: throw new IllegalArgumentException("Unknown marker "+mark);
        }
        if(material!=null){
            Material custom=Material.matchMaterial(material.replace("minecraft:","").toUpperCase(java.util.Locale.ROOT));
            if(custom!=null&&custom.isBlock())selected=custom;
        }
        BukkitConnector.blockData.sendBlockChange(player,new Location(target,BlockPos.getX(pos),BlockPos.getY(pos),BlockPos.getZ(pos)),selected);
    }
    @Override
    public void debugRestore(com.thizthizzydizzy.treefeller.core.connector.world.IWorldConnector world,long pos){
        org.bukkit.World target=((BukkitWorldConnector)world).getWorld();
        if(player.getWorld()!=target)return;
        BukkitConnector.blockData.restoreBlockChange(player,new Location(target,BlockPos.getX(pos),BlockPos.getY(pos),BlockPos.getZ(pos)));
    }
}
