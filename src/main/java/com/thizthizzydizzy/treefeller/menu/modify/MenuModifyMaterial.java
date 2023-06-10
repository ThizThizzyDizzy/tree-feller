package com.thizthizzydizzy.treefeller.menu.modify;
import com.thizthizzydizzy.simplegui.Button;
import com.thizthizzydizzy.simplegui.ItemBuilder;
import com.thizthizzydizzy.simplegui.Menu;
import java.util.function.Consumer;
import java.util.function.Function;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
public class MenuModifyMaterial extends Menu{
    private Material value;
    private final boolean allowNull;
    private final String filterName;
    private final Function<Material, Boolean> filter;
    private final Consumer<Material> setFunc;
    public MenuModifyMaterial(Menu parent, Plugin plugin, Player player, String name, boolean allowNull, String filterName, Material defaultValue, Function<Material, Boolean> filter, Consumer<Material> setFunc){
        super(parent, plugin, player, "Modify Material ("+name+")", 9);
        this.value = defaultValue;
        this.allowNull = allowNull;
        this.filterName = filterName;
        refresh();
        this.filter = filter;
        this.setFunc = setFunc;
    }
    public void refresh(){
        components.clear();
        ItemBuilder builder = makeItem(value==null?Material.BLACK_CONCRETE:value).addLore("Click any "+filterName+" in your inventory to set the material.");
        if(value==null)builder.setDisplayName("NULL");
        if(allowNull)builder.addLore("Right click this item to set to NULL");
        add(new Button(0, builder, (click) -> {
            if(click==ClickType.RIGHT){
                if(allowNull)value = null;
                setFunc.accept(value);
                refresh();
            }
        }));
        add(new Button(size-1, makeItem(Material.BARRIER).setDisplayName("Back"), (click) -> {
            if(click!=ClickType.LEFT)return;
            open(parent);
        }));
        updateInventory();
    }
    @Override
    public void onInventoryClick(int slot, ClickType click){
        if(click!=ClickType.LEFT)return;
        ItemStack stack = player.getInventory().getItem(slot);
        if(stack==null)return;
        if(filter.apply(stack.getType())){
            value = stack.getType();
            setFunc.accept(value);
            refresh();
        }
    }
}