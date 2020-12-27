package com.thizthizzydizzy.treefeller.compat;
import com.thizthizzydizzy.treefeller.Modifier;
import com.thizthizzydizzy.treefeller.Tool;
import com.thizthizzydizzy.treefeller.Tree;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
public class TreeFellerCompat{
    private static final ArrayList<PluginCompatibility> compatibilities = new ArrayList<>();
    static{
        compatibilities.add(new MMOCoreCompat());
        compatibilities.add(new JobsRebornCompat());
        compatibilities.add(new McMMOCompat());
        compatibilities.add(new McMMOClassicCompat());
        compatibilities.add(new CoreProtectCompat());
        compatibilities.add(new WorldGuardCompat());
        compatibilities.add(new GriefPreventionCompat());
        compatibilities.add(new TownyCompat());
    }
    public static void init(){}//placeholder method for loading the class
    public static void breakBlock(Tree tree, Tool tool, Player player, ItemStack axe, Block block, List<Modifier> modifiers){
        for(PluginCompatibility compat : getCompatibilities()){
            compat.breakBlock(tree, tool, player, axe, block, modifiers);
        }
    }
    public static void addBlock(Player player, Block block){
        for(PluginCompatibility compat : getCompatibilities()){
            compat.addBlock(player, block);
        }
    }
    public static void removeBlock(Player player, Block block){
        for(PluginCompatibility compat : getCompatibilities()){
            compat.removeBlock(player, block);
        }
    }
    static String test(Player player, Block block){
        for(PluginCompatibility compat : getCompatibilities()){
            if(!compat.test(player, block))return compat.getPluginName();
        }
        return null;
    }
    public static TestResult test(Player player, Iterable<Block> blocks){
        for(PluginCompatibility compat : getCompatibilities()){
            Block block = compat.test(player, blocks);
            if(block!=null){
                return new TestResult(compat.getPluginName(), block);
            }
        }
        return null;
    }
    public static void addPluginCompatibility(PluginCompatibility compatibility){
        PluginCompatibility override = null;
        for(PluginCompatibility compat : compatibilities){
            if(compat.getPluginName().equalsIgnoreCase(compatibility.getPluginName())){
                if(compat instanceof InternalCompatibility){
                    Bukkit.getLogger().log(Level.WARNING, "Overriding internal compatibility for {0}!", compat.getPluginName());
                    override = compat;
                    break;
                }else{
                    Bukkit.getLogger().log(Level.SEVERE, "External compatibility already exists for {0}! Ignoring...", compat.getPluginName());
                    return;
                }
            }
        }
        if(override!=null)compatibilities.remove(override);
        compatibilities.add(compatibility);
    }
    public static void reload(){
        for(PluginCompatibility compat : getCompatibilities()){
            compat.reload();
        }
    }
    private static ArrayList<PluginCompatibility> getCompatibilities(){
        ArrayList<PluginCompatibility> compats = new ArrayList<>();
        for(PluginCompatibility compat : compatibilities){
            if(!compat.isEnabled())continue;
            if(Bukkit.getPluginManager().getPlugin(compat.getPluginName())!=null)compats.add(compat);
        }
        return compats;
    }
}