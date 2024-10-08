package com.thizthizzydizzy.treefeller.compat;
import dev.aurelium.auraskills.api.skill.Skill;
import com.thizthizzydizzy.simplegui.ItemBuilder;
import com.thizthizzydizzy.treefeller.Modifier;
import com.thizthizzydizzy.treefeller.Option;
import com.thizthizzydizzy.treefeller.OptionBoolean;
import com.thizthizzydizzy.treefeller.Tool;
import com.thizthizzydizzy.treefeller.Tree;
import com.thizthizzydizzy.treefeller.TreeFeller;
import com.thizthizzydizzy.treefeller.menu.MenuGlobalConfiguration;
import com.thizthizzydizzy.treefeller.menu.MenuToolConfiguration;
import com.thizthizzydizzy.treefeller.menu.MenuTreeConfiguration;
import com.thizthizzydizzy.treefeller.menu.modify.MenuModifyStringDoubleMap;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
public class AuraSkillsCompat extends InternalCompatibility{
    public static Option<HashMap<String, Double>> AURASKILLS_TRUNK_XP = new Option<HashMap<String, Double>>("AuraSkills Trunk XP", true, false, true, new HashMap<>(), "\n   - foraging: 1"){
        @Override
        public String getDesc(boolean ingame){
            return "EXP will be provided to these skills when a tree is felled\n"
                    + "EXP is provided per-block (a value of 1 means 1 EXP per block of trunk)"+(ingame?"":("\n"
                    + "ex:\n"
                    + "- foraging: 8"));
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
    public static Option<HashMap<String, Double>> AURASKILLS_LEAVES_XP = new Option<HashMap<String, Double>>("AuraSkills Leaves XP", true, false, true, new HashMap<>(), "\n   - foraging: 0"){
        @Override
        public String getDesc(boolean ingame){
            return "EXP will be provided to these skills when a tree is felled\n"
                    + "EXP is provided per-block (a value of 1 means 1 EXP per block of leaves)"+(ingame?"":("\n"
                    + "ex:\n"
                    + "- foraging: 8"));
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
    public static OptionBoolean AURASKILLS_APPLY_MODIFIERS = new OptionBoolean("AuraSkills Apply Modifiers", true, true, true, true){
        @Override
        public String getDesc(boolean ingame){
            return "Should AuraSkills modifiers be applied to experience earned through TreeFeller?";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Boolean value){
            return new ItemBuilder(Material.EXPERIENCE_BOTTLE);
        }
    };
    private BukkitTask pendingTask = null;
    private TreeFeller treefeller;
    private HashMap<Player, HashMap<dev.aurelium.auraskills.api.skill.Skill, Double>> modsMap = new HashMap<>();
    private HashMap<Player, HashMap<dev.aurelium.auraskills.api.skill.Skill, Double>> noModsMap = new HashMap<>();
    @Override
    public String getPluginName(){
        return "AuraSkills";
    }
    @Override
    public void init(TreeFeller treefeller){
        this.treefeller = treefeller;
    }
    @Override
    public void breakBlock(Tree tree, Tool tool, Player player, ItemStack axe, Block block, List<Modifier> modifiers){
        if(player==null)return;
        HashMap<String, Double> xp = null;
        if(tree.trunk.contains(block.getType())){
            xp = AURASKILLS_TRUNK_XP.get(tool, tree);
        }else if(tree.leaves.contains(block.getType())){
            xp = AURASKILLS_LEAVES_XP.get(tool, tree);
        }
        if(xp==null||xp.isEmpty())return;
        boolean applyMods = AURASKILLS_APPLY_MODIFIERS.get(tool, tree);
        for(String key : xp.keySet()){
            double amount = xp.get(key);
            dev.aurelium.auraskills.api.skill.Skill skill = dev.aurelium.auraskills.api.AuraSkillsApi.get().getGlobalRegistry().getSkill(NamespacedId.fromString(key));
            if(skill==null)continue;
            HashMap<Player, HashMap<dev.aurelium.auraskills.api.skill.Skill, Double>> map = null;
            if(applyMods){
                map = modsMap;
            }else{
                map = noModsMap;
            }
            if(!map.containsKey(player)){
                map.put(player, new HashMap<>());
            }
            HashMap<Skill, Double> mp = map.get(player);
            mp.put(skill, mp.getOrDefault(skill, 0d)+amount);
        }
        if(pendingTask==null)pendingTask = new BukkitRunnable() {
            @Override
            public void run(){
                pendingTask = null;
                for(Player p : noModsMap.keySet()){
                    HashMap<Skill, Double> map = noModsMap.get(p);
                    for(dev.aurelium.auraskills.api.skill.Skill skill : map.keySet()){
                        dev.aurelium.auraskills.api.AuraSkillsApi.get().getUser(p.getUniqueId()).addSkillXpRaw(skill, map.get(skill));
                    }
                }
                noModsMap.clear();
                for(Player p : modsMap.keySet()){
                    HashMap<Skill, Double> map = modsMap.get(p);
                    for(dev.aurelium.auraskills.api.skill.Skill skill : map.keySet()){
                        dev.aurelium.auraskills.api.AuraSkillsApi.get().getUser(p.getUniqueId()).addSkillXp(skill, map.get(skill));
                    }
                }
                modsMap.clear();
            }
        }.runTaskLater(treefeller, Option.CUTTING_ANIMATION.get(tool, tree)==true?10:1);
    }
}