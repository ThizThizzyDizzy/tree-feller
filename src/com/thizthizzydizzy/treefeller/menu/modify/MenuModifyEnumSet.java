package com.thizthizzydizzy.treefeller.menu.modify;
import com.thizthizzydizzy.simplegui.Button;
import com.thizthizzydizzy.simplegui.ItemBuilder;
import com.thizthizzydizzy.simplegui.Label;
import com.thizthizzydizzy.simplegui.Menu;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.Consumer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;
public class MenuModifyEnumSet<T> extends Menu{//TODO make multi-page
    private HashSet<T> value;
    private final boolean allowNull;
    private final Material[] materials;
    private final Consumer<HashSet<T>> setFunc;
    private final T[] values;
    public MenuModifyEnumSet(Menu parent, Plugin plugin, Player player, String name, String enumName, boolean allowNull, Collection<T> defaultValue, T[] values, Material[] materials, Consumer<HashSet<T>> setFunc){
        super(parent, plugin, player, "Modify "+enumName+" Set ("+name+")", getSize(values.length, allowNull));
        this.value = defaultValue==null?null:new HashSet<>(defaultValue);
        this.allowNull = allowNull;
        this.values = values;
        this.materials = materials;
        refresh();
        this.setFunc = setFunc;
    }
    private void refresh(){
        components.clear();
        ItemBuilder builder = makeItem(Material.PAPER).setDisplayName(value==null?"null":value.size()+" selected:");
        if(value!=null){
            for(T t : value){
                builder.addLore(t.toString());
            }
        }
        add(new Label(0, builder));
        for(int i = 0; i<Math.min(allowNull?51:52, values.length); i++){
            T t = values[i];
            boolean added = false;
            if(value!=null){
                added = value.contains(t);
            }
            add(new Button(i+1, makeItem(materials[i]).setDisplayName(t.toString()).addLore("Left click to add").addLore("Right click to remove").addLore(added?"Added":"Not Added"), (click) -> {
                if(click==ClickType.LEFT){
                    if(value==null)value = new HashSet<>();
                    value.add(t);
                    setFunc.accept(value);
                }
                if(click==ClickType.RIGHT){
                    if(value==null)value = new HashSet<>();
                    value.remove(t);
                    setFunc.accept(value);
                }
                refresh();
            }));
        }
        if(allowNull){
            add(new Button(values.length+1, makeItem(Material.BLACK_CONCRETE).setDisplayName("Set to NULL"), (click) -> {
                if(click!=ClickType.LEFT)return;
                value = null;
                setFunc.accept(value);
                refresh();
            }));
        }
        add(new Button(size-1, makeItem(Material.BARRIER).setDisplayName("Back"), (click) -> {
            if(click!=ClickType.LEFT)return;
            open(parent);
        }));
        updateInventory();
    }
    private static int getSize(int count, boolean allowNull){
        int actualCount = count+2;
        if(allowNull)actualCount++;
        if(actualCount>54)throw new IllegalArgumentException("MenuModifyEnumSet only supports up to 52 values! (including null)");
        if(actualCount/9*9==actualCount)return actualCount;
        return actualCount/9*9+9;
    }
}