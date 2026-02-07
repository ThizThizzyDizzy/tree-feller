package com.thizthizzydizzy.treefeller.bukkit;
import com.thizthizzydizzy.treefeller.connector.TreeFellerConnector;
public class BukkitConnector implements TreeFellerConnector{
    private final TreeFellerBukkit plugin;
    public BukkitConnector(TreeFellerBukkit plugin){
        this.plugin = plugin;
    }
    @Override
    public void log(String text){
        plugin.getLogger().info(text);
    }
}
