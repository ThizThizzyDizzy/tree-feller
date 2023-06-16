package com.thizthizzydizzy.treefeller.compat;
import com.thizthizzydizzy.treefeller.Modifier;
import com.thizthizzydizzy.treefeller.Tool;
import com.thizthizzydizzy.treefeller.Tree;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
public class McMMOClassicCompat extends InternalCompatibility{
    private boolean installed;
    private HashMap<Material, Integer> exp = new HashMap<>();
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
    public void breakBlock(Tree tree, Tool tool, Player player, ItemStack axe, Block block, List<Modifier> modifiers){
        if(!installed||player==null)return;
        if(!exp.containsKey(block.getType()))return;
        com.gmail.nossr50.api.ExperienceAPI.addXP(player, "Woodcutting", exp.get(block.getType()));
    }
}