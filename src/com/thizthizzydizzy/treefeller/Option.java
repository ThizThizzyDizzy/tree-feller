package com.thizthizzydizzy.treefeller;
import com.thizthizzydizzy.simplegui.ItemBuilder;
import static com.thizthizzydizzy.treefeller.DebugResult.Type.*;
import com.thizthizzydizzy.treefeller.menu.MenuGlobalConfiguration;
import com.thizthizzydizzy.treefeller.menu.MenuToolConfiguration;
import com.thizthizzydizzy.treefeller.menu.MenuTreeConfiguration;
import com.thizthizzydizzy.treefeller.menu.modify.MenuModifyDouble;
import com.thizthizzydizzy.treefeller.menu.modify.MenuModifyEnchantmentMap;
import com.thizthizzydizzy.treefeller.menu.modify.MenuModifyFloat;
import com.thizthizzydizzy.treefeller.menu.modify.MenuModifyInteger;
import com.thizthizzydizzy.treefeller.menu.modify.MenuModifyMaterialMaterialMap;
import com.thizthizzydizzy.treefeller.menu.modify.MenuModifyMaterialSet;
import com.thizthizzydizzy.treefeller.menu.modify.MenuModifyShort;
import com.thizthizzydizzy.treefeller.menu.modify.MenuModifyStringList;
import com.thizthizzydizzy.treefeller.menu.modify.MenuModifyStringSet;
import com.thizthizzydizzy.treefeller.menu.modify.special.MenuModifyDirectionalFallBehavior;
import com.thizthizzydizzy.treefeller.menu.modify.special.MenuModifyEffectList;
import com.thizthizzydizzy.treefeller.menu.modify.special.MenuModifyFellBehavior;
import com.thizthizzydizzy.treefeller.menu.modify.special.MenuModifySpawnSaplings;
import com.thizthizzydizzy.treefeller.menu.modify.special.MenuModifyTreeSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
public abstract class Option<E>{
    private static final HashSet<Material> defaultOverridables = new HashSet<>();
    private static final HashSet<Material> defaultGrasses = new HashSet<>();
    private static final HashMap<Material, Material> defaultDropConversions = new HashMap<>();
    private static final HashMap<Material, Material> defaultBlockConversions = new HashMap<>();
    static{
        defaultOverridables.add(Material.GRASS);
        defaultOverridables.add(Material.AIR);
        defaultOverridables.add(Material.CAVE_AIR);
        defaultOverridables.add(Material.WATER);
        defaultOverridables.add(Material.TALL_GRASS);
        defaultOverridables.add(Material.SEAGRASS);
        defaultOverridables.add(Material.TALL_SEAGRASS);
        defaultOverridables.add(Material.FERN);
        defaultOverridables.add(Material.LARGE_FERN);
        defaultGrasses.add(Material.GRASS_BLOCK);
        defaultGrasses.add(Material.DIRT);
        defaultGrasses.add(Material.PODZOL);
        Material rootedDirt = Material.matchMaterial("ROOTED_DIRT");
        if(rootedDirt!=null)defaultGrasses.add(rootedDirt);
        defaultDropConversions.put(Material.OAK_WOOD, Material.OAK_LOG);
        defaultDropConversions.put(Material.BIRCH_WOOD, Material.BIRCH_LOG);
        defaultDropConversions.put(Material.SPRUCE_WOOD, Material.SPRUCE_LOG);
        defaultDropConversions.put(Material.JUNGLE_WOOD, Material.JUNGLE_LOG);
        defaultDropConversions.put(Material.ACACIA_WOOD, Material.ACACIA_LOG);
        defaultDropConversions.put(Material.DARK_OAK_WOOD, Material.DARK_OAK_LOG);
        defaultDropConversions.put(Material.CRIMSON_HYPHAE, Material.CRIMSON_STEM);
        defaultDropConversions.put(Material.WARPED_HYPHAE, Material.WARPED_STEM);
        Material roots = Material.matchMaterial("MUDDY_MANGROVE_ROOTS");
        Material mud = Material.matchMaterial("MUD");
        if(roots!=null)defaultBlockConversions.put(roots, mud);
    }
    public static ArrayList<Option> options = new ArrayList<>();
    //console/debugging stuff
    public static OptionBoolean STARTUP_LOGS = new OptionBoolean("Startup Logs", true, false, false, true){
        @Override
        public String getDesc(boolean ingame){
            return "If set to false, the tree feller will not list all its settings in the console on startup";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Boolean value){
            ItemBuilder builder = new ItemBuilder(Material.OAK_LOG);
            if(Objects.equals(value, true)){
                builder.enchant(Enchantment.BINDING_CURSE, 1);
                builder.addFlag(ItemFlag.HIDE_ENCHANTS);
            }
            return new ItemBuilder(Material.OAK_LOG);
        }
    };
    public static OptionBoolean DEFAULT_ENABLED = new OptionBoolean("Default Enabled", true, false, false, true, true){
        @Override
        public String getDesc(boolean ingame){
            return "If set to false, the tree feller will be toggled off for each player by default (as with /treefeller toggle)"+(ingame?"\nOnly takes effect on reload/restart":"");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Boolean value){
            return new ItemBuilder(Objects.equals(value, true)?Material.GREEN_CONCRETE:Material.RED_CONCRETE);
        }
    };
    //tree detection
    public static Option<Integer> SCAN_DISTANCE = new Option<Integer>("Scan Distance", true, true, false, 256){
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
        @Override
        public String getDesc(boolean ingame){
            return "How far should the plugin scan for logs? (If a tree is larger, only the part within this distance will be felled)";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Integer value){
            return new ItemBuilder(Material.RAIL).setCount(value);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<Integer> LEAF_DETECT_RANGE = new Option<Integer>("Leaf Detect Range", true, true, true, 6){
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
        @Override
        public String getDesc(boolean ingame){
            return "How far away from logs should leaf blocks be detected?";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Integer value){
            return new ItemBuilder(Material.OAK_LEAVES).setCount(value);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<Integer> LEAF_BREAK_RANGE = new Option<Integer>("Leaf Break Range", true, true, true, 6){
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
        @Override
        public String getDesc(boolean ingame){
            return "How far away from logs should leaf blocks be destroyed? (set to 0 to prevent leaves from being destroyed) (Values over 6 are useless for vanilla trees, as these leaves would naturally decay anyway)";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Integer value){
            return new ItemBuilder(Material.OAK_LEAVES).setCount(value);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<Integer> REQUIRED_LOGS = new Option<Integer>("Required Logs", true, true, true, 4){
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
        @Override
        public DebugResult doCheckTrunk(TreeFeller plugin, Tool tool, Tree tree, HashMap<Integer, ArrayList<Block>> blocks, Block block){
            int total = getTotal(blocks);
            if(toolValues.get(tool)==null&&treeValues.get(tree)==null&&globalValue!=null&&total<globalValue){
                return new DebugResult(this, GLOBAL, total, globalValue);
            }
            if(toolValues.get(tool)!=null&&total<toolValues.get(tool)){
                return new DebugResult(this, TOOL, total, toolValues.get(tool));
            }
            if(treeValues.get(tree)!=null&&total<treeValues.get(tree)){
                return new DebugResult(this, TREE, total, treeValues.get(tree));
            }
            return new DebugResult(this, SUCCESS, total);
        }
        @Override
        public String getDesc(boolean ingame){
            return "How many logs should be required for logs to be counted as a tree?";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Tree has too few logs$: {0}<{1}", "Tree has enough logs");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Integer value){
            return new ItemBuilder(Material.OAK_LOG).setCount(value);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<Integer> REQUIRED_LEAVES = new Option<Integer>("Required Leaves", true, true, true, 10){
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
        @Override
        public DebugResult doCheckTree(TreeFeller plugin, Tool tool, Tree tree, HashMap<Integer, ArrayList<Block>> blocks, int leaves){
            if(toolValues.get(tool)==null&&treeValues.get(tree)==null&&globalValue!=null&&leaves<globalValue){
                return new DebugResult(this, GLOBAL, leaves, globalValue);
            }
            if(toolValues.get(tool)!=null&&leaves<toolValues.get(tool)){
                return new DebugResult(this, TOOL, leaves, toolValues.get(tool));
            }
            if(treeValues.get(tree)!=null&&leaves<treeValues.get(tree)){
                return new DebugResult(this, TREE, leaves, treeValues.get(tree));
            }
            return new DebugResult(this, SUCCESS, leaves);
        }
        @Override
        public String getDesc(boolean ingame){
            return "How many leaves should be required for logs to be counted as a tree?";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Tree has too few leaves$: {0}<{1}", "Tree has enough leaves");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Integer value){
            return new ItemBuilder(Material.OAK_LEAVES).setCount(value);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<Integer> MAX_LOGS = new Option<Integer>("Max Logs", true, true, true, 250){
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
        @Override
        public DebugResult doCheckTrunk(TreeFeller plugin, Tool tool, Tree tree, HashMap<Integer, ArrayList<Block>> blocks, Block block){
            int total = getTotal(blocks);
            if(toolValues.get(tool)==null&&treeValues.get(tree)==null&&globalValue!=null&&total>globalValue){
                return new DebugResult(this, GLOBAL, total, globalValue);
            }
            if(toolValues.get(tool)!=null&&total>toolValues.get(tool)){
                return new DebugResult(this, TOOL, total, toolValues.get(tool));
            }
            if(treeValues.get(tree)!=null&&total>treeValues.get(tree)){
                return new DebugResult(this, TREE, total, treeValues.get(tree));
            }
            return new DebugResult(this, SUCCESS, total);
        }
        @Override
        public String getDesc(boolean ingame){
            return "What is the maximum number of logs that a tree may have and still be counted as a tree?";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Tree has too many logs$: {0}>{1}", "Tree has few enough logs");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Integer value){
            return new ItemBuilder(Material.OAK_LOG).setCount(value);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
        @Override
        public Integer get(Tool tool, Tree tree){
            Integer val = null;
            if(getValue(tool)==null&&getValue(tree)==null&&getValue()!=null)val = Math.min(val==null?Integer.MAX_VALUE:val, getValue());
            if(getValue(tool)!=null)val = Math.min(val==null?Integer.MAX_VALUE:val, getValue(tool));
            if(getValue(tree)!=null)val = Math.min(val==null?Integer.MAX_VALUE:val, getValue(tree));
            return val;
        }
    };
    public static Option<Integer> MAX_HEIGHT = new Option<Integer>("Max Height", true, true, true, 5){
        @Override
        public Integer load(Object o) {
            return loadInt(o);
        }
        @Override
        public DebugResult doCheckTrunk(TreeFeller plugin, Tool tool, Tree tree, HashMap<Integer, ArrayList<Block>> blocks, Block block){
            if(plugin.cascading)return new DebugResult(this, SUCCESS);
            int minY = block.getY();
            for(int i : blocks.keySet()){
                for(Block b : blocks.get(i)){
                    minY = Math.min(minY, b.getY());
                }
            }
            int h = block.getY()-minY+1;
            if(toolValues.get(tool)==null&&treeValues.get(tree)==null&&globalValue!=null&&h>globalValue){
                int i = h-globalValue;
                return new DebugResult(this, GLOBAL, i, h, globalValue);
            }
            if(toolValues.containsKey(tool)&&h>toolValues.get(tool)){
                int i = h-toolValues.get(tool);
                return new DebugResult(this, TOOL, i, h, toolValues.get(tool));
            }
            if(treeValues.containsKey(tree)&&h>treeValues.get(tree)){
                int i = h-treeValues.get(tree);
                return new DebugResult(this, TREE, i, h, treeValues.get(tree));
            }
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(boolean ingame){
            return "How far from the bottom can you cut down a tree? (Prevents you from cutting it down from the top) 1 = bottom block";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Tree was cut {0} blocks too high$", "Tree was cut low enough");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Integer value){
            return new ItemBuilder(Material.LADDER).setCount(value);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 1, Integer.MAX_VALUE, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 1, Integer.MAX_VALUE, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 1, Integer.MAX_VALUE, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static OptionBoolean ALLOW_PARTIAL = new OptionBoolean("Allow Partial", true, true, true, false){
        @Override
        public String getDesc(boolean ingame){
            return "Should trees be able to be partially cut down if the tool has insufficient durability? It cannot be guaranteed what part of the tree will be cut down!";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Boolean value){
            return new ItemBuilder(Material.STICK);
        }
    };
    public static OptionBoolean PLAYER_LEAVES = new OptionBoolean("Player Leaves", true, true, true, false){
        @Override
        public String getDesc(boolean ingame){
            return "Should leaves placed by players be cut down also? (Only works with _LEAVES materials)";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Boolean value){
            return new ItemBuilder(Material.OAK_LEAVES);
        }
    };
    public static OptionBoolean DIAGONAL_LEAVES = new OptionBoolean("Diagonal Leaves", true, false, true, false){
        @Override
        public String getDesc(boolean ingame){
            return "If set to true, leaves will be detected diagonally; May require ignore-leaf-data to work properly";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Boolean value){
            return new ItemBuilder(Material.OAK_LEAVES);
        }
    };
    public static OptionBoolean IGNORE_LEAF_DATA = new OptionBoolean("Ignore Leaf Data", true, false, true, false){
        @Override
        public String getDesc(boolean ingame){
            return "If set to true, leaves' blockdata will be ignored. This should only be set if custom trees' leaves are not being destroyed when they should.";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Boolean value){
            return new ItemBuilder(Material.OAK_LEAVES);
        }
    };
    public static OptionBoolean REQUIRE_CROSS_SECTION = new OptionBoolean("Require Cross Section", true, true, true, false){
        @Override
        public DebugResult doCheckTrunk(TreeFeller plugin, Tool tool, Tree tree, HashMap<Integer, ArrayList<Block>> blocks, Block block){
            if((toolValues.get(tool)==null&&treeValues.get(tree)==null&&Objects.equals(globalValue, true))||Objects.equals(toolValues.get(tool), true)||Objects.equals(treeValues.get(tree), true)){
                for(int x = -1; x<=1; x++){
                    for(int z = -1; z<=1; z++){
                        if(x==0&&z==0)continue;
                        if(tree.trunk.contains(block.getRelative(x, 0, z).getType())){
                            if(toolValues.get(tool)==null&&treeValues.get(tree)==null&&Objects.equals(globalValue, true))return new DebugResult(this, GLOBAL, block.getX()+x, block.getY()+" "+block.getZ()+z);
                            if(Objects.equals(toolValues.get(tool), true))return new DebugResult(this, TOOL, block.getX()+x, block.getY()+" "+block.getZ()+z);
                            if(Objects.equals(treeValues.get(tree), true))return new DebugResult(this, TREE, block.getX()+x, block.getY()+" "+block.getZ()+z);
                        }
                    }
                }
            }
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(boolean ingame){
            return "Should trees larger than 1x1 require an entire horizontal cross-section to be mined before the tree fells? (Works for up to 2x2 trees)";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("A full cross-section has not been cut$ at ({0}, {1}, {2})", "A full cross-section has been cut");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Boolean value){
            return new ItemBuilder(Material.DARK_OAK_LOG);
        }
    };
    public static OptionBoolean FORCE_DISTANCE_CHECK = new OptionBoolean("Force Distance Check", true, false, true, false){
        @Override
        public String getDesc(boolean ingame){
            return "If set to true, all non-leaf-block leaves will be distance-checked to make sure they belong to the tree being felled (ex. mushrooms or nether 'tree' leaves)\n"
                    + "WARNING: THIS CAN CAUSE SIGNIFICANT LAG";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Boolean value){
            return new ItemBuilder(Material.DETECTOR_RAIL);
        }
    };
    //unnatural tree detection settings
    public static Option<HashSet<Material>> BANNED_LOGS = new Option<HashSet<Material>>("Banned Logs", true, true, true, new HashSet<>()){
        @Override
        public String getDesc(boolean ingame){
            return "Which trunk blocks should prevent a tree from being felled? (Intended to help prevent player structures from being cut down)\n(For this to work, these must also be included in the tree trunk materials)\nEx. planks/glass";
        }
        @Override
        public HashSet<Material> load(Object o){
            return loadMaterialSet(o);
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(HashSet<Material> value){
            return new ItemBuilder(Material.OAK_PLANKS);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyMaterialSet(parent, parent.plugin, parent.player, name, true, "block", globalValue, (material) -> {
                return material.isBlock();
            }, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyMaterialSet(parent, parent.plugin, parent.player, name, true, "block", toolValues.get(tool), (material) -> {
                return material.isBlock();
            }, (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyMaterialSet(parent, parent.plugin, parent.player, name, true, "block", treeValues.get(tree), (material) -> {
                return material.isBlock();
            }, (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
        @Override
        protected DebugResult doCheckTrunk(TreeFeller plugin, Tool tool, Tree tree, HashMap<Integer, ArrayList<Block>> blocks, Block block){
            for(ArrayList<Block> blox : blocks.values()){
                for(Block blok : blox){
                    Material mat = blok.getType();
                    if(toolValues.get(tool)==null&&treeValues.get(tree)==null&&globalValue!=null&&globalValue.contains(mat)){
                        return new DebugResult(this, GLOBAL, mat);
                    }
                    if(toolValues.get(tool)!=null&&toolValues.get(tool).contains(mat)){
                        return new DebugResult(this, TOOL, mat);
                    }
                    if(treeValues.get(tree)!=null&&treeValues.get(tree).contains(mat)){
                        return new DebugResult(this, TREE, mat);
                    }
                }
            }
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Tree contains banned log$: {0}", "Tree does not contain any banned logs");
        }
    };
    public static Option<HashSet<Material>> BANNED_LEAVES = new Option<HashSet<Material>>("Banned Leaves", true, true, true, new HashSet<>()){
        @Override
        public String getDesc(boolean ingame){
            return "Which blocks should prevent a tree from being felled? (Intended to help prevent player structures from being cut down)\n(For this to work, these must also be included in the tree leaf materials)\nEx. planks or glass";
        }
        @Override
        public HashSet<Material> load(Object o){
            return loadMaterialSet(o);
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(HashSet<Material> value){
            return new ItemBuilder(Material.OAK_PLANKS);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyMaterialSet(parent, parent.plugin, parent.player, name, true, "block", globalValue, (material) -> {
                return material.isBlock();
            }, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyMaterialSet(parent, parent.plugin, parent.player, name, true, "block", toolValues.get(tool), (material) -> {
                return material.isBlock();
            }, (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyMaterialSet(parent, parent.plugin, parent.player, name, true, "block", treeValues.get(tree), (material) -> {
                return material.isBlock();
            }, (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
        @Override
        protected DebugResult doCheckTree(TreeFeller plugin, Tool tool, Tree tree, HashMap<Integer, ArrayList<Block>> blocks, int leaves){
            for(ArrayList<Block> blox : blocks.values()){
                for(Block blok : blox){
                    Material mat = blok.getType();
                    if(toolValues.get(tool)==null&&treeValues.get(tree)==null&&globalValue!=null&&globalValue.contains(mat)){
                        return new DebugResult(this, GLOBAL, mat);
                    }
                    if(toolValues.get(tool)!=null&&toolValues.get(tool).contains(mat)){
                        return new DebugResult(this, TOOL, mat);
                    }
                    if(treeValues.get(tree)!=null&&treeValues.get(tree).contains(mat)){
                        return new DebugResult(this, TREE, mat);
                    }
                }
            }
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Tree contains banned leaf$: {0}", "Tree does not contain any banned leaves");
        }
    };
    public static Option<Integer> MAX_HORIZONTAL_TRUNK_PILLAR_LENGTH = new Option<Integer>("Max Horizontal Trunk Pillar Length", true, true, true, 6){
        @Override
        public String getDesc(boolean ingame){
            return "What is the maximum number of trunk blocks that may be in a horizontal line?\n(This is to help detect structures with long horizontal pillars of logs)";
        }
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Integer value){
            return new ItemBuilder(Material.SCAFFOLDING);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 1, Integer.MAX_VALUE, true, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 1, Integer.MAX_VALUE, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 1, Integer.MAX_VALUE, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
        @Override
        protected DebugResult doCheckTrunk(TreeFeller plugin, Tool tool, Tree tree, HashMap<Integer, ArrayList<Block>> blocks, Block block){
            int actual = 0;
            ArrayList<Block> trunk = new ArrayList<>();
            for(ArrayList<Block> blox : blocks.values())trunk.addAll(blox);
            ArrayList<Block> xAxis = new ArrayList<>(trunk);
            while(!xAxis.isEmpty()){
                Block b = xAxis.get(0);
                boolean stillPartOfTheTree;
                do{
                    stillPartOfTheTree = false;
                    Block previous = b.getRelative(-1, 0, 0);
                    //dunno if Block is immutable, so I'll check it a more complicated way
                    for(Block checking : xAxis){
                        if(checking.getX()==previous.getX()&&checking.getY()==previous.getY()&&checking.getZ()==previous.getZ()){
                            b = checking;
                            stillPartOfTheTree = true;
                            break;
                        }
                    }
                }while(stillPartOfTheTree);
                //b should now be the first block in this row
                ArrayList<Block> line = new ArrayList<>();
                do{
                    line.add(b);
                    stillPartOfTheTree = false;
                    Block next = b.getRelative(1, 0, 0);
                    //dunno if Block is immutable, so I'll check it a more complicated way
                    for(Block checking : xAxis){
                        if(checking.getX()==next.getX()&&checking.getY()==next.getY()&&checking.getZ()==next.getZ()){
                            b = checking;
                            stillPartOfTheTree = true;
                            break;
                        }
                    }
                }while(stillPartOfTheTree);
                actual = Math.max(actual, line.size());
                xAxis.removeAll(line);//don't need to recheck the same line many times. Maybe this will actually make it faster even with all the nonsense above
            }
            //DUPLICATED CODE ALERT
            ArrayList<Block> zAxis = new ArrayList<>(trunk);
            while(!zAxis.isEmpty()){
                Block b = zAxis.get(0);
                boolean stillPartOfTheTree;
                do{
                    stillPartOfTheTree = false;
                    Block previous = b.getRelative(0, 0, -1);
                    //dunno if Block is immutable, so I'll check it a more complicated way
                    for(Block checking : zAxis){
                        if(checking.getX()==previous.getX()&&checking.getY()==previous.getY()&&checking.getZ()==previous.getZ()){
                            b = checking;
                            stillPartOfTheTree = true;
                            break;
                        }
                    }
                }while(stillPartOfTheTree);
                //b should now be the first block in this row
                ArrayList<Block> line = new ArrayList<>();
                do{
                    line.add(b);
                    stillPartOfTheTree = false;
                    Block next = b.getRelative(0, 0, 1);
                    //dunno if Block is immutable, so I'll check it a more complicated way
                    for(Block checking : zAxis){
                        if(checking.getX()==next.getX()&&checking.getY()==next.getY()&&checking.getZ()==next.getZ()){
                            b = checking;
                            stillPartOfTheTree = true;
                            break;
                        }
                    }
                }while(stillPartOfTheTree);
                actual = Math.max(actual, line.size());
                zAxis.removeAll(line);//don't need to recheck the same line many times. Maybe this will actually make it faster even with all the nonsense above
            }
            if(toolValues.get(tool)==null&&treeValues.get(tree)==null&&globalValue!=null&&actual>globalValue)return new DebugResult(this, GLOBAL, actual, globalValue);
            if(treeValues.containsKey(tree)&&actual>treeValues.get(tree))return new DebugResult(this, TREE, actual, treeValues.get(tree));
            if(toolValues.containsKey(tool)&&actual>toolValues.get(tool))return new DebugResult(this, TOOL, actual, toolValues.get(tool));
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Found a horizontal trunk pillar that was too long$! {0}>{1}", "No excessive horizontal trunk pillars found");
        }
    };
    public static Option<Integer> MAX_TRUNKS = new Option<Integer>("Max Trunks", true, true, true, 1){
        @Override
        public String getDesc(boolean ingame){
            return "What is the maximum number of trunks a tree may have?\nNote that the trunks are counted at the level at which the tree is cut; not at the base of the tree\nSimilarly to leave-stump, this may include low-hanging leaves";
        }
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Integer value){
            return new ItemBuilder(Material.TRIDENT);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 1, Integer.MAX_VALUE, true, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 1, Integer.MAX_VALUE, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 1, Integer.MAX_VALUE, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
        @Override
        protected DebugResult doCheckTrunk(TreeFeller plugin, Tool tool, Tree tree, HashMap<Integer, ArrayList<Block>> blocks, Block block){
            int value = 0;
            ArrayList<Block> trunkSlice = new ArrayList<>();
            for(ArrayList<Block> blox : blocks.values()){
                for(Block b : blox){
                    if(b.getY()==block.getY())trunkSlice.add(b);
                }
            }
            while(!trunkSlice.isEmpty()){
                ArrayList<Block> trunk = getTrunkBit(trunkSlice, trunkSlice.get(0), true);
                trunkSlice.removeAll(trunk);
                value++;
            }
            if(toolValues.get(tool)==null&&treeValues.get(tree)==null&&globalValue!=null&&value>globalValue)return new DebugResult(this, GLOBAL, value, globalValue);
            if(treeValues.containsKey(tree)&&value>treeValues.get(tree))return new DebugResult(this, TREE, value, treeValues.get(tree));
            if(toolValues.containsKey(tool)&&value>toolValues.get(tool))return new DebugResult(this, TOOL, value, toolValues.get(tool));
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Tree has too many trunks$! {0}>{1}", "Trunk count is valid");
        }
        private ArrayList<Block> getTrunkBit(ArrayList<Block> theWholeTrunkSlice, Block startingBlock, boolean diagonal){
            //layer zero
            HashMap<Integer, ArrayList<Block>> results = new HashMap<>();
            ArrayList<Block> zero = new ArrayList<>();
            zero.add(startingBlock);
            results.put(0, zero);
            //all the other layers
            int i = -1;
            while(true){
                i++;
                ArrayList<Block> layer = new ArrayList<>();
                ArrayList<Block> lastLayer = new ArrayList<>(results.get(i));
                if(i==0&&lastLayer.isEmpty()){
                    lastLayer.add(startingBlock);
                }
                for(Block block : lastLayer){
                    if(diagonal){
                        for(int x = -1; x<=1; x++){
                            for(int z = -1; z<=1; z++){
                                if(x==0&&z==0)continue;//same block
                                Block newBlock = block.getRelative(x,0,z);
                                boolean yep = false;
                                for(Block checking : theWholeTrunkSlice){
                                    if(checking.getX()==newBlock.getX()&&checking.getY()==newBlock.getY()&&checking.getZ()==newBlock.getZ()){
                                        yep = true;
                                        newBlock = checking;//just to be sure
                                        break;
                                    }
                                }
                                if(!yep)continue;
                                if(lastLayer.contains(newBlock))continue;//if the new block is on the same layer, ignore
                                if(i>0&&results.get(i-1).contains(newBlock))continue;//if the new block is on the previous layer, ignore
                                if(layer.contains(newBlock))continue;//if the new block is on the next layer, but already processed, ignore
                                layer.add(newBlock);
                            }
                        }
                    }else{
                        for(int j = 0; j<4; j++){
                            int x=0,z=0;
                            switch(j){
                                case 0:
                                    x = -1;
                                    break;
                                case 1:
                                    x = 1;
                                    break;
                                case 2:
                                    z = -1;
                                    break;
                                case 3:
                                    z = 1;
                                    break;
                                default:
                                    throw new IllegalArgumentException("How did this happen?");
                            }
                            Block newBlock = block.getRelative(x,0,z);
                            boolean yep = false;
                            for(Block checking : theWholeTrunkSlice){
                                if(checking.getX()==newBlock.getX()&&checking.getY()==newBlock.getY()&&checking.getZ()==newBlock.getZ()){
                                    yep = true;
                                    newBlock = checking;//just to be sure
                                    break;
                                }
                            }
                            if(!yep)continue;
                            if(lastLayer.contains(newBlock))continue;//if the new block is on the same layer, ignore
                            if(i>0&&results.get(i-1).contains(newBlock))continue;//if the new block is on the previous layer, ignore
                            if(layer.contains(newBlock))continue;//if the new block is on the next layer, but already processed, ignore
                            layer.add(newBlock);
                        }
                    }
                }
                if(layer.isEmpty())break;
                results.put(i+1, layer);
            }
            ArrayList<Block> properResults = new ArrayList<>();
            for(ArrayList<Block> blox : results.values()){
                properResults.addAll(blox);
            }
            return properResults;
        }
    };
    //tree cutting details
    public static OptionBoolean CUTTING_ANIMATION = new OptionBoolean("Cutting Animation", true, true, true, false){
        @Override
        public String getDesc(boolean ingame){
            return "Should the tree cut down with an animation?";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Boolean value){
            return new ItemBuilder(Material.GOLDEN_AXE);
        }
    };
    public static Option<Integer> ANIM_DELAY = new Option<Integer>("Anim Delay", true, true, true, 1){
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
        @Override
        public String getDesc(boolean ingame){
            return "Animation delay, in ticks";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Integer value){
            return new ItemBuilder(Material.CLOCK).setCount(value);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 1, Integer.MAX_VALUE, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 1, Integer.MAX_VALUE, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 1, Integer.MAX_VALUE, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static OptionBoolean REPLANT_SAPLINGS = new OptionBoolean("Replant Saplings", true, true, true, false){
        @Override
        public String getDesc(boolean ingame){
            return "Should saplings be replanted?";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Boolean value){
            return new ItemBuilder(Material.OAK_SAPLING);
        }
    };
    public static OptionBoolean USE_INVENTORY_SAPLINGS = new OptionBoolean("Use Inventory Saplings", true, true, true, false){
        @Override
        public String getDesc(boolean ingame){
            return "Should saplings be taken from the player's inventory to replant trees?";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Boolean value){
            return new ItemBuilder(Material.CHEST);
        }
    };
    public static Option<Integer> SPAWN_SAPLINGS = new Option<Integer>("Spawn Saplings", true, true, true, 0){
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
        @Override
        public Integer get(Tool tool, Tree tree){
            if(toolValues.containsKey(tool)||treeValues.containsKey(tree)){
                Integer tl = toolValues.getOrDefault(tool, 0);
                Integer tr = treeValues.getOrDefault(tree, 0);
                return Math.max(tl, tr);
            }
            return globalValue;
        }
        @Override
        public String getDesc(boolean ingame){
            return "Should saplings be spawned?\n" +
                "0 = No, only replant if the leaves drop saplings\n" +
                "1 = Yes, but only if the leaves do not drop enough\n" +
                "2 = Yes, always spawn new saplings";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Integer value){
            return new ItemBuilder(Material.OAK_SAPLING);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifySpawnSaplings(parent, parent.plugin, parent.player, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifySpawnSaplings(parent, parent.plugin, parent.player, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifySpawnSaplings(parent, parent.plugin, parent.player, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<HashSet<Material>> SAPLING = new Option<HashSet<Material>>("Sapling", false, false, true, null){
        @Override
        public HashSet<Material> load(Object o){
            return loadMaterialSet(o);
        }
        @Override
        public String getDesc(boolean ingame){
            return "If replant-saplings is enabled, this will replant the tree with this type of sapling";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(HashSet<Material> value){
            return new ItemBuilder(Material.OAK_SAPLING);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyMaterialSet(parent, parent.plugin, parent.player, name, false, "block", globalValue, (material) -> {
                return material.isBlock();
            }, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyMaterialSet(parent, parent.plugin, parent.player, name, true, "block", toolValues.get(tool), (material) -> {
                return material.isBlock();
            }, (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyMaterialSet(parent, parent.plugin, parent.player, name, true, "block", treeValues.get(tree), (material) -> {
                return material.isBlock();
            }, (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<Integer> SAPLING_TIMEOUT = new Option<Integer>("Sapling Timeout", true, true, true, 50){
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
        @Override
        public Integer get(Tool tool, Tree tree){
            if(toolValues.containsKey(tool)||treeValues.containsKey(tree)){
                Integer tl = toolValues.getOrDefault(tool, 0);
                Integer tr = treeValues.getOrDefault(tree, 0);
                return Math.max(tl, tr);
            }
            return globalValue;
        }
        @Override
        public String getDesc(boolean ingame){
            return "The amount of delay a sapling may take to respawn (if spawn saplings is set to 1, saplings will be spawned only after this amount of time, but will still be immediately planted upon dropping from leaves)";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Integer value){
            return new ItemBuilder(Material.CLOCK);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<Integer> MAX_SAPLINGS = new Option<Integer>("Max Saplings", false, false, true, 1){
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
        @Override
        public String getDesc(boolean ingame){
            return "If replant-saplings is enabled, this will limit the number of saplings that can be replanted";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Integer value){
            return new ItemBuilder(Material.OAK_SAPLING).setCount(value);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 1, Integer.MAX_VALUE, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 1, Integer.MAX_VALUE, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 1, Integer.MAX_VALUE, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<HashSet<Material>> GRASS = new Option<HashSet<Material>>("Grass", true, false, true, defaultGrasses){
        @Override
        public HashSet<Material>load(Object o){
            return loadMaterialSet(o);
        }
        @Override
        public String getDesc(boolean ingame){
            return "What blocks can saplings be planted on?";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(HashSet<Material> value){
            return new ItemBuilder(Material.GRASS_BLOCK);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyMaterialSet(parent, parent.plugin, parent.player, name, false, "block", globalValue, (material) -> {
                return material.isBlock();
            }, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyMaterialSet(parent, parent.plugin, parent.player, name, true, "block", toolValues.get(tool), (material) -> {
                return material.isBlock();
            }, (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyMaterialSet(parent, parent.plugin, parent.player, name, true, "block", treeValues.get(tree), (material) -> {
                return material.isBlock();
            }, (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<HashSet<Material>> ROOTS = new Option<HashSet<Material>>("Roots", false, false, true, null){
        @Override
        public HashSet<Material> load(Object o){
            return loadMaterialSet(o);
        }
        @Override
        public String getDesc(boolean ingame){
            return "Roots can be used to cut down a tree when you can't reach the trunk. (The nearest tree will be attempted to be cut down)";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(HashSet<Material> value){
            Material m = Material.matchMaterial("MANGROVE_ROOTS");
            if(m==null)m = Material.OAK_WOOD;
            return new ItemBuilder(m);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyMaterialSet(parent, parent.plugin, parent.player, name, false, "block", globalValue, (material) -> {
                return material.isBlock();
            }, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyMaterialSet(parent, parent.plugin, parent.player, name, true, "block", toolValues.get(tool), (material) -> {
                return material.isBlock();
            }, (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyMaterialSet(parent, parent.plugin, parent.player, name, true, "block", treeValues.get(tree), (material) -> {
                return material.isBlock();
            }, (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<Integer> ROOT_DISTANCE = new Option<Integer>("Root Distance", true, false, true, 6){
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
        @Override
        public String getDesc(boolean ingame){
            return "How far away from the trunk can you use roots to cut down a tree?";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Integer value){
            Material m = Material.matchMaterial("MANGROVE_ROOTS");
            if(m==null)m = Material.OAK_WOOD;
            return new ItemBuilder(m).setCount(value);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    //tree falling details
    public static Option<FellBehavior> LOG_BEHAVIOR = new Option<FellBehavior>("Log Behavior", true, true, true, FellBehavior.BREAK){
        @Override
        public FellBehavior load(Object o){
            if(o instanceof FellBehavior)return (FellBehavior)o;
            if(o instanceof String){
                try{
                    return FellBehavior.valueOf((String)o);
                }catch(Exception ex){
                    return null;
                }
            }
            return null;
        }
        @Override
        public String getDesc(boolean ingame){
            String s = "What felling behavior should logs have?\n";
            if(!ingame){
                s+="Valid options:\n";
                int width = findMaxWidth(FellBehavior.values());
                for(FellBehavior behavior : FellBehavior.values()){
                    s+=normalize(behavior.name(), width)+" "+behavior.getDescription()+"\n";
                }
            }
            s+="Note that falling blocks occasionally drop as items if they land wrong";
            return s;
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(FellBehavior value){
            return new ItemBuilder(Material.OAK_LOG);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyFellBehavior(parent, parent.plugin, parent.player, name, false, globalValue, FellBehavior.values(), (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyFellBehavior(parent, parent.plugin, parent.player, name, true, toolValues.get(tool), FellBehavior.values(), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyFellBehavior(parent, parent.plugin, parent.player, name, true, treeValues.get(tree), FellBehavior.values(), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<FellBehavior> LEAF_BEHAVIOR = new Option<FellBehavior>("Leaf Behavior", true, true, true, FellBehavior.BREAK){
        @Override
        public FellBehavior load(Object o){
            if(o instanceof FellBehavior)return (FellBehavior)o;
            if(o instanceof String){
                try{
                    return FellBehavior.valueOf((String)o);
                }catch(Exception ex){
                    return null;
                }
            }
            return null;
        }
        @Override
        public String getDesc(boolean ingame){
            String s = "What felling behavior should leaves have?\n";
            if(!ingame){
                s+="Valid options:\n";
                int len = findMaxWidth(FellBehavior.values());
                for(FellBehavior behavior : FellBehavior.values()){
                    s+=normalize(behavior.name(), len)+" "+behavior.getDescription()+"\n";
                }
            }
            s+="Note that falling blocks occasionally drop as items if they land wrong";
            return s;
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(FellBehavior value){
            return new ItemBuilder(Material.OAK_LEAVES);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyFellBehavior(parent, parent.plugin, parent.player, name, false, globalValue, FellBehavior.values(), (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyFellBehavior(parent, parent.plugin, parent.player, name, true, toolValues.get(tool), FellBehavior.values(), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyFellBehavior(parent, parent.plugin, parent.player, name, true, treeValues.get(tree), FellBehavior.values(), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<Float> FALL_HURT_AMOUNT = new Option<Float>("Fall Hurt Amount", true, true, true, 2.0f){
        @Override
        public Float load(Object o){
            return loadFloat(o);
        }
        @Override
        public String getDesc(boolean ingame){
            return "How much damage should falling blocks deal per block fallen? (Only applies to FALL_HURT fell behaviors)";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Float value){
            return new ItemBuilder(Material.CHIPPED_ANVIL);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyFloat(parent, parent.plugin, parent.player, name, 0f, Integer.MAX_VALUE, 1, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyFloat(parent, parent.plugin, parent.player, name, 0f, Integer.MAX_VALUE, 1, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyFloat(parent, parent.plugin, parent.player, name, 0f, Integer.MAX_VALUE, 1, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<Integer> FALL_HURT_MAX = new Option<Integer>("Fall Hurt Max", true, true, true, 40){
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
        @Override
        public String getDesc(boolean ingame){
            return "What is the maximum amount of damage a falling block may deal upon landing? (Only applies to FALL_HURT fell behaviors)";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Integer value){
            return new ItemBuilder(Material.DAMAGED_ANVIL);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<DirectionalFallBehavior> DIRECTIONAL_FALL_BEHAVIOR = new Option<DirectionalFallBehavior>("Directional Fall Behavior", true, true, true, DirectionalFallBehavior.RANDOM){
        @Override
        public DirectionalFallBehavior load(Object o){
            if(o instanceof DirectionalFallBehavior)return (DirectionalFallBehavior)o;
            if(o instanceof String){
                try{
                    return DirectionalFallBehavior.valueOf((String)o);
                }catch(Exception ex){
                    return null;
                }
            }
            return null;
        }
        @Override
        public String getDesc(boolean ingame){
            String s = "Which direction should the tree fall in?\n" +
                "(Only used when log or leaf behavior is set to FALL or similar)\n";
            if(!ingame){
                s+="Valid options:\n";
                int i = findMaxWidth(DirectionalFallBehavior.values());
                for(DirectionalFallBehavior behavior : DirectionalFallBehavior.values()){
                    s+=normalize(behavior.name(), i)+" "+behavior.getDescription()+"\n";
                }
            }
            return s;
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(DirectionalFallBehavior value){
            return new ItemBuilder(Material.OAK_LOG);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyDirectionalFallBehavior(parent, parent.plugin, parent.player, name, false, globalValue, DirectionalFallBehavior.values(), (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyDirectionalFallBehavior(parent, parent.plugin, parent.player, name, true, toolValues.get(tool), DirectionalFallBehavior.values(), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyDirectionalFallBehavior(parent, parent.plugin, parent.player, name, true, treeValues.get(tree), DirectionalFallBehavior.values(), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<HashSet<Material>> OVERRIDABLES = new Option<HashSet<Material>>("Overridables", true, true, true, defaultOverridables){
        @Override
        public HashSet<Material> load(Object o){
            return loadMaterialSet(o);
        }
        @Override
        public String getDesc(boolean ingame){
            return "This is the list of blocks that may be overridden when a tree falls onto them (e.g air, grass, water)\n" +
"(Only used for NATURAL fell behavior)";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(HashSet<Material> value){
            return new ItemBuilder(Material.GRASS);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyMaterialSet(parent, parent.plugin, parent.player, name, false, "block", globalValue, (material) -> {
                return material.isBlock();
            }, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyMaterialSet(parent, parent.plugin, parent.player, name, true, "block", toolValues.get(tool), (material) -> {
                return material.isBlock();
            }, (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyMaterialSet(parent, parent.plugin, parent.player, name, true, "block", treeValues.get(tree), (material) -> {
                return material.isBlock();
            }, (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static OptionBoolean LOCK_FALL_CARDINAL = new OptionBoolean("Lock Fall Cardinal", true, true, true, false){
        @Override
        public String getDesc(boolean ingame){
            return "If set to true, trees can only fall in one of the cardinal directions (N/S/E/W)";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Boolean value){
            return new ItemBuilder(Material.COMPASS);
        }
    };
    public static Option<Double> DIRECTIONAL_FALL_VELOCITY = new Option<Double>("Directional Fall Velocity", true, true, true, .35d){
        @Override
        public Double load(Object o){
            return loadDouble(o);
        }
        @Override
        public Double get(Tool tool, Tree tree){
            double glob = 0;
            if(toolValues.get(tool)==null||treeValues.get(tree)==null)glob = globalValue;
            Double tl = toolValues.getOrDefault(tool, 0d);
            Double tr = treeValues.getOrDefault(tree, 0d);
            return tl+tr+glob;
        }
        @Override
        public String getDesc(boolean ingame){
            return "How much horizontal velocity should falling trees get?\n" +
                "(Only used when log or leaf behavior is set to FALL or similar)\n" +
                "All of the blocks in the tree will fall in the same direction with this velocity.";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Double value){
            return new ItemBuilder(Material.OAK_LOG);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, Integer.MIN_VALUE, Integer.MAX_VALUE, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, Integer.MIN_VALUE, Integer.MAX_VALUE, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, Integer.MIN_VALUE, Integer.MAX_VALUE, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<Double> VERTICAL_FALL_VELOCITY = new Option<Double>("Vertical Fall Velocity", true, true, true, .05d){
        @Override
        public Double load(Object o){
            return loadDouble(o);
        }
        @Override
        public Double get(Tool tool, Tree tree){
            double glob = 0;
            if(toolValues.get(tool)==null||treeValues.get(tree)==null)glob = globalValue;
            Double tl = toolValues.getOrDefault(tool, 0d);
            Double tr = treeValues.getOrDefault(tree, 0d);
            return tl+tr+glob;
        }
        @Override
        public String getDesc(boolean ingame){
            return "How much upwards velocity should falling trees get?\n" +
                "(Only used when log or leaf behavior is set to FALL or similar)";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Double value){
            return new ItemBuilder(Material.OAK_LOG);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, Integer.MIN_VALUE, Integer.MAX_VALUE, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, Integer.MIN_VALUE, Integer.MAX_VALUE, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, Integer.MIN_VALUE, Integer.MAX_VALUE, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<Double> EXPLOSIVE_FALL_VELOCITY = new Option<Double>("Explosive Fall Velocity", true, true, true, 0d){
        @Override
        public Double load(Object o){
            return loadDouble(o);
        }
        @Override
        public Double get(Tool tool, Tree tree){
            double glob = 0;
            if(toolValues.get(tool)==null||treeValues.get(tree)==null)glob = globalValue;
            Double tl = toolValues.getOrDefault(tool, 0d);
            Double tr = treeValues.getOrDefault(tree, 0d);
            return tl+tr+glob;
        }
        @Override
        public String getDesc(boolean ingame){
            return "How much explosive sideways velocity should falling blocks get? Velocity is applied away from the block that was used to cut down the tree (no velocity will be applied to blocks in the exact center)\n" +
                "(Only used when log or leaf behavior is set to FALL or similar)";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Double value){
            return new ItemBuilder(Material.TNT);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, Integer.MIN_VALUE, Integer.MAX_VALUE, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, Integer.MIN_VALUE, Integer.MAX_VALUE, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, Integer.MIN_VALUE, Integer.MAX_VALUE, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<Double> RANDOM_FALL_VELOCITY = new Option<Double>("Random Fall Velocity", true, true, true, 0d){
        @Override
        public Double load(Object o){
            return loadDouble(o);
        }
        @Override
        public Double get(Tool tool, Tree tree){
            double glob = 0;
            if(toolValues.get(tool)==null||treeValues.get(tree)==null)glob = globalValue;
            Double tl = toolValues.getOrDefault(tool, 0d);
            Double tr = treeValues.getOrDefault(tree, 0d);
            return tl+tr+glob;
        }
        @Override
        public String getDesc(boolean ingame){
            return "How much random sideways velocity should falling blocks get?\n" +
                "(Only used when log or leaf behavior is set to FALL or similar)";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Double value){
            return new ItemBuilder(Material.OAK_LOG);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, Integer.MIN_VALUE, Integer.MAX_VALUE, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, Integer.MIN_VALUE, Integer.MAX_VALUE, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, Integer.MIN_VALUE, Integer.MAX_VALUE, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<Integer> FALL_DELAY = new Option<Integer>("Fall Delay", true, true, true, 0){
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
        @Override
        public String getDesc(boolean ingame){
            return "An extra delay between breaking a block and spawning a falling block. (Only affects FALL behaviors)\nThis is intended as a workaround for collision issues between fall behaviors and the cutting animation";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Integer value){
            return new ItemBuilder(Material.CLOCK).setCount(value).enchant(Enchantment.VANISHING_CURSE).addFlag(ItemFlag.HIDE_ENCHANTS);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    //effects and settings
    public static OptionBoolean RESPECT_UNBREAKING = new OptionBoolean("Respect Unbreaking", true, true, true, true){
        @Override
        public String getDesc(boolean ingame){
            return "If a tool has unbreaking, should it take less damage from felling trees?";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Boolean value){
            ItemBuilder builder = new ItemBuilder(Material.IRON_AXE);
            if(Objects.equals(value, true))builder.enchant(Enchantment.DURABILITY);
            return builder;
        }
    };
    public static OptionBoolean RESPECT_UNBREAKABLE = new OptionBoolean("Respect Unbreakable", true, true, true, true){
        @Override
        public String getDesc(boolean ingame){
            return "If a tool has the (vanilla) Unbreakable tag, should it take no damage from felling trees?";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Boolean value){
            ItemBuilder builder = new ItemBuilder(Material.NETHERITE_AXE);
            if(Objects.equals(value, true))builder.setUnbreakable(true);
            return builder;
        }
    };
    public static Option<Double> DAMAGE_MULT = new Option<Double>("Damage Mult", true, true, true, 1d){
        @Override
        public Double load(Object o){
            return loadDouble(o);
        }
        @Override
        public String getDesc(boolean ingame){
            return "How much damage should tools take per log? (Multiplier)";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Double value){
            return new ItemBuilder(Material.WOODEN_AXE);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, 1, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, 1, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, 1, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static OptionBoolean STACKED_TOOLS = new OptionBoolean("Stacked Tools", true, true, false, false){
        @Override
        public String getDesc(boolean ingame){
            return "If set to true, stacked tools will be consumed one at a time.\nThis will treat the entire stack as one tool, so prevent-breakage will not keep individual tools from breaking, only the whole stack.\nWARNING: Stacked tools are not recommended!";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Boolean value){
            return new ItemBuilder(Material.DIAMOND_AXE).setCount(Objects.equals(value, true)?64:16);
        }
    };
    public static OptionBoolean LEAF_FORTUNE = new OptionBoolean("Leaf Fortune", true, true, true, true){
        @Override
        public String getDesc(boolean ingame){
            return "Should Fortune on an axe be applied to leaves?";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Boolean value){
            return new ItemBuilder(Material.OAK_LEAVES).enchant(Enchantment.LOOT_BONUS_BLOCKS).addFlag(ItemFlag.HIDE_ENCHANTS);
        }
    };
    public static OptionBoolean LEAF_SILK_TOUCH = new OptionBoolean("Leaf Silk Touch", true, true, true, false){
        @Override
        public String getDesc(boolean ingame){
            return "Should Silk Touch on an axe be applied to leaves?";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Boolean value){
            return new ItemBuilder(Material.OAK_LEAVES).enchant(Enchantment.SILK_TOUCH).addFlag(ItemFlag.HIDE_ENCHANTS);
        }
    };
    public static OptionBoolean LOG_FORTUNE = new OptionBoolean("Log Fortune", true, true, true, true){
        @Override
        public String getDesc(boolean ingame){
            return "Should Fortune on an axe be applied to logs?";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Boolean value){
            return new ItemBuilder(Material.OAK_LOG).enchant(Enchantment.LOOT_BONUS_BLOCKS).addFlag(ItemFlag.HIDE_ENCHANTS);
        }
    };
    public static OptionBoolean LOG_SILK_TOUCH = new OptionBoolean("Log Silk Touch", true, true, true, true){
        @Override
        public String getDesc(boolean ingame){
            return "Should Silk Touch on an axe be applied to leaves?";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Boolean value){
            return new ItemBuilder(Material.OAK_LOG).enchant(Enchantment.SILK_TOUCH).addFlag(ItemFlag.HIDE_ENCHANTS);
        }
    };
    public static OptionBoolean LEAVE_STUMP = new OptionBoolean("Leave Stump", true, true, true, false){
        @Override
        public String getDesc(boolean ingame){
            return "When a tree is felled, should a stump be left? (The stump consists of any log blocks below the point at which the tree was felled)\n" +
                "This may cause issues with custom trees that have multiple trunks or branches that extend very low";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Boolean value){
            return new ItemBuilder(Material.OAK_LOG);
        }
    };
    public static OptionBoolean ROTATE_LOGS = new OptionBoolean("Rotate Logs", true, true, true, true){
        @Override
        public String getDesc(boolean ingame){
            return "When trees fall, should the logs rotate as they fall? (This makes it look more realistic, with logs landing horizontally)\n" +
                "(Only used when log or leaf behavior is set to FALL or similar)";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Boolean value){
            return new ItemBuilder(Material.OAK_LOG);
        }
    };
    public static Option<HashMap<Material, Material>> DROP_CONVERSIONS = new Option<HashMap<Material, Material>>("Drop Conversions", true, true, true, defaultDropConversions){
        @Override
        public String writeToConfig(HashMap<Material, Material> value){
            String s = "";
            if(value==null)return s;
            ArrayList<Material> keys = new ArrayList<>(value.keySet());
            Collections.sort(keys);
            for(Material m : keys){
                s+="\n    "+m.toString()+": "+value.get(m).toString();
            }
            return s;
        }
        @Override
        public String getDefaultConfigValue(){
            return writeToConfig(defaultValue);
        }
        @Override
        public String getDesc(boolean ingame){
            return "A list of drops to convert into other drops when felling."+(ingame?"":" add entries like this:\n"
                    + "    oak_wood: oak_log\n"
                    + "    oak_fence: stick");
        }
        @Override
        public HashMap<Material, Material> get(Tool tool, Tree tree){
            HashMap<Material, Material> conversions = new HashMap<>();
            if(toolValues.get(tool)==null&&treeValues.get(tree)==null&&globalValue!=null)conversions.putAll(globalValue);
            if(toolValues.containsKey(tool))conversions.putAll(toolValues.get(tool));
            if(treeValues.containsKey(tree))conversions.putAll(treeValues.get(tree));
            return conversions;
        }
        @Override
        public HashMap<Material, Material> load(Object o){
            if(o instanceof MemorySection){
                MemorySection m = (MemorySection)o;
                HashMap<Material, Material> conversions = new HashMap<>();
                for(String key : m.getKeys(false)){
                    conversions.put(loadMaterial(key), loadMaterial(m.get(key)));
                }
                return conversions;
            }
            if(o instanceof Map){
                Map m = (Map)o;
                HashMap<Material, Material> conversions = new HashMap<>();
                for(Object okey : m.keySet()){
                    if(okey instanceof String){
                        String key = (String)okey;
                        conversions.put(loadMaterial(key), loadMaterial(m.get(key)));
                    }
                }
                return conversions;
            }
            if(o instanceof List){
                List l = (List)o;
                HashMap<Material, Material> conversions = new HashMap<>();
                for(Object ob : l){
                    if(ob instanceof MemorySection){
                        MemorySection m = (MemorySection)ob;
                        for(String key : m.getKeys(false)){
                            conversions.put(loadMaterial(key), loadMaterial(m.get(key)));
                        }
                    }
                }
                return conversions;
            }
            return null;
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(HashMap<Material, Material> value){
            return new ItemBuilder(Material.OAK_WOOD);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyMaterialMaterialMap(parent, parent.plugin, parent.player, name, "item", (t)->{
                return t.isItem();
            }, "item", (t)->{
                return t.isItem();
            }, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyMaterialMaterialMap(parent, parent.plugin, parent.player, name, "item", (t)->{
                return t.isItem();
            }, "item", (t)->{
                return t.isItem();
            }, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyMaterialMaterialMap(parent, parent.plugin, parent.player, name, "item", (t)->{
                return t.isItem();
            }, "item", (t)->{
                return t.isItem();
            }, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<HashMap<Material, Material>> BLOCK_CONVERSIONS = new Option<HashMap<Material, Material>>("Block Conversions", true, true, true, defaultBlockConversions){
        @Override
        public String writeToConfig(HashMap<Material, Material> value){
            String s = "";
            if(value==null)return s;
            ArrayList<Material> keys = new ArrayList<>(value.keySet());
            Collections.sort(keys);
            for(Material m : keys){
                s+="\n    "+m.toString()+": "+value.get(m).toString();
            }
            return s;
        }
        @Override
        public String getDefaultConfigValue(){
            return writeToConfig(defaultValue);
        }
        @Override
        public String getDesc(boolean ingame){
            return "Blocks in this list will never be broken; they will be converted instead.";
        }
        @Override
        public HashMap<Material, Material> get(Tool tool, Tree tree){
            HashMap<Material, Material> conversions = new HashMap<>();
            if(toolValues.get(tool)==null&&treeValues.get(tree)==null&&globalValue!=null)conversions.putAll(globalValue);
            if(toolValues.containsKey(tool))conversions.putAll(toolValues.get(tool));
            if(treeValues.containsKey(tree))conversions.putAll(treeValues.get(tree));
            return conversions;
        }
        @Override
        public HashMap<Material, Material> load(Object o){
            if(o instanceof MemorySection){
                MemorySection m = (MemorySection)o;
                HashMap<Material, Material> conversions = new HashMap<>();
                for(String key : m.getKeys(false)){
                    conversions.put(loadMaterial(key), loadMaterial(m.get(key)));
                }
                return conversions;
            }
            if(o instanceof Map){
                Map m = (Map)o;
                HashMap<Material, Material> conversions = new HashMap<>();
                for(Object okey : m.keySet()){
                    if(okey instanceof String){
                        String key = (String)okey;
                        conversions.put(loadMaterial(key), loadMaterial(m.get(key)));
                    }
                }
                return conversions;
            }
            if(o instanceof List){
                List l = (List)o;
                HashMap<Material, Material> conversions = new HashMap<>();
                for(Object ob : l){
                    if(ob instanceof MemorySection){
                        MemorySection m = (MemorySection)ob;
                        for(String key : m.getKeys(false)){
                            conversions.put(loadMaterial(key), loadMaterial(m.get(key)));
                        }
                    }
                }
                return conversions;
            }
            return null;
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(HashMap<Material, Material> value){
            return new ItemBuilder(Material.OAK_WOOD);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyMaterialMaterialMap(parent, parent.plugin, parent.player, name, "item", (t)->{
                return t.isItem();
            }, "item", (t)->{
                return t.isItem();
            }, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyMaterialMaterialMap(parent, parent.plugin, parent.player, name, "item", (t)->{
                return t.isItem();
            }, "item", (t)->{
                return t.isItem();
            }, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyMaterialMaterialMap(parent, parent.plugin, parent.player, name, "item", (t)->{
                return t.isItem();
            }, "item", (t)->{
                return t.isItem();
            }, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<Double> LEAF_DROP_CHANCE = new Option<Double>("Leaf Drop Chance", true, true, true, 1d){
        @Override
        public Double load(Object o){
            return loadDouble(o);
        }
        @Override
        public Double get(Tool tool, Tree tree){
            double glob = 1;
            if(toolValues.get(tool)==null||treeValues.get(tree)==null)glob = globalValue;
            Double tl = toolValues.getOrDefault(tool, 1d);
            Double tr = treeValues.getOrDefault(tree, 1d);
            return tl*tr*glob;
        }
        @Override
        public String getDesc(boolean ingame){
            return "How often should leaves drop items? Set this to 0.0 to stop leaves from dropping items altogether (Only works with BREAK behavior)";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Double value){
            return new ItemBuilder(Material.OAK_LEAVES);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, 1, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, 1, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, 1, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<Double> LOG_DROP_CHANCE = new Option<Double>("Log Drop Chance", true, true, true, 1d){
        @Override
        public Double load(Object o){
            return loadDouble(o);
        }
        @Override
        public Double get(Tool tool, Tree tree){
            double glob = 1;
            if(toolValues.get(tool)==null||treeValues.get(tree)==null)glob = globalValue;
            Double tl = toolValues.getOrDefault(tool, 1d);
            Double tr = treeValues.getOrDefault(tree, 1d);
            return tl*tr*glob;
        }
        @Override
        public String getDesc(boolean ingame){
            return "How often should logs drop items? Set this to 0.0 to stop logs from dropping items altogether (Only works with BREAK behavior)";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Double value){
            return new ItemBuilder(Material.OAK_LOG);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, 1, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, 1, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, 1, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<ArrayList<Effect>> EFFECTS = new Option<ArrayList<Effect>>("Effects", true, true, true, null, "\n    - ALL"){
        @Override
        public ArrayList<Effect> load(Object o){
            if(o instanceof Iterable){
                ArrayList<Effect> effects = new ArrayList<>();
                for(Object ob : (Iterable)o){
                    ArrayList<Effect> newEffects = load(ob);
                    if(newEffects!=null)effects.addAll(newEffects);
                }
                return effects;
            }
            if(o instanceof Effect){
                ArrayList<Effect> effects = new ArrayList<>();
                effects.add((Effect)o);
                return effects;
            }
            if(o instanceof String){
                if(o.equals("ALL")){
                    return new ArrayList<>(TreeFeller.effects);
                }
                ArrayList<Effect> effects = new ArrayList<>();
                for(Effect e : TreeFeller.effects){
                    if(e.name.equalsIgnoreCase((String)o))effects.add(e);
                }
                return effects;
            }
            return null;
        }
        @Override
        public ArrayList<Effect> get(Tool tool, Tree tree){
            ArrayList<Effect> effects = new ArrayList<>();
            if(toolValues.get(tool)==null&&treeValues.get(tree)==null&&globalValue!=null)effects.addAll(globalValue);
            if(toolValues.containsKey(tool))effects.addAll(toolValues.get(tool));
            if(treeValues.containsKey(tree))effects.addAll(treeValues.get(tree));
            return effects;
        }
        @Override
        public String getGlobalName(){
            return "global-effects";
        }
        @Override
        public String getDesc(boolean ingame){
            return "Global effects are applied every time a tree is felled, regardless of tree type or tool\n" +
                "use ALL for all effects"+(ingame?"":("\n" +
                "ex:\n" +
                "  - ghost sound\n" +
                "  - smoke"));
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(ArrayList<Effect> value){
            return new ItemBuilder(Material.POTION).addFlag(ItemFlag.HIDE_POTION_EFFECTS);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyEffectList(parent, parent.plugin, parent.player, name, true, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyEffectList(parent, parent.plugin, parent.player, name, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyEffectList(parent, parent.plugin, parent.player, name, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<Double> CONSUMED_FOOD_BASE = new Option<Double>("Consumed Food Base", true, true, true, 0d){
        @Override
        public Double load(Object o){
            return loadDouble(o);
        }
        @Override
        public String getDesc(boolean ingame){
            return "How much food should be be consumed upon felling a tree? (This is in addition to per-block settings)";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Double value){
            return new ItemBuilder(Material.COOKED_CHICKEN);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<Double> CONSUMED_FOOD_LOGS = new Option<Double>("Consumed Food Logs", true, true, true, 0d){
        @Override
        public Double load(Object o){
            return loadDouble(o);
        }
        @Override
        public String getDesc(boolean ingame){
            return "How much food should be be consumed per block of log upon felling a tree? (This is in addition to the base setting)";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Double value){
            return new ItemBuilder(Material.ACACIA_LOG);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<Double> CONSUMED_FOOD_LEAVES = new Option<Double>("Consumed Food Leaves", true, true, true, 0d){
        @Override
        public Double load(Object o){
            return loadDouble(o);
        }
        @Override
        public String getDesc(boolean ingame){
            return "How much food should be be consumed per block of leaves upon felling a tree? (This is in addition to the base setting)";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Double value){
            return new ItemBuilder(Material.ACACIA_LEAVES);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<Double> CONSUMED_HEALTH_BASE = new Option<Double>("Consumed Health Base", true, true, true, 0d){
        @Override
        public Double load(Object o){
            return loadDouble(o);
        }
        @Override
        public String getDesc(boolean ingame){
            return "How much health should be be consumed upon felling a tree? (This is in addition to per-block settings)";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Double value){
            return new ItemBuilder(Material.SALMON);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<Double> CONSUMED_HEALTH_LOGS = new Option<Double>("Consumed Health Logs", true, true, true, 0d){
        @Override
        public Double load(Object o){
            return loadDouble(o);
        }
        @Override
        public String getDesc(boolean ingame){
            return "How much health should be be consumed per block of log upon felling a tree? (This is in addition to the base setting)";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Double value){
            return new ItemBuilder(Material.CRIMSON_STEM);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<Double> CONSUMED_HEALTH_LEAVES = new Option<Double>("Consumed Health Leaves", true, true, true, 0d){
        @Override
        public Double load(Object o){
            return loadDouble(o);
        }
        @Override
        public String getDesc(boolean ingame){
            return "How much health should be be consumed per block of leaves upon felling a tree? (This is in addition to the base setting)";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Double value){
            return new ItemBuilder(Material.NETHER_WART_BLOCK);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    //requirements
    public static Option<HashMap<Enchantment, Integer>> REQUIRED_ENCHANTMENTS = new Option<HashMap<Enchantment, Integer>>("Required Enchantments", true, true, true, null){
        @Override
        public HashMap<Enchantment, Integer> load(Object o){
            if(o instanceof MemorySection){
                HashMap<Enchantment, Integer> enchantments = new HashMap<>();
                MemorySection m = (MemorySection)o;
                for(String key : m.getKeys(false)){
                    Enchantment e = getEnchantment(key);
                    if(e==null)continue;
                    Integer level = loadInt(m.get(key));
                    if(level==null)continue;
                    if(enchantments.containsKey(e)){
                        enchantments.put(e, Math.max(enchantments.get(e), level));
                    }else{
                        enchantments.put(e, level);
                    }
                }
                return enchantments;
            }
            if(o instanceof Map){
                HashMap<Enchantment, Integer> enchantments = new HashMap<>();
                Map m = (Map)o;
                for(Object obj : m.keySet()){
                    Enchantment e = null;
                    if(obj instanceof Enchantment){
                        e = (Enchantment)obj;
                    }
                    if(obj instanceof String){
                        e = getEnchantment((String)obj);
                    }
                    if(e==null)continue;
                    Integer level = loadInt(m.get(obj));
                    if(level==null)continue;
                    if(enchantments.containsKey(e)){
                        enchantments.put(e, Math.max(enchantments.get(e), level));
                    }else{
                        enchantments.put(e, level);
                    }
                }
                return enchantments;
            }
            if(o instanceof List){
                List l = (List)o;
                HashMap<Enchantment, Integer> enchantments = new HashMap<>();
                for(Object lbj : l){
                    if(lbj instanceof Map){
                        Map m = (Map)lbj;
                        for(Object obj : m.keySet()){
                            Enchantment e = null;
                            if(obj instanceof Enchantment){
                                e = (Enchantment)obj;
                            }
                            if(obj instanceof String){
                                e = getEnchantment((String)obj);
                            }
                            if(e==null)continue;
                            Integer level = loadInt(m.get(obj));
                            if(level==null)continue;
                            if(enchantments.containsKey(e)){
                                enchantments.put(e, Math.max(enchantments.get(e), level));
                            }else{
                                enchantments.put(e, level);
                            }
                        }
                    }
                }
                return enchantments;
            }
            return null;
        }
        @Override
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe){
            if(toolValues.get(tool)==null&&treeValues.get(tree)==null&&globalValue!=null){
                for(Enchantment e : globalValue.keySet()){
                    if(axe.getEnchantmentLevel(e)<globalValue.get(e)){
                        return new DebugResult(this, GLOBAL, e.toString(), axe.getEnchantmentLevel(e), globalValue.get(e));
                    }
                }
            }
            HashMap<Enchantment, Integer> values = toolValues.get(tool);
            if(values!=null){
                for(Enchantment e : values.keySet()){
                    if(axe.getEnchantmentLevel(e)<values.get(e)){
                        return new DebugResult(this, TOOL, e.toString(), axe.getEnchantmentLevel(e), values.get(e));
                    }
                }
            }
            values = treeValues.get(tree);
            if(values!=null){
                for(Enchantment e : values.keySet()){
                    if(axe.getEnchantmentLevel(e)<values.get(e)){
                        return new DebugResult(this, TREE, e.toString(), axe.getEnchantmentLevel(e), values.get(e));
                    }
                }
            }
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(boolean ingame){
            return "Tools must have these enchantments at this level or higher to fell trees"+(ingame?"":("\n"
                + "ex:\n"
                + "- unbreaking: 2\n"
                + "- efficiency: 5"));
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Enchantment missing$: {0} ({1}<{2})", "All required enchantments met");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(HashMap<Enchantment, Integer> value){
            ItemBuilder builder = new ItemBuilder(Material.ENCHANTED_BOOK);
            if(value!=null){
                for(Enchantment enchantment : value.keySet()){
                    builder.enchant(enchantment, value.get(enchantment));
                }
            }
            return builder;
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyEnchantmentMap(parent, parent.plugin, parent.player, name, true, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyEnchantmentMap(parent, parent.plugin, parent.player, name, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyEnchantmentMap(parent, parent.plugin, parent.player, name, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
        @Override
        public String writeToConfig(HashMap<Enchantment, Integer> value){
            if(value==null)return "";
            String s = "{";
            String str = "";
            for(Enchantment e : value.keySet()){
                str+=", "+e.getKey().getKey()+": "+value.get(e);
            }
            if(!str.isEmpty())s+=str.substring(2);
            return s+"}";
        }
    };
    public static Option<HashMap<Enchantment, Integer>> BANNED_ENCHANTMENTS = new Option<HashMap<Enchantment, Integer>>("Banned Enchantments", true, true, true, null){
        @Override
        public HashMap<Enchantment, Integer> load(Object o){
            if(o instanceof MemorySection){
                HashMap<Enchantment, Integer> enchantments = new HashMap<>();
                MemorySection m = (MemorySection)o;
                for(String key : m.getKeys(false)){
                    Enchantment e = getEnchantment(key);
                    if(e==null)continue;
                    Integer level = loadInt(m.get(key));
                    if(level==null)continue;
                    if(enchantments.containsKey(e)){
                        enchantments.put(e, Math.min(enchantments.get(e), level));
                    }else{
                        enchantments.put(e, level);
                    }
                }
                return enchantments;
            }
            if(o instanceof Map){
                HashMap<Enchantment, Integer> enchantments = new HashMap<>();
                Map m = (Map)o;
                for(Object obj : m.keySet()){
                    Enchantment e = null;
                    if(obj instanceof Enchantment){
                        e = (Enchantment)obj;
                    }
                    if(obj instanceof String){
                        e = getEnchantment((String)obj);
                    }
                    if(e==null)continue;
                    Integer level = loadInt(m.get(obj));
                    if(level==null)continue;
                    if(enchantments.containsKey(e)){
                        enchantments.put(e, Math.min(enchantments.get(e), level));
                    }else{
                        enchantments.put(e, level);
                    }
                }
                return enchantments;
            }
            if(o instanceof List){
                List l = (List)o;
                HashMap<Enchantment, Integer> enchantments = new HashMap<>();
                for(Object lbj : l){
                    if(lbj instanceof Map){
                        Map m = (Map)lbj;
                        for(Object obj : m.keySet()){
                            Enchantment e = null;
                            if(obj instanceof Enchantment){
                                e = (Enchantment)obj;
                            }
                            if(obj instanceof String){
                                e = getEnchantment((String)obj);
                            }
                            if(e==null)continue;
                            Integer level = loadInt(m.get(obj));
                            if(level==null)continue;
                            if(enchantments.containsKey(e)){
                                enchantments.put(e, Math.min(enchantments.get(e), level));
                            }else{
                                enchantments.put(e, level);
                            }
                        }
                    }
                }
                return enchantments;
            }
            return null;
        }
        @Override
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe){
            if(toolValues.get(tool)==null&&treeValues.get(tree)==null&&globalValue!=null){
                for(Enchantment e : globalValue.keySet()){
                    if(axe.getEnchantmentLevel(e)>=globalValue.get(e)){
                        return new DebugResult(this, GLOBAL, e.toString(), axe.getEnchantmentLevel(e), globalValue.get(e)-1);
                    }
                }
            }
            HashMap<Enchantment, Integer> values = toolValues.get(tool);
            if(values!=null){
                for(Enchantment e : values.keySet()){
                    if(axe.getEnchantmentLevel(e)>=values.get(e)){
                        return new DebugResult(this, TOOL, e.toString(), axe.getEnchantmentLevel(e), values.get(e)-1);
                    }
                }
            }
            values = treeValues.get(tree);
            if(values!=null){
                for(Enchantment e : values.keySet()){
                    if(axe.getEnchantmentLevel(e)>=values.get(e)){
                        return new DebugResult(this, TREE, e.toString(), axe.getEnchantmentLevel(e), values.get(e)-1);
                    }
                }
            }
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(boolean ingame){
            return "Tools must not have these enchantments or have them lower than this level to fell trees"+(ingame?"":("\n"
                + "ex:\n"
                + "- silk_touch: 1\n"
                + "- unbreaking: 3"));
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Tool contains banned enchantment$: {0} ({1}>{2})", "No banned enchantments found");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(HashMap<Enchantment, Integer> value){
            ItemBuilder builder = new ItemBuilder(Material.ENCHANTED_BOOK);
            if(value!=null){
                for(Enchantment enchantment : value.keySet()){
                    builder.enchant(enchantment, value.get(enchantment));
                }
            }
            return builder;
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyEnchantmentMap(parent, parent.plugin, parent.player, name, true, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyEnchantmentMap(parent, parent.plugin, parent.player, name, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyEnchantmentMap(parent, parent.plugin, parent.player, name, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
        @Override
        public String writeToConfig(HashMap<Enchantment, Integer> value){
            if(value==null)return "";
            String s = "{";
            String str = "";
            for(Enchantment e : value.keySet()){
                str+=", "+e.getKey().getKey()+": "+value.get(e);
            }
            if(!str.isEmpty())s+=str.substring(2);
            return s+"}";
        }
    };
    public static Option<Short> MIN_DURABILITY = new Option<Short>("Min Durability", true, true, true, null){
        @Override
        public Short load(Object o){
            return loadShort(o);
        }
        @Override
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe){
            int durability = axe.getType().getMaxDurability()-axe.getDurability();
            if(axe.getType().getMaxDurability()>0){
                if(toolValues.get(tool)==null&&treeValues.get(tree)==null&&globalValue!=null){
                    if(durability<globalValue)return new DebugResult(this, GLOBAL, durability, globalValue);
                }
                if(toolValues.containsKey(tool)){
                    if(durability<toolValues.get(tool))return new DebugResult(this, TOOL, durability, toolValues.get(tool));
                }
                if(treeValues.containsKey(tree)){
                    if(durability<treeValues.get(tree))return new DebugResult(this, TREE, durability, treeValues.get(tree));
                }
            }
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(boolean ingame){
            return "Tools with less than this much durability will be unable to fell trees";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Tool durability is less than minimum allowed$: {0}<{1}", "Tool meets minimum durability requirement");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Short value){
            return new ItemBuilder(Material.DIAMOND_AXE).setDurability(value);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyShort(parent, parent.plugin, parent.player, name, (short)0, Short.MAX_VALUE, true, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyShort(parent, parent.plugin, parent.player, name, (short)0, Short.MAX_VALUE, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyShort(parent, parent.plugin, parent.player, name, (short)0, Short.MAX_VALUE, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<Short> MAX_DURABILITY = new Option<Short>("Max Durability", true, true, true, null){
        @Override
        public Short load(Object o){
            return loadShort(o);
        }
        @Override
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe){
            int durability = axe.getType().getMaxDurability()-axe.getDurability();
            if(axe.getType().getMaxDurability()>0){
                if(toolValues.get(tool)==null&&treeValues.get(tree)==null&&globalValue!=null){
                    if(durability>globalValue)return new DebugResult(this, GLOBAL, durability, globalValue);
                }
                if(toolValues.containsKey(tool)){
                    if(durability>toolValues.get(tool))return new DebugResult(this, TOOL, durability, toolValues.get(tool));
                }
                if(treeValues.containsKey(tree)){
                    if(durability>treeValues.get(tree))return new DebugResult(this, TREE, durability, treeValues.get(tree));
                }
            }
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(boolean ingame){
            return "Tools with more than this much durability will be unable to fell trees";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Tool durability is greater than maximum allowed: {0}>{1}", "Tool meets maximum durability requirement");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Short value){
            return new ItemBuilder(Material.DIAMOND_AXE).setDurability(value);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyShort(parent, parent.plugin, parent.player, name, (short)0, Short.MAX_VALUE, true, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyShort(parent, parent.plugin, parent.player, name, (short)0, Short.MAX_VALUE, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyShort(parent, parent.plugin, parent.player, name, (short)0, Short.MAX_VALUE, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<Float> MIN_DURABILITY_PERCENT = new Option<Float>("Min Durability Percent", true, true, true, null){
        @Override
        public Float load(Object o){
            return loadFloat(o);
        }
        @Override
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe){
            int durability = axe.getType().getMaxDurability()-axe.getDurability();
            float durabilityPercent = durability/(float)axe.getType().getMaxDurability();
            if(axe.getType().getMaxDurability()>0){
                if(toolValues.get(tool)==null&&treeValues.get(tree)==null&&globalValue!=null){
                    if(durabilityPercent<globalValue)return new DebugResult(this, GLOBAL, Math.round(durabilityPercent*1000)/10f, Math.round(globalValue*1000)/10f);
                }
                if(toolValues.containsKey(tool)){
                    if(durabilityPercent<toolValues.get(tool))return new DebugResult(this, TOOL, Math.round(durabilityPercent*1000)/10f, Math.round(toolValues.get(tool)*1000)/10f);
                }
                if(treeValues.containsKey(tree)){
                    if(durabilityPercent<treeValues.get(tree))return new DebugResult(this, TREE, Math.round(durabilityPercent*1000)/10f, Math.round(treeValues.get(tree)*1000)/10f);
                }
            }
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(boolean ingame){
            return "Tools with less than this percentage of durability will be unable to fell trees";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Tool durability is less than minimum allowed$: {0}%<{1}%", "Tool meets minimum durability percentage requirement");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Float value){
            return new ItemBuilder(Material.DIAMOND_AXE).setDurability((value==null?1:value)*Material.DIAMOND_AXE.getMaxDurability());
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyFloat(parent, parent.plugin, parent.player, name, 0f, 1f, true, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyFloat(parent, parent.plugin, parent.player, name, 0f, 1f, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyFloat(parent, parent.plugin, parent.player, name, 0f, 1f, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<Float> MAX_DURABILITY_PERCENT = new Option<Float>("Max Durability Percent", true, true, true, null){
        @Override
        public Float load(Object o){
            return loadFloat(o);
        }
        @Override
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe){
            int durability = axe.getType().getMaxDurability()-axe.getDurability();
            float durabilityPercent = durability/(float)axe.getType().getMaxDurability();
            if(axe.getType().getMaxDurability()>0){
                if(toolValues.get(tool)==null&&treeValues.get(tree)==null&&globalValue!=null){
                    if(durabilityPercent>globalValue)return new DebugResult(this, GLOBAL, Math.round(durabilityPercent*1000)/10f, Math.round(globalValue*1000)/10f);
                }
                if(toolValues.containsKey(tool)){
                    if(durabilityPercent>toolValues.get(tool))return new DebugResult(this, TOOL, Math.round(durabilityPercent*1000)/10f, Math.round(toolValues.get(tool)*1000)/10f);
                }
                if(treeValues.containsKey(tree)){
                    if(durabilityPercent>treeValues.get(tree))return new DebugResult(this, TREE, Math.round(durabilityPercent*1000)/10f, Math.round(treeValues.get(tree)*1000)/10f);
                }
            }
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(boolean ingame){
            return "Tools with more than this percentage of durability will be unable to fell trees";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Tool durability is greater than maximum allowed: {0}%>{1}%", "Tool meets maximum durability percentage requirement");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Float value){
            return new ItemBuilder(Material.DIAMOND_AXE).setDurability((value==null?1:value)*Material.DIAMOND_AXE.getMaxDurability());
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyFloat(parent, parent.plugin, parent.player, name, 0f, 1f, true, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyFloat(parent, parent.plugin, parent.player, name, 0f, 1f, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyFloat(parent, parent.plugin, parent.player, name, 0f, 1f, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static OptionBoolean PREVENT_BREAKAGE = new OptionBoolean("Prevent Breakage", true, true, true, false){
        @Override
        public String getDesc(boolean ingame){
            return "If set to true, tools will not be able to fell a tree if doing so would break the tool.";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Boolean value){
            return new ItemBuilder(Material.WOODEN_AXE);
        }
    };
    public static Option<ArrayList<String>> REQUIRED_LORE = new Option<ArrayList<String>>("Required Lore", true, true, true, null){
        @Override
        public ArrayList<String> load(Object o){
            if(o instanceof Iterable){
                ArrayList<String> lores = new ArrayList<>();
                for(Object ob : (Iterable)o){
                    ArrayList<String> newLores = load(ob);
                    if(newLores!=null)lores.addAll(newLores);
                }
                return lores;
            }
            if(o instanceof String){
                ArrayList<String> lores = new ArrayList<>();
                lores.add((String)o);
                return lores;
            }
            return null;
        }
        @Override
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe) {
            ArrayList<String> lore = new ArrayList<>();
            if(axe.hasItemMeta()){
                ItemMeta meta = axe.getItemMeta();
                if(meta.hasLore()){
                    lore = new ArrayList<>(meta.getLore());
                }
            }
            if(toolValues.get(tool)==null&&treeValues.get(tree)==null&&globalValue!=null){
                for(String s : globalValue){
                    boolean has = false;
                    for(String lor : lore){
                        if(lor.contains(s))has = true;
                    }
                    if(!has)return new DebugResult(this, GLOBAL, s);
                }
            }
            if(toolValues.containsKey(tool)){
                for(String s : toolValues.get(tool)){
                    boolean has = false;
                    for(String lor : lore){
                        if(lor.contains(s))has = true;
                    }
                    if(!has)return new DebugResult(this, TOOL, s);
                }
            }
            if(treeValues.containsKey(tree)){
                for(String s : treeValues.get(tree)){
                    boolean has = false;
                    for(String lor : lore){
                        if(lor.contains(s))has = true;
                    }
                    if(!has)return new DebugResult(this, TREE, s);
                }
            }
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(boolean ingame){
            return "Tools must have all literal strings in this list in order to fell trees"+(ingame?"":("\n"
                + "ex:\n"
                + "- Can fell trees"));
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Tool is missing required lore$: {0}", "Tool has all required lore");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(ArrayList<String> value){
            return new ItemBuilder(Material.BOOK);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyStringList(parent, parent.plugin, parent.player, name, true, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyStringList(parent, parent.plugin, parent.player, name, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyStringList(parent, parent.plugin, parent.player, name, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };//TODO make this a HashSet
    public static Option<String> REQUIRED_NAME = new Option<String>("Required Name", true, true, true, null){
        @Override
        public String load(Object o){
            return loadString(o);
        }
        @Override
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe) {
            String name = null;
            if(axe.hasItemMeta()){
                ItemMeta meta = axe.getItemMeta();
                name = meta.getDisplayName();
            }
            if(toolValues.get(tool)==null&&treeValues.get(tree)==null&&globalValue!=null){
                if(!globalValue.equals(name))return new DebugResult(this, GLOBAL, name, globalValue);
            }
            if(toolValues.containsKey(tool)){
                if(!toolValues.get(tool).equals(name))return new DebugResult(this, TOOL, name, toolValues.get(tool));
            }
            if(treeValues.containsKey(tree)){
                if(!treeValues.get(tree).equals(name))return new DebugResult(this, TREE, name, treeValues.get(tree));
            }
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(boolean ingame){
            return "A tool's name must match exactly in order to fell trees (colors can be designated with §)";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Tool name {0} "+ChatColor.RESET+"does not match required name$: {1}", "Tool name matches");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(String value){
            return new ItemBuilder(Material.PAPER);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.openAnvilGUI(globalValue==null?"":globalValue, "Edit Required Name", (p, str) -> {
                globalValue = str==null||str.isEmpty()||str.equalsIgnoreCase("null")?null:str;
            });
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.openAnvilGUI(toolValues.containsKey(tool)?toolValues.get(tool):"", "Edit Required Name", (p, str) -> {
                String value = str==null||str.isEmpty()||str.equalsIgnoreCase("null")?null:str;
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            });
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.openAnvilGUI(treeValues.containsKey(tree)?treeValues.get(tree):"", "Edit Required Name", (p, str) -> {
                String value = str==null||str.isEmpty()||str.equalsIgnoreCase("null")?null:str;
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            });
        }
    };
    public static Option<HashSet<String>> REQUIRED_PERMISSIONS = new Option<HashSet<String>>("Required Permissions", true, true, true, new HashSet<>(), null){
        @Override
        public HashSet<String> load(Object o){
            if(o instanceof Iterable){
                HashSet<String> permissions = new HashSet<>();
                for(Object ob : (Iterable)o){
                    HashSet<String> newPerms = load(ob);
                    if(newPerms!=null)permissions.addAll(newPerms);
                }
                return permissions;
            }
            if(o instanceof String){
                HashSet<String> permissions = new HashSet<>();
                permissions.add((String)o);
                return permissions;
            }
            return null;
        }
        @Override
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe) {
            if(toolValues.get(tool)==null&&treeValues.get(tree)==null&&globalValue!=null){
                for(String s : globalValue){
                    if(!player.hasPermission(s)){
                        return new DebugResult(this, GLOBAL, s);
                    }
                }
            }
            if(toolValues.containsKey(tool)){
                for(String s : toolValues.get(tool)){
                    if(!player.hasPermission(s)){
                        return new DebugResult(this, TOOL, s);
                    }
                }
            }
            if(treeValues.containsKey(tree)){
                for(String s : treeValues.get(tree)){
                    if(!player.hasPermission(s)){
                        return new DebugResult(this, TREE, s);
                    }
                }
            }
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(boolean ingame){
            return "Trees can only be cut down by players who have all permissons listed here"+(ingame?"":("\n"
                + "ex:\n"
                + "- treefeller.example"));
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Player is missing required permission$: {0}", "Player has all required permissions");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(HashSet<String> value){
            return new ItemBuilder(Material.PAPER);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyStringSet(parent, parent.plugin, parent.player, name, true, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyStringSet(parent, parent.plugin, parent.player, name, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyStringSet(parent, parent.plugin, parent.player, name, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<Integer> MIN_TIME = new Option<Integer>("Min Time", true, true, true, null){
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
        @Override
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe){
            long dayTime = block.getWorld().getTime();
            if(axe.getType().getMaxDurability()>0){
                if(toolValues.get(tool)==null&&treeValues.get(tree)==null&&globalValue!=null){
                    if(dayTime<globalValue)return new DebugResult(this, GLOBAL, dayTime, globalValue);
                }
                if(toolValues.containsKey(tool)){
                    if(dayTime<toolValues.get(tool))return new DebugResult(this, TOOL, dayTime, toolValues.get(tool));
                }
                if(treeValues.containsKey(tree)){
                    if(dayTime<treeValues.get(tree))return new DebugResult(this, TREE, dayTime, treeValues.get(tree));
                }
            }
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(boolean ingame){
            return "What should the minimum time be for felling trees? (0-24000)";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Time is less than minimum allowed$: {0}<{1}", "Time meets minimum requirement");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Integer value){
            return new ItemBuilder(Material.CLOCK);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, 24000, true, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, 24000, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, 24000, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<Integer> MAX_TIME = new Option<Integer>("Max Time", true, true, true, null){
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
        @Override
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe){
            long dayTime = block.getWorld().getTime();
            if(axe.getType().getMaxDurability()>0){
                if(toolValues.get(tool)==null&&treeValues.get(tree)==null&&globalValue!=null){
                    if(dayTime>globalValue)return new DebugResult(this, GLOBAL, dayTime, globalValue);
                }
                if(toolValues.containsKey(tool)){
                    if(dayTime>toolValues.get(tool))return new DebugResult(this, TOOL, dayTime, toolValues.get(tool));
                }
                if(treeValues.containsKey(tree)){
                    if(dayTime>treeValues.get(tree))return new DebugResult(this, TREE, dayTime, treeValues.get(tree));
                }
            }
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(boolean ingame){
            return "What should the maximum time be for felling trees? (0-24000)";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Time is greater than maximum allowed$: {0}>{1}", "Time meets maximum requirement");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Integer value){
            return new ItemBuilder(Material.CLOCK);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, 24000, true, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, 24000, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, 24000, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<Integer> MIN_PHASE = new Option<Integer>("Min Phase", true, true, true, null){
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
        @Override
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe){
            long gameTime = block.getWorld().getFullTime();
            long day = gameTime/24000;
            long phase = day%8;
            if(axe.getType().getMaxDurability()>0){
                if(toolValues.get(tool)==null&&treeValues.get(tree)==null&&globalValue!=null){
                    if(phase<globalValue)return new DebugResult(this, GLOBAL, phase, globalValue);
                }
                if(toolValues.containsKey(tool)){
                    if(phase<toolValues.get(tool))return new DebugResult(this, TOOL, phase, toolValues.get(tool));
                }
                if(treeValues.containsKey(tree)){
                    if(phase<treeValues.get(tree))return new DebugResult(this, TREE, phase, treeValues.get(tree));
                }
            }
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(boolean ingame){
            return "What should the minimum phase be for felling trees? (0-7)\n" +
                "Phases:\n" +
                "0 = full moon\n" +
                "1 = waning gibbous\n" +
                "2 = first quarter\n" +
                "3 = waning crescent\n" +
                "4 = new moon\n" +
                "5 = waxing crescent\n" +
                "6 = third quarter\n" +
                "7 = waxing gibbous\n";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Phase is less than minimum allowed$: {0}<{1}", "Phase meets minimum requirement");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Integer value){
            return new ItemBuilder(Material.CLOCK);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, 7, true, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, 7, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, 7, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<Integer> MAX_PHASE = new Option<Integer>("Max Phase", true, true, true, null){
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
        @Override
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe){
            long gameTime = block.getWorld().getFullTime();
            long day = gameTime/24000;
            long phase = day%8;
            if(axe.getType().getMaxDurability()>0){
                if(toolValues.get(tool)==null&&treeValues.get(tree)==null&&globalValue!=null){
                    if(phase>globalValue)return new DebugResult(this, GLOBAL, phase, globalValue);
                }
                if(toolValues.containsKey(tool)){
                    if(phase>toolValues.get(tool))return new DebugResult(this, TOOL, phase, toolValues.get(tool));
                }
                if(treeValues.containsKey(tree)){
                    if(phase>treeValues.get(tree))return new DebugResult(this, TREE, phase, treeValues.get(tree));
                }
            }
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(boolean ingame){
            return "What should the maximum phase be for felling trees? (0-7)\n" +
                "Phases:\n" +
                "0 = full moon\n" +
                "1 = waning gibbous\n" +
                "2 = first quarter\n" +
                "3 = waning crescent\n" +
                "4 = new moon\n" +
                "5 = waxing crescent\n" +
                "6 = third quarter\n" +
                "7 = waxing gibbous\n";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Phase is greater than maximum allowed$: {0}>{1}", "Phase meets maximum requirement");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Integer value){
            return new ItemBuilder(Material.CLOCK);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, 7, true, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, 7, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, 7, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<Integer> MIN_FOOD = new Option<Integer>("Min Food", true, true, true, null){
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
        @Override
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe){
            int food = player.getFoodLevel();
            if(toolValues.get(tool)==null&&treeValues.get(tree)==null&&globalValue!=null){
                if(food<globalValue)return new DebugResult(this, GLOBAL, food, globalValue);
            }
            if(toolValues.containsKey(tool)){
                if(food<toolValues.get(tool))return new DebugResult(this, TOOL, food, toolValues.get(tool));
            }
            if(treeValues.containsKey(tree)){
                if(food<treeValues.get(tree))return new DebugResult(this, TREE, food, treeValues.get(tree));
            }
            return new DebugResult(this, SUCCESS, food);
        }
        @Override
        public String getDesc(boolean ingame){
            return "How much food should be required to fell trees? (0 - 20)";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Not enough food$: {0}<{1}", "Food meets minimum requirement");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Integer value){
            return new ItemBuilder(Material.BREAD);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, 20, true, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, 20, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, 20, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<Integer> MAX_FOOD = new Option<Integer>("Max Food", true, true, true, null){
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
        @Override
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe){
            int food = player.getFoodLevel();
            if(toolValues.get(tool)==null&&treeValues.get(tree)==null&&globalValue!=null){
                if(food>globalValue)return new DebugResult(this, GLOBAL, food, globalValue);
            }
            if(toolValues.containsKey(tool)){
                if(food>toolValues.get(tool))return new DebugResult(this, TOOL, food, toolValues.get(tool));
            }
            if(treeValues.containsKey(tree)){
                if(food>treeValues.get(tree))return new DebugResult(this, TREE, food, treeValues.get(tree));
            }
            return new DebugResult(this, SUCCESS, food);
        }
        @Override
        public String getDesc(boolean ingame){
            return "What is the maximum food level allowed in order to fell trees? (0 - 20)";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Too much food$: {0}>{1}", "Food meets maximum requirement");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Integer value){
            return new ItemBuilder(Material.COOKED_BEEF);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, 20, true, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, 20, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, 20, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<Double> MIN_HEALTH = new Option<Double>("Min Health", true, true, true, null){
        @Override
        public Double load(Object o){
            return loadDouble(o);
        }
        @Override
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe){
            double health = player.getHealth();
            if(toolValues.get(tool)==null&&treeValues.get(tree)==null&&globalValue!=null){
                if(health<globalValue)return new DebugResult(this, GLOBAL, health, globalValue);
            }
            if(toolValues.containsKey(tool)){
                if(health<toolValues.get(tool))return new DebugResult(this, TOOL, health, toolValues.get(tool));
            }
            if(treeValues.containsKey(tree)){
                if(health<treeValues.get(tree))return new DebugResult(this, TREE, health, treeValues.get(tree));
            }
            return new DebugResult(this, SUCCESS, health);
        }
        @Override
        public String getDesc(boolean ingame){
            return "How much health should be required to fell trees?";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Not enough health$: {0}<{1}", "Health meets minimum requirement");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Double value){
            return new ItemBuilder(Material.GOLDEN_CARROT);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, 1, true, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, 1, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, 1, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<Double> MAX_HEALTH = new Option<Double>("Max Health", true, true, true, null){
        @Override
        public Double load(Object o){
            return loadDouble(o);
        }
        @Override
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe){
            double health = player.getHealth();
            if(toolValues.get(tool)==null&&treeValues.get(tree)==null&&globalValue!=null){
                if(health>globalValue)return new DebugResult(this, GLOBAL, health, globalValue);
            }
            if(toolValues.containsKey(tool)){
                if(health>toolValues.get(tool))return new DebugResult(this, TOOL, health, toolValues.get(tool));
            }
            if(treeValues.containsKey(tree)){
                if(health>treeValues.get(tree))return new DebugResult(this, TREE, health, treeValues.get(tree));
            }
            return new DebugResult(this, SUCCESS, health);
        }
        @Override
        public String getDesc(boolean ingame){
            return "What is the maximum health level allowed in order to fell trees?";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Too much health$: {0}>{1}", "Health meets maximum requirement");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Double value){
            return new ItemBuilder(Material.GOLDEN_APPLE);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, 1, true, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, 1, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, 1, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<Integer> CUSTOM_MODEL_DATA = new Option<Integer>("Custom Model Data", true, true, true, null){
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
        @Override
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe){
            ItemMeta meta = axe.getItemMeta();
            int data = (meta==null||!meta.hasCustomModelData()?0:meta.getCustomModelData());
            if(toolValues.get(tool)==null&&treeValues.get(tree)==null&&globalValue!=null){
                if(data!=globalValue)return new DebugResult(this, GLOBAL, data, globalValue);
            }
            if(toolValues.containsKey(tool)){
                if(data!=toolValues.get(tool))return new DebugResult(this, TOOL, data, toolValues.get(tool));
            }
            if(treeValues.containsKey(tree)){
                if(data!=treeValues.get(tree))return new DebugResult(this, TREE, data, treeValues.get(tree));
            }
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(boolean ingame){
            return "Tool's CustomModelData must match in order to fell trees";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Custom model data does not match$: {0} != {1}", "Custom model data matches!");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Integer value){
            return new ItemBuilder(Material.CLOCK);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, Integer.MIN_VALUE, Integer.MAX_VALUE, true, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, Integer.MIN_VALUE, Integer.MAX_VALUE, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, Integer.MIN_VALUE, Integer.MAX_VALUE, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<ArrayList<Tree>> ALLOWED_TREES = new Option<ArrayList<Tree>>("Allowed Trees", false, true, false, null){
        @Override
        public ArrayList<Tree> load(Object o){
            if(o instanceof Iterable){
                ArrayList<Tree> trees = new ArrayList<>();
                for(Object ob : (Iterable)o){
                    ArrayList<Tree> newTrees = load(ob);
                    if(newTrees!=null)trees.addAll(newTrees);
                }
                return trees;
            }
            if(o instanceof Tree){
                ArrayList<Tree> trees = new ArrayList<>();
                trees.add((Tree)o);
                return trees;
            }
            if(o instanceof String){
                ArrayList<Tree> trees = new ArrayList<>();
                for(Tree tree : TreeFeller.trees){
                    for(Material m : tree.trunk){
                        if(m.toString().contains(((String)o).toUpperCase().replace(" ", "_"))){
                            trees.add(tree);
                            break;
                        }
                    }
                }
                return trees;
            }
            if(o instanceof Integer){
                ArrayList<Tree> trees = new ArrayList<>();
                int i = (int)o;
                if(i>=0&&i<TreeFeller.trees.size()){
                    trees.add(TreeFeller.trees.get(i));
                    return trees;
                }
            }
            return null;
        }
        @Override
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe){
            if(toolValues.containsKey(tool)){
                if(!toolValues.get(tool).contains(tree)){
                    return new DebugResult(this, TOOL, TreeFeller.trees.indexOf(tree));
                }
            }
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(boolean ingame){
            return "If set, the tool can only fell specific trees. The given value is a list of tree indicies, starting at 0 (the first tree defined is 0, the second is 1, etc.)";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Tree is not allowed$: {0}", "Tree is allowed for tool");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(ArrayList<Tree> value){
            return new ItemBuilder(Material.SPRUCE_SAPLING);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyTreeSet(parent, parent.plugin, parent.player, name, true, globalValue, (value) -> {
                globalValue = new ArrayList<>(value);
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyTreeSet(parent, parent.plugin, parent.player, name, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, new ArrayList<>(value));
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyTreeSet(parent, parent.plugin, parent.player, name, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, new ArrayList<>(value));
            }));
        }
        @Override
        public String writeToConfig(ArrayList<Tree> value){
            ArrayList<Integer> indicies = new ArrayList<>();
            for(Tree t : value){
                indicies.add(TreeFeller.trees.indexOf(t));
            }
            return indicies.toString();
        }
    };//TODO make this a HashSet
    public static OptionBoolean ENABLE_ADVENTURE = new OptionBoolean("Enable Adventure", true, true, true, false){
        @Override
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe){
            if(player==null||player.getGameMode()!=GameMode.ADVENTURE)return null;
            if(toolValues.get(tool)==null&&treeValues.get(tree)==null&&Objects.equals(globalValue, false))return new DebugResult(this, GLOBAL);
            if(Objects.equals(toolValues.get(tool), false))return new DebugResult(this, TOOL);
            if(Objects.equals(treeValues.get(tree), false))return new DebugResult(this, TREE);
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(boolean ingame){
            return "Should the tree feller work in adventure mode?";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("TreeFeller is disabled in adventure mode", "Tool is disabled in adventure mode", "Tree is disabled in adventure mode", "All components OK for adventure mode");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Boolean value){
            return new ItemBuilder(Material.MAP);
        }
    };
    public static OptionBoolean ENABLE_SURVIVAL = new OptionBoolean("Enable Survival", true, true, true, true){
        @Override
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe){
            if(player==null||player.getGameMode()!=GameMode.SURVIVAL)return null;
            if(toolValues.get(tool)==null&&treeValues.get(tree)==null&&Objects.equals(globalValue, false))return new DebugResult(this, GLOBAL);
            if(Objects.equals(toolValues.get(tool), false))return new DebugResult(this, TOOL);
            if(Objects.equals(treeValues.get(tree), false))return new DebugResult(this, TREE);
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(boolean ingame){
            return "Should the tree feller work in survival mode?";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("TreeFeller is disabled in survival mode", "Tool is disabled in survival mode", "Tree is disabled in survival mode", "All components OK for survival mode");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Boolean value){
            return new ItemBuilder(Material.IRON_SWORD);
        }
    };
    public static OptionBoolean ENABLE_CREATIVE = new OptionBoolean("Enable Creative", true, true, true, false){
        @Override
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe){
            if(player==null||player.getGameMode()!=GameMode.CREATIVE)return null;
            if(toolValues.get(tool)==null&&treeValues.get(tree)==null&&Objects.equals(globalValue, false))return new DebugResult(this, GLOBAL);
            if(Objects.equals(toolValues.get(tool), false))return new DebugResult(this, TOOL);
            if(Objects.equals(treeValues.get(tree), false))return new DebugResult(this, TREE);
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(boolean ingame){
            return "Should the tree feller work in creative mode?";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("TreeFeller is disabled in creative mode", "Tool is disabled in creative mode", "Tree is disabled in creative mode", "All components OK for creative mode");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Boolean value){
            return new ItemBuilder(Material.GRASS_BLOCK);
        }
    };
    public static OptionBoolean WITH_SNEAK = new OptionBoolean("With Sneak", true, true, true, false){
        @Override
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe){
            if(player==null||!player.isSneaking())return null;
            if(toolValues.get(tool)==null&&treeValues.get(tree)==null&&Objects.equals(globalValue, false))return new DebugResult(this, GLOBAL);
            if(Objects.equals(toolValues.get(tool), false))return new DebugResult(this, TOOL);
            if(Objects.equals(treeValues.get(tree), false))return new DebugResult(this, TREE);
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(boolean ingame){
            return "Should the tree feller work when sneaking?";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("TreeFeller is disabled when sneaking", "Tool is disabled when sneaking", "Tree is disabled when sneaking", "Felling allowed when sneaking");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Boolean value){
            return new ItemBuilder(Material.LEATHER_BOOTS);
        }
    };
    public static OptionBoolean WITHOUT_SNEAK = new OptionBoolean("Without Sneak", true, true, true, true){
        @Override
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe){
            if(player==null||player.isSneaking())return null;
            if(toolValues.get(tool)==null&&treeValues.get(tree)==null&&Objects.equals(globalValue, false))return new DebugResult(this, GLOBAL);
            if(Objects.equals(treeValues.get(tree), false))return new DebugResult(this, TREE);
            if(Objects.equals(toolValues.get(tool), false))return new DebugResult(this, TOOL);
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(boolean ingame){
            return "Should the tree feller work when not sneaking?";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("TreeFeller is disabled when not sneaking", "Tool is disabled when not sneaking", "Tree is disabled when not sneaking", "Felling allowed when not sneaking");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Boolean value){
            return new ItemBuilder(Material.IRON_BOOTS);
        }
    };
    public static Option<HashSet<String>> WORLDS = new Option<HashSet<String>>("Worlds", true, true, true, null){
        @Override
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe){
            if(toolValues.containsKey(tool)){
                HashSet<String> worlds = toolValues.get(tool);
                boolean blacklist = Objects.equals(WORLD_BLACKLIST.getValue(tool), true);
                boolean foundWorld = false;
                for(String world : worlds){
                    if(world.equalsIgnoreCase(block.getWorld().getName())||world.equalsIgnoreCase(block.getWorld().getUID().toString())){
                        if(blacklist){
                            return new DebugResult(this, TOOL, block.getWorld().getName()+" ("+block.getWorld().getUID().toString()+")");
                        }else{
                            foundWorld = true;
                            break;
                        }
                    }
                }
                if(!blacklist&&!foundWorld)return new DebugResult(this, TOOL, block.getWorld().getName()+" ("+block.getWorld().getUID().toString()+")");
            }
            if(treeValues.containsKey(tree)){
                HashSet<String> worlds = treeValues.get(tree);
                boolean blacklist = Objects.equals(WORLD_BLACKLIST.getValue(tree), true);
                boolean foundWorld = false;
                for(String world : worlds){
                    if(world.equalsIgnoreCase(block.getWorld().getName())||world.equalsIgnoreCase(block.getWorld().getUID().toString())){
                        if(blacklist){
                            return new DebugResult(this, TREE, block.getWorld().getName()+" ("+block.getWorld().getUID().toString()+")");
                        }else{
                            foundWorld = true;
                            break;
                        }
                    }
                }
                if(!blacklist&&!foundWorld)return new DebugResult(this, TREE, block.getWorld().getName()+" ("+block.getWorld().getUID().toString()+")");
            }
            if(toolValues.get(tool)==null&&treeValues.get(tree)==null&&globalValue!=null){
                boolean blacklist = Objects.equals(WORLD_BLACKLIST.globalValue, true);
                boolean foundWorld = false;
                for(String world : globalValue){
                    if(world.equalsIgnoreCase(block.getWorld().getName())||world.equalsIgnoreCase(block.getWorld().getUID().toString())){
                        if(blacklist){
                            return new DebugResult(this, GLOBAL, block.getWorld().getName()+" ("+block.getWorld().getUID().toString()+")");
                        }else{
                            foundWorld = true;
                            break;
                        }
                    }
                }
                if(!blacklist&&!foundWorld)return new DebugResult(this, GLOBAL, block.getWorld().getName()+" ("+block.getWorld().getUID().toString()+")");
            }
            return new DebugResult(this, SUCCESS, block.getWorld().getName()+" ("+block.getWorld().getUID().toString()+")");
        }
        @Override
        public HashSet<String> load(Object o){
            if(o instanceof String){
                HashSet<String> worlds = new HashSet<>();
                worlds.add((String)o);
                return worlds;
            }
            if(o instanceof Iterable){
                HashSet<String> worlds = new HashSet<>();
                for(Object ob : ((Iterable)o)){
                    if(ob instanceof String){
                        worlds.add((String) ob);
                    }
                }
                return worlds;
            }
            return null;
        }
        @Override
        public String getDesc(boolean ingame){
            return "In what worlds should the tree feller work? (Inverted if world-blacklist is set to true)";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("World {0} is invalid$", "World {0} is valid");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(HashSet<String> value){
            return new ItemBuilder(Material.GRASS_BLOCK);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyStringSet(parent, parent.plugin, parent.player, name, true, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyStringSet(parent, parent.plugin, parent.player, name, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyStringSet(parent, parent.plugin, parent.player, name, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static OptionBoolean WORLD_BLACKLIST = new OptionBoolean("World Blacklist", true, true, true, false){
        @Override
        public String getDesc(boolean ingame){
            return null;
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Boolean value){
            return new ItemBuilder(Material.GRASS_BLOCK);
        }
    };
    public static Option<Integer> COOLDOWN = new Option<Integer>("Cooldown", true, true, true, null){
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
        @Override
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe){
            if(player==null)return null;
            Cooldown cooldown = TreeFeller.cooldowns.get(player.getUniqueId());
            if(cooldown!=null){
                long now = System.currentTimeMillis();
                if(globalValue!=null){
                    long diff = now-cooldown.globalCooldown;
                    if(diff/50<=globalValue){
                        return new DebugResult(this, GLOBAL, (globalValue-diff), (globalValue-diff)/50, (globalValue-diff)/1000);
                    }
                }
                if(toolValues.containsKey(tool)&&cooldown.toolCooldowns.containsKey(tool)){
                    long diff = now-cooldown.toolCooldowns.get(tool);
                    if(diff/50<=toolValues.get(tool)){
                        return new DebugResult(this, TOOL, (toolValues.get(tool)-diff), (toolValues.get(tool)-diff)/50, (toolValues.get(tool)-diff)/1000);
                    }
                }
                if(treeValues.containsKey(tree)&&cooldown.treeCooldowns.containsKey(tree)){
                    long diff = now-cooldown.treeCooldowns.get(tree);
                    if(diff/50<=treeValues.get(tree)){
                        return new DebugResult(this, TREE, (treeValues.get(tree)-diff), (treeValues.get(tree)-diff)/50, (treeValues.get(tree)-diff)/1000);
                    }
                }
            }
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(boolean ingame){
            return "How long (in ticks) should players have to wait before felling another tree?";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Cooldown remaining: {0}ms", "Tool cooldown remaining: {0}ms", "Tree cooldown remaining: {0}ms", "Cooldown ready");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Integer value){
            return new ItemBuilder(Material.CLOCK);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, true, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 0, Integer.MAX_VALUE, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    
    public static OptionBoolean CASCADE = new OptionBoolean("Cascade", true, true, true, false){
        @Override
        public String getDesc(boolean ingame){
            return "If enabled, connected trees will also be felled resulting in a cascade that could fell an entire forest";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Boolean value){
            ItemBuilder builder = new ItemBuilder(Material.CHAIN_COMMAND_BLOCK);
            if(Objects.equals(value, true))builder.enchant(Enchantment.ARROW_INFINITE);
            return builder;
        }
    };
    public static Option<Integer> PARALLEL_CASCADE_LIMIT = new Option<Integer>("Parallel Cascade Limit", true, false, false, 1){
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
        @Override
        public String getDesc(boolean ingame){
            return "How many cascades can happen in the same tick? (Keep low to prevent runaway performance)";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Integer value){
            return new ItemBuilder(Material.CHAIN).setCount(value);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 1, Integer.MAX_VALUE, true, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 1, Integer.MAX_VALUE, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 1, Integer.MAX_VALUE, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<Integer> CASCADE_CHECK_LIMIT = new Option<Integer>("Cascade Check Limit", true, false, false, 64){
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
        @Override
        public String getDesc(boolean ingame){
            return "How many cascade checks can happen in the same tick? (Much less performance impact, but will help performance when cutting down big trees)";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Integer value){
            return new ItemBuilder(Material.CHAIN).setCount(value);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 1, Integer.MAX_VALUE, true, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 1, Integer.MAX_VALUE, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyInteger(parent, parent.plugin, parent.player, name, 1, Integer.MAX_VALUE, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static Option<ArrayList<Tree>> CASCADE_TREES = new Option<ArrayList<Tree>>("Cascade Trees", false, false, true, null){
        @Override
        public ArrayList<Tree> load(Object o){
            if(o instanceof Iterable){
                ArrayList<Tree> trees = new ArrayList<>();
                for(Object ob : (Iterable)o){
                    ArrayList<Tree> newTrees = load(ob);
                    if(newTrees!=null)trees.addAll(newTrees);
                }
                return trees;
            }
            if(o instanceof Tree){
                ArrayList<Tree> trees = new ArrayList<>();
                trees.add((Tree)o);
                return trees;
            }
            if(o instanceof String){
                ArrayList<Tree> trees = new ArrayList<>();
                for(Tree tree : TreeFeller.trees){
                    for(Material m : tree.trunk){
                        if(m.toString().contains(((String)o).toUpperCase().replace(" ", "_"))){
                            trees.add(tree);
                            break;
                        }
                    }
                }
                return trees;
            }
            if(o instanceof Integer){
                ArrayList<Tree> trees = new ArrayList<>();
                int i = (int)o;
                if(i>=0&&i<TreeFeller.trees.size()){
                    trees.add(TreeFeller.trees.get(i));
                    return trees;
                }
            }
            return null;
        }
        @Override
        public String getDesc(boolean ingame){
            return "Which trees should be checked during a cascade? (If not set, only the tree which was originally cut will be checked) The given value is a list of tree indicies, starting at 0 (the first tree defined is 0, the second is 1, etc.)";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(ArrayList<Tree> value){
            return new ItemBuilder(Material.JUNGLE_SAPLING);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyTreeSet(parent, parent.plugin, parent.player, name, true, globalValue, (value) -> {
                globalValue = new ArrayList<>(value);
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyTreeSet(parent, parent.plugin, parent.player, name, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, new ArrayList<>(value));
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyTreeSet(parent, parent.plugin, parent.player, name, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, new ArrayList<>(value));
            }));
        }
        @Override
        public String writeToConfig(ArrayList<Tree> value){
            ArrayList<Integer> indicies = new ArrayList<>();
            for(Tree t : value){
                indicies.add(TreeFeller.trees.indexOf(t));
            }
            return indicies.toString();
        }
    };//TODO make this a HashSet
    protected final String name;
    public final boolean global;
    public final boolean tool;
    public final boolean tree;
    /**
     * The default or global value of this Option.
     */
    public final E defaultValue;
    public E globalValue;
    public HashMap<Tool, E> toolValues = new HashMap<>();
    public HashMap<Tree, E> treeValues = new HashMap<>();
    private final Object defaultConfigValue;
    protected Option(String name, boolean global, boolean tool, boolean tree, E defaultValue){
        this(name, global, tool, tree, defaultValue, defaultValue);
    }
    protected Option(String name, boolean global, boolean tool, boolean tree, E defaultValue, Object defaultConfigValue){
        this.name = name;
        this.global = global;
        this.tool = tool;
        this.tree = tree;
        this.defaultValue = defaultValue;
        options.add(this);
        this.defaultConfigValue = defaultConfigValue;
    }
    public String getFriendlyName(){
        return name;
    }
    public ArrayList<String> getDescription(boolean ingame){
        ArrayList<String> description = new ArrayList<>();
        String s = getDesc(ingame);
        if(s==null)return description;
        if(s.contains("\n")){
            for(String str : s.split("\n")){
                description.add(str);
            }
        }else description.add(s);
        return description;
    }
    public abstract String getDesc(boolean ingame);
    /**
     * @return the name in-this-format
     */
    public String getGlobalName(){
        return name.replace(" ", "-").toLowerCase();
    }
    /**
     * @return the name inthisformat
     */
    public String getLocalName(){
        return name.replace(" ", "").toLowerCase();
    }
    /**
     * Loads the option from an object.<br>
     * This object is the result of <code>getConfig().get(getGlobalName())</code> for global options, or <code>list.get(getLocalName()) for local options</code>
     * @param o the object to load from
     * @return the loaded value
     */
    public abstract E load(Object o);
    public E loadFromConfig(FileConfiguration config){
        return load(config.get(getGlobalName()));
    }
    public static HashSet<Material> loadMaterialSet(Object o){
        if(o instanceof Iterable){
            HashSet<Material> materials = new HashSet<>();
            for(Object ob : (Iterable)o){
                HashSet<Material> newMaterials = loadMaterialSet(ob);
                if(newMaterials!=null)materials.addAll(newMaterials);
            }
            return materials;
        }
        HashSet<Material> materials = new HashSet<>();
        Material m = loadMaterial(o);
        if(m==null)return null;
        materials.add(m);
        return materials;
    }
    public static Material loadMaterial(Object o){
        if(o instanceof Material)return (Material)o;
        if(o instanceof String)return Material.matchMaterial((String)o);
        return null;
    }
    public ArrayList<E> loadList(Object o){
        if(o instanceof Iterable){
            ArrayList<E> list = new ArrayList<>();
            for(Object ob : (Iterable)o){
                E item = load(ob);
                if(item!=null)list.add(item);
            }
            return list;
        }
        return null;
    }
    public static String loadString(Object o){
        if(o==null)return null;
        return o.toString();
    }
    public static Integer loadInt(Object o){
        if(o instanceof Number){
            return ((Number)o).intValue();
        }
        if(o instanceof String){
            try{
                return Integer.parseInt((String)o);
            }catch(NumberFormatException ex){
                return null;
            }
        }
        return null;
    }
    public static Short loadShort(Object o){
        if(o instanceof Number){
            return ((Number)o).shortValue();
        }
        if(o instanceof String){
            try{
                return Short.parseShort((String)o);
            }catch(NumberFormatException ex){
                return null;
            }
        }
        return null;
    }
    public static Long loadLong(Object o){
        if(o instanceof Number){
            return ((Number)o).longValue();
        }
        if(o instanceof String){
            try{
                return Long.parseLong((String)o);
            }catch(NumberFormatException ex){
                return null;
            }
        }
        return null;
    }
    public static Float loadFloat(Object o){
        if(o instanceof Number){
            return ((Number)o).floatValue();
        }
        if(o instanceof String){
            try{
                return Float.parseFloat((String)o);
            }catch(NumberFormatException ex){
                return null;
            }
        }
        return null;
    }
    public static Double loadDouble(Object o){
        if(o instanceof Number){
            return ((Number)o).doubleValue();
        }
        if(o instanceof String){
            try{
                return Double.parseDouble((String)o);
            }catch(NumberFormatException ex){
                return null;
            }
        }
        return null;
    }
    protected String makeReadable(E value){
        return value.toString();
    }
    public void setValue(E value){
        globalValue = value;
    }
    public void setValue(Tree tree, E value){
        treeValues.put(tree, value);
    }
    public void setValue(Tool tool, E value){
        toolValues.put(tool, value);
    }
    public E getValue(){
        return globalValue;
    }
    public E getValue(Tree tree){
        return treeValues.get(tree);
    }
    public E getValue(Tool tool){
        return toolValues.get(tool);
    }
    public final DebugResult check(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe){
        if(globalValue==null&&!treeValues.containsKey(tree)&&!toolValues.containsKey(tool))return null;
        return doCheck(plugin, tool, tree, block, player, axe);
    }
    public final DebugResult checkTrunk(TreeFeller plugin, Tool tool, Tree tree, HashMap<Integer, ArrayList<Block>> blocks, Block block){
        if(globalValue==null&&!treeValues.containsKey(tree)&&!toolValues.containsKey(tool))return null;
        return doCheckTrunk(plugin, tool, tree, blocks, block);
    }
    public final DebugResult checkTree(TreeFeller plugin, Tool tool, Tree tree, HashMap<Integer, ArrayList<Block>> blocks, int leaves){
        if(globalValue==null&&!treeValues.containsKey(tree)&&!toolValues.containsKey(tool))return null;
        return doCheckTree(plugin, tool, tree, blocks, leaves);
    }
    protected DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe){
        return null;
    }
    protected DebugResult doCheckTrunk(TreeFeller plugin, Tool tool, Tree tree, HashMap<Integer, ArrayList<Block>> blocks, Block block){
        return null;
    }
    protected DebugResult doCheckTree(TreeFeller plugin, Tool tool, Tree tree, HashMap<Integer, ArrayList<Block>> blocks, int leaves){
        return null;
    }
    private static Enchantment getEnchantment(String string){
        switch(string.toLowerCase().replaceAll("_", " ")){
            case "power":
            case "arrow damage":
                return Enchantment.ARROW_DAMAGE;
            case "flame":
            case "arrow fire":
                return Enchantment.ARROW_FIRE;
            case "arrow infinite":
            case "infinity":
                return Enchantment.ARROW_INFINITE;
            case "arrow knockback":
            case "punch":
                return Enchantment.ARROW_KNOCKBACK;
            case "binding":
            case "binding curse":
            case "curse of binding":
                return Enchantment.BINDING_CURSE;
            case "channeling":
                return Enchantment.CHANNELING;
            case "sharpness":
            case "damage all":
                return Enchantment.DAMAGE_ALL;
            case "damage arthropods":
            case "bane of arthropods":
                return Enchantment.DAMAGE_ARTHROPODS;
            case "damage undead":
            case "smite":
                return Enchantment.DAMAGE_UNDEAD;
            case "depth strider":
                return Enchantment.DEPTH_STRIDER;
            case "efficiency":
            case "dig speed":
                return Enchantment.DIG_SPEED;
            case "durability":
            case "unbreaking":
                return Enchantment.DURABILITY;
            case "fire aspect":
                return Enchantment.FIRE_ASPECT;
            case "frost walker":
                return Enchantment.FROST_WALKER;
            case "impaling":
                return Enchantment.IMPALING;
            case "knockback":
                return Enchantment.KNOCKBACK;
            case "fortune":
            case "loot bonus blocks":
                return Enchantment.LOOT_BONUS_BLOCKS;
            case "looting":
            case "loot bonus mobs":
                return Enchantment.LOOT_BONUS_MOBS;
            case "loyalty":
                return Enchantment.LOYALTY;
            case "luck":
            case "luck of the sea":
                return Enchantment.LUCK;
            case "lure":
                return Enchantment.LURE;
            case "mending":
                return Enchantment.MENDING;
            case "oxygen":
            case "respiration":
                return Enchantment.OXYGEN;
            case "protection environmental":
            case "protection":
                return Enchantment.PROTECTION_ENVIRONMENTAL;
            case "protection explosions":
            case "blast protection":
                return Enchantment.PROTECTION_EXPLOSIONS;
            case "protection fall":
            case "feather falling":
            case "feather fall":
                return Enchantment.PROTECTION_FALL;
            case "protection fire":
            case "fire protection":
                return Enchantment.PROTECTION_FIRE;
            case "protection projectile":
            case "projectile protection":
                return Enchantment.PROTECTION_PROJECTILE;
            case "riptide":
                return Enchantment.RIPTIDE;
            case "silk touch":
                return Enchantment.SILK_TOUCH;
            case "sweeping":
            case "sweeping edge":
                return Enchantment.SWEEPING_EDGE;
            case "thorns":
                return Enchantment.THORNS;
            case "vanishing":
            case "vanishing curse":
            case "curse of vanishing":
                return Enchantment.VANISHING_CURSE;
            case "water worker":
            case "aqua affinity":
                return Enchantment.WATER_WORKER;
            default:
                if(string.contains(":")){
                    String[] strs = string.split("\\:");
                    return Enchantment.getByKey(new NamespacedKey(strs[0], strs[1]));
                }else{
                    return Enchantment.getByName(string);
                }
        }
    }
    public E get(Tool tool, Tree tree){
        if(toolValues.containsKey(tool))return toolValues.get(tool);
        if(treeValues.containsKey(tree))return treeValues.get(tree);
        return globalValue;
    }
    private static int getTotal(HashMap<Integer, ArrayList<Block>> blocks){
        int total = 0;
        for(int i : blocks.keySet()){
            total+=blocks.get(i).size();
        }
        return total;
    }
    public String getDefaultConfigValue(){
        if(defaultConfigValue==null)return null;
        if(defaultConfigValue instanceof HashSet){
            HashSet hs = (HashSet)defaultConfigValue;
            String str = "";
            ArrayList<String> lst = new ArrayList<>();
            for(Object o : hs){
                lst.add(Objects.toString(o));
            }
            Collections.sort(lst);
            for(String s : lst){
                str += "\n    - "+s;
            }
            return str;
        }
        return defaultConfigValue.toString();
    }
    public boolean hasDebugText(){
        return getGlobalDebugText()!=null||getToolDebugText()!=null||getTreeDebugText()!=null||getSuccessDebugText()!=null;
    }
    public String[] getDebugText(){
        return new String[]{null,null,null,null};
    }
    public String getGlobalDebugText(){
        return getDebugText()[0];
    }
    public String getToolDebugText(){
        return getDebugText()[1];
    }
    public String getTreeDebugText(){
        return getDebugText()[2];
    }
    public String getSuccessDebugText(){
        return getDebugText()[3];
    }
    public static String[] generateDebugText(String globalWith$, String success){
        return new String[]{globalWith$.replace("$", ""),globalWith$.replace("$", " for tool"),globalWith$.replace("$", " for tree"),success};
    }
    public static String[] generateDebugText(String global, String tool, String tree, String success){
        return new String[]{global,tool,tree,success};
    }
    public abstract ItemBuilder getConfigurationDisplayItem(E value);
    public ItemBuilder getConfigurationDisplayItem(){
        return getConfigurationDisplayItem(getValue());
    }
    public ItemBuilder getConfigurationDisplayItem(Tool tool){
        return getConfigurationDisplayItem(getValue(tool));
    }
    public ItemBuilder getConfigurationDisplayItem(Tree tree){
        return getConfigurationDisplayItem(getValue(tree));
    }
    public abstract void openGlobalModifyMenu(MenuGlobalConfiguration parent);
    public abstract void openToolModifyMenu(MenuToolConfiguration parent, Tool tool);
    public abstract void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree);
    public String writeToConfig(E value){
        return value==null?"":value.toString();
    }
    public String writeToConfig(){
        return writeToConfig(getValue());
    }
    public String writeToConfig(Tool tool){
        return writeToConfig(getValue(tool));
    }
    public String writeToConfig(Tree tree){
        return writeToConfig(getValue(tree));
    }
    private static int findMaxWidth(Object[] objs){
        int len = 0;
        for(Object o : objs)len = Math.max(len, Objects.toString(o).length());
        return len;
    }
    private static String normalize(String name, int width){
        while(name.length()<width)name+=" ";
        return name;
    }
}