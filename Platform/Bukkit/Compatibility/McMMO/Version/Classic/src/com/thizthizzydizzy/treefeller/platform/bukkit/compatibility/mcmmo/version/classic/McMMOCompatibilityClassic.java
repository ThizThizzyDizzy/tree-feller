package com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.mcmmo.version.classic;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.compatibility.BukkitPluginCompatibility;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.TreeFellerBukkit;
import com.thizthizzydizzy.treefeller.core.compatibility.*;
import java.io.File;
import java.util.HashMap;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
public class McMMOCompatibilityClassic extends BukkitPluginCompatibility{
    public McMMOCompatibilityClassic(TreeFellerBukkit plugin){super(plugin);}
    private boolean installed;
    private HashMap<Material, Integer> exp = new HashMap<>();
    @Override protected String requiredClass(){return "com.gmail.nossr50.api.ExperienceAPI";}
    @Override protected String excludedClass(){return "com.gmail.nossr50.datatypes.skills.SubSkillType";}
    @Override
    public String getPluginName(){
        return "mcMMO";
    }
    @Override
    public String getCompatibilityName() {
        return "mcMMO Classic";
    }
    @Override
    public void reload(){
        super.reload();
        try{
            File file = new File("plugins/mcMMO/experience.yml");
            ConfigurationSection woodExp = YamlConfiguration.loadConfiguration(file).getConfigurationSection("Experience").getConfigurationSection("Woodcutting");
            for(String key : woodExp.getKeys(false)){
                exp.put(Material.matchMaterial(key), ((Number)woodExp.get(key)).intValue());
            }
            installed = true;
        }catch(Exception ex){
            installed = false;
        }
    }
    @Override
    public double breakBlock(CompatibilityContext context, Player player, ItemStack axe, Block block){
        if(!installed||player==null)return 1;
        if(!exp.containsKey(block.getType()))return 1;
        com.gmail.nossr50.api.ExperienceAPI.addXP(player, "Woodcutting", exp.get(block.getType()));

        return 1;
    }
}
