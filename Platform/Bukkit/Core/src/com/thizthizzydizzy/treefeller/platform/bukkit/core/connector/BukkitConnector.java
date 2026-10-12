package com.thizthizzydizzy.treefeller.platform.bukkit.core.connector;
import com.thizthizzydizzy.treefeller.core.config.ISpecialConfigObject;
import com.thizthizzydizzy.treefeller.core.config.DefaultConfigBuilder;
import com.thizthizzydizzy.treefeller.core.config.legacy.LegacyBukkitConfigImporter;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeFellerConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IBlockDefinition;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IItemDefinition;
import com.thizthizzydizzy.treefeller.core.connector.TreeFellerConnector;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.Config;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.ConfigValue;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.BukkitBlockDataConnector;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.TreeFellerBukkit;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.definition.BukkitBlockDefinition;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.definition.BukkitItemDefinition;
import com.thizthizzydizzy.treefeller.platform.utility.version.VersionMatcher;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
public class BukkitConnector implements TreeFellerConnector{
    @Override
    public void schedule(int delayTicks, Runnable action){
        Bukkit.getScheduler().runTaskLater(treefeller, action, delayTicks);
    }

    private final TreeFellerBukkit treefeller;
    public static BukkitBlockDataConnector blockData;
    static{
        blockData = findConnector(BukkitBlockDataConnector.class, VersionMatcher.by(VersionMatcher.VersionType.MINECRAFT).ascending("1_8")
            .atVersion("1.13", "1_13")
            .match(Bukkit.getBukkitVersion()));
    }
    private static <T> T findConnector(Class<T> clazz, String version){
        String packageName = clazz.getPackageName();
        String className = packageName+".version.v"+version.toLowerCase(Locale.ROOT)+"."+clazz.getSimpleName()+"V"+version;
        try{
            return (T)Class.forName(className).getConstructor().newInstance();
        }catch(Exception ex){
            throw new RuntimeException("Could not find bukkit connector version "+version+" of type "+clazz.getName(), ex);
        }
    }
    
    public BukkitConnector(TreeFellerBukkit treefeller){
        this.treefeller = treefeller;
    }
    @Override
    public void log(String text){
        treefeller.getLogger().info(text);
    }
    @Override
    public TreeFellerConfiguration loadConfig(Function<Path, TreeFellerConfiguration> defaultLoader){
        Path config = new File(treefeller.getDataFolder(), "config.conf").toPath();
        Path legacy = new File(treefeller.getDataFolder(), "config.yml").toPath();
        if(!Files.exists(config)&&Files.exists(legacy)){
            try{
                String yaml = new String(Files.readAllBytes(legacy), StandardCharsets.UTF_8);
                YamlConfiguration parsed = new YamlConfiguration();
                parsed.loadFromString(yaml);
                LegacyBukkitConfigImporter.Result importResult =
                        LegacyBukkitConfigImporter.importConfiguration(yamlValues(parsed), name -> {
                            Material material=Material.matchMaterial(name);
                            return material==null?null:material.name();
                        });
                for(String warning : importResult.warnings)
                    treefeller.getLogger().warning("Legacy Bukkit configuration import: "+warning);
                Path candidate = config.resolveSibling("config.imported.conf");
                Path report = config.resolveSibling("config.import-report.md");
                importResult.write(candidate, report);
                throw new IllegalStateException("Legacy Bukkit configuration imported to "+candidate+". Review "+report
                        +" for unmatched fields before placing the reviewed configuration at "+config);
            }catch(IOException|InvalidConfigurationException ex){
                throw new IllegalStateException("Could not import legacy Bukkit configuration; the original config.yml has been retained", ex);
            }
        }
        return defaultLoader.apply(config);
    }
    private static Map<String, Object> yamlValues(ConfigurationSection section){
        Map<String, Object> result = new LinkedHashMap<>();
        for(String key : section.getKeys(false))result.put(key, yamlValue(section.get(key)));
        return result;
    }
    private static Object yamlValue(Object value){
        if(value instanceof ConfigurationSection)
            return yamlValues((ConfigurationSection)value);
        if(value instanceof Map){
            Map<String, Object> result = new LinkedHashMap<>();
            for(Map.Entry<?, ?> entry : ((Map<?, ?>)value).entrySet())
                result.put(String.valueOf(entry.getKey()), yamlValue(entry.getValue()));
            return result;
        }
        if(value instanceof List){
            List<Object> result = new ArrayList<>();
            for(Object entry : (List<?>)value)result.add(yamlValue(entry));
            return result;
        }
        return value;
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
        Map<String, IBlockDefinition> blocks = new LinkedHashMap<>();
        Map<String, IItemDefinition> items = new LinkedHashMap<>();
        for(Material m : Material.values()){
            String name = m.toString().toLowerCase(Locale.ROOT);
            if(m.isBlock())blocks.put(name, new BukkitBlockDefinition(m));
            BukkitItemDefinition item = new BukkitItemDefinition();
            item.material = m;
            items.put(name, item);
        }
        DefaultConfigBuilder.build(config, blocks, items);
    }
    @Override
    public Collection<IPlayerConnector> getAdminPlayers(){
        ArrayList<IPlayerConnector> admins = new ArrayList<>();
        for(World world : treefeller.getServer().getWorlds()){
            for(Player player : world.getPlayers()){
                if(player.isOp())admins.add(treefeller.getPlayerConnector(player));
            }
        }
        return admins;
    }
}
