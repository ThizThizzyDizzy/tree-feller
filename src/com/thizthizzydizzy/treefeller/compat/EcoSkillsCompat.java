package com.thizthizzydizzy.treefeller.compat;
import com.thizthizzydizzy.simplegui.ItemBuilder;
import com.thizthizzydizzy.treefeller.Modifier;
import com.thizthizzydizzy.treefeller.Option;
import com.thizthizzydizzy.treefeller.Tool;
import com.thizthizzydizzy.treefeller.Tree;
import com.thizthizzydizzy.treefeller.menu.MenuGlobalConfiguration;
import com.thizthizzydizzy.treefeller.menu.MenuToolConfiguration;
import com.thizthizzydizzy.treefeller.menu.MenuTreeConfiguration;
import com.thizthizzydizzy.treefeller.menu.modify.MenuModifyStringDoubleMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
public class EcoSkillsCompat extends InternalCompatibility{
    public static Option<HashMap<String, Double>> ECOSKILLS_TRUNK_XP = new Option<HashMap<String, Double>>("EcoSkills Trunk XP", true, false, true, new HashMap<>(), "\n   - Woodcutting: 1"){
        @Override
        public String getDesc(boolean ingame){
            return "EXP will be provided to these skills when a tree is felled\n"
                    + "EXP is provided per-block (a value of 1 means 1 EXP per block of trunk)";
        }
        @Override
        public HashMap<String, Double> load(Object o){
            if(o instanceof MemorySection){
                HashMap<String, Double> skills = new HashMap<>();
                MemorySection m = (MemorySection)o;
                for(String key : m.getKeys(false)){
                    String skill = key;
                    if(skill==null)continue;
                    Double xp = Option.loadDouble(m.get(key));
                    if(xp==null)continue;
                    if(skills.containsKey(skill)){
                        skills.put(skill, skills.get(skill)+xp);
                    }else{
                        skills.put(skill, xp);
                    }
                }
                return skills;
            }
            if(o instanceof Map){
                HashMap<String, Double> skills = new HashMap<>();
                Map m = (Map)o;
                for(Object obj : m.keySet()){
                    String skill = null;
                    if(obj instanceof String){
                        skill = (String)obj;
                    }
                    if(skill==null)continue;
                    Double xp = Option.loadDouble(m.get(obj));
                    if(xp==null)continue;
                    if(skills.containsKey(skill)){
                        skills.put(skill, skills.get(skill)+xp);
                    }else{
                        skills.put(skill, xp);
                    }
                }
                return skills;
            }
            if(o instanceof List){
                List l = (List)o;
                HashMap<String, Double> skills = new HashMap<>();
                for(Object lbj : l){
                    if(lbj instanceof Map){
                        Map m = (Map)lbj;
                        for(Object obj : m.keySet()){
                            String skill = null;
                            if(obj instanceof String){
                                skill = (String)obj;
                            }
                            if(skill==null)continue;
                            Double xp = Option.loadDouble(m.get(obj));
                            if(xp==null)continue;
                            if(skills.containsKey(skill)){
                                skills.put(skill, skills.get(skill)+xp);
                            }else{
                                skills.put(skill, xp);
                            }
                        }
                    }
                }
                return skills;
            }
            return null;
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(HashMap<String, Double> value){
            return new ItemBuilder(Material.OAK_LOG);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyStringDoubleMap(parent, parent.plugin, parent.player, name, 0, Double.MAX_VALUE, false, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyStringDoubleMap(parent, parent.plugin, parent.player, name, 0, Double.MAX_VALUE, true, false, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyStringDoubleMap(parent, parent.plugin, parent.player, name, 0, Double.MAX_VALUE, true, false, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
        @Override
        public String writeToConfig(HashMap<String, Double> value){
            if(value==null)return "";
            String s = "{";
            String str = "";
            for(String st : value.keySet()){
                str+=", "+st+": "+value.get(st);
            }
            if(!str.isEmpty())s+=str.substring(2);
            return s+"}";
        }
    };
    public static Option<HashMap<String, Double>> ECOSKILLS_LEAVES_XP = new Option<HashMap<String, Double>>("EcoSkills Leaves XP", true, false, true, new HashMap<>(), "\n   - woodcutting: 0"){
        @Override
        public String getDesc(boolean ingame){
            return "EXP will be provided to these skills when a tree is felled\n"
                    + "EXP is provided per-block (a value of 1 means 1 EXP per block of leaves)\n"
                    + "use \"global\" to add global experience"+(ingame?"":("\n"
                    + "ex:\n"
                    + "- global: 3\n"
                    + "- woodcutting: 8"));
        }
        @Override
        public HashMap<String, Double> load(Object o){
            if(o instanceof MemorySection){
                HashMap<String, Double> skills = new HashMap<>();
                MemorySection m = (MemorySection)o;
                for(String key : m.getKeys(false)){
                    String skill = key;
                    if(skill==null)continue;
                    Double xp = Option.loadDouble(m.get(key));
                    if(xp==null)continue;
                    if(skills.containsKey(skill)){
                        skills.put(skill, skills.get(skill)+xp);
                    }else{
                        skills.put(skill, xp);
                    }
                }
                return skills;
            }
            if(o instanceof Map){
                HashMap<String, Double> skills = new HashMap<>();
                Map m = (Map)o;
                for(Object obj : m.keySet()){
                    String skill = null;
                    if(obj instanceof String){
                        skill = (String)obj;
                    }
                    if(skill==null)continue;
                    Double xp = Option.loadDouble(m.get(obj));
                    if(xp==null)continue;
                    if(skills.containsKey(skill)){
                        skills.put(skill, skills.get(skill)+xp);
                    }else{
                        skills.put(skill, xp);
                    }
                }
                return skills;
            }
            if(o instanceof List){
                List l = (List)o;
                HashMap<String, Double> skills = new HashMap<>();
                for(Object lbj : l){
                    if(lbj instanceof Map){
                        Map m = (Map)lbj;
                        for(Object obj : m.keySet()){
                            String skill = null;
                            if(obj instanceof String){
                                skill = (String)obj;
                            }
                            if(skill==null)continue;
                            Double xp = Option.loadDouble(m.get(obj));
                            if(xp==null)continue;
                            if(skills.containsKey(skill)){
                                skills.put(skill, skills.get(skill)+xp);
                            }else{
                                skills.put(skill, xp);
                            }
                        }
                    }
                }
                return skills;
            }
            return null;
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(HashMap<String, Double> value){
            return new ItemBuilder(Material.OAK_LEAVES);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyStringDoubleMap(parent, parent.plugin, parent.player, name, 0, Double.MAX_VALUE, false, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyStringDoubleMap(parent, parent.plugin, parent.player, name, 0, Double.MAX_VALUE, true, false, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyStringDoubleMap(parent, parent.plugin, parent.player, name, 0, Double.MAX_VALUE, true, false, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
        @Override
        public String writeToConfig(HashMap<String, Double> value){
            if(value==null)return "";
            String s = "{";
            String str = "";
            for(String st : value.keySet()){
                str+=", "+st+": "+value.get(st);
            }
            if(!str.isEmpty())s+=str.substring(2);
            return s+"}";
        }
    };
    public static Option<HashMap<String, Double>> ECOSKILLS_TREE_XP = new Option<HashMap<String, Double>>("EcoSkills Tree XP", true, false, true, new HashMap<>(), "\n   - woodcutting: 0"){
        @Override
        public String getDesc(boolean ingame){
            return "EXP will be provided to these skills when a tree is felled\n"
                    + "EXP is provided per-tree (a value of 1 means 1 EXP per tree)\n"
                    + "use \"global\" to add global experience"+(ingame?"":("\n"
                    + "ex:\n"
                    + "- global: 3\n"
                    + "- woodcutting: 8"));
        }
        @Override
        public HashMap<String, Double> load(Object o){
            if(o instanceof MemorySection){
                HashMap<String, Double> skills = new HashMap<>();
                MemorySection m = (MemorySection)o;
                for(String key : m.getKeys(false)){
                    String skill = key;
                    if(skill==null)continue;
                    Double xp = Option.loadDouble(m.get(key));
                    if(xp==null)continue;
                    if(skills.containsKey(skill)){
                        skills.put(skill, skills.get(skill)+xp);
                    }else{
                        skills.put(skill, xp);
                    }
                }
                return skills;
            }
            if(o instanceof Map){
                HashMap<String, Double> skills = new HashMap<>();
                Map m = (Map)o;
                for(Object obj : m.keySet()){
                    String skill = null;
                    if(obj instanceof String){
                        skill = (String)obj;
                    }
                    if(skill==null)continue;
                    Double xp = Option.loadDouble(m.get(obj));
                    if(xp==null)continue;
                    if(skills.containsKey(skill)){
                        skills.put(skill, skills.get(skill)+xp);
                    }else{
                        skills.put(skill, xp);
                    }
                }
                return skills;
            }
            if(o instanceof List){
                List l = (List)o;
                HashMap<String, Double> skills = new HashMap<>();
                for(Object lbj : l){
                    if(lbj instanceof Map){
                        Map m = (Map)lbj;
                        for(Object obj : m.keySet()){
                            String skill = null;
                            if(obj instanceof String){
                                skill = (String)obj;
                            }
                            if(skill==null)continue;
                            Double xp = Option.loadDouble(m.get(obj));
                            if(xp==null)continue;
                            if(skills.containsKey(skill)){
                                skills.put(skill, skills.get(skill)+xp);
                            }else{
                                skills.put(skill, xp);
                            }
                        }
                    }
                }
                return skills;
            }
            return null;
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(HashMap<String, Double> value){
            return new ItemBuilder(Material.JUNGLE_LOG);
        }
        @Override
        public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
            parent.open(new MenuModifyStringDoubleMap(parent, parent.plugin, parent.player, name, 0, Double.MAX_VALUE, false, false, globalValue, (value) -> {
                globalValue = value;
            }));
        }
        @Override
        public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
            parent.open(new MenuModifyStringDoubleMap(parent, parent.plugin, parent.player, name, 0, Double.MAX_VALUE, true, false, toolValues.get(tool), (value) -> {
                if(value==null)toolValues.remove(tool);
                else toolValues.put(tool, value);
            }));
        }
        @Override
        public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
            parent.open(new MenuModifyStringDoubleMap(parent, parent.plugin, parent.player, name, 0, Double.MAX_VALUE, true, false, treeValues.get(tree), (value) -> {
                if(value==null)treeValues.remove(tree);
                else treeValues.put(tree, value);
            }));
        }
        @Override
        public String writeToConfig(HashMap<String, Double> value){
            if(value==null)return "";
            String s = "{";
            String str = "";
            for(String st : value.keySet()){
                str+=", "+st+": "+value.get(st);
            }
            if(!str.isEmpty())s+=str.substring(2);
            return s+"}";
        }
    };
    @Override
    public String getPluginName(){
        return "EcoSkills";
    }
    @Override
    public void fellTree(Block block, Player player, ItemStack axe, Tool tool, Tree tree, HashMap<Integer, ArrayList<Block>> blocks){
        if(player==null)return;
        HashMap<String, Double> treeXp = ECOSKILLS_TREE_XP.get(tool, tree);
        if(treeXp==null||treeXp.isEmpty())return;
        for(String skill : treeXp.keySet()){
            com.willfp.ecoskills.api.EcoSkillsAPI.gainSkillXP(player, com.willfp.ecoskills.skills.Skills.INSTANCE.getByID(skill), treeXp.get(skill));
        }
    }
    @Override
    public void breakBlock(Tree tree, Tool tool, Player player, ItemStack axe, Block block, List<Modifier> modifiers){
        if(player==null)return;
        //Per-block trunk/leaf xp
        HashMap<String, Double> xp = null;
        if(tree.trunk.contains(block.getType())){
            xp = ECOSKILLS_TRUNK_XP.get(tool, tree);
        }else if(tree.leaves.contains(block.getType())){
            xp = ECOSKILLS_LEAVES_XP.get(tool, tree);
        }
        if(xp!=null&&!xp.isEmpty()){
            for(String skill : xp.keySet()){
                com.willfp.ecoskills.api.EcoSkillsAPI.gainSkillXP(player, com.willfp.ecoskills.skills.Skills.INSTANCE.getByID(skill), xp.get(skill));
            }
        }
    }
}