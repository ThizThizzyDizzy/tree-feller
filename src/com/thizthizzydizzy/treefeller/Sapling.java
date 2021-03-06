package com.thizthizzydizzy.treefeller;
import java.util.ArrayList;
import java.util.HashSet;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
public class Sapling{
    public final boolean spawn;
    public final DetectedTree detectedTree;
    public final Block block;
    private final HashSet<Material> materials;
    private final int timeout;
    private int timer;
    private boolean placed = false;
    public Sapling(DetectedTree detectedTree, Block block, HashSet<Material> materials, boolean spawn, int timeout){
        this.detectedTree = detectedTree;
        this.block = block;
        this.materials = materials;
        this.spawn = spawn;
        this.timeout = timeout;
    }
    public boolean isDead(){
        if(materials.contains(block.getType()))placed = true;
        return placed||timer>timeout;
    }
    public void tick(){
        timer++;
        if(spawn&&timer==timeout)place(null);
    }
    public boolean canPlace(){
        if(isDead())return false;
        return block.getType()==Material.AIR;
    }
    public boolean place(Material material){
        if(!canPlace())return false;
        placed = true;
        if(material==null)material = new ArrayList<>(materials).get(0);
        block.setType(material);
        return true;
    }
    public boolean tryPlace(ItemStack stack){
        if(materials.contains(stack.getType())){
            if(place(stack.getType())){
                stack.setAmount(stack.getAmount()-1);
                return true;
            }
        }
        return false;
    }
}