package com.thizthizzydizzy.treefeller.decoration;
import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
public abstract class DecorationDetector{
    public static final ArrayList<DecorationDetector> detectors = new ArrayList<>();
    static{
        detectors.add(new AdjacentDecorationDetector("snow", Material.SNOW, BlockFace.UP));
        detectors.add(new AdjacentColumnDecorationDetector("vines", Material.VINE, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST));
        detectors.add(new AdjacentDecorationDetector("cocoa", new Material[]{Material.COCOA_BEANS, Material.COCOA}, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST));
        detectors.add(new AdjacentColumnDecorationDetector("weeping vines", new Material[]{Material.WEEPING_VINES, Material.WEEPING_VINES_PLANT}, BlockFace.DOWN));
        Material hangingMoss = Material.matchMaterial("PALE_HANGING_MOSS");
        if(hangingMoss!=null)detectors.add(new AdjacentColumnDecorationDetector("pale hanging moss", hangingMoss, BlockFace.DOWN));
        Material mossCarpet = Material.matchMaterial("PALE_MOSS_CARPET");
        if(hangingMoss!=null)detectors.add(new AdjacentDecorationDetector("pale moss carpet", mossCarpet, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST));
        Material resinClump = Material.matchMaterial("RESIN_CLUMP");
        if(resinClump!=null)detectors.add(new AdjacentDecorationDetector("resin clump", resinClump, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN));
        Material moss = Material.matchMaterial("moss_carpet");
        if(moss!=null)detectors.add(new AdjacentDecorationDetector("moss", moss, BlockFace.UP));
    }
    public final String name;
    private final Material material;
    public DecorationDetector(String name, Material material){
        this.name = name;
        this.material = material;
    }
    public abstract void detect(Block baseBlock, ArrayList<Block> blocks);
    public static DecorationDetector[] getDetectors(){
        return detectors.toArray(new DecorationDetector[detectors.size()]);
    }
    public static Material[] getMaterials(){
        Material[] mats = new Material[detectors.size()];
        for(int i = 0; i<mats.length; i++){
            mats[i] = detectors.get(i).material;
        }
        return mats;
    }
    @Override
    public String toString(){
        return name;
    }
}