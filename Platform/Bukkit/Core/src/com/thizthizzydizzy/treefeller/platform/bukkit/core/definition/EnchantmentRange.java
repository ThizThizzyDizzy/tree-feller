package com.thizthizzydizzy.treefeller.platform.bukkit.core.definition;
import com.thizthizzydizzy.treefeller.core.config.structure.general.IntegerRange;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;
public class EnchantmentRange{
    public Enchantment enchantment;
    public IntegerRange level = new IntegerRange(1, null);
    public boolean matches(ItemMeta item){
        int lvl = 0;
        if(item.hasEnchant(enchantment))lvl = item.getEnchantLevel(enchantment);
        return level.matches(lvl);
    }
}
