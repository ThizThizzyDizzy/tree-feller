package com.thizthizzydizzy.treefeller.menu.modify;
import com.thizthizzydizzy.simplegui.Button;
import com.thizthizzydizzy.simplegui.Label;
import com.thizthizzydizzy.simplegui.Menu;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
public class MenuModifyMaterialMaterialMap extends Menu{//TODO make this multi-page allowing more to be visible
    private HashMap<Material, Material> value;
    private final boolean allowNull;
    private final String keyFilterName;
    private final Predicate<Material> keyFilter;
    private final String valueFilterName;
    private final Predicate<Material> valueFilter;
    private final Consumer<HashMap<Material, Material>> setFunc;
    private Material selected = null;
    public MenuModifyMaterialMaterialMap(Menu parent, Plugin plugin, Player player, String name, String keyFilterName, Predicate<Material> keyFilter, String valueFilterName, Predicate<Material> valueFilter, boolean allowNull, HashMap<Material, Material> defaultValue, Consumer<HashMap<Material, Material>> setFunc){
        super(parent, plugin, player, "Modify Material Map ("+name+")", 54);
        this.value = defaultValue;
        this.setFunc = setFunc;
        this.valueFilter = valueFilter;
        this.allowNull = allowNull;
        this.keyFilterName = keyFilterName;
        this.keyFilter = keyFilter;
        this.valueFilterName = valueFilterName;
        refresh();
    }
    @Override
    public void onOpen(){
        refresh();
    }
    public void refresh(){
        components.clear();
        if(selected==null){
            add(new Label(0, makeItem(Material.PAPER).setDisplayName("Click "+an(keyFilterName)+" "+keyFilterName+" in your inventory to select it.")));
        }else{
            add(new Button(0, makeItem(selected).setDisplayName("Click "+an(valueFilterName)+" "+valueFilterName+" in your inventory to add them to the list.").addLore("Right click to clear selection"), (click)->{
                if(click==ClickType.RIGHT){
                    selected = null;
                    refresh();
                }
            }));
        }
        if(value!=null){
            ArrayList<Material> materials = new ArrayList<>(value.keySet());
            Collections.sort(materials);
            for(int i = 0; i<Math.min(value.size(), allowNull?51:52); i++){
                int idx = i;
                add(new Button(i+1, makeItem(materials.get(i)).addLore("Value: "+value.get(materials.get(i)).toString()).addLore("Right click to remove"), (click)->{
                    if(click==ClickType.RIGHT){
                        value.remove(materials.get(idx));
                        setFunc.accept(value);
                        refresh();
                    }
                }));
            }
        }
        if(allowNull){
            add(new Button(value==null?1:(value.size()+1), makeItem(Material.BLACK_CONCRETE).setDisplayName("Set to NULL"), (click)->{
                if(click!=ClickType.LEFT)return;
                value = null;
                setFunc.accept(value);
                refresh();
            }));
        }
        add(new Button(size-1, makeItem(Material.BARRIER).setDisplayName("Back"), (click)->{
            if(click!=ClickType.LEFT)return;
            open(parent);
        }));
        updateInventory();
    }
    private String an(String s){
        s = s.toLowerCase(Locale.ENGLISH);
        return s.startsWith("a")||s.startsWith("e")||s.startsWith("i")||s.startsWith("o")||s.startsWith("u")?"an":"a";
    }
    @Override
    public void onInventoryClick(int slot, ClickType click){
        if(click!=ClickType.LEFT)return;
        ItemStack stack = player.getInventory().getItem(slot);
        if(stack==null)return;
        if(selected==null){
            if(keyFilter.test(stack.getType())){
                selected = stack.getType();
                refresh();
            }
        }else{
            if(valueFilter.test(stack.getType())){
                if(value==null)value = new HashMap<>();
                value.put(selected, stack.getType());
                selected = null;
                setFunc.accept(value);
                refresh();
            }
        }
    }
}