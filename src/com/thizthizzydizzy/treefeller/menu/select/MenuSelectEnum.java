package com.thizthizzydizzy.treefeller.menu.select;
import com.thizthizzydizzy.simplegui.Button;
import com.thizthizzydizzy.simplegui.Label;
import com.thizthizzydizzy.simplegui.Menu;
import java.util.Objects;
import java.util.function.BiConsumer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;
public class MenuSelectEnum<T> extends Menu{
    T value;
    private final T[] values;
    private final boolean allowNull;
    private final BiConsumer<MenuSelectEnum<T>, T> setFunc;
    public MenuSelectEnum(Menu parent, Plugin plugin, Player player, String name, String enumName, boolean allowNull, T defaultValue, T[] values, BiConsumer<MenuSelectEnum<T>, T> setFunc){
        super(parent, plugin, player, "Select "+enumName+" ("+name+")", getSize(values.length, allowNull));
        value = defaultValue;
        this.values = values;
        this.allowNull = allowNull;
        this.setFunc = setFunc;
        refresh();
    }
    private void refresh(){
        components.clear();
        add(new Label(0, makeItem(Material.PAPER).setDisplayName(Objects.toString(value))));
        for(int i = 0; i<values.length; i++){
            int idx = i;
            add(new Button(i+1, makeItem(getItem(values[i])).setDisplayName("Set to "+Objects.toString(values[i])), (click) -> {
                if(click!=ClickType.LEFT)return;
                value = values[idx];
                refresh();
            }));
        }
        if(allowNull){
            add(new Button(values.length+1, makeItem(Material.BLACK_CONCRETE).setDisplayName("Set to NULL"), (click) -> {
                if(click!=ClickType.LEFT)return;
                value = null;
                refresh();
            }));
        }
        add(new Button(size-1, makeItem(value==null?Material.BARRIER:Material.GREEN_CONCRETE).setDisplayName(value==null?"Back":"Select"), (click) -> {
            if(click!=ClickType.LEFT)return;
            if(value!=null){
                setFunc.accept(this, value);
            }else{
                open(parent);
            }
        }));
        updateInventory();
    }
    public Material getItem(T value){
        return Material.PAPER;
    }
    private static int getSize(int count, boolean allowNull){
        int actualCount = count+2;
        if(allowNull)actualCount++;
        if(actualCount>54)throw new IllegalArgumentException("MenuSelectEnum only supports up to 52 values! (including null)");
        if(actualCount/9*9==actualCount)return actualCount;
        return actualCount/9*9+9;
    }
}