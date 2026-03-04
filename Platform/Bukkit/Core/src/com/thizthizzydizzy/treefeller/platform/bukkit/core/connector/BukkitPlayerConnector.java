package com.thizthizzydizzy.treefeller.platform.bukkit.core.connector;
import com.thizthizzydizzy.treefeller.core.connector.item.IItemConnector;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import com.thizthizzydizzy.treefeller.core.connector.player.PlayerGameMode;
import org.bukkit.entity.Player;
public class BukkitPlayerConnector implements IPlayerConnector{
    private final Player player;
    public BukkitPlayerConnector(Player player){
        this.player = player;
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
}
