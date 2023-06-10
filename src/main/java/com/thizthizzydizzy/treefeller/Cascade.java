package com.thizthizzydizzy.treefeller;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
public class Cascade{
    public final DetectedTree detectedTree;
    public final boolean dropItems;
    public final Tree tree;
    public final Tool tool;
    public final ItemStack axe;
    public final Block block;
    public final Player player;
    public Cascade(DetectedTree detectedTree, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Player player){
        this.detectedTree = detectedTree;
        this.dropItems = dropItems;
        this.tree = tree;
        this.tool = tool;
        this.axe = axe;
        this.block = block;
        this.player = player;
    }
}