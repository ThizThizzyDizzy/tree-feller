package com.thizthizzydizzy.treefeller;
import com.thizthizzydizzy.treefeller.compat.TreeFellerCompat;
import java.util.ArrayList;
import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
public class NaturalFall{
    private static final double interval = 0.1;
    private final Player player;
    private final Vector v;
    private final Block origin;
    private final Block block;
    private final int height;
    private final Material material;
    private Axis axis = null;
    private final ArrayList<Material> overridables;
    private boolean fell = false;
    public NaturalFall(Player player, Vector v, Block origin, Block block, int height, boolean rotate, ArrayList<Material> overridables){
        this.player = player;
        this.v = v.multiply(interval);
        this.origin = origin;
        this.block = block;
        this.height = height;
        this.material = block.getType();
        if(rotate&&block.getBlockData() instanceof Orientable){
            axis = ((Orientable)block.getBlockData()).getAxis();
        }
        this.overridables = overridables;
    }
    public void fall(TreeFeller plugin){
        if(fell)return;
        fell = true;
        double dist = 0;
        Block target = block;
        BlockState was = block.getState();
        Location l = block.getLocation().add(.5,.5,.5);
        while(dist<height){
            dist+=interval;
            l = l.add(v);
            Block b = l.getBlock();
            triggerNaturalFall(plugin, b);
            if(overridables.contains(b.getType()))target = b;
            else break;
        }
        Block b;
        while(overridables.contains((b = target.getRelative(0, -1, 0)).getType())){
            triggerNaturalFall(plugin, b);
            target = b;
        }
        target.setType(material);
        if(axis!=null){
            double xDiff = Math.abs(origin.getX()-target.getX());
            double yDiff = Math.abs(origin.getY()-target.getY());
            double zDiff = Math.abs(origin.getZ()-target.getZ());
            Axis newAxis = Axis.Y;
            if(Math.max(Math.max(xDiff, yDiff), zDiff)==xDiff)newAxis = Axis.X;
            if(Math.max(Math.max(xDiff, yDiff), zDiff)==zDiff)newAxis = Axis.Z;
            if(newAxis==Axis.X){
                switch(axis){
                    case X:
                        axis = Axis.Y;
                        break;
                    case Y:
                        axis = Axis.X;
                        break;
                    case Z:
                        break;
                }
            }
            if(newAxis==Axis.Z){
                switch(axis){
                    case X:
                        break;
                    case Y:
                        axis = Axis.Z;
                        break;
                    case Z:
                        axis = Axis.X;
                        break;
                }
            }
            BlockData data = target.getBlockData();
            ((Orientable)data).setAxis(axis);
            target.setBlockData(data);
        }
        TreeFellerCompat.addBlock(plugin, player, target, was);
    }
    private void triggerNaturalFall(TreeFeller plugin, Block b){
        for(NaturalFall fall : plugin.naturalFalls){
            if(fall==this)continue;
            if(fall.block.equals(b))fall.fall(plugin);
        }
    }
}