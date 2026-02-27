package com.thizthizzydizzy.treefeller.platform.bukkit.core;
import com.thizthizzydizzy.treefeller.core.config.ISpecialConfigObject;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeFellerConfiguration;
import com.thizthizzydizzy.treefeller.core.connector.TreeFellerConnector;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.Config;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.ConfigValue;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.definition.BukkitBlockDefinition;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.definition.BukkitItemDefinition;
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
    @Override
    public Class<? extends ISpecialConfigObject> mapBlockDefinitionClass(Config rawConfig, String key, ConfigValue value){
        return BukkitBlockDefinition.class;
    }
    @Override
    public Class<? extends ISpecialConfigObject> mapItemDefinitionClass(Config rawConfig, String key, ConfigValue value){
        return BukkitItemDefinition.class;
    }
}
