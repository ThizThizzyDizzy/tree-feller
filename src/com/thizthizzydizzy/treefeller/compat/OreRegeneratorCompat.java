package com.thizthizzydizzy.treefeller.compat;
import com.thizthizzydizzy.simplegui.ItemBuilder;
import com.thizthizzydizzy.treefeller.Modifier;
import com.thizthizzydizzy.treefeller.Option;
import com.thizthizzydizzy.treefeller.Tool;
import com.thizthizzydizzy.treefeller.Tree;
import com.thizthizzydizzy.treefeller.menu.MenuGlobalConfiguration;
import com.thizthizzydizzy.treefeller.menu.modify.MenuModifyInteger;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
public class OreRegeneratorCompat extends InternalCompatibility{
    public static Option<Integer> OREREGENERATOR_REGEN_DELAY = new Option<Integer>("OreRegenerator Regen Delay", true, false, true, 1200, 1200){
        @Override
        public String getDesc(){
            return "The delay before trees should regenerate";
        }
        @Override
        public Integer load(Object o){
            return loadInt(o);
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
    };
    @Override
    public String getPluginName(){
        return "OreRegenerator";
    }
    @Override
    public void breakBlock(Tree tree, Tool tool, Player player, ItemStack axe, Block block, List<Modifier> modifiers){
        dev.mrshawn.oreregenerator.api.utils.RegenUtils.doRegen(block.getLocation(), block.getType(), block.getBlockData(), OREREGENERATOR_REGEN_DELAY.get(tool, tree));
    }
}