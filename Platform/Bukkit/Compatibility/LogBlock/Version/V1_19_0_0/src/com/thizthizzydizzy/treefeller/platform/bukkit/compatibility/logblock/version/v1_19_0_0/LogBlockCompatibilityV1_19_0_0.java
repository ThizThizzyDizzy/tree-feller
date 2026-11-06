package com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.logblock.version.v1_19_0_0;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.compatibility.BukkitPluginCompatibility;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.TreeFellerBukkit;
import com.thizthizzydizzy.treefeller.core.compatibility.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
public class LogBlockCompatibilityV1_19_0_0 extends BukkitPluginCompatibility{
    public LogBlockCompatibilityV1_19_0_0(TreeFellerBukkit plugin){super(plugin);}
    @Override protected String requiredClass(){return "de.diddiz.LogBlock.util.LoggingUtil";}
    @Override
    public String getFriendlyName(){
        return "LogBlock (1.19)";
    }
    @Override
    public String getPluginName(){
        return "LogBlock";
    }
    @Override
    public void addBlock(Player player, Block block, BlockState was){
        de.diddiz.LogBlock.util.LoggingUtil.smartLogBlockPlace(de.diddiz.LogBlock.LogBlock.getInstance().getConsumer(), de.diddiz.LogBlock.Actor.actorFromEntity(player), was, block.getState());
    }
    @Override
    public void removeBlock(Player player, Block block){
        de.diddiz.LogBlock.util.LoggingUtil.smartLogBlockBreak(de.diddiz.LogBlock.LogBlock.getInstance().getConsumer(), de.diddiz.LogBlock.Actor.actorFromEntity(player), block);
    }
    @Override
    public double breakBlock(CompatibilityContext context, Player player, ItemStack axe, Block block){
        de.diddiz.LogBlock.util.LoggingUtil.smartLogBlockBreak(de.diddiz.LogBlock.LogBlock.getInstance().getConsumer(), de.diddiz.LogBlock.Actor.actorFromEntity(player), block);

        return 1;
    }
}
