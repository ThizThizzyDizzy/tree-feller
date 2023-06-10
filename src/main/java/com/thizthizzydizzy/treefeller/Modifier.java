package com.thizthizzydizzy.treefeller;
import org.bukkit.block.Block;
public class Modifier{
    public final Type type;
    public final double value;
    public Modifier(Type type, double value){
        this.type = type;
        this.value = value;
    }
    double apply(double dropChance, Tree tree, Block block){
        return type.apply(this, dropChance, tree, block);
    }
    public static enum Type{
        LOG_MULT{
            @Override
            double apply(Modifier mod, double dropChance, Tree tree, Block block){
                if(tree.trunk.contains(block.getType()))dropChance*=mod.value;
                return dropChance;
            }
        },
        LEAF_MULT{
            @Override
            double apply(Modifier mod, double dropChance, Tree tree, Block block){
                if(!tree.trunk.contains(block.getType()))dropChance*=mod.value;
                return dropChance;
            }
        },
        DROPS_MULT{
            @Override
            double apply(Modifier mod, double dropChance, Tree tree, Block block){
                return dropChance*mod.value;
            }
        };
        abstract double apply(Modifier mod, double dropChance, Tree tree, Block block);
    }
}