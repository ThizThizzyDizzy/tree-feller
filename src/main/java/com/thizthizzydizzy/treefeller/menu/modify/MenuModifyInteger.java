package com.thizthizzydizzy.treefeller.menu.modify;
import com.thizthizzydizzy.simplegui.Button;
import com.thizthizzydizzy.simplegui.Menu;
import java.util.Objects;
import java.util.function.Consumer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;
public class MenuModifyInteger extends Menu{
    private Integer value;
    private final Button display;
    public MenuModifyInteger(Menu parent, Plugin plugin, Player player, String name, int min, int max, boolean allowNull, Integer defaultValue, Consumer<Integer> setFunc){
        super(parent, plugin, player, "Modify Integer ("+name+")", 9);
        value = defaultValue;
        display = add(new Button(0, makeItem(Material.PAPER).setDisplayName(Objects.toString(defaultValue)).addLore("Click to set value"), (click) -> {
            if(click!=ClickType.LEFT)return;
            openAnvilGUI(value==null?"0":value.toString(), "Edit "+name, (plyr, string) -> {
                string = string.trim();
                try{
                    value = Math.min(max,Math.max(min,Integer.parseInt(string)));
                }catch(NumberFormatException ex){}
                setFunc.accept(value);
                refresh();
            });
        }));
        add(new Button(2, makeItem(Material.RED_CONCRETE).setDisplayName("-10"), (click) -> {
            if(click!=ClickType.LEFT)return;
            if(value==null)value = 0;
            value = Math.min(max,Math.max(min, value-10));
            setFunc.accept(value);
                refresh();
        }));
        add(new Button(3, makeItem(Material.RED_TERRACOTTA).setDisplayName("-1"), (click) -> {
            if(click!=ClickType.LEFT)return;
            if(value==null)value = 0;
            value = Math.min(max,Math.max(min, value-1));
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
                value = Math.min(max,Math.max(min, 0));
                setFunc.accept(value);
                refresh();
            }));
        }
        add(new Button(5, makeItem(Material.GREEN_TERRACOTTA).setDisplayName("+1"), (click) -> {
            if(click!=ClickType.LEFT)return;
            if(value==null)value = 0;
            value = Math.min(max,Math.max(min, value+1));
            setFunc.accept(value);
                refresh();
        }));
        add(new Button(6, makeItem(Material.GREEN_CONCRETE).setDisplayName("+10"), (click) -> {
            if(click!=ClickType.LEFT)return;
            if(value==null)value = 0;
            value = Math.min(max,Math.max(min, value+10));
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