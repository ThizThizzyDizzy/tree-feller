package com.thizthizzydizzy.treefeller;
import org.bukkit.Axis;
import org.bukkit.block.Block;
import org.bukkit.block.data.Orientable;
public class RotationData{
    public final Axis axis;
    public final int x;
    public final int y;
    public final int z;
    public RotationData(Orientable data, Block origin){
        this(data.getAxis(), origin.getX(), origin.getY(), origin.getZ());
    }
    public RotationData(Axis axis, int x, int y, int z){
        this.axis = axis;
        this.x = x;
        this.y = y;
        this.z = z;
    }
}