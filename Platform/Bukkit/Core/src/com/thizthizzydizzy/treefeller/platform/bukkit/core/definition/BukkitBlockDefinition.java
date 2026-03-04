package com.thizthizzydizzy.treefeller.platform.bukkit.core.definition;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IBlockDefinition;
import org.bukkit.Material;
import org.bukkit.block.Block;
public class BukkitBlockDefinition implements IBlockDefinition{
    public Material material;
    public BukkitBlockDefinition(){
    }
    public BukkitBlockDefinition(String str){
        material = Material.matchMaterial(str);
    }
    @Override
    public Object asSimplified(){
        if(material==null)return null;
        return material.toString();
    }
    public boolean matches(Block block){
        if(material!=null&&material!=block.getType())return false;
        return true;
    }
}
