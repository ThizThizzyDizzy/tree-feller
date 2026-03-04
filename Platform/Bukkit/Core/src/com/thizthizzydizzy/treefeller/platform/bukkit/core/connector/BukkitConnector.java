package com.thizthizzydizzy.treefeller.platform.bukkit.core.connector;
import com.thizthizzydizzy.treefeller.core.config.ISpecialConfigObject;
import com.thizthizzydizzy.treefeller.core.config.structure.ToolConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeFellerConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IBlockDefinition;
import com.thizthizzydizzy.treefeller.core.connector.TreeFellerConnector;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.Config;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.ConfigValue;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.TreeFellerBukkit;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.definition.BukkitBlockDefinition;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.definition.BukkitItemDefinition;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Locale;
import java.util.function.Function;
import org.bukkit.Material;
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
    @Override
    public void buildDefaultConfig(TreeFellerConfiguration config){
        ArrayList<ToolConfiguration> tools = new ArrayList();
        ArrayList<TreeConfiguration> trees = new ArrayList();
        for(Material m : Material.values()){
            String name = m.toString().toLowerCase(Locale.ROOT);
            if(name.endsWith("_axe")){
                ToolConfiguration tool = new ToolConfiguration();
                tool.item = new BukkitItemDefinition(name);
                tools.add(tool);
            }
            if(name.endsWith("_log")&&!name.startsWith("stripped")){
                TreeConfiguration tree = new TreeConfiguration();
                tree.trunk = new IBlockDefinition[]{
                    new BukkitBlockDefinition(name)
                };
                tree.leaves = new IBlockDefinition[]{
                    new BukkitBlockDefinition(name.replace("_log", "_leaves"))
                };
                trees.add(tree);
            }
        }
        config.tools = tools.toArray(ToolConfiguration[]::new);
        config.trees = trees.toArray(TreeConfiguration[]::new);
    }
}
