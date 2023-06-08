package com.thizthizzydizzy.treefeller.compat;
import com.thizthizzydizzy.simplegui.ItemBuilder;
import com.thizthizzydizzy.treefeller.Modifier;
import com.thizthizzydizzy.treefeller.Option;
import com.thizthizzydizzy.treefeller.OptionBoolean;
import com.thizthizzydizzy.treefeller.Tool;
import com.thizthizzydizzy.treefeller.Tree;
import com.thizthizzydizzy.treefeller.menu.MenuGlobalConfiguration;
import com.thizthizzydizzy.treefeller.menu.MenuToolConfiguration;
import com.thizthizzydizzy.treefeller.menu.MenuTreeConfiguration;
import com.thizthizzydizzy.treefeller.menu.modify.MenuModifyDouble;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
public class EcoSkillsCompat extends InternalCompatibility{
    public static Option<Double> ECOSKILLS_TRUNK_WOODCUTTING_XP = new Option<Double>("EcoSkills Trunk Woodcutting XP", true, true, true, 1d){
        @Override
        public Double load(Object o){
            return loadDouble(o);
        }
        @Override
        public Double get(Tool tool, Tree tree){
            double d = globalValue;
            if(toolValues.containsKey(tool))d+= toolValues.get(tool);
            if(treeValues.containsKey(tree))d+= treeValues.get(tree);
            return d;
        }
        @Override
        public String getDesc(boolean ingame){
            return "How much Woodcutting experience should be given per block of trunk felled?";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Double value){
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
    public static Option<Double> ECOSKILLS_TRUNK_MINING_XP = new Option<Double>("EcoSkills Trunk Mining XP", true, true, true, 0d){
        @Override
        public Double load(Object o){
            return loadDouble(o);
        }
        @Override
        public Double get(Tool tool, Tree tree){
            double d = globalValue;
            if(toolValues.containsKey(tool))d+= toolValues.get(tool);
            if(treeValues.containsKey(tree))d+= treeValues.get(tree);
            return d;
        }
        @Override
        public String getDesc(boolean ingame){
            return "How much Mining experience should be given per block of trunk felled?";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Double value){
            return new ItemBuilder(Material.GOLD_ORE);
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
    public static Option<Double> ECOSKILLS_LEAF_WOODCUTTING_XP = new Option<Double>("EcoSkills Leaf Woodcutting XP", true, true, true, 0d){
        @Override
        public Double load(Object o){
            return loadDouble(o);
        }
        @Override
        public Double get(Tool tool, Tree tree){
            double d = globalValue;
            if(toolValues.containsKey(tool))d+= toolValues.get(tool);
            if(treeValues.containsKey(tree))d+= treeValues.get(tree);
            return d;
        }
        @Override
        public String getDesc(boolean ingame){
            return "How much Woodcutting experience should be given per block of leaves felled?";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Double value){
            return new ItemBuilder(Material.OAK_LEAVES);
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
    public static Option<Double> ECOSKILLS_LEAF_MINING_XP = new Option<Double>("EcoSkills Leaf Mining XP", true, true, true, 0d){
        @Override
        public Double load(Object o){
            return loadDouble(o);
        }
        @Override
        public Double get(Tool tool, Tree tree){
            double d = globalValue;
            if(toolValues.containsKey(tool))d+= toolValues.get(tool);
            if(treeValues.containsKey(tree))d+= treeValues.get(tree);
            return d;
        }
        @Override
        public String getDesc(boolean ingame){
            return "How much Mining experience should be given per block of leaves felled?";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Double value){
            return new ItemBuilder(Material.IRON_ORE);
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
    public static OptionBoolean ECOSKILLS_APPLY_MODIFIERS = new OptionBoolean("EcoSkills Apply Modifiers", true, true, true, true){
        @Override
        public String getDesc(boolean ingame){
            return "Should EcoSkills modifiers be applied to experience earned through TreeFeller?";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Boolean value){
            return new ItemBuilder(Material.EXPERIENCE_BOTTLE);
        }
    };
    @Override
    public String getPluginName(){
        return "EcoSkills";
    }
    @Override
    public void breakBlock(Tree tree, Tool tool, Player player, ItemStack axe, Block block, List<Modifier> modifiers){
        if(player==null)return;
        double woodcutting = 0, mining = 0;
        if(tree.trunk.contains(block.getType())){
            woodcutting+=ECOSKILLS_TRUNK_WOODCUTTING_XP.get(tool, tree);
            mining+=ECOSKILLS_TRUNK_MINING_XP.get(tool, tree);
        }else if(tree.leaves.contains(block.getType())){
            woodcutting+=ECOSKILLS_LEAF_WOODCUTTING_XP.get(tool, tree);
            mining+=ECOSKILLS_LEAF_MINING_XP.get(tool, tree);
        }
        if(mining==0&&woodcutting==0)return;
        boolean applyModifiers = ECOSKILLS_APPLY_MODIFIERS.get(tool, tree);
        com.willfp.ecoskills.api.EcoSkillsAPI api = com.willfp.ecoskills.api.EcoSkillsAPI.getInstance();
        if(woodcutting!=0)api.giveSkillExperience(player, com.willfp.ecoskills.skills.Skills.WOODCUTTING, woodcutting, applyModifiers);
        if(mining!=0)api.giveSkillExperience(player, com.willfp.ecoskills.skills.Skills.MINING, mining, applyModifiers);
    }
}