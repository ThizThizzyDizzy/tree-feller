package com.thizthizzydizzy.treefeller.menu.modify;
import com.thizthizzydizzy.simplegui.Button;
import com.thizthizzydizzy.simplegui.Menu;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.function.Consumer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;
public class MenuModifyStringSet extends Menu{
    private HashSet<String> value;
    private final Consumer<HashSet<String>> setFunc;
    private final boolean allowNull;
    public MenuModifyStringSet(Menu parent, Plugin plugin, Player player, String name, boolean allowNull, HashSet<String> defaultValue, Consumer<HashSet<String>> setFunc){
        super(parent, plugin, player, "Modify String Set ("+name+")", 54);
        this.value = defaultValue;
        this.setFunc = setFunc;
        this.allowNull = allowNull;
        refresh();
    }
    public void refresh(){
        components.clear();
        add(new Button(0, makeItem(Material.GREEN_CONCRETE).setDisplayName("Click to add a String"), (click) -> {
            if(click!=ClickType.LEFT)return;
            openAnvilGUI("", "Add String", (p, str) -> {
                if(value==null)value = new HashSet<>();
                value.add(str);
                setFunc.accept(value);
                refresh();
            });
        }));
        if(value!=null){
            ArrayList<String> strs = new ArrayList<>(value);
            Collections.sort(strs);
            for(int i = 0; i<Math.min(value.size(), allowNull?51:52); i++){
                int idx = i;
                add(new Button(i+1, makeItem(Material.PAPER).setDisplayName(strs.get(i)), (click) -> {
                    if(click!=ClickType.RIGHT)return;
                    value.remove(strs.get(idx));
                    setFunc.accept(value);
                    refresh();
                }));
            }
        }
        if(allowNull){
            add(new Button(value==null?1:(value.size()+1), makeItem(Material.BLACK_CONCRETE).setDisplayName("Set to NULL"), (click) -> {
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
}