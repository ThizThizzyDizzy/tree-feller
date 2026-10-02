package com.thizthizzydizzy.treefeller.platform.bukkit.core.connector;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IItemDefinition;
import com.thizthizzydizzy.treefeller.core.connector.item.IItemConnector;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.definition.BukkitItemDefinition;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
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
            return item.matches(stack);
        }
        throw new AssertionError("Unsupported item definition: "+definition.getClass().getName());
    }
    @Override
    public Object getItemId(){
        return stack==null?Material.AIR.toString():stack.getType().toString();
    }
    @Override
    public int getMaxDurability(){
        return stack==null?0:stack.getType().getMaxDurability();
    }
    @Override
    public int getCurrentDurability(){
        return stack==null?0:Math.max(0, stack.getType().getMaxDurability()-stack.getDurability());
    }
    @Override
    public int getCount(){
        return stack==null?0:stack.getAmount();
    }
    @Override
    public int getUnbreakingLevel(){
        return stack==null?0:stack.getEnchantmentLevel(Enchantment.DURABILITY);
    }
    @Override
    public boolean isUnbreakable(){
        if(stack==null||!stack.hasItemMeta())return false;
        // The shared Bukkit module compiles against 1.8; newer metadata APIs
        // are discovered at runtime to keep older servers loadable.
        try{
            return (Boolean)org.bukkit.inventory.meta.ItemMeta.class.getMethod("isUnbreakable").invoke(stack.getItemMeta());
        }catch(NoSuchMethodException ex){
            return Boolean.TRUE.equals(stack.getItemMeta().serialize().get("unbreakable"));
        }catch(ReflectiveOperationException ex){
            throw new IllegalStateException("Could not read unbreakable item metadata", ex);
        }
    }
}
