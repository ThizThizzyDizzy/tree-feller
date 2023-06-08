package com.thizthizzydizzy.treefeller.compat;
import com.thizthizzydizzy.treefeller.Modifier;
import com.thizthizzydizzy.treefeller.Tool;
import com.thizthizzydizzy.treefeller.Tree;
import com.thizthizzydizzy.treefeller.TreeFeller;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Item;
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
        compatibilities.add(new OreRegeneratorCompat());
        compatibilities.add(new Drop2InventoryCompat());
        compatibilities.add(new EcoSkillsCompat());
        compatibilities.add(new EcoJobsCompat());
        compatibilities.add(new LogBlockCompat());
        compatibilities.add(new LandsCompat());
        compatibilities.add(new PlaceholderAPICompat());
        compatibilities.add(new SaberFactionsCompat());
        compatibilities.add(new AureliumSkillsCompat());
        compatibilities.add(new BlockRegenCompat());
    }
    public static void init(TreeFeller treeFeller){
        if(treeFeller!=null){
            for(PluginCompatibility compat : getCompatibilities()){
                compat.init(treeFeller);
            }
        }
    }
    public static void breakBlock(Tree tree, Tool tool, Player player, ItemStack axe, Block block, List<Modifier> modifiers){
        for(PluginCompatibility compat : getCompatibilities()){
            compat.breakBlock(tree, tool, player, axe, block, modifiers);
        }
    }
    public static void addBlock(Player player, Block block, BlockState was){
        for(PluginCompatibility compat : getCompatibilities()){
            compat.addBlock(player, block, was);
        }
    }
    public static void removeBlock(Player player, Block block){
        for(PluginCompatibility compat : getCompatibilities()){
            compat.removeBlock(player, block);
        }
    }
    public static void dropItem(Player player, Item item){
        for(PluginCompatibility compat : getCompatibilities()){
            compat.dropItem(player, item);
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
    public static void fellTree(Block block, Player player, ItemStack axe, Tool tool, Tree tree, HashMap<Integer, ArrayList<Block>> blocks){
        for(PluginCompatibility compat : getCompatibilities()){
            compat.fellTree(block, player, axe, tool, tree, blocks);
        }
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
        if(override!=null){
            compatibilities.remove(override);
            compatibility.enabled = override.enabled;
        }
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
            if(compat.isEnabled()&&compat.isInstalled())compats.add(compat);
        }
        return compats;
    }
}
