package com.thizthizzydizzy.treefeller;
import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Material;
import org.bukkit.block.Block;
public class DetectedTree{
    public final HashMap<Integer, ArrayList<Block>> trunk;
    public final HashMap<Integer, ArrayList<Block>> leaves;
    public ArrayList<Block> stump = new ArrayList<>();
    public ArrayList<Sapling> saplings = new ArrayList<>();
    public DetectedTree(HashMap<Integer, ArrayList<Block>> trunk, HashMap<Integer, ArrayList<Block>> leaves){
        this.trunk = trunk;
        this.leaves = leaves;
    }
    public void addSapling(Block block, Material sapling, boolean autofill){
        saplings.add(new Sapling(block, sapling, autofill));
    }
}