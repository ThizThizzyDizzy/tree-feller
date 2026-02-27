package com.thizthizzydizzy.treefeller.platform.bukkit.core.connector;
import com.thizthizzydizzy.treefeller.core.connector.IPlayerConnector;
import org.bukkit.entity.Player;
public class BukkitPlayerConnector implements IPlayerConnector{
    private final Player player;
    public BukkitPlayerConnector(Player player){
        this.player = player;
    }
}
