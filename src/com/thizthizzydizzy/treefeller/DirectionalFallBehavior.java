package com.thizthizzydizzy.treefeller;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
public enum DirectionalFallBehavior{
    RANDOM(Material.BELL),
    TOWARD(Material.DISPENSER),
    AWAY(Material.DROPPER),
    LEFT(Material.CROSSBOW),
    RIGHT(Material.WOODEN_SWORD),
    NORTH(Material.RED_CONCRETE),
    SOUTH(Material.BLUE_CONCRETE),
    EAST(Material.YELLOW_CONCRETE),
    WEST(Material.GREEN_CONCRETE),
    NORTH_EAST(Material.RED_TERRACOTTA),
    SOUTH_EAST(Material.YELLOW_TERRACOTTA),
    NORTH_WEST(Material.GREEN_TERRACOTTA),
    SOUTH_WEST(Material.BLUE_TERRACOTTA);
    private final Material item;
    private DirectionalFallBehavior(Material item){
        this.item = item;
    }
    public static DirectionalFallBehavior match(String s){
        return valueOf(s.toUpperCase().trim().replace("-", "_"));
    }
    public Vector getDirectionalVel(long seed, Player player, Block block, boolean lockCardinal, double directionalFallVelocity){
        Vector directionalVel = new Vector(0, 0, 0);
        switch(this){
            case RANDOM:
                double angle = new Random(seed).nextDouble()*Math.PI*2;
                directionalVel = new Vector(Math.cos(angle),0,Math.sin(angle));
                break;
            case TOWARD:
                if(player!=null){
                    directionalVel = new Vector(player.getLocation().getX()-block.getLocation().getX(),player.getLocation().getY()-block.getLocation().getY(),player.getLocation().getZ()-block.getLocation().getZ());
                }
                break;
            case AWAY:
                if(player!=null){
                    directionalVel = new Vector(player.getLocation().getX()-block.getLocation().getX(),player.getLocation().getY()-block.getLocation().getY(),player.getLocation().getZ()-block.getLocation().getZ()).multiply(-1);
                }
                break;
            case LEFT:
                if(player!=null){
                    directionalVel = new Vector(-(player.getLocation().getZ()-block.getLocation().getZ()),player.getLocation().getY()-block.getLocation().getY(),player.getLocation().getX()-block.getLocation().getX());
                }
                break;
            case RIGHT:
                if(player!=null){
                    directionalVel = new Vector(-(player.getLocation().getZ()-block.getLocation().getZ()),player.getLocation().getY()-block.getLocation().getY(),player.getLocation().getX()-block.getLocation().getX()).multiply(-1);
                }
                break;
            case NORTH:
                directionalVel = new Vector(0, 0, -1);
                break;
            case SOUTH:
                directionalVel = new Vector(0, 0, 1);
                break;
            case EAST:
                directionalVel = new Vector(1, 0, 0);
                break;
            case WEST:
                directionalVel = new Vector(-1, 0, 0);
                break;
            case NORTH_EAST:
                directionalVel = new Vector(1, 0, -1);
                break;
            case SOUTH_EAST:
                directionalVel = new Vector(1, 0, 1);
                break;
            case SOUTH_WEST:
                directionalVel = new Vector(-1, 0, 1);
                break;
            case NORTH_WEST:
                directionalVel = new Vector(-1, 0, -1);
                break;
            default:
                throw new IllegalArgumentException("Invalid fall behavior: "+this);
        }
        if(lockCardinal){
            if(Math.abs(directionalVel.getX())>Math.abs(directionalVel.getZ())){
                if(directionalVel.getX()>0)directionalVel = new Vector(1, 0, 0);
                else directionalVel = new Vector(-1, 0, 0);
            }else{
                if(directionalVel.getZ()>0)directionalVel = new Vector(0, 0, 1);
                else directionalVel = new Vector(0, 0, -1);
            }
        }
        directionalVel = directionalVel.normalize();
        directionalVel = directionalVel.multiply(directionalFallVelocity);
        return directionalVel;
    }
    public Material getItem(){
        return item;
    }
}