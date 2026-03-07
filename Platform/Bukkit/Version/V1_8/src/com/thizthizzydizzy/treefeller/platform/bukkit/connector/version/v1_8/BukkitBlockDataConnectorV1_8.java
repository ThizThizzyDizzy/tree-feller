package com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_8;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockAxis;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.BukkitBlockDataConnector;
import org.bukkit.Material;
import org.bukkit.block.Block;
public class BukkitBlockDataConnectorV1_8 implements BukkitBlockDataConnector{
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
