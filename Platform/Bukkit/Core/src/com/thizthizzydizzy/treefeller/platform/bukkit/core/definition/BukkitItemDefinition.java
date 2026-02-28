package com.thizthizzydizzy.treefeller.platform.bukkit.core.definition;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
import com.thizthizzydizzy.treefeller.core.config.structure.general.DualList;
import com.thizthizzydizzy.treefeller.core.config.structure.general.VariableRange;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IItemDefinition;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
public class BukkitItemDefinition implements IItemDefinition{
    @ConfigComment("An item ID or item tag")
    public Material material;
    
    public VariableRange durability;
    
    public DualList<String> custom_name;
    
    public DualList<String> lore;
    
    public DualList<Integer> custom_model_data;
    
    public DualList<ItemFlag> attributes;
    
    public DualList<EnchantmentRange> enchantments;
    
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
