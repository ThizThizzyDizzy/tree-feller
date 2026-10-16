package com.thizthizzydizzy.treefeller.platform.bukkit.core.definition;
import com.thizthizzydizzy.treefeller.core.config.structure.special.BlockDefinition;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.connector.BukkitConnector;
import org.bukkit.Material;
import org.bukkit.block.Block;
public class BukkitBlockDefinition extends BlockDefinition{
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
        return simplify(material==null?null:material.toString());
    }
    public boolean matches(Block block){
        if(material!=null&&material!=block.getType())return false;
        return matchesPersistent(BukkitConnector.blockData.getPersistent(block));
    }
}
