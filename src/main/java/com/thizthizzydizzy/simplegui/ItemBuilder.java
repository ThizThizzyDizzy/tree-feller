package com.thizthizzydizzy.simplegui;
import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
public class ItemBuilder{
    public static ItemStack setDisplayName(Material item, String name){
        return setDisplayName(new ItemStack(item), name);
    }
    public static ItemStack setDisplayName(ItemStack item, String name){
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }
    public static ItemStack dye(Material item, Color color){
        return dye(new ItemStack(item), color);
    }
    public static ItemStack dye(ItemStack item, Color color){
        ItemMeta meta = item.getItemMeta();
        if(meta instanceof LeatherArmorMeta){
            ((LeatherArmorMeta) meta).setColor(color);
            item.setItemMeta(meta);
        }
        return item;
    }
    protected final Material type;
    protected String displayName = null;
    protected String localizedName = null;
    protected ArrayList<String> lore = new ArrayList<>();
    protected HashMap<Enchantment, Integer> enchantments = new HashMap<>();
    protected Color color = null;
    protected Integer customModelData = null;
    protected int count = 1;
    protected Short durability = null;
    protected ArrayList<ItemFlag> flags = new ArrayList<>();
    protected Boolean unbreakable = null;
    protected ArrayList<AttributeAndModifier> attributes = new ArrayList<>();
    public ItemBuilder(Material type){
        this.type = type;
    }
    public ItemBuilder(ItemBuilder builder){
        type = builder.type;
        displayName = builder.displayName;
        localizedName = builder.localizedName;
        lore.addAll(builder.lore);
        enchantments.putAll(builder.enchantments);
        color = builder.color;
        customModelData = builder.customModelData;
        count = builder.count;
        durability = builder.durability;
        flags.addAll(builder.flags);
        unbreakable = builder.unbreakable;
        for(AttributeAndModifier m : builder.attributes)attributes.add(new AttributeAndModifier(m));
    }
    public ItemBuilder setDisplayName(String name){
        if(name!=null){
            if(!name.startsWith(ChatColor.RESET.toString())){
                name = ChatColor.RESET.toString()+name;
            }
        }
        displayName = name;
        return this;
    }
    public ItemBuilder setLocalizedName(String name){
        if(!name.startsWith(ChatColor.RESET.toString())){
            name = ChatColor.RESET.toString()+name;
        }
        localizedName = name;
        return this;
    }
    public ItemBuilder addLore(String str){
        if(str!=null)lore.add(str);
        return this;
    }
    public ItemBuilder addLore(Iterable<String> strs){
        for(String str : strs)addLore(str);
        return this;
    }
    public ItemBuilder addLore(String[] strs){
        for(String str : strs)addLore(str);
        return this;
    }
    public ItemBuilder dye(Color color){
        this.color = color;
        return this;
    }
    public ItemBuilder setCustomModelData(Integer data){
        this.customModelData = data;
        return this;
    }
    public ItemBuilder setCustomModelData(int data){
        this.customModelData = data;
        return this;
    }
    public ItemBuilder setCount(Integer count){
        return setCount(count==null?1:(int)count);
    }
    public ItemBuilder setCount(int count){
        this.count = Math.max(1, Math.min(64, count));
        return this;
    }
    public ItemBuilder setDurability(Short durability){
        this.durability = durability;
        return this;
    }
    public ItemBuilder setDurability(short durability){
        this.durability = durability;
        return this;
    }
    public ItemBuilder setDurability(Float durability){
        if(durability==null)return this;
        this.durability = (short)(type.getMaxDurability()*durability);
        return this;
    }
    public ItemBuilder setDurability(float durability){
        this.durability = (short)(type.getMaxDurability()*durability);
        return this;
    }
    public ItemBuilder addFlag(ItemFlag flag){
        flags.add(flag);
        return this;
    }
    public ItemBuilder enchant(Enchantment ench){
        return enchant(ench, 1);
    }
    public ItemBuilder enchant(Enchantment ench, int level){
        if(enchantments.containsKey(ench)){
            enchantments.put(ench, Math.max(enchantments.get(ench),level));
        }else{
            this.enchantments.put(ench, level);
        }
        return this;
    }
    public ItemBuilder addAttributeModifier(Attribute attribute, AttributeModifier modifier){
        attributes.add(new AttributeAndModifier(attribute, modifier));
        return this;
    }
    public ItemBuilder setUnbreakable(boolean unbreakable){
        this.unbreakable = unbreakable;
        return this;
    }
    public ItemStack build(){
        ItemStack stack = new ItemStack(type, count);
        ItemMeta meta = stack.getItemMeta();
        if(durability!=null)stack.setDurability((short)(stack.getType().getMaxDurability()-durability));
        if(displayName!=null)meta.setDisplayName(displayName);
        if(localizedName!=null)meta.setLocalizedName(localizedName);
        if(!lore.isEmpty())meta.setLore(lore);
        if(customModelData!=null)meta.setCustomModelData(customModelData);
        if(color!=null&&meta instanceof LeatherArmorMeta)((LeatherArmorMeta) meta).setColor(color);
        for(ItemFlag flag : flags)meta.addItemFlags(flag);
        for(Enchantment e : enchantments.keySet())meta.addEnchant(e, enchantments.get(e), true);
        for(AttributeAndModifier mod : attributes)meta.addAttributeModifier(mod.attribute, mod.modifier);
        if(unbreakable!=null)meta.setUnbreakable(unbreakable);
        stack.setItemMeta(meta);
        return stack;
    }
    private static class AttributeAndModifier{
        private final Attribute attribute;
        private final AttributeModifier modifier;
        public AttributeAndModifier(Attribute attribute, AttributeModifier modifier){
            this.attribute = attribute;
            this.modifier = modifier;
        }
        private AttributeAndModifier(AttributeAndModifier m){
            this(m.attribute, m.modifier);
        }
    }
}