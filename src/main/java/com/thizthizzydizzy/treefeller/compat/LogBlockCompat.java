package com.thizthizzydizzy.treefeller.compat;

import com.thizthizzydizzy.treefeller.Modifier;
import com.thizthizzydizzy.treefeller.Tool;
import com.thizthizzydizzy.treefeller.Tree;
import de.diddiz.LogBlock.Actor;
import de.diddiz.LogBlock.LogBlock;
import de.diddiz.util.LoggingUtil;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class LogBlockCompat extends InternalCompatibility {
    @Override
    public String getPluginName() {
        return "LogBlock";
    }

    @Override
    public void addBlock(Player player, Block block, BlockState was) {
        LoggingUtil.smartLogBlockPlace(LogBlock.getInstance().getConsumer(),
                Actor.actorFromEntity(player), was, block.getState());
    }

    @Override
    public void removeBlock(Player player, Block block) {
        LoggingUtil.smartLogBlockBreak(LogBlock.getInstance().getConsumer(),
                Actor.actorFromEntity(player), block);
    }

    @Override
    public void breakBlock(Tree tree, Tool tool, Player player, ItemStack axe, Block block, List<Modifier> modifiers) {
        LoggingUtil.smartLogBlockBreak(LogBlock.getInstance().getConsumer(),
                Actor.actorFromEntity(player), block);
    }
}