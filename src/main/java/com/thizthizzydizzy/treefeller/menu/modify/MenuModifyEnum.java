package com.thizthizzydizzy.treefeller.menu.modify;
import com.thizthizzydizzy.simplegui.Button;
import com.thizthizzydizzy.simplegui.Label;
import com.thizthizzydizzy.simplegui.Menu;
import java.util.Objects;
import java.util.function.Consumer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;
public class MenuModifyEnum<T> extends Menu{//TODO make multi-page
    public MenuModifyEnum(Menu parent, Plugin plugin, Player player, String name, String enumName, boolean allowNull, T defaultValue, T[] values, Consumer<T> setFunc){
        super(parent, plugin, player, "Modify "+enumName+" ("+name+")", getSize(values.length, allowNull));
        Label label = add(new Label(0, makeItem(Material.PAPER).setDisplayName(Objects.toString(defaultValue))));
        for(int i = 0; i<values.length; i++){
            int idx = i;
            add(new Button(i+1, makeItem(getItem(values[i])).setDisplayName("Set to "+Objects.toString(values[i])), (click) -> {
                if(click!=ClickType.LEFT)return;
                setFunc.accept(values[idx]);
                label.label = makeItem(Material.PAPER).setDisplayName(Objects.toString(values[idx])).build();
                updateInventory();
            }));
        }
        if(allowNull){
            add(new Button(values.length+1, makeItem(Material.BLACK_CONCRETE).setDisplayName("Set to NULL"), (click) -> {
                if(click!=ClickType.LEFT)return;
                setFunc.accept(null);
                label.label = makeItem(Material.PAPER).setDisplayName(Objects.toString(null)).build();
                updateInventory();
            }));
        }
        add(new Button(size-1, makeItem(Material.BARRIER).setDisplayName("Back"), (click) -> {
            if(click!=ClickType.LEFT)return;
            open(parent);
        }));
    }
    public Material getItem(T value){
        return Material.PAPER;
    }
    private static int getSize(int count, boolean allowNull){
        int actualCount = count+2;
        if(allowNull)actualCount++;
        if(actualCount>54)throw new IllegalArgumentException("MenuModifyEnum only supports up to 52 values! (including null)");
        if(actualCount/9*9==actualCount)return actualCount;
        return actualCount/9*9+9;
    }
}