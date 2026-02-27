package com.thizthizzydizzy.treefeller.platform.bukkit.core.definition;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IItemDefinition;
import org.bukkit.Material;
public class BukkitItemDefinition implements IItemDefinition{
    public Material material;
    public BukkitItemDefinition(){
    }
    public BukkitItemDefinition(String str){
        material = Material.matchMaterial(str);
    }
    @Override
    public Object asSimplified(){
        return material.toString();
    }
}
