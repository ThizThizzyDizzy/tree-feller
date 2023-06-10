package com.thizthizzydizzy.treefeller.compat;
import com.thizthizzydizzy.simplegui.ItemBuilder;
import com.thizthizzydizzy.treefeller.Modifier;
import com.thizthizzydizzy.treefeller.Option;
import com.thizthizzydizzy.treefeller.Tool;
import com.thizthizzydizzy.treefeller.Tree;
import com.thizthizzydizzy.treefeller.menu.MenuGlobalConfiguration;
import com.thizthizzydizzy.treefeller.menu.MenuToolConfiguration;
import com.thizthizzydizzy.treefeller.menu.MenuTreeConfiguration;
import com.thizthizzydizzy.treefeller.menu.modify.MenuModifyInteger;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
public class OreRegeneratorCompat extends InternalCompatibility{
    public static Option<Integer> OREREGENERATOR_REGEN_DELAY = new Option<Integer>("OreRegenerator Regen Delay", true, false, true, 1200, 1200){
        @Override
        public String getDesc(boolean ingame){
            return "The delay before trees should regenerate";
        }
        @Override
        public Integer load(Object o){
            return loadInt(o);
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
    @Override
    public String getPluginName(){
        return "OreRegenerator";
    }
    @Override
    public void breakBlock(Tree tree, Tool tool, Player player, ItemStack axe, Block block, List<Modifier> modifiers){
        dev.mrshawn.oreregenerator.api.utils.RegenUtils.doRegen(block.getLocation(), block.getType(), block.getBlockData(), OREREGENERATOR_REGEN_DELAY.get(tool, tree));
    }
    @Override
    public boolean defaultEnabled(){
        return false;
    }
}