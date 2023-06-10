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
public class MenuModifyBoolean extends Menu{
    public MenuModifyBoolean(Menu parent, Plugin plugin, Player player, String name, boolean allowNull, Boolean defaultValue, Consumer<Boolean> setFunc){
        super(parent, plugin, player, "Modify Boolean ("+name+")", 9);
        Label label = add(new Label(0, makeItem(Material.PAPER).setDisplayName(Objects.toString(defaultValue))));
        add(new Button(2, makeItem(Material.RED_CONCRETE).setDisplayName("Set to FALSE"), (click) -> {
            if(click!=ClickType.LEFT)return;
            setFunc.accept(false);
            label.label = makeItem(Material.PAPER).setDisplayName(Objects.toString(false)).build();
            updateInventory();
        }));
        if(allowNull)add(new Button(4, makeItem(Material.BLACK_CONCRETE).setDisplayName("Set to NULL"), (click) -> {
            if(click!=ClickType.LEFT)return;
            setFunc.accept(null);
            label.label = makeItem(Material.PAPER).setDisplayName(Objects.toString(null)).build();
            updateInventory();
        }));
        add(new Button(6, makeItem(Material.GREEN_CONCRETE).setDisplayName("Set to TRUE"), (click) -> {
            if(click!=ClickType.LEFT)return;
            setFunc.accept(true);
            label.label = makeItem(Material.PAPER).setDisplayName(Objects.toString(true)).build();
            updateInventory();
        }));
        add(new Button(8, makeItem(Material.BARRIER).setDisplayName("Back"), (click) -> {
            if(click!=ClickType.LEFT)return;
            open(parent);
        }));
    }
}