package com.thizthizzydizzy.treefeller;
import com.thizthizzydizzy.simplegui.ItemBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import static com.thizthizzydizzy.treefeller.DebugResult.Type.GLOBAL;
import static com.thizthizzydizzy.treefeller.DebugResult.Type.SUCCESS;
import static com.thizthizzydizzy.treefeller.DebugResult.Type.TOOL;
import static com.thizthizzydizzy.treefeller.DebugResult.Type.TREE;
import com.thizthizzydizzy.treefeller.menu.MenuGlobalConfiguration;
import com.thizthizzydizzy.treefeller.menu.MenuToolConfiguration;
import com.thizthizzydizzy.treefeller.menu.MenuTreeConfiguration;
import com.thizthizzydizzy.treefeller.menu.modify.MenuModifyDouble;
import com.thizthizzydizzy.treefeller.menu.modify.MenuModifyEnchantmentMap;
import com.thizthizzydizzy.treefeller.menu.modify.MenuModifyEnum;
import com.thizthizzydizzy.treefeller.menu.modify.MenuModifyFloat;
import com.thizthizzydizzy.treefeller.menu.modify.MenuModifyInteger;
import com.thizthizzydizzy.treefeller.menu.modify.MenuModifyMaterial;
import com.thizthizzydizzy.treefeller.menu.modify.MenuModifyMaterialSet;
import com.thizthizzydizzy.treefeller.menu.modify.MenuModifyShort;
import com.thizthizzydizzy.treefeller.menu.modify.MenuModifyStringList;
import com.thizthizzydizzy.treefeller.menu.modify.MenuModifyStringSet;
import com.thizthizzydizzy.treefeller.menu.modify.special.MenuModifyEffectList;
import com.thizthizzydizzy.treefeller.menu.modify.special.MenuModifySpawnSaplings;
import com.thizthizzydizzy.treefeller.menu.modify.special.MenuModifyTreeSet;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
public abstract class Option<E>{
    private static final HashSet<Material> defaultOverridables = new HashSet<>();
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
    }
    private static final HashSet<Material> defaultGrasses = new HashSet<>();
    static{
        defaultGrasses.add(Material.GRASS_BLOCK);
        defaultGrasses.add(Material.DIRT);
        defaultGrasses.add(Material.PODZOL);
    }
    public static ArrayList<Option> options = new ArrayList<>();
    public static OptionBoolean STARTUP_LOGS = new OptionBoolean("Startup Logs", true, false, false, true){
        @Override
        public String getDesc(){
            return "If set to false, the tree feller will not list all its settings in the console on startup";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            ItemBuilder builder = new ItemBuilder(Material.OAK_LOG);
            if(getValue()==true){
                builder.enchant(Enchantment.BINDING_CURSE, 1);
                builder.addFlag(ItemFlag.HIDE_ENCHANTS);
            }
            return new ItemBuilder(Material.OAK_LOG);
        }
    };
    
    public static Option<Integer> SCAN_DISTANCE = new Option<Integer>("Scan Distance", true, true, false, 256){
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
        @Override
        public String getDesc(){
            return "How far should the plugin scan for logs? (If a tree is larger, only the part within this distance will be felled)";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.RAIL).setCount(globalValue);
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
    public static Option<Integer> LEAF_RANGE = new Option<Integer>("Leaf Range", true, true, true, 6){
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
        @Override
        public String getDesc(){
            return "How far away from logs should leaf blocks be destroyed? (set to 0 to prevent leaves from being destroyed) (Values over 6 are useless for vanilla trees, as these leaves would naturally decay anyway)";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.OAK_LEAVES).setCount(globalValue);
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
            if(globalValue!=null&&total<globalValue){
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
        public String getDesc(){
            return "How many logs should be required for logs to be counted as a tree?";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Tree has too few logs$: {0}<{1}", "Tree has enough logs");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.OAK_LOG).setCount(getValue());
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
            if(globalValue!=null&&leaves<globalValue){
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
        public String getDesc(){
            return "How many leaves should be required for logs to be counted as a tree?";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Tree has too few leaves$: {0}<{1}", "Tree has enough leaves");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.OAK_LEAVES).setCount(getValue());
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
            if(globalValue!=null&&total>globalValue){
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
        public String getDesc(){
            return "How many logs may be felled at with one stroke?";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Tree has too many logs$: {0}>{1}", "Tree has few enough logs");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.OAK_LOG).setCount(getValue());
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
    public static Option<Integer> MAX_HEIGHT = new Option<Integer>("Max Height", true, true, true, 5){
        @Override
        public Integer load(Object o) {
            return loadInt(o);
        }
        @Override
        public DebugResult doCheckTrunk(TreeFeller plugin, Tool tool, Tree tree, HashMap<Integer, ArrayList<Block>> blocks, Block block){
            int minY = block.getY();
            for(int i : blocks.keySet()){
                for(Block b : blocks.get(i)){
                    minY = Math.min(minY, b.getY());
                }
            }
            if(globalValue!=null&&block.getY()-minY>globalValue-1){
                int i = block.getY()-minY-(globalValue-1);
                return new DebugResult(this, GLOBAL, i);
            }
            if(toolValues.containsKey(tool)&&block.getY()-minY>toolValues.get(tool)-1){
                int i = block.getY()-minY-(toolValues.get(tool)-1);
                return new DebugResult(this, TOOL, i);
            }
            if(treeValues.containsKey(tree)&&block.getY()-minY>treeValues.get(tree)-1){
                int i = block.getY()-minY-(treeValues.get(tree)-1);
                return new DebugResult(this, TREE, i);
            }
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(){
            return "How far from the bottom can you cut down a tree? (Prevents you from cutting it down from the top) 1 = bottom block";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Tree was cut {0} blocks too high$", "Tree was cut low enough");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.LADDER).setCount(getValue());
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
        public String getDesc(){
            return "Should trees be able to be partially cut down if the tool has insufficient durability? It cannot be guaranteed what part of the tree will be cut down!";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.STICK);
        }
    };
    public static OptionBoolean PLAYER_LEAVES = new OptionBoolean("Player Leaves", true, true, true, false){
        @Override
        public String getDesc(){
            return "Should leaves placed by players be cut down also? (Only works with _LEAVES materials)";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.OAK_LEAVES);
        }
    };
    public static OptionBoolean DIAGONAL_LEAVES = new OptionBoolean("Diagonal Leaves", true, false, true, false){
        @Override
        public String getDesc(){
            return "If set to true, leaves will be detected diagonally; May require ignore-leaf-data to work properly";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.OAK_LEAVES);
        }
    };
    public static OptionBoolean IGNORE_LEAF_DATA = new OptionBoolean("Ignore Leaf Data", true, false, true, false){
        @Override
        public String getDesc(){
            return "If set to true, leaves' blockdata will be ignored. This should only be set if custom trees' leaves are not being destroyed when they should.";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.OAK_LEAVES);
        }
    };
    public static OptionBoolean REQUIRE_CROSS_SECTION = new OptionBoolean("Require Cross Section", true, true, true, false){
        @Override
        public DebugResult doCheckTrunk(TreeFeller plugin, Tool tool, Tree tree, HashMap<Integer, ArrayList<Block>> blocks, Block block){
            if(Objects.equals(globalValue, true)||Objects.equals(toolValues.get(tool), true)||Objects.equals(treeValues.get(tree), true)){
                for(int x = -1; x<=1; x++){
                    for(int z = -1; z<=1; z++){
                        if(x==0&&z==0)continue;
                        if(tree.trunk.contains(block.getRelative(x, 0, z).getType())){
                            if(Objects.equals(globalValue, true))return new DebugResult(this, GLOBAL);
                            if(Objects.equals(toolValues.get(tool), true))return new DebugResult(this, TOOL);
                            if(Objects.equals(treeValues.get(tree), true))return new DebugResult(this, TREE);
                        }
                    }
                }
            }
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(){
            return "Should trees larger than 1x1 require an entire horizontal cross-section to be mined before the tree fells? (Works for up to 2x2 trees)";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("A full cross-section has not been cut$", "A full cross-section has been cut");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.DARK_OAK_LOG);
        }
    };
    public static OptionBoolean FORCE_DISTANCE_CHECK = new OptionBoolean("Force Distance Check", true, false, true, false){
        @Override
        public String getDesc(){
            return "If set to true, all non-leaf-block leaves will be distance-checked to make sure they belong to the tree being felled (ex. mushrooms or nether 'tree' leaves)\n"
                    + "WARNING: THIS CAN CAUSE SIGNIFICANT LAG AND MAY LEAD TO UNSTABLE BEHAVIOR";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.DETECTOR_RAIL);
        }
    };
    
    public static OptionBoolean CUTTING_ANIMATION = new OptionBoolean("Cutting Animation", true, true, true, false){
        @Override
        public String getDesc(){
            return "Should the tree cut down with an animation?";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.GOLDEN_AXE);
        }
    };
    public static Option<Integer> ANIM_DELAY = new Option<Integer>("Anim Delay", true, true, true, 1){
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
        @Override
        public String getDesc(){
            return "Animation delay, in ticks";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.CLOCK).setCount(getValue());
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
        public String getDesc(){
            return "Should saplings be replanted?";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.OAK_SAPLING);
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
                Integer tl = toolValues.containsKey(tool)?toolValues.get(tool):0;
                Integer tr = treeValues.containsKey(tree)?treeValues.get(tree):0;
                return Math.max(tl, tr);
            }
            return globalValue;
        }
        @Override
        public String getDesc(){
            return "Should saplings be spawned?\n" +
                "0 = No, only replant if the leaves drop saplings\n" +
                "1 = Yes, but only if the leaves do not drop enough\n" +
                "2 = Yes, always spawn new saplings";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
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
    public static Option<Material> SAPLING = new Option<Material>("Sapling", false, false, true, null){
        @Override
        public Material load(Object o){
            return loadMaterial(o);
        }
        @Override
        public String getDesc(){
            return "If replant-saplings is enabled, this will replant the tree with this type of sapling";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.OAK_SAPLING);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyMaterial(parent, parent.plugin, parent.player, name, true, "block", globalValue, (mat) -> {
                return mat.isBlock();
            }, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyMaterial(parent, parent.plugin, parent.player, name, true, "block", toolValues.get(tool), (mat) -> {
                return mat.isBlock();
            }, (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyMaterial(parent, parent.plugin, parent.player, name, true, "block", treeValues.get(tree), (mat) -> {
                return mat.isBlock();
            }, (value) -> {
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
        public String getDesc(){
            return "If replant-saplings is enabled, this will limit the number of saplings that can be replanted";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.OAK_SAPLING);
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
            if(o instanceof Iterable){
                HashSet<Material> materials = new HashSet<>();
                for(Object ob : (Iterable)o){
                    HashSet<Material> newMaterials = load(ob);
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
        @Override
        public String getDesc(){
            return "What blocks can saplings be planted on?";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
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
        public String getDesc(){
            return "What felling behavior should logs have?\n" +
                "Valid options:\n" +
                "BREAK (default)     The logs will break and fall as items\n" +
                "FALL                The logs will fall as falling blocks\n" +
                "FALL_HURT           The logs will fall as falling blocks and hurt any entity they land on\n" +
                "FALL_BREAK          The logs will fall as falling blocks and break when they reach the ground\n" +
                "FALL_HURT_BREAK     The logs will fall as falling brocks, hurt entities they land on, and break when they reach the ground\n" +
                "INVENTORY           The logs will appear in the player's inventory as items\n" +
                "FALL_INVENTORY      The logs will fall as falling blocks, break, and appear in the player's inventory upon reaching the ground\n" +
                "FALL_HURT_INVENTORY The logs will fall as falling blocks, break, hurt entities they land on, and appear in the player's inventory upon reaching the ground\n" +
                "NATURAL             The logs will instantly fall in a more natural way (May not work with cutting-animation)\n" +
                "Note that falling blocks occasionally drop as items if they land wrong";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.OAK_LOG);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyEnum<FellBehavior>(parent, parent.plugin, parent.player, name, "FellBehavior", false, globalValue, FellBehavior.values(), (value) -> {
                globalValue = value;
            }){
                @Override
                public Material getItem(FellBehavior value){
                    return value.getItem();
                }
            });
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyEnum<FellBehavior>(parent, parent.plugin, parent.player, name, "FellBehavior", true, toolValues.get(tool), FellBehavior.values(), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }){
                @Override
                public Material getItem(FellBehavior value){
                    return value.getItem();
                }
            });
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyEnum<FellBehavior>(parent, parent.plugin, parent.player, name, "FellBehavior", true, treeValues.get(tree), FellBehavior.values(), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }){
                @Override
                public Material getItem(FellBehavior value){
                    return value.getItem();
                }
            });
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
        public String getDesc(){
            return "What felling behavior should leaves have?\n" +
                "Valid options:\n" +
                "BREAK (default)     The leaves will break and fall as items\n" +
                "FALL                The leaves will fall as falling blocks\n" +
                "FALL_HURT           The leaves will fall as falling blocks and hurt any entity they land on\n" +
                "FALL_BREAK          The leaves will fall as falling blocks and break when they reach the ground (Leaf blocks will be dropped)\n" +
                "FALL_HURT_BREAK     The leaves will fall as falling brocks, hurt entities they land on, and break when they reach the ground. (Leaf blocks will be dropped)\n" +
                "INVENTORY           The leaves will appear in the player's inventory as items\n" +
                "FALL_INVENTORY      The leaves will fall as falling blocks, break, and appear in the player's inventory upon reaching the ground\n" +
                "FALL_HURT_INVENTORY The leaves will fall as falling blocks, break, hurt entities they land on, and appear in the player's inventory upon reaching the ground\n" +
                "NATURAL             The leaves will instantly fall in a more natural way (May not work with cutting-animation)\n" +
                "Note that falling blocks occasionally drop as items if they land wrong, and falling leaves will drop leaf blocks if they do";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.OAK_LEAVES);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyEnum<FellBehavior>(parent, parent.plugin, parent.player, name, "FellBehavior", false, globalValue, FellBehavior.values(), (value) -> {
                globalValue = value;
            }){
                @Override
                public Material getItem(FellBehavior value){
                    return value.getItem();
                }
            });
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyEnum<FellBehavior>(parent, parent.plugin, parent.player, name, "FellBehavior", true, toolValues.get(tool), FellBehavior.values(), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }){
                @Override
                public Material getItem(FellBehavior value){
                    return value.getItem();
                }
            });
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyEnum<FellBehavior>(parent, parent.plugin, parent.player, name, "FellBehavior", true, treeValues.get(tree), FellBehavior.values(), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }){
                @Override
                public Material getItem(FellBehavior value){
                    return value.getItem();
                }
            });
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
        public String getDesc(){
            return "Which direction should the tree fall in?\n" +
                "(Only used when log or leaf behavior is set to FALL or similar)\n" +
                "Valid options:\n" +
                "RANDOM     The tree will fall in a random direction\n" +
                "TOWARD     The tree will fall towards the player\n" +
                "AWAY       The tree will fall away from the player\n" +
                "LEFT       The tree will fall to the player's left\n" +
                "RIGHT      The tree will fall to the player's right\n" +
                "NORTH      The tree will fall to the north\n" +
                "SOUTH      The tree will fall to the south\n" +
                "EAST       The tree will fall to the east\n" +
                "WEST       The tree will fall to the west\n" +
                "NORTH_WEST The tree fill fall to the northwest\n" +
                "NORTH_EAST The tree fill fall to the northeast\n" +
                "SOUTH_WEST The tree fill fall to the southwest\n" +
                "SOUTH_EAST The tree fill fall to the southeast";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.OAK_LOG);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyEnum<DirectionalFallBehavior>(parent, parent.plugin, parent.player, name, "DirectionalFallBehavior", false, globalValue, DirectionalFallBehavior.values(), (value) -> {
                globalValue = value;
            }){
                @Override
                public Material getItem(DirectionalFallBehavior value){
                    return value.getItem();
                }
            });
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyEnum<DirectionalFallBehavior>(parent, parent.plugin, parent.player, name, "DirectionalFallBehavior", true, toolValues.get(tool), DirectionalFallBehavior.values(), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }){
                @Override
                public Material getItem(DirectionalFallBehavior value){
                    return value.getItem();
                }
            });
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyEnum<DirectionalFallBehavior>(parent, parent.plugin, parent.player, name, "DirectionalFallBehavior", true, treeValues.get(tree), DirectionalFallBehavior.values(), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }){
                @Override
                public Material getItem(DirectionalFallBehavior value){
                    return value.getItem();
                }
            });
        }
    };
    public static Option<HashSet<Material>> OVERRIDABLES = new Option<HashSet<Material>>("Overridables", true, true, true, defaultOverridables){
        @Override
        public HashSet<Material> load(Object o){
            if(o instanceof Collection){
                HashSet<Material> overridables = new HashSet<>();
                Collection c = (Collection)o;
                for(Object ob : c){
                    Material m = loadMaterial(ob);
                    if(m!=null)overridables.add(m);
                }
                return overridables;
            }else{
                HashSet<Material> overridables = new HashSet<>();
                Material m = loadMaterial(o);
                if(m!=null){
                    overridables.add(m);
                    return overridables;
                }
            }
            return null;
        }
        @Override
        public String getDesc(){
            return "This is the list of blocks that may be overridden when a tree falls onto them (e.g air, grass, water)\n" +
"(Only used for NATURAL fell behavior)";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
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
        public String getDesc(){
            return "If set to true, trees can only fall in one of the cardinal directions (N/S/E/W)";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
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
            Double tl = toolValues.containsKey(tool)?toolValues.get(tool):0d;
            Double tr = treeValues.containsKey(tree)?treeValues.get(tree):0d;
            return tl+tr+globalValue;
        }
        @Override
        public String getDesc(){
            return "How much horizontal velocity should falling trees get?\n" +
                "(Only used when log or leaf behavior is set to FALL or similar)\n" +
                "All of the blocks in the tree will fall in the same direction with this velocity.";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.OAK_LOG);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, -Double.MAX_VALUE, Double.MAX_VALUE, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, -Double.MAX_VALUE, Double.MAX_VALUE, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, -Double.MAX_VALUE, Double.MAX_VALUE, true, treeValues.get(tree), (value) -> {
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
            Double tl = toolValues.containsKey(tool)?toolValues.get(tool):0d;
            Double tr = treeValues.containsKey(tree)?treeValues.get(tree):0d;
            return tl+tr+globalValue;
        }
        @Override
        public String getDesc(){
            return "How much upwards velocity should falling trees get?\n" +
                "(Only used when log or leaf behavior is set to FALL or similar)";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.OAK_LOG);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, -Double.MAX_VALUE, Double.MAX_VALUE, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, -Double.MAX_VALUE, Double.MAX_VALUE, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, -Double.MAX_VALUE, Double.MAX_VALUE, true, treeValues.get(tree), (value) -> {
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
            Double tl = toolValues.containsKey(tool)?toolValues.get(tool):0d;
            Double tr = treeValues.containsKey(tree)?treeValues.get(tree):0d;
            return tl+tr+globalValue;
        }
        @Override
        public String getDesc(){
            return "How much random sideways velocity should falling blocks get?\n" +
                "(Only used when log or leaf behavior is set to FALL or similar)";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.OAK_LOG);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, -Double.MAX_VALUE, Double.MAX_VALUE, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, -Double.MAX_VALUE, Double.MAX_VALUE, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, -Double.MAX_VALUE, Double.MAX_VALUE, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    
    public static OptionBoolean RESPECT_UNBREAKING = new OptionBoolean("Respect Unbreaking", true, true, true, true){
        @Override
        public String getDesc(){
            return "If a tool has unbreaking, should it take less damage from cutting trees?";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            ItemBuilder builder = new ItemBuilder(Material.IRON_AXE);
            if(getValue())builder.enchant(Enchantment.DURABILITY);
            return builder;
        }
    };
    public static Option<Double> DAMAGE_MULT = new Option<Double>("Damage Mult", true, true, true, 1d){
        @Override
        public Double load(Object o){
            return loadDouble(o);
        }
        @Override
        public String getDesc(){
            return "How much damage should tools take per log? (Multiplier)";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.WOODEN_AXE);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, 0, Double.MAX_VALUE, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, 0, Double.MAX_VALUE, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, 0, Double.MAX_VALUE, true, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
    };
    public static OptionBoolean STACKED_TOOLS = new OptionBoolean("Stacked Tools", true, true, false, false){
        @Override
        public String getDesc(){
            return "If set to true, stacked tools will be consumed one at a time.\nThis will treat the entire stack as one tool, so prevent-breakage will not keep individual tools from breaking, only the whole stack.\nWARNING: Stacked tools are not recommended!";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.DIAMOND_AXE).setCount(64);
        }
    };
    public static OptionBoolean LEAF_FORTUNE = new OptionBoolean("Leaf Fortune", true, true, true, true){
        @Override
        public String getDesc(){
            return "Should Fortune on an axe be applied to leaves?";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.OAK_LEAVES).enchant(Enchantment.LOOT_BONUS_BLOCKS).addFlag(ItemFlag.HIDE_ENCHANTS);
        }
    };
    public static OptionBoolean LEAF_SILK_TOUCH = new OptionBoolean("Leaf Silk Touch", true, true, true, false){
        @Override
        public String getDesc(){
            return "Should Silk Touch on an axe be applied to leaves?";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.OAK_LEAVES).enchant(Enchantment.SILK_TOUCH).addFlag(ItemFlag.HIDE_ENCHANTS);
        }
    };
    public static OptionBoolean LOG_FORTUNE = new OptionBoolean("Log Fortune", true, true, true, true){
        @Override
        public String getDesc(){
            return "Should Fortune on an axe be applied to logs?";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.OAK_LOG).enchant(Enchantment.LOOT_BONUS_BLOCKS);
        }
    };
    public static OptionBoolean LOG_SILK_TOUCH = new OptionBoolean("Log Silk Touch", true, true, true, true){
        @Override
        public String getDesc(){
            return "Should Silk Touch on an axe be applied to leaves?";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.OAK_LOG).enchant(Enchantment.SILK_TOUCH);
        }
    };
    public static OptionBoolean LEAVE_STUMP = new OptionBoolean("Leave Stump", true, true, true, false){
        @Override
        public String getDesc(){
            return "When a tree is felled, should a stump be left? (The stump consists of any log blocks below the point at which the tree was felled)\n" +
                "This may cause issues with custom trees that have multiple trunks or branches that extend very low";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.OAK_LOG);
        }
    };
    public static OptionBoolean ROTATE_LOGS = new OptionBoolean("Rotate Logs", true, true, true, true){
        @Override
        public String getDesc(){
            return "When trees fall, should the logs rotate as they fall? (This makes it look more realistic, with logs landing horizontally)\n" +
                "(Only used when log or leaf behavior is set to FALL or similar)";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.OAK_LOG);
        }
    };
    public static OptionBoolean CONVERT_WOOD_TO_LOG = new OptionBoolean("Convert Wood To Log", true, true, true, true){
        @Override
        public String getDesc(){
            return "Should _WOOD blocks in trees be converted to _LOG when they drop as items?";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.OAK_WOOD);
        }
    };
    public static Option<Double> LEAF_DROP_CHANCE = new Option<Double>("Leaf Drop Chance", true, true, true, 1d){
        @Override
        public Double load(Object o){
            return loadDouble(o);
        }
        @Override
        public Double get(Tool tool, Tree tree){
            Double tl = toolValues.containsKey(tool)?toolValues.get(tool):1d;
            Double tr = treeValues.containsKey(tree)?treeValues.get(tree):1d;
            return tl*tr*globalValue;
        }
        @Override
        public String getDesc(){
            return "How often should leaves drop items? Set this to 0.0 to stop leaves from dropping items altogether (Only works with BREAK behavior)";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.OAK_LEAVES);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, 0, Double.MAX_VALUE, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, 0, Double.MAX_VALUE, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, 0, Double.MAX_VALUE, true, treeValues.get(tree), (value) -> {
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
            Double tl = toolValues.containsKey(tool)?toolValues.get(tool):1d;
            Double tr = treeValues.containsKey(tree)?treeValues.get(tree):1d;
            return tl*tr*globalValue;
        }
        @Override
        public String getDesc(){
            return "How often should logs drop items? Set this to 0.0 to stop logs from dropping items altogether (Only works with BREAK behavior)";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.OAK_LOG);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, 0, Double.MAX_VALUE, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, 0, Double.MAX_VALUE, true, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyDouble(parent, parent.plugin, parent.player, name, 0, Double.MAX_VALUE, true, treeValues.get(tree), (value) -> {
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
            if(globalValue!=null)effects.addAll(globalValue);
            if(toolValues.containsKey(tool))effects.addAll(toolValues.get(tool));
            if(treeValues.containsKey(tree))effects.addAll(treeValues.get(tree));
            return effects;
        }
        @Override
        public String getGlobalName(){
            return "global-effects";
        }
        @Override
        public String getDesc(){
            return "Global effects are applied every time a tree is felled, regardless of tree type or tool\n" +
                "use ALL for all effects\n" +
                "ex:\n" +
                "  - ghost sound\n" +
                "  - smoke";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
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
    
    public static Option<HashMap<Enchantment, Integer>> REQUIRED_ENCHANTMENTS = new Option<HashMap<Enchantment, Integer>>("Required Enchantments", true, true, true, null){
        @Override
        public HashMap<Enchantment, Integer> load(Object o){
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
            return null;
        }
        @Override
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe, GameMode gamemode, boolean sneaking, boolean dropItems){
            if(globalValue!=null){
                for(Enchantment e : globalValue.keySet()){
                    if(axe.getEnchantmentLevel(e)<globalValue.get(e)){
                        return new DebugResult(this, GLOBAL, e.toString(), globalValue.get(e));
                    }
                }
            }
            HashMap<Enchantment, Integer> values = toolValues.get(tool);
            if(values!=null){
                for(Enchantment e : values.keySet()){
                    if(axe.getEnchantmentLevel(e)<values.get(e)){
                        return new DebugResult(this, TOOL, e.toString(), values.get(e));
                    }
                }
            }
            values = treeValues.get(tree);
            if(values!=null){
                for(Enchantment e : values.keySet()){
                    if(axe.getEnchantmentLevel(e)<values.get(e)){
                        return new DebugResult(this, TREE, e.toString(), values.get(e));
                    }
                }
            }
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(){
            return "Tools must have these enchantments at this level or higher to fell trees\n"
                + "ex:\n"
                + "- unbreaking: 2\n"
                + "- efficiency: 5";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Enchantment missing$: {0} at minimum level {1}", "All required enchantments met");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            ItemBuilder builder = new ItemBuilder(Material.ENCHANTED_BOOK);
            HashMap<Enchantment, Integer> value = getValue();
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
    };
    public static Option<HashMap<Enchantment, Integer>> BANNED_ENCHANTMENTS = new Option<HashMap<Enchantment, Integer>>("Banned Enchantments", true, true, true, null){
        @Override
        public HashMap<Enchantment, Integer> load(Object o){
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
            return null;
        }
        @Override
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe, GameMode gamemode, boolean sneaking, boolean dropItems){
            if(globalValue!=null){
                for(Enchantment e : globalValue.keySet()){
                    if(axe.getEnchantmentLevel(e)>=globalValue.get(e)){
                        return new DebugResult(this, GLOBAL, e.toString(), globalValue.get(e)-1);
                    }
                }
            }
            HashMap<Enchantment, Integer> values = toolValues.get(tool);
            if(values!=null){
                for(Enchantment e : values.keySet()){
                    if(axe.getEnchantmentLevel(e)<values.get(e)){
                        return new DebugResult(this, TOOL, e.toString(), globalValue.get(e)-1);
                    }
                }
            }
            values = treeValues.get(tree);
            if(values!=null){
                for(Enchantment e : values.keySet()){
                    if(axe.getEnchantmentLevel(e)<values.get(e)){
                        return new DebugResult(this, TREE, e.toString(), globalValue.get(e)-1);
                    }
                }
            }
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(){
            return "Tools must not have these enchantments or have them lower than this level to fell trees\n"
                + "ex:\n"
                + "- silk_touch: 1\n"
                + "- unbreaking: 3";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Tool contains banned enchantment$: {0} above level {1}", "No banned enchantments found");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            ItemBuilder builder = new ItemBuilder(Material.ENCHANTED_BOOK);
            HashMap<Enchantment, Integer> value = getValue();
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
    };
    public static Option<Short> MIN_DURABILITY = new Option<Short>("Min Durability", true, true, true, null){
        @Override
        public Short load(Object o){
            return loadShort(o);
        }
        @Override
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe, GameMode gamemode, boolean sneaking, boolean dropItems){
            int durability = axe.getType().getMaxDurability()-axe.getDurability();
            if(axe.getType().getMaxDurability()>0){
                if(globalValue!=null){
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
        public String getDesc(){
            return "Tools with less than this much durability will be unable to fell trees";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Tool durability is less than minimum allowed$: {1}", "Tool meets minimum durability requirement");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.DIAMOND_AXE).setDurability(getValue());
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
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe, GameMode gamemode, boolean sneaking, boolean dropItems){
            int durability = axe.getType().getMaxDurability()-axe.getDurability();
            if(axe.getType().getMaxDurability()>0){
                if(globalValue!=null){
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
        public String getDesc(){
            return "Tools with more than this much durability will be unable to fell trees";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Tool durability is greater than maximum allowed: {1}", "Tool meets maximum durability requirement");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.DIAMOND_AXE).setDurability(getValue());
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
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe, GameMode gamemode, boolean sneaking, boolean dropItems){
            int durability = axe.getType().getMaxDurability()-axe.getDurability();
            float durabilityPercent = durability/(float)axe.getType().getMaxDurability();
            if(axe.getType().getMaxDurability()>0){
                if(globalValue!=null){
                    if(durabilityPercent<globalValue)return new DebugResult(this, GLOBAL, durability, globalValue*100+"%");
                }
                if(toolValues.containsKey(tool)){
                    if(durabilityPercent<toolValues.get(tool))return new DebugResult(this, TOOL, durability, toolValues.get(tool)*100+"%");
                }
                if(treeValues.containsKey(tree)){
                    if(durabilityPercent<treeValues.get(tree))return new DebugResult(this, TREE, durability, treeValues.get(tree)*100+"%");
                }
            }
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(){
            return "Tools with less than this percentage of durability will be unable to fell trees";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Tool durability is less than minimum allowed$: {1}", "Tool meets minimum durability requirement");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.DIAMOND_AXE).setDurability(getValue());
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
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe, GameMode gamemode, boolean sneaking, boolean dropItems){
            int durability = axe.getType().getMaxDurability()-axe.getDurability();
            float durabilityPercent = durability/(float)axe.getType().getMaxDurability();
            if(axe.getType().getMaxDurability()>0){
                if(globalValue!=null){
                    if(durabilityPercent>globalValue)return new DebugResult(this, GLOBAL, durability, globalValue*100+"%");
                }
                if(toolValues.containsKey(tool)){
                    if(durabilityPercent>toolValues.get(tool))return new DebugResult(this, TOOL, durability, toolValues.get(tool)*100+"%");
                }
                if(treeValues.containsKey(tree)){
                    if(durabilityPercent>treeValues.get(tree))return new DebugResult(this, TREE, durability, treeValues.get(tree)*100+"%");
                }
            }
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(){
            return "Tools with more than this percentage of durability will be unable to fell trees";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Tool durability is greater than maximum allowed: {1}", "Tool meets maximum durability requirement");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.DIAMOND_AXE).setDurability(getValue());
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
        public String getDesc(){
            return "If set to true, tools will not be able to fell a tree if doing so would break the tool.";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
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
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe, GameMode gamemode, boolean sneaking, boolean dropItems) {
            ArrayList<String> lore = new ArrayList<>();
            if(axe.hasItemMeta()){
                ItemMeta meta = axe.getItemMeta();
                if(meta.hasLore()){
                    lore = new ArrayList<>(meta.getLore());
                }
            }
            if(globalValue!=null){
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
        public String getDesc(){
            return "Tools must have all literal strings in this list in order to fell trees\n"
                + "ex:\n"
                + "- Can fell trees";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Tool is missing required lore$: {0}", "Tool has all required lore");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
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
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe, GameMode gamemode, boolean sneaking, boolean dropItems) {
            String name = null;
            if(axe.hasItemMeta()){
                ItemMeta meta = axe.getItemMeta();
                name = meta.getDisplayName();
            }
            if(globalValue!=null){
                if(!globalValue.equals(name))return new DebugResult(this, GLOBAL, globalValue);
            }
            if(toolValues.containsKey(tool)){
                if(!toolValues.get(tool).equals(name))return new DebugResult(this, TOOL, toolValues.get(tool));
            }
            if(treeValues.containsKey(tree)){
                if(!treeValues.get(tree).equals(name))return new DebugResult(this, TREE, treeValues.get(tree));
            }
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(){
            return "A tool's name must match exactly in order to fell trees (colors can be designated with &)";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Tool name does not match required name$: {0}", "Tool name matches");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.PAPER).addLore(getValue());
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
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe, GameMode gamemode, boolean sneaking, boolean dropItems) {
            if(globalValue!=null){
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
        public String getDesc(){
            return "Trees can only be cut down by players who have all permissons listed here\n"
                + "ex:\n"
                + "- treefeller.example";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Player is missing required permission$: {0}", "Player has all required permissions");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
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
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe, GameMode gamemode, boolean sneaking, boolean dropItems){
            long dayTime = block.getWorld().getTime();
            if(axe.getType().getMaxDurability()>0){
                if(globalValue!=null){
                    if(dayTime<globalValue)return new DebugResult(this, GLOBAL, globalValue);
                }
                if(toolValues.containsKey(tool)){
                    if(dayTime<toolValues.get(tool))return new DebugResult(this, TOOL, toolValues.get(tool));
                }
                if(treeValues.containsKey(tree)){
                    if(dayTime<treeValues.get(tree))return new DebugResult(this, TREE, treeValues.get(tree));
                }
            }
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(){
            return "What should the minimum time be for felling trees? (0-24000)";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Time is less than minimum allowed$: {0}", "Time meets minimum requirement");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
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
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe, GameMode gamemode, boolean sneaking, boolean dropItems){
            long dayTime = block.getWorld().getTime();
            if(axe.getType().getMaxDurability()>0){
                if(globalValue!=null){
                    if(dayTime>globalValue)return new DebugResult(this, GLOBAL, globalValue);
                }
                if(toolValues.containsKey(tool)){
                    if(dayTime>toolValues.get(tool))return new DebugResult(this, TOOL, toolValues.get(tool));
                }
                if(treeValues.containsKey(tree)){
                    if(dayTime>treeValues.get(tree))return new DebugResult(this, TREE, treeValues.get(tree));
                }
            }
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(){
            return "What should the maximum time be for felling trees? (0-24000)";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Time is greater than maximum allowed$: {0}", "Time meets maximum requirement");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
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
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe, GameMode gamemode, boolean sneaking, boolean dropItems){
            long gameTime = block.getWorld().getFullTime();
            long day = gameTime/24000;
            long phase = day%8;
            if(axe.getType().getMaxDurability()>0){
                if(globalValue!=null){
                    if(phase<globalValue)return new DebugResult(this, GLOBAL, globalValue);
                }
                if(toolValues.containsKey(tool)){
                    if(phase<toolValues.get(tool))return new DebugResult(this, TOOL, toolValues.get(tool));
                }
                if(treeValues.containsKey(tree)){
                    if(phase<treeValues.get(tree))return new DebugResult(this, TREE, treeValues.get(tree));
                }
            }
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(){
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
            return generateDebugText("Phase is less than minimum allowed$: {0}", "Phase meets minimum requirement");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
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
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe, GameMode gamemode, boolean sneaking, boolean dropItems){
            long gameTime = block.getWorld().getFullTime();
            long day = gameTime/24000;
            long phase = day%8;
            if(axe.getType().getMaxDurability()>0){
                if(globalValue!=null){
                    if(phase>globalValue)return new DebugResult(this, GLOBAL, globalValue);
                }
                if(toolValues.containsKey(tool)){
                    if(phase>toolValues.get(tool))return new DebugResult(this, TOOL, toolValues.get(tool));
                }
                if(treeValues.containsKey(tree)){
                    if(phase>treeValues.get(tree))return new DebugResult(this, TREE, treeValues.get(tree));
                }
            }
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(){
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
            return generateDebugText("Phase is greater than maximum allowed$: {0}", "Phase meets maximum requirement");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
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
    public static Option<Integer> CUSTOM_MODEL_DATA = new Option<Integer>("Custom Model Data", true, true, true, null){
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
        @Override
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe, GameMode gamemode, boolean sneaking, boolean dropItems){
            ItemMeta meta = axe.getItemMeta();
            int data = (meta==null||!meta.hasCustomModelData()?0:meta.getCustomModelData());
            if(globalValue!=null){
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
        public String getDesc(){
            return "Tool's CustomModelData must match in order to fell trees";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Custom model data does not match#: {0} != {1}", "Custom model data matches!");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
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
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe, GameMode gamemode, boolean sneaking, boolean dropItems){
            if(toolValues.containsKey(tool)){
                if(!toolValues.get(tool).contains(tree)){
                    return new DebugResult(this, TOOL, tree.toString());
                }
            }
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(){
            return "The tool can only fell specific trees. <values> is a list of tree indexes, starting at 0 (the first tree defined is 0, the second is 1, etc.)";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Tree is not allowed$: {0}", "Tree is allowed for tool");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.SPRUCE_SAPLING);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyTreeSet(parent, parent.plugin, parent.player, name, true, new HashSet<>(globalValue), (value) -> {
                globalValue = new ArrayList<>(value);
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyTreeSet(parent, parent.plugin, parent.player, name, true, new HashSet<>(toolValues.get(tool)), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, new ArrayList<>(value));
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyTreeSet(parent, parent.plugin, parent.player, name, true, new HashSet<>(treeValues.get(tree)), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, new ArrayList<>(value));
            }));
        }
    };//TODO make this a HashSet
    public static OptionBoolean ENABLE_ADVENTURE = new OptionBoolean("Enable Adventure", true, true, true, false){
        @Override
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe, GameMode gamemode, boolean sneaking, boolean dropItems){
            if(player.getGameMode()!=GameMode.ADVENTURE)return null;
            if(Objects.equals(globalValue, false))return new DebugResult(this, GLOBAL);
            if(Objects.equals(toolValues.get(tool), false))return new DebugResult(this, TOOL);
            if(Objects.equals(treeValues.get(tree), false))return new DebugResult(this, TREE);
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(){
            return "Should the tree feller work in adventure mode?";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("TreeFeller is disabled in adventure mode", "Tool is disabled in adventure mode", "Tree is disabled in adventure mode", "All components OK for adventure mode");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.MAP);
        }
    };
    public static OptionBoolean ENABLE_SURVIVAL = new OptionBoolean("Enable Survival", true, true, true, true){
        @Override
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe, GameMode gamemode, boolean sneaking, boolean dropItems){
            if(player.getGameMode()!=GameMode.SURVIVAL)return null;
            if(Objects.equals(globalValue, false))return new DebugResult(this, GLOBAL);
            if(Objects.equals(toolValues.get(tool), false))return new DebugResult(this, TOOL);
            if(Objects.equals(treeValues.get(tree), false))return new DebugResult(this, TREE);
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(){
            return "Should the tree feller work in survival mode?";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("TreeFeller is disabled in survival mode", "Tool is disabled in survival mode", "Tree is disabled in survival mode", "All components OK for survival mode");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.IRON_SWORD);
        }
    };
    public static OptionBoolean ENABLE_CREATIVE = new OptionBoolean("Enable Creative", true, true, true, false){
        @Override
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe, GameMode gamemode, boolean sneaking, boolean dropItems){
            if(player.getGameMode()!=GameMode.CREATIVE)return null;
            if(Objects.equals(globalValue, false))return new DebugResult(this, GLOBAL);
            if(Objects.equals(toolValues.get(tool), false))return new DebugResult(this, TOOL);
            if(Objects.equals(treeValues.get(tree), false))return new DebugResult(this, TREE);
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(){
            return "Should the tree feller work in creative mode?";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("TreeFeller is disabled in creative mode", "Tool is disabled in creative mode", "Tree is disabled in creative mode", "All components OK for creative mode");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.GRASS_BLOCK);
        }
    };
    public static OptionBoolean WITH_SNEAK = new OptionBoolean("With Sneak", true, true, true, false){
        @Override
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe, GameMode gamemode, boolean sneaking, boolean dropItems){
            if(!player.isSneaking())return null;
            if(Objects.equals(globalValue, false))return new DebugResult(this, GLOBAL);
            if(Objects.equals(toolValues.get(tool), false))return new DebugResult(this, TOOL);
            if(Objects.equals(treeValues.get(tree), false))return new DebugResult(this, TREE);
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(){
            return "Should the tree feller work when sneaking?";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("TreeFeller is disabled when sneaking", "Tool is disabled when sneaking", "Tree is disabled when sneaking", "Felling allowed when sneaking");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.LEATHER_BOOTS);
        }
    };
    public static OptionBoolean WITHOUT_SNEAK = new OptionBoolean("Without Sneak", true, true, true, true){
        @Override
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe, GameMode gamemode, boolean sneaking, boolean dropItems){
            if(player.isSneaking())return null;
            if(Objects.equals(globalValue, false))return new DebugResult(this, GLOBAL);
            if(Objects.equals(treeValues.get(tree), false))return new DebugResult(this, TREE);
            if(Objects.equals(toolValues.get(tool), false))return new DebugResult(this, TOOL);
            return new DebugResult(this, SUCCESS);
        }
        @Override
        public String getDesc(){
            return "Should the tree feller work when not sneaking?";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("TreeFeller is disabled when not sneaking", "Tool is disabled when not sneaking", "Tree is disabled when not sneaking", "Felling allowed when not sneaking");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.IRON_BOOTS);
        }
    };
    public static Option<HashSet<String>> WORLDS = new Option<HashSet<String>>("Worlds", true, true, true, null){
        @Override
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe, GameMode gamemode, boolean sneaking, boolean dropItems){
            if(toolValues.containsKey(tool)){
                HashSet<String> worlds = toolValues.get(tool);
                boolean blacklist = Objects.equals(WORLD_BLACKLIST.getValue(tool), true);
                for(String world : worlds){
                    if(world.equalsIgnoreCase(block.getWorld().getName())||world.equalsIgnoreCase(block.getWorld().getUID().toString())){
                        if(blacklist){
                            return new DebugResult(this, TOOL, block.getWorld().getName()+" ("+block.getWorld().getUID().toString()+")");
                        }else{
                            break;
                        }
                    }
                }
            }
            if(treeValues.containsKey(tree)){
                HashSet<String> worlds = treeValues.get(tree);
                boolean blacklist = Objects.equals(WORLD_BLACKLIST.getValue(tree), true);
                for(String world : worlds){
                    if(world.equalsIgnoreCase(block.getWorld().getName())||world.equalsIgnoreCase(block.getWorld().getUID().toString())){
                        if(blacklist){
                            return new DebugResult(this, TREE, block.getWorld().getName()+" ("+block.getWorld().getUID().toString()+")");
                        }else{
                            break;
                        }
                    }
                }
                
            }
            if(globalValue!=null){
                boolean blacklist = Objects.equals(WORLD_BLACKLIST.globalValue, true);
                for(String world : globalValue){
                    if(world.equalsIgnoreCase(block.getWorld().getName())||world.equalsIgnoreCase(block.getWorld().getUID().toString())){
                        if(blacklist){
                            return new DebugResult(this, GLOBAL, block.getWorld().getName()+" ("+block.getWorld().getUID().toString()+")");
                        }else{
                            break;
                        }
                    }
                }
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
        public String getDesc(){
            return "In what worlds should the tree feller work? (Inverted if world-blacklist is set to true)";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("World {0} is invalid$", "World {0} is valid");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
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
        public String getDesc(){
            return null;
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
            return new ItemBuilder(Material.GRASS_BLOCK);
        }
    };
    public static Option<Integer> COOLDOWN = new Option<Integer>("Cooldown", true, true, true, null){
        @Override
        public Integer load(Object o){
            return loadInt(o);
        }
        @Override
        public DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe, GameMode gamemode, boolean sneaking, boolean dropItems){
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
        public String getDesc(){
            return "How long (in ticks) should players have to wait before felling another tree?";
        }
        @Override
        public String[] getDebugText(){
            return generateDebugText("Cooldown remaining: {0}ms", "Tool cooldown remaining: {0}ms", "Tree cooldown remaining: {0}ms", "Cooldown ready");
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(){
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
    public ArrayList<String> getDescription(){
        ArrayList<String> description = new ArrayList<>();
        String s = getDesc();
        if(s==null)return description;
        if(s.contains("\n")){
            for(String str : s.split("\n")){
                description.add(str);
            }
        }else description.add(s);
        return description;
    }
    public abstract String getDesc();
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
    public DebugResult check(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe, GameMode gamemode, boolean sneaking, boolean dropItems){
        if(globalValue==null&&!treeValues.containsKey(tree)&&!toolValues.containsKey(tool))return null;
        return doCheck(plugin, tool, tree, block, player, axe, gamemode, sneaking, dropItems);
    }
    public DebugResult checkTrunk(TreeFeller plugin, Tool tool, Tree tree, HashMap<Integer, ArrayList<Block>> blocks, Block block){
        if(globalValue==null&&!treeValues.containsKey(tree)&&!toolValues.containsKey(tool))return null;
        return doCheckTrunk(plugin, tool, tree, blocks, block);
    }
    public DebugResult checkTree(TreeFeller plugin, Tool tool, Tree tree, HashMap<Integer, ArrayList<Block>> blocks, int leaves){
        if(globalValue==null&&!treeValues.containsKey(tree)&&!toolValues.containsKey(tool))return null;
        return doCheckTree(plugin, tool, tree, blocks, leaves);
    }
    protected DebugResult doCheck(TreeFeller plugin, Tool tool, Tree tree, Block block, Player player, ItemStack axe, GameMode gamemode, boolean sneaking, boolean dropItems){
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
            String s = "";
            for(Object o : (HashSet)defaultConfigValue){
                s+="\n    - "+o;
            }
            return s;
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
    private static String[] generateDebugText(String globalWith$, String success){
        return new String[]{globalWith$.replace("$", ""),globalWith$.replace("$", " for tool"),globalWith$.replace("$", " for tree"),success};
    }
    private static String[] generateDebugText(String global, String tool, String tree, String success){
        return new String[]{global,tool,tree,success};
    }
    public abstract ItemBuilder getConfigurationDisplayItem();
    public abstract void openGlobalModifyMenu(MenuGlobalConfiguration parent);
    public abstract void openToolModifyMenu(MenuToolConfiguration parent, Tool tool);
    public abstract void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree);
}