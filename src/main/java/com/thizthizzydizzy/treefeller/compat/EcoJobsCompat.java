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
import com.thizthizzydizzy.treefeller.menu.modify.MenuModifyStringDoubleMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
public class EcoJobsCompat extends InternalCompatibility{
    public static Option<HashMap<String, Double>> ECOJOBS_TRUNK_XP = new Option<HashMap<String, Double>>("EcoJobs Trunk XP", true, true, true, null){
        @Override
        public HashMap<String, Double> load(Object o){
            if(o instanceof MemorySection){
                HashMap<String, Double> jobs = new HashMap<>();
                MemorySection m = (MemorySection)o;
                for(String key : m.getKeys(false)){
                    String job = key;
                    if(job==null)continue;
                    Double xp = Option.loadDouble(m.get(key));
                    if(xp==null)continue;
                    if(jobs.containsKey(job)){
                        jobs.put(job, jobs.get(job)+xp);
                    }else{
                        jobs.put(job, xp);
                    }
                }
                return jobs;
            }
            if(o instanceof Map){
                HashMap<String, Double> jobs = new HashMap<>();
                Map m = (Map)o;
                for(Object obj : m.keySet()){
                    String job = null;
                    if(obj instanceof String){
                        job = (String)obj;
                    }
                    if(job==null)continue;
                    Double xp = Option.loadDouble(m.get(obj));
                    if(xp==null)continue;
                    if(jobs.containsKey(job)){
                        jobs.put(job, jobs.get(job)+xp);
                    }else{
                        jobs.put(job, xp);
                    }
                }
                return jobs;
            }
            if(o instanceof List){
                List l = (List)o;
                HashMap<String, Double> jobs = new HashMap<>();
                for(Object lbj : l){
                    if(lbj instanceof Map){
                        Map m = (Map)lbj;
                        for(Object obj : m.keySet()){
                            String job = null;
                            if(obj instanceof String){
                                job = (String)obj;
                            }
                            if(job==null)continue;
                            Double xp = Option.loadDouble(m.get(obj));
                            if(xp==null)continue;
                            if(jobs.containsKey(job)){
                                jobs.put(job, jobs.get(job)+xp);
                            }else{
                                jobs.put(job, xp);
                            }
                        }
                    }
                }
                return jobs;
            }
            return null;
        }
        @Override
        public String getDesc(boolean ingame){
            return "How much Job experience should be given per block of trunk felled?";
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
        public HashMap<String, Double> get(Tool tool, Tree tree){
            HashMap<String, Double> val = new HashMap<>();
            if(globalValue!=null)val.putAll(globalValue);
            HashMap<String, Double> vals = toolValues.get(tool);
            if(vals!=null)for(String job : vals.keySet())val.put(job, val.getOrDefault(job, 0d)+vals.get(job));
            vals = treeValues.get(tree);
            if(vals!=null)for(String job : vals.keySet())val.put(job, val.getOrDefault(job, 0d)+vals.get(job));
            return val;
        }
    };
    public static Option<HashMap<String, Double>> ECOJOBS_LEAF_XP = new Option<HashMap<String, Double>>("EcoJobs Leaf XP", true, true, true, null){
        @Override
        public HashMap<String, Double> load(Object o){
            if(o instanceof MemorySection){
                HashMap<String, Double> jobs = new HashMap<>();
                MemorySection m = (MemorySection)o;
                for(String key : m.getKeys(false)){
                    String job = key;
                    if(job==null)continue;
                    Double xp = Option.loadDouble(m.get(key));
                    if(xp==null)continue;
                    if(jobs.containsKey(job)){
                        jobs.put(job, jobs.get(job)+xp);
                    }else{
                        jobs.put(job, xp);
                    }
                }
                return jobs;
            }
            if(o instanceof Map){
                HashMap<String, Double> jobs = new HashMap<>();
                Map m = (Map)o;
                for(Object obj : m.keySet()){
                    String job = null;
                    if(obj instanceof String){
                        job = (String)obj;
                    }
                    if(job==null)continue;
                    Double xp = Option.loadDouble(m.get(obj));
                    if(xp==null)continue;
                    if(jobs.containsKey(job)){
                        jobs.put(job, jobs.get(job)+xp);
                    }else{
                        jobs.put(job, xp);
                    }
                }
                return jobs;
            }
            if(o instanceof List){
                List l = (List)o;
                HashMap<String, Double> jobs = new HashMap<>();
                for(Object lbj : l){
                    if(lbj instanceof Map){
                        Map m = (Map)lbj;
                        for(Object obj : m.keySet()){
                            String job = null;
                            if(obj instanceof String){
                                job = (String)obj;
                            }
                            if(job==null)continue;
                            Double xp = Option.loadDouble(m.get(obj));
                            if(xp==null)continue;
                            if(jobs.containsKey(job)){
                                jobs.put(job, jobs.get(job)+xp);
                            }else{
                                jobs.put(job, xp);
                            }
                        }
                    }
                }
                return jobs;
            }
            return null;
        }
        @Override
        public String getDesc(boolean ingame){
            return "How much Job experience should be given per block of leaves felled?";
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
        public HashMap<String, Double> get(Tool tool, Tree tree){
            HashMap<String, Double> val = new HashMap<>();
            if(globalValue!=null)val.putAll(globalValue);
            HashMap<String, Double> vals = toolValues.get(tool);
            if(vals!=null)for(String job : vals.keySet())val.put(job, val.getOrDefault(job, 0d)+vals.get(job));
            vals = treeValues.get(tree);
            if(vals!=null)for(String job : vals.keySet())val.put(job, val.getOrDefault(job, 0d)+vals.get(job));
            return val;
        }
    };
    public static OptionBoolean ECOJOBS_APPLY_MULTIPLIERS = new OptionBoolean("EcoJobs Apply Multipliers", true, true, true, true){
        @Override
        public String getDesc(boolean ingame){
            return "Should EcoJobs multipliers be applied to experience earned through TreeFeller?";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Boolean value){
            return new ItemBuilder(Material.GLISTERING_MELON_SLICE);
        }
    };
    @Override
    public String getPluginName(){
        return "EcoJobs";
    }
    @Override
    public void breakBlock(Tree tree, Tool tool, Player player, ItemStack axe, Block block, List<Modifier> modifiers){
        if(player==null)return;
        
        HashMap<com.willfp.ecojobs.jobs.Job, Double> exp = new HashMap<>();
        if(tree.trunk.contains(block.getType())){
            HashMap<String, Double> trunk = ECOJOBS_TRUNK_XP.get(tool, tree);
            for(String key : trunk.keySet()){
                com.willfp.ecojobs.jobs.Job job = com.willfp.ecojobs.jobs.Jobs.getByID(key);
                exp.put(job, exp.getOrDefault(job, 0d)+trunk.get(key));
            }
        }else if(tree.leaves.contains(block.getType())){
            HashMap<String, Double> leaves = ECOJOBS_LEAF_XP.get(tool, tree);
            for(String key : leaves.keySet()){
                com.willfp.ecojobs.jobs.Job job = com.willfp.ecojobs.jobs.Jobs.getByID(key);
                exp.put(job, exp.getOrDefault(job, 0d)+leaves.get(key));
            }
        }
        boolean applyMultipliers = ECOJOBS_APPLY_MULTIPLIERS.get(tool, tree);
        com.willfp.ecojobs.api.EcoJobsAPI api = com.willfp.ecojobs.api.EcoJobsAPI.instance;
        for(com.willfp.ecojobs.jobs.Job job : exp.keySet()){
            if(exp.get(job)!=0)api.giveJobExperience(player, job, exp.get(job), applyMultipliers);
        }
    }
}
