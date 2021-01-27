package com.thizthizzydizzy.treefeller.menu.modify.special;
import com.thizthizzydizzy.simplegui.Button;
import com.thizthizzydizzy.simplegui.ItemBuilder;
import com.thizthizzydizzy.simplegui.Label;
import com.thizthizzydizzy.simplegui.Menu;
import java.util.Objects;
import java.util.function.Consumer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;
public class MenuModifySpawnSaplings extends Menu{
    private Integer value;
    private final Label display;
    public MenuModifySpawnSaplings(Menu parent, Plugin plugin, Player player, boolean allowNull, Integer defaultValue, Consumer<Integer> setFunc){
        super(parent, plugin, player, "Modify Spawn Saplings", 9);
        value = defaultValue;
        display = add(new Label(0, new ItemBuilder(Material.PAPER).setDisplayName(Objects.toString(defaultValue))));
        if(allowNull){
            add(new Button(2, new ItemBuilder(Material.RED_CONCRETE).setDisplayName("set to NULL"), (click) -> {
                if(click!=ClickType.LEFT)return;
                value = null;
                setFunc.accept(value);
                refresh();
            }));
        }
        add(new Button(3, new ItemBuilder(Material.RED_CONCRETE).setDisplayName("No, only replant if the leaves drop saplings"), (click) -> {
            if(click!=ClickType.LEFT)return;
            value = 0;
            setFunc.accept(value);
            refresh();
        }));
        add(new Button(4, new ItemBuilder(Material.YELLOW_CONCRETE).setDisplayName("Yes, but only if the leaves do not drop enough"), (click) -> {
            if(click!=ClickType.LEFT)return;
            value = 1;
            setFunc.accept(value);
            refresh();
        }));
        add(new Button(5, new ItemBuilder(Material.GREEN_CONCRETE).setDisplayName("Yes, always spawn new saplings"), (click) -> {
            if(click!=ClickType.LEFT)return;
            value = 2;
            setFunc.accept(value);
            refresh();
        }));
        add(new Button(8, new ItemBuilder(Material.BARRIER).setDisplayName("Back"), (click) -> {
            if(click!=ClickType.LEFT)return;
            open(parent);
        }));
    }
    public void refresh(){
        display.label = new ItemBuilder(Material.PAPER).setDisplayName(Objects.toString(value)).build();
        updateInventory();
    }
}