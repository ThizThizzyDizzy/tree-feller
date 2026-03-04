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
            return item.matches(stack);
        }
        throw new AssertionError("Unsupported item definition: "+definition.getClass().getName());
    }
    @Override
    public Object getItemId(){
        return stack.getType().toString();
    }
}
