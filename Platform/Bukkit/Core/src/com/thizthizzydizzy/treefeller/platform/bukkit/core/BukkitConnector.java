package com.thizthizzydizzy.treefeller.platform.bukkit.core;
import com.thizthizzydizzy.treefeller.core.TreeFellerConnector;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeFellerConfiguration;
import java.io.File;
import java.nio.file.Path;
import java.util.function.Function;
public class BukkitConnector implements TreeFellerConnector{
    private final TreeFellerBukkit treefeller;
    public BukkitConnector(TreeFellerBukkit treefeller){
        this.treefeller = treefeller;
    }
    @Override
    public void log(String text){
        treefeller.getLogger().info(text);
    }
    @Override
    public TreeFellerConfiguration loadConfig(Function<Path, TreeFellerConfiguration> defaultLoader){
        return defaultLoader.apply(new File(treefeller.getDataFolder(), "config.conf").toPath());
    }
}
