package com.thizthizzydizzy.treefeller.compat;
import com.thizthizzydizzy.treefeller.Modifier;
import com.thizthizzydizzy.treefeller.Tool;
import com.thizthizzydizzy.treefeller.Tree;
import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
public class JobsRebornCompat extends InternalCompatibility{
    @Override
    public String getPluginName(){
        return "Jobs";
    }
    @Override
    public String getFriendlyName(){
        return "Jobs Reborn";
    }
    @Override
    public void breakBlock(Tree tree, Tool tool, Player player, ItemStack axe, Block block, List<Modifier> modifiers){
        JobsRebornCompat2.breakBlock(player, block);
    }
}