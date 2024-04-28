package com.thizthizzydizzy.treefeller.compat;
import com.thizthizzydizzy.treefeller.Modifier;
import com.thizthizzydizzy.treefeller.Tool;
import com.thizthizzydizzy.treefeller.Tree;
import com.thizthizzydizzy.treefeller.TreeFeller;
import java.util.List;
import me.botsko.prism.Prism;
import me.botsko.prism.actionlibs.ActionFactory;
import me.botsko.prism.listeners.PrismBlockEvents;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
public class PrismCompat extends InternalCompatibility{
    private Prism prism;
    private PrismBlockEvents prismBlockEvents;
    @Override
    public String getPluginName(){
        return "Prism";
    }
    @Override
    public void init(TreeFeller treeFeller){
        prism = (Prism)getPlugin();
        prismBlockEvents = new PrismBlockEvents(prism);
    }
    @Override
    public void addBlock(Player player, Block block, BlockState was){
        prismBlockEvents.onBlockPlace(new BlockPlaceEvent(block, was, block, new ItemStack(block.getType(), 1), player, true));
    }
    @Override
    public void breakBlock(Tree tree, Tool tool, Player player, ItemStack axe, Block block, List<Modifier> modifiers){
        prismBlockEvents.onBlockBreak(new BlockBreakEvent(block, player));
    }
    @Override
    public void dropItem(Player player, Item item){
        ActionFactory.createItemStack("item-drop", item.getItemStack(), item.getItemStack().getAmount(), -1, item.getItemStack().getEnchantments(), item.getLocation(), "Environment");
    }
}