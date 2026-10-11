package com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_8;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockAxis;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.BukkitBlockDataConnector;
import java.util.Collection;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
public class BukkitBlockDataConnectorV1_8 implements BukkitBlockDataConnector{
    private Material testMaterial(String name){
        switch(name){
            case "grass_block": return Material.GRASS;
            case "oak_log": case "spruce_log": case "birch_log": case "jungle_log": return Material.LOG;
            case "acacia_log": case "dark_oak_log": return Material.LOG_2;
            case "oak_leaves": case "spruce_leaves": case "birch_leaves": case "jungle_leaves": return Material.LEAVES;
            case "acacia_leaves": case "dark_oak_leaves": return Material.LEAVES_2;
            default: return Material.matchMaterial(name);
        }
    }
    private int species(String name){
        if(name.startsWith("spruce_")||name.startsWith("dark_oak_"))return 1;
        if(name.startsWith("birch_"))return 2;
        if(name.startsWith("jungle_"))return 3;
        return 0;
    }
    @Override public boolean supportsTestMaterial(String name){
        Material material = testMaterial(name);
        return material!=null&&material.isBlock();
    }
    @Override public void setTestBlock(Block block, String name, BlockAxis axis, int leafDistance){
        Material material = testMaterial(name);
        if(material==null)throw new IllegalArgumentException("Unavailable material: "+name);
        int data = species(name);
        if(axis==BlockAxis.X)data |= 4;
        if(axis==BlockAxis.Z)data |= 8;
        // Leave the non-decaying bit clear, as on naturally generated leaves.
        block.setTypeIdAndData(material.getId(), (byte)data, false);
    }
    @Override public String testBlockState(Block block){return block.getType().name()+":"+block.getData();}
    @Override public boolean isTestMaterial(Block block, String name){
        return block.getType()==testMaterial(name)&&(block.getData()&3)==species(name);
    }
    @Override
    public Collection<ItemStack> getDrops(Block block, ItemStack tool, Player player){
        return block.getDrops(tool);
    }
    @Override
    public void sendBlockChange(Player player, Location location, Material material){
        player.sendBlockChange(location, material, (byte)0);
    }
    @Override
    public BlockAxis getBlockAxis(Block block){
        byte data = block.getData();
        switch(block.getType()){
            case LOG:
            case LOG_2:
                switch(data){
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 12:
                    case 13:
                    case 14:
                    case 15:
                        return BlockAxis.Y;
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                        return BlockAxis.X;
                    case 8:
                    case 9:
                    case 10:
                    case 11:
                        return BlockAxis.Z;
                }
                break;
            case HAY_BLOCK:
                switch(data){
                    case 0:
                        return BlockAxis.Y;
                    case 4:
                        return BlockAxis.X;
                    case 8:
                        return BlockAxis.Z;
                }
            case QUARTZ_BLOCK:
                switch(data){
                    case 2:
                        return BlockAxis.Y;
                    case 3:
                        return BlockAxis.X;
                    case 4:
                        return BlockAxis.Z;
                }
        }
        if(block.getType()==Material.matchMaterial("BONE_BLOCK")||block.getType()==Material.matchMaterial("PURPUR_PILLAR")){
            switch(data){
                case 0:
                    return BlockAxis.Y;
                case 4:
                    return BlockAxis.X;
                case 8:
                    return BlockAxis.Z;
            }
        }
        return null;
    }
    @Override
    public int getLeafDistance(Block block){
        return -1;
    }
}
