package com.thizthizzydizzy.treefeller.compat;
import com.thizthizzydizzy.treefeller.Modifier;
import com.thizthizzydizzy.treefeller.Tool;
import com.thizthizzydizzy.treefeller.Tree;
import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
public class CoreProtectCompat extends InternalCompatibility{
    @Override
    public String getPluginName(){
        return "CoreProtect";
    }
    @Override
    public void addBlock(Player player, Block block, BlockState was){
        net.coreprotect.CoreProtect.getInstance().getAPI().logPlacement(player==null?null:player.getName(), block.getLocation(), block.getType(), block.getBlockData());
    }
    @Override
    public void removeBlock(Player player, Block block){
        net.coreprotect.CoreProtect.getInstance().getAPI().logRemoval(player==null?null:player.getName(), block.getLocation(), block.getType(), block.getBlockData());
    }
    @Override
    public void breakBlock(Tree tree, Tool tool, Player player, ItemStack axe, Block block, List<Modifier> modifiers){
        net.coreprotect.CoreProtect.getInstance().getAPI().logRemoval(player==null?null:player.getName(), block.getLocation(), block.getType(), block.getBlockData());
    }
}