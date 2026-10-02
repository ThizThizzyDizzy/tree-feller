package com.thizthizzydizzy.treefeller.platform.bukkit.core.definition;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IBlockDefinition;
import org.bukkit.Material;
import org.bukkit.block.Block;
public class BukkitBlockDefinition implements IBlockDefinition{
    public Material material;
    public BukkitBlockDefinition(){
    }
    public BukkitBlockDefinition(String str){
        this(Material.matchMaterial(str));
        if(material==null)throw new IllegalArgumentException("Unknown block material: "+str);
    }
    public BukkitBlockDefinition(Material material){
        this.material = material;
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
