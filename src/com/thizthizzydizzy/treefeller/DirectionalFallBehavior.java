package com.thizthizzydizzy.treefeller;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
public enum DirectionalFallBehavior{
    RANDOM(Material.BELL){
        @Override
        Vector getDefaultDirectionalVel(long seed, Player player, Block block, boolean lockCardinal, double directionalFallVelocity){
            double angle = new Random(seed).nextDouble()*Math.PI*2;
            return new Vector(Math.cos(angle),0,Math.sin(angle));
        }
    },
    TOWARD(Material.DISPENSER) {
        @Override
        Vector getDefaultDirectionalVel(long seed, Player player, Block block, boolean lockCardinal, double directionalFallVelocity){
            return player==null?null:new Vector(player.getLocation().getX()-block.getLocation().getX(),player.getLocation().getY()-block.getLocation().getY(),player.getLocation().getZ()-block.getLocation().getZ());
        }
    },
    AWAY(Material.DROPPER) {
        @Override
        Vector getDefaultDirectionalVel(long seed, Player player, Block block, boolean lockCardinal, double directionalFallVelocity){
            return player==null?null:new Vector(player.getLocation().getX()-block.getLocation().getX(),player.getLocation().getY()-block.getLocation().getY(),player.getLocation().getZ()-block.getLocation().getZ()).multiply(-1);
        }
    },
    LEFT(Material.CROSSBOW) {
        @Override
        Vector getDefaultDirectionalVel(long seed, Player player, Block block, boolean lockCardinal, double directionalFallVelocity){
            return player==null?null:new Vector(-(player.getLocation().getZ()-block.getLocation().getZ()),player.getLocation().getY()-block.getLocation().getY(),player.getLocation().getX()-block.getLocation().getX());
        }
    },
    RIGHT(Material.WOODEN_SWORD) {
        @Override
        Vector getDefaultDirectionalVel(long seed, Player player, Block block, boolean lockCardinal, double directionalFallVelocity){
            return player==null?null:new Vector(-(player.getLocation().getZ()-block.getLocation().getZ()),player.getLocation().getY()-block.getLocation().getY(),player.getLocation().getX()-block.getLocation().getX()).multiply(-1);
        }
    },
    NORTH(Material.RED_CONCRETE) {
        @Override
        Vector getDefaultDirectionalVel(long seed, Player player, Block block, boolean lockCardinal, double directionalFallVelocity){
            return new Vector(0, 0, -1);
        }
    },
    SOUTH(Material.BLUE_CONCRETE) {
        @Override
        Vector getDefaultDirectionalVel(long seed, Player player, Block block, boolean lockCardinal, double directionalFallVelocity){
            return new Vector(0, 0, 1);
        }
    },
    EAST(Material.YELLOW_CONCRETE) {
        @Override
        Vector getDefaultDirectionalVel(long seed, Player player, Block block, boolean lockCardinal, double directionalFallVelocity){
            return new Vector(1, 0, 0);
        }
    },
    WEST(Material.GREEN_CONCRETE) {
        @Override
        Vector getDefaultDirectionalVel(long seed, Player player, Block block, boolean lockCardinal, double directionalFallVelocity){
            return new Vector(-1, 0, 0);
        }
    },
    NORTH_EAST(Material.RED_TERRACOTTA) {
        @Override
        Vector getDefaultDirectionalVel(long seed, Player player, Block block, boolean lockCardinal, double directionalFallVelocity){
            return new Vector(1, 0, -1);
        }
    },
    SOUTH_EAST(Material.YELLOW_TERRACOTTA) {
        @Override
        Vector getDefaultDirectionalVel(long seed, Player player, Block block, boolean lockCardinal, double directionalFallVelocity){
            return new Vector(1, 0, 1);
        }
    },
    NORTH_WEST(Material.GREEN_TERRACOTTA) {
        @Override
        Vector getDefaultDirectionalVel(long seed, Player player, Block block, boolean lockCardinal, double directionalFallVelocity){
            return new Vector(-1, 0, -1);
        }
    },
    SOUTH_WEST(Material.BLUE_TERRACOTTA) {
        @Override
        Vector getDefaultDirectionalVel(long seed, Player player, Block block, boolean lockCardinal, double directionalFallVelocity){
            return new Vector(-1, 0, 1);
        }
    };
    private final Material item;
    private DirectionalFallBehavior(Material item){
        this.item = item;
    }
    public static DirectionalFallBehavior match(String s){
        return valueOf(s.toUpperCase().trim().replace("-", "_"));
    }
    abstract Vector getDefaultDirectionalVel(long seed, Player player, Block block, boolean lockCardinal, double directionalFallVelocity);
    public Vector getDirectionalVel(long seed, Player player, Block block, boolean lockCardinal, double directionalFallVelocity){
        Vector directionalVel = getDefaultDirectionalVel(seed, player, block, lockCardinal, directionalFallVelocity);
        if(directionalVel==null)directionalVel = new Vector(0, 0, 0);
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