package com.thizthizzydizzy.treefeller;
import org.bukkit.Material;
import org.bukkit.block.Block;
public class Sapling{
    private static final float timeout = 2.5f;//seconds
    public final boolean autofill;
    public final Block block;
    private final Material material;
    private final long time;
    private boolean placed = false;
    public Sapling(Block block, Material material, boolean autofill){
        this(block, material, autofill, -1);
    }
    public Sapling(Block block, Material material, boolean autofill, long time){
        this.block = block;
        this.material = material;
        this.autofill = autofill;
        this.time = time;
    }
    public boolean isDead(){
        if(block.getType()==material)placed = true;
        return placed||System.currentTimeMillis()>time+timeout*1000;
    }
    public boolean canPlace(){
        if(isDead())return false;
        return block.getType()==Material.AIR;
    }
    public boolean place(){
        if(!canPlace())return false;
        placed = true;
        block.setType(material);
        return true;
    }
    public Material getMaterial(){
        return material;
    }
}