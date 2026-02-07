package com.thizthizzydizzy.treefeller.bukkit;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import org.bukkit.plugin.java.JavaPlugin;
public class TreeFellerBukkit extends JavaPlugin{
    public BukkitConnector connector;
    @Override
    public void onEnable(){
        TreeFellerCore.initialize(connector = new BukkitConnector(this));
    }
}