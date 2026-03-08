package com.thizthizzydizzy.treefeller.platform.bukkit.core.connector;
import com.thizthizzydizzy.treefeller.core.config.ISpecialConfigObject;
import com.thizthizzydizzy.treefeller.core.config.structure.ToolConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeFellerConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.general.SimpleDirection;
import com.thizthizzydizzy.treefeller.core.config.structure.section.DetectionConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.detection.DecorationConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IBlockDefinition;
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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
public class BukkitConnector implements TreeFellerConnector{
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
                Material rootsMaterial = Material.matchMaterial(name.replace("_log", "_roots"));
                if(rootsMaterial!=null){
                    tree.roots = new IBlockDefinition[]{
                        new BukkitBlockDefinition(rootsMaterial)
                    };
                }
                ArrayList<IBlockDefinition> leaves = new ArrayList<>();
                leaves.add(new BukkitBlockDefinition(name.replace("_log", "_leaves")));
                if(rootsMaterial!=null)leaves.add(new BukkitBlockDefinition(rootsMaterial));
                if(rootsMaterial!=null){
                    if(tree.detection==null)tree.detection = new DetectionConfiguration();
                    tree.detection.max_leaf_distance_from_top = 4096; 
                    tree.detection.leaf_detect_range = config.global.detection.root_distance; // ensure detect range includes roots (mangrove)
                }
                tree.leaves = leaves.toArray(IBlockDefinition[]::new);
                trees.add(tree);
            }
        }
        config.tools = tools.toArray(ToolConfiguration[]::new);
        config.trees = trees.toArray(TreeConfiguration[]::new);
        config.global.detection.decorations = new DecorationConfiguration[]{
            new DecorationConfiguration(SimpleDirection.UP, false, new BukkitBlockDefinition(Material.SNOW)),
            new DecorationConfiguration(SimpleDirection.SIDE_AND_DOWN, true, new BukkitBlockDefinition(Material.VINE)),
            new DecorationConfiguration(SimpleDirection.SIDE, false, new BukkitBlockDefinition(Material.COCOA))
        };
        
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
