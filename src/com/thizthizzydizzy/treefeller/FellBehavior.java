package com.thizthizzydizzy.treefeller;
import org.bukkit.Material;
public enum FellBehavior{
    BREAK(Material.COBBLESTONE),
    FALL(Material.SAND),
    FALL_HURT(Material.ANVIL),
    FALL_BREAK(Material.GRAVEL),
    FALL_HURT_BREAK(Material.DAMAGED_ANVIL),
    INVENTORY(Material.CHEST),
    FALL_INVENTORY(Material.ENDER_CHEST),
    FALL_HURT_INVENTORY(Material.TRAPPED_CHEST),
    NATURAL(Material.OAK_SAPLING);
    private final Material item;
    private FellBehavior(Material item){
        this.item = item;
    }
    public static FellBehavior match(String s){
        return valueOf(s.toUpperCase().trim().replace("-", "_"));
    }
    public Material getItem(){
        return item;
    }
}