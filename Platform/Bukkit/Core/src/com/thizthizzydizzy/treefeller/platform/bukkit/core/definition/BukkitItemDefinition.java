package com.thizthizzydizzy.treefeller.platform.bukkit.core.definition;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
import com.thizthizzydizzy.treefeller.core.config.structure.general.DualList;
import com.thizthizzydizzy.treefeller.core.config.structure.general.VariableRange;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IItemDefinition;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
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
        if(material==null)return null;
        if(durability==null&&custom_name==null&&lore==null&&custom_model_data==null&&attributes==null&&enchantments==null)
            return material.toString();
        return this;
    }
    public boolean matches(ItemStack stack){
        if(material!=null&&material!=stack.getType())return false;
        if(durability!=null&&!durability.matches(0, stack.getType().getMaxDurability(), stack.getType().getMaxDurability()-stack.getDurability()))
            return false;

        if(custom_name!=null&&!custom_name.applySingle(stack.getItemMeta().getDisplayName()))
            return false;
        if(lore!=null&&!lore.applyMulti(stack.getItemMeta().getLore()::contains))
            return false;
        if(attributes!=null&&!attributes.applyMulti(stack.getItemMeta().getItemFlags()::contains))
            return false;
        if(enchantments!=null&&!enchantments.applyMulti(range -> range.matches(stack.getItemMeta())))
            return false;
        //TODO custom model data (1.14+)
        return true;
    }
}
