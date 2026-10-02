package com.thizthizzydizzy.treefeller.platform.bukkit.core.definition;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
import com.thizthizzydizzy.treefeller.core.config.structure.general.DualList;
import com.thizthizzydizzy.treefeller.core.config.structure.general.VariableIntegerRange;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IItemDefinition;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
public class BukkitItemDefinition implements IItemDefinition{
    @ConfigComment("An item ID or item tag")
    public Material material;

    public VariableIntegerRange durability;

    public DualList<String> custom_name;

    public DualList<String> lore;

    public DualList<Integer> custom_model_data;

    public DualList<ItemFlag> attributes;

    public DualList<EnchantmentRange> enchantments;

    public BukkitItemDefinition(){
    }
    public BukkitItemDefinition(String str){
        material = Material.matchMaterial(str);
        if(material==null)throw new IllegalArgumentException("Unknown item material: "+str);
    }
    @Override
    public Object asSimplified(){
        if(durability==null&&custom_name==null&&lore==null&&custom_model_data==null&&attributes==null&&enchantments==null)
            return material==null?null:material.toString();
        return this;
    }
    public boolean matches(ItemStack stack){
        if(stack==null||stack.getType()==Material.AIR)
            return (material==null||material==Material.AIR)&&durability==null&&custom_name==null
                    &&lore==null&&custom_model_data==null&&attributes==null&&enchantments==null;
        if(material!=null&&material!=stack.getType())return false;
        if(durability!=null&&!durability.matches(0, stack.getType().getMaxDurability(), stack.getType().getMaxDurability()-stack.getDurability()))
            return false;

        ItemMeta meta = stack.getItemMeta();
        if(custom_name!=null&&!custom_name.applySingle(meta==null?null:meta.getDisplayName()))
            return false;
        if(lore!=null&&!lore.applyMulti(line -> meta!=null&&meta.hasLore()&&meta.getLore().contains(line)))
            return false;
        if(attributes!=null&&!attributes.applyMulti(flag -> meta!=null&&meta.hasItemFlag(flag)))
            return false;
        if(enchantments!=null&&!enchantments.applyMulti(range -> range.matches(meta)))
            return false;
        //TODO custom model data (1.14+)
        return true;
    }
}
