package com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_13;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockAxis;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.BukkitBlockDataConnector;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.entity.Player;
public class BukkitBlockDataConnectorV1_13 implements BukkitBlockDataConnector{
    @Override
    public void sendBlockChange(Player player, Location location, Material material){
        // The material/byte overload initializes expensive legacy conversion tables.
        player.sendBlockChange(location, material.createBlockData());
    }
    @Override
    public BlockAxis getBlockAxis(Block block){
        BukkitBlockDataConnectorV1_13 c;
        BlockData data = block.getBlockData();
        if(data instanceof Orientable){
            Orientable orientable = (Orientable)data;
            switch(orientable.getAxis()){
                case X: return BlockAxis.X;
                case Y: return BlockAxis.Y;
                case Z: return BlockAxis.Z;
                default: throw new AssertionError(orientable.getAxis().name());
            }
        }
        return null;
    }
    @Override
    public int getLeafDistance(Block block){
        BlockData data = block.getBlockData();
        if(data instanceof Leaves){
            Leaves leaves = (Leaves)data;
            return leaves.getDistance();
        }
        return -1;
    }
}
