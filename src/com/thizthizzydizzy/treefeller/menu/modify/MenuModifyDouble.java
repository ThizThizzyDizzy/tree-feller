package com.thizthizzydizzy.treefeller.menu.modify;
import com.thizthizzydizzy.simplegui.Button;
import com.thizthizzydizzy.simplegui.ItemBuilder;
import com.thizthizzydizzy.simplegui.Menu;
import java.util.Objects;
import java.util.function.Consumer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;
public class MenuModifyDouble extends Menu{
    private Double value;
    private final Button display;
    public MenuModifyDouble(Menu parent, Plugin plugin, Player player, String name, double min, double max, boolean allowNull, Double defaultValue, Consumer<Double> setFunc){
        super(parent, plugin, player, "Modify Double ("+name+")", 9);
        value = defaultValue;
        display = add(new Button(0, new ItemBuilder(Material.PAPER).setDisplayName(Objects.toString(defaultValue)).addLore("Click to set value"), (click) -> {
            if(click!=ClickType.LEFT)return;
            openAnvilGUI(value==null?"0":value.toString(), "Edit "+name, (plyr, string) -> {
                string = string.trim();
                try{
                    value = Math.min(max,Math.max(min,Double.parseDouble(string)));
                }catch(NumberFormatException ex){}
                setFunc.accept(value);
                refresh();
            });
        }));
        add(new Button(2, new ItemBuilder(Material.RED_CONCRETE).setDisplayName("-.1"), (click) -> {
            if(click!=ClickType.LEFT)return;
            if(value==null)value = 0d;
            value = Math.min(max,Math.max(min, value-.1));
            setFunc.accept(value);
                refresh();
        }));
        add(new Button(3, new ItemBuilder(Material.RED_TERRACOTTA).setDisplayName("-.01"), (click) -> {
            if(click!=ClickType.LEFT)return;
            if(value==null)value = 0d;
            value = Math.min(max,Math.max(min, value-.01));
            setFunc.accept(value);
                refresh();
        }));
        if(allowNull){
            add(new Button(4, new ItemBuilder(Material.BLACK_CONCRETE).setDisplayName("Set to NULL"), (click) -> {
                if(click!=ClickType.LEFT)return;
                value = null;
                setFunc.accept(value);
                refresh();
            }));
        }else{
            add(new Button(4, new ItemBuilder(Material.BLACK_CONCRETE).setDisplayName("Set to "+Math.min(max,Math.max(min, 0))), (click) -> {
                if(click!=ClickType.LEFT)return;
                value = Math.min(max,Math.max(min, 0d));
                setFunc.accept(value);
                refresh();
            }));
        }
        add(new Button(5, new ItemBuilder(Material.GREEN_TERRACOTTA).setDisplayName("+.01"), (click) -> {
            if(click!=ClickType.LEFT)return;
            if(value==null)value = 0d;
            value = Math.min(max,Math.max(min, value+.01));
            setFunc.accept(value);
                refresh();
        }));
        add(new Button(6, new ItemBuilder(Material.GREEN_CONCRETE).setDisplayName("+.1"), (click) -> {
            if(click!=ClickType.LEFT)return;
            if(value==null)value = 0d;
            value = Math.min(max,Math.max(min, value+.1));
            setFunc.accept(value);
                refresh();
        }));
        add(new Button(8, new ItemBuilder(Material.BARRIER).setDisplayName("Back"), (click) -> {
            if(click!=ClickType.LEFT)return;
            open(parent);
        }));
    }
    public void refresh(){
        display.label = new ItemBuilder(Material.PAPER).setDisplayName(Objects.toString(value)).addLore("Click to set value").build();
        updateInventory();
    }
}