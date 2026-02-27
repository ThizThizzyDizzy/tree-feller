package com.thizthizzydizzy.treefeller.platform.bukkit.core.definition;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IBlockDefinition;
import org.bukkit.Material;
public class BukkitBlockDefinition implements IBlockDefinition{
    public Material material;
    public BukkitBlockDefinition(){
    }
    public BukkitBlockDefinition(String str){
        material = Material.matchMaterial(str);
    }
    @Override
    public Object asSimplified(){
        return material.toString();
    }
}
