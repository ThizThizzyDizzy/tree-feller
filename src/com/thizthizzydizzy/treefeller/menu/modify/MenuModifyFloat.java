package com.thizthizzydizzy.treefeller.menu.modify;
import com.thizthizzydizzy.simplegui.Button;
import com.thizthizzydizzy.simplegui.Menu;
import java.util.Objects;
import java.util.function.Consumer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;
public class MenuModifyFloat extends Menu{
    private Float value;
    private final Button display;
    public MenuModifyFloat(Menu parent, Plugin plugin, Player player, String name, float min, float max, boolean allowNull, Float defaultValue, Consumer<Float> setFunc){
        this(parent, plugin, player, name, min, max, 0.1f, allowNull, defaultValue, setFunc);
    }
    public MenuModifyFloat(Menu parent, Plugin plugin, Player player, String name, float min, float max, float majorIncrement, boolean allowNull, Float defaultValue, Consumer<Float> setFunc){
        this(parent, plugin, player, name, min, max, majorIncrement, majorIncrement/10f, allowNull, defaultValue, setFunc);
    }
    public MenuModifyFloat(Menu parent, Plugin plugin, Player player, String name, float min, float max, float majorIncrement, float minorIncrement, boolean allowNull, Float defaultValue, Consumer<Float> setFunc){
        super(parent, plugin, player, "Modify Float ("+name+")", 9);
        value = defaultValue;
        display = add(new Button(0, makeItem(Material.PAPER).setDisplayName(Objects.toString(defaultValue)).addLore("Click to set value"), (click) -> {
            if(click!=ClickType.LEFT)return;
            openAnvilGUI(value==null?"0":value.toString(), "Edit "+name, (plyr, string) -> {
                string = string.trim();
                try{
                    value = Math.min(max,Math.max(min,Float.parseFloat(string)));
                }catch(NumberFormatException ex){}
                setFunc.accept(value);
                refresh();
            });
        }));
        add(new Button(2, makeItem(Material.RED_CONCRETE).setDisplayName("-"+majorIncrement), (click) -> {
            if(click!=ClickType.LEFT)return;
            if(value==null)value = 0f;
            value = Math.min(max,Math.max(min, value-majorIncrement));
            setFunc.accept(value);
                refresh();
        }));
        add(new Button(3, makeItem(Material.RED_TERRACOTTA).setDisplayName("-"+minorIncrement), (click) -> {
            if(click!=ClickType.LEFT)return;
            if(value==null)value = 0f;
            value = Math.min(max,Math.max(min, value-minorIncrement));
            setFunc.accept(value);
                refresh();
        }));
        if(allowNull){
            add(new Button(4, makeItem(Material.BLACK_CONCRETE).setDisplayName("Set to NULL"), (click) -> {
                if(click!=ClickType.LEFT)return;
                value = null;
                setFunc.accept(value);
                refresh();
            }));
        }else{
            add(new Button(4, makeItem(Material.BLACK_CONCRETE).setDisplayName("Set to "+Math.min(max,Math.max(min, 0))), (click) -> {
                if(click!=ClickType.LEFT)return;
                value = Math.min(max,Math.max(min, 0f));
                setFunc.accept(value);
                refresh();
            }));
        }
        add(new Button(5, makeItem(Material.GREEN_TERRACOTTA).setDisplayName("+"+minorIncrement), (click) -> {
            if(click!=ClickType.LEFT)return;
            if(value==null)value = 0f;
            value = Math.min(max,Math.max(min, value+minorIncrement));
            setFunc.accept(value);
                refresh();
        }));
        add(new Button(6, makeItem(Material.GREEN_CONCRETE).setDisplayName("+"+majorIncrement), (click) -> {
            if(click!=ClickType.LEFT)return;
            if(value==null)value = 0f;
            value = Math.min(max,Math.max(min, value+majorIncrement));
            setFunc.accept(value);
                refresh();
        }));
        add(new Button(8, makeItem(Material.BARRIER).setDisplayName("Back"), (click) -> {
            if(click!=ClickType.LEFT)return;
            open(parent);
        }));
    }
    public void refresh(){
        display.label = makeItem(Material.PAPER).setDisplayName(Objects.toString(value)).addLore("Click to set value").build();
        updateInventory();
    }
}