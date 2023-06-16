package com.thizthizzydizzy.treefeller.decoration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
public class AdjacentColumnDecorationDetector extends DecorationDetector{
    private final BlockFace[] directions;
    private final List<Material> materials;
    public AdjacentColumnDecorationDetector(String name, Material material, BlockFace... directions){
        this(name, new Material[]{material}, directions);
    }
    public AdjacentColumnDecorationDetector(String name, Material[] materials, BlockFace... directions){
        super(name, materials[0]);
        this.materials = Arrays.asList(materials);
        this.directions = directions;
    }
    public void detect(Block baseBlock, ArrayList<Block> blocks){
        for(BlockFace direction : directions){
            Block block = baseBlock.getRelative(direction);
            if(!materials.contains(block.getType()))continue;
            Block up;
            while(materials.contains((up = block.getRelative(BlockFace.UP)).getType()))block = up;
            while(materials.contains(block.getType())){
                blocks.add(block);
                block = block.getRelative(BlockFace.DOWN);
            }
        }
    }
}