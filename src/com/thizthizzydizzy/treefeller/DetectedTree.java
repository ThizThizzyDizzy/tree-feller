package com.thizthizzydizzy.treefeller;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
public class DetectedTree{
    public final Tool tool;
    public final Tree tree;
    public final HashMap<Integer, ArrayList<Block>> trunk;
    public final HashMap<Integer, ArrayList<Block>> leaves;
    public ArrayList<Block> stump = new ArrayList<>();
    public ArrayList<Sapling> saplings = new ArrayList<>();
    public final HashMap<Block, ArrayList<Block>> decorations;
    public DetectedTree(Tool tool, Tree tree, HashMap<Integer, ArrayList<Block>> trunk, HashMap<Integer, ArrayList<Block>> leaves, HashMap<Block, ArrayList<Block>> decorations){
        this.tool = tool;
        this.tree = tree;
        this.trunk = trunk;
        this.leaves = leaves;
        this.decorations = decorations;
    }
    public void addSapling(TreeFeller treefeller, Player player, Block block, HashSet<Material> saplings){
        this.saplings.add(new Sapling(treefeller, this, player, block, saplings, Option.SPAWN_SAPLINGS.get(tool, tree)==2, Option.SAPLING_TIMEOUT.get(tool, tree)));
    }
    public Iterable<Block> getDecorations(Block b){
        return decorations.getOrDefault(b, new ArrayList<>());
    }
}