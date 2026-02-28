package com.thizthizzydizzy.treefeller.platform.bukkit.core.connector;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IItemDefinition;
import com.thizthizzydizzy.treefeller.core.connector.item.IItemConnector;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.definition.BukkitItemDefinition;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
public class BukkitItemConnector implements IItemConnector{
    private final ItemStack stack;
    public BukkitItemConnector(ItemStack stack){
        this.stack = stack;
    }
    @Override
    public boolean matches(IItemDefinition definition){
        if(definition==null)
            return stack==null||stack.getAmount()==0||stack.getType()==Material.AIR;
        if(definition instanceof BukkitItemDefinition){
            BukkitItemDefinition item = (BukkitItemDefinition)definition;
            if(item.material!=null&&item.material!=stack.getType())return false;
            if(item.durability!=null&&!item.durability.matches(0, stack.getType().getMaxDurability(), stack.getType().getMaxDurability()-stack.getDurability()))
                return false;
            
            if(item.custom_name!=null&&!item.custom_name.applySingle(stack.getItemMeta().getDisplayName()))
                return false;
            if(item.lore!=null&&!item.lore.applyMulti(stack.getItemMeta().getLore()::contains))
                return false;
            if(item.attributes!=null&&!item.attributes.applyMulti(stack.getItemMeta().getItemFlags()::contains))
                return false;
            if(item.enchantments!=null&&!item.enchantments.applyMulti(range -> range.matches(stack.getItemMeta())))return false;
            //TODO custom model data (1.14+)
            return true;
        }
        throw new AssertionError("Unsupported item definition: "+definition.getClass().getName());
    }
}
