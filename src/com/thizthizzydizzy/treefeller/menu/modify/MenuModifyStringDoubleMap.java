package com.thizthizzydizzy.treefeller.menu.modify;
import com.thizthizzydizzy.simplegui.Button;
import com.thizthizzydizzy.simplegui.Menu;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.function.Consumer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;
public class MenuModifyStringDoubleMap extends Menu{
    private HashMap<String, Double> value;
    private final Consumer<HashMap<String, Double>> setFunc;
    private final double max;
    private final boolean allowNull;
    private final double min;
    private final boolean allowNullDoubles;
    public MenuModifyStringDoubleMap(Menu parent, Plugin plugin, Player player, String name, double min, double max, boolean allowNull, boolean allowNullDoubles, HashMap<String, Double> defaultValue, Consumer<HashMap<String, Double>> setFunc){
        super(parent, plugin, player, "Modify String-Double Map ("+name+")", 54);
        this.value = defaultValue;
        this.setFunc = setFunc;
        this.min = min;
        this.max = max;
        this.allowNull = allowNull;
        this.allowNullDoubles = allowNullDoubles;
        refresh();
    }
    @Override
    public void onOpen(){
        refresh();
    }
    public void refresh(){
        components.clear();
        add(new Button(0, makeItem(Material.GREEN_CONCRETE).setDisplayName("Click to add a String"), (click) -> {
            if(click!=ClickType.LEFT)return;
            openAnvilGUI("", "Add String", (p, str) -> {
                if(value==null)value = new HashMap<>();
                value.put(str, Math.min(max, Math.max(min, 0)));
                setFunc.accept(value);
                refresh();
            });
        }));
        if(value!=null){
            ArrayList<String> strs = new ArrayList<>(value.keySet());
            Collections.sort(strs);
            for(int i = 0; i<Math.min(value.size(), allowNull?51:52); i++){
                int idx = i;
                add(new Button(i+1, makeItem(Material.PAPER).setDisplayName(strs.get(i)+" ("+value.get(strs.get(i))+")").addLore("Left click to modify").addLore("Right click to remove"), (click) -> {
                    if(click==ClickType.LEFT){
                        open(new MenuModifyDouble(this, plugin, player, "Modify "+strs.get(idx), min, max, allowNullDoubles, value.get(strs.get(idx)), (value) -> {
                            this.value.put(strs.get(idx), value);
                        }));
                    }
                    if(click==ClickType.RIGHT){
                        value.remove(strs.get(idx));
                        setFunc.accept(value);
                        refresh();
                    }
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