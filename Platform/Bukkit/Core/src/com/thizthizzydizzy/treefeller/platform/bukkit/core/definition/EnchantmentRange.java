package com.thizthizzydizzy.treefeller.platform.bukkit.core.definition;
import com.thizthizzydizzy.treefeller.core.config.structure.general.Range;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;
public class EnchantmentRange{
    public Enchantment enchantment;
    public Range level = new Range(1f, null);
    public boolean matches(ItemMeta item){
        int lvl = 0;
        if(item.hasEnchant(enchantment))lvl = item.getEnchantLevel(enchantment);
        return level.matches(lvl);
    }
}
