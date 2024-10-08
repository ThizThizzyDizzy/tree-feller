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
import org.bukkit.ChatColor;
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
        compatibilities.add(new LegacyLogBlockCompat());
        compatibilities.add(new LogBlockCompat());
        compatibilities.add(new LandsCompat());
        compatibilities.add(new PlaceholderAPICompat());
        compatibilities.add(new SaberFactionsCompat());
        compatibilities.add(new AureliumSkillsCompat());
        compatibilities.add(new AuraSkillsCompat());
        compatibilities.add(new BlockRegenCompat());
        compatibilities.add(new PrismCompat());
    }
    public static void init(TreeFeller treefeller){
        if(treefeller!=null){
            for(PluginCompatibility compat : getCompatibilities()){
                try{
                    compat.init(treefeller);
                }catch(Exception ex){
                    treefeller.getLogger().log(Level.SEVERE, compat.getFriendlyName()+" compatibility failed to initalize!", ex);
                    compat.enabled.setValue(false);
                }
            }
        }
    }
    public static void breakBlock(TreeFeller treefeller, Tree tree, Tool tool, Player player, ItemStack axe, Block block, List<Modifier> modifiers){
        for(PluginCompatibility compat : getCompatibilities()){
            try{
                compat.breakBlock(tree, tool, player, axe, block, modifiers);
            }catch(Exception ex){
                treefeller.getLogger().log(Level.SEVERE, "Exception in "+compat.getFriendlyName()+" compatibility!", ex);
                if(player.isOp()||treefeller.debug)player.sendMessage(ChatColor.RED+"[TreeFeller] Caught exception in "+compat.getFriendlyName()+" compatiblity! Check server logs for details");
            }
        }
    }
    public static void addBlock(TreeFeller treefeller, Player player, Block block, BlockState was){
        for(PluginCompatibility compat : getCompatibilities()){
            try{
                compat.addBlock(player, block, was);
            }catch(Exception ex){
                treefeller.getLogger().log(Level.SEVERE, "Exception in "+compat.getFriendlyName()+" compatibility!", ex);
                if(player.isOp()||treefeller.debug)player.sendMessage(ChatColor.RED+"[TreeFeller] Caught exception in "+compat.getFriendlyName()+" compatiblity! Check server logs for details");
            }
        }
    }
    public static void removeBlock(TreeFeller treefeller, Player player, Block block){
        for(PluginCompatibility compat : getCompatibilities()){
            try{
                compat.removeBlock(player, block);
            }catch(Exception ex){
                treefeller.getLogger().log(Level.SEVERE, "Exception in "+compat.getFriendlyName()+" compatibility!", ex);
                if(player.isOp()||treefeller.debug)player.sendMessage(ChatColor.RED+"[TreeFeller] Caught exception in "+compat.getFriendlyName()+" compatiblity! Check server logs for details");
            }
        }
    }
    public static void dropItem(TreeFeller treefeller, Player player, Item item){
        for(PluginCompatibility compat : getCompatibilities()){
            try{
                compat.dropItem(player, item);
            }catch(Exception ex){
                treefeller.getLogger().log(Level.SEVERE, "Exception in "+compat.getFriendlyName()+" compatibility!", ex);
                if(player.isOp()||treefeller.debug)player.sendMessage(ChatColor.RED+"[TreeFeller] Caught exception in "+compat.getFriendlyName()+" compatiblity! Check server logs for details");
            }
        }
    }
    static String test(TreeFeller treefeller, Player player, Block block){
        for(PluginCompatibility compat : getCompatibilities()){
            try{
                if(!compat.test(player, block))return compat.getPluginName();
            }catch(Exception ex){
                treefeller.getLogger().log(Level.SEVERE, "Exception in "+compat.getFriendlyName()+" compatibility!", ex);
                if(player.isOp()||treefeller.debug)player.sendMessage(ChatColor.RED+"[TreeFeller] Caught exception in "+compat.getFriendlyName()+" compatiblity! Check server logs for details");
            }
        }
        return null;
    }
    public static TestResult test(TreeFeller treefeller, Player player, Iterable<Block> blocks){
        for(PluginCompatibility compat : getCompatibilities()){
            try{
                Block block = compat.test(player, blocks);
                if(block!=null){
                    return new TestResult(compat.getPluginName(), block);
                }
            }catch(Exception ex){
                treefeller.getLogger().log(Level.SEVERE, "Exception in "+compat.getFriendlyName()+" compatibility!", ex);
                if(player.isOp()||treefeller.debug)player.sendMessage(ChatColor.RED+"[TreeFeller] Caught exception in "+compat.getFriendlyName()+" compatiblity! Check server logs for details");
            }
        }
        return null;
    }
    public static void fellTree(TreeFeller treefeller, Block block, Player player, ItemStack axe, Tool tool, Tree tree, HashMap<Integer, ArrayList<Block>> blocks){
        for(PluginCompatibility compat : getCompatibilities()){
            try{
                compat.fellTree(block, player, axe, tool, tree, blocks);
            }catch(Exception ex){
                treefeller.getLogger().log(Level.SEVERE, "Exception in "+compat.getFriendlyName()+" compatibility!", ex);
                if(player.isOp()||treefeller.debug)player.sendMessage(ChatColor.RED+"[TreeFeller] Caught exception in "+compat.getFriendlyName()+" compatiblity! Check server logs for details");
            }
        }
    }
    public static void addPluginCompatibility(PluginCompatibility compatibility){
        ArrayList<PluginCompatibility> override = new ArrayList<>();
        for(PluginCompatibility compat : compatibilities){
            if(compat.getPluginName().equalsIgnoreCase(compatibility.getPluginName())){
                if(compat instanceof InternalCompatibility){
                    Bukkit.getLogger().log(Level.WARNING, "Overriding internal compatibility for {0}!", compat.getPluginName());
                    override.add(compat);
                    break;
                }else{
                    Bukkit.getLogger().log(Level.SEVERE, "External compatibility already exists for {0}! Ignoring...", compat.getPluginName());
                    return;
                }
            }
        }
        if(!override.isEmpty()){
            compatibilities.removeAll(override);
            compatibility.enabled = override.get(0).enabled;//they should all be the same, so first one is fine
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
