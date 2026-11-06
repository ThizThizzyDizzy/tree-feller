package com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.coreprotect;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.compatibility.BukkitPluginCompatibility;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.TreeFellerBukkit;
import com.thizthizzydizzy.treefeller.core.compatibility.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
public class CoreProtectCompat extends BukkitPluginCompatibility{
    public CoreProtectCompat(TreeFellerBukkit plugin){super(plugin);}
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
    public double breakBlock(CompatibilityContext context, Player player, ItemStack axe, Block block){
        net.coreprotect.CoreProtect.getInstance().getAPI().logRemoval(player==null?null:player.getName(), block.getLocation(), block.getType(), block.getBlockData());

        return 1;
    }
}
