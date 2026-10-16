package com.thizthizzydizzy.treefeller.core.config.structure.special;
import com.thizthizzydizzy.treefeller.core.config.structure.general.IntegerRange;
import java.util.function.ToIntFunction;
/** A named enchantment requirement; platforms supply the item's actual level. */
public class EnchantmentRange{
    public String enchantment;
    public IntegerRange level = new IntegerRange(1, null);
    public boolean matches(ToIntFunction<String> levels){
        return level.matches(levels.applyAsInt(enchantment));
    }
}
