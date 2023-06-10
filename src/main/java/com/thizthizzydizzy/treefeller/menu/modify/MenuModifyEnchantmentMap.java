package com.thizthizzydizzy.treefeller.menu.modify;
import com.thizthizzydizzy.simplegui.Button;
import com.thizthizzydizzy.simplegui.ItemBuilder;
import com.thizthizzydizzy.simplegui.Label;
import com.thizthizzydizzy.simplegui.Menu;
import java.util.HashMap;
import java.util.function.Consumer;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;
public class MenuModifyEnchantmentMap extends Menu{
    private HashMap<Enchantment, Integer> value;
    private final boolean allowNull;
    private final Consumer<HashMap<Enchantment, Integer>> setFunc;
    public MenuModifyEnchantmentMap(Menu parent, Plugin plugin, Player player, String name, boolean allowNull, HashMap<Enchantment, Integer> defaultValue, Consumer<HashMap<Enchantment, Integer>> setFunc){
        super(parent, plugin, player, "Modify Enchantment Map ("+name+")", getSize(allowNull));
        this.value = defaultValue;
        this.allowNull = allowNull;
        refresh();
        this.setFunc = setFunc;
    }
    private void refresh(){
        components.clear();
        ItemBuilder builder = makeItem(Material.PAPER).setDisplayName(value==null?"null":value.size()+" enchantments:");
        if(value!=null){
            for(Enchantment e : value.keySet()){
                builder.addLore(e.toString()+" "+value.get(e));
            }
        }
        add(new Label(0, builder));
        for(int i = 0; i<Math.min(allowNull?51:52, Enchantment.values().length); i++){
            Enchantment enchantment = Enchantment.values()[i];
            Integer level = value==null?null:value.get(enchantment);
            add(new Button(i+1, makeItem(Material.ENCHANTED_BOOK).enchant(enchantment,level==null?1:level).setDisplayName(enchantment.toString()).addLore("Left click to increase enchantment level").addLore("Right click to decrease enchantment level").addLore("Current level: "+(level==null?"null":level)), (click) -> {
                if(click==ClickType.LEFT){
                    if(value==null)value = new HashMap<>();
                    value.put(enchantment, value.containsKey(enchantment)?value.get(enchantment)+1:1);
                    setFunc.accept(value);
                }
                if(click==ClickType.RIGHT){
                    if(value==null)value = new HashMap<>();
                    value.put(enchantment, value.containsKey(enchantment)?value.get(enchantment)-1:0);
                    if(value.get(enchantment)<0)value.remove(enchantment);
                    setFunc.accept(value);
                }
                refresh();
            }));
        }
        if(allowNull){
            add(new Button(Enchantment.values().length+1, makeItem(Material.BLACK_CONCRETE).setDisplayName("Set to NULL"), (click) -> {
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
    private static int getSize(boolean allowNull){
        int actualCount = Enchantment.values().length+2;
        if(allowNull)actualCount++;
        if(actualCount>54)throw new IllegalArgumentException("MenuModifyEnchantments only supports up to "+(allowNull?51:52)+" values!");//TODO fix
        if(actualCount/9*9==actualCount)return actualCount;
        return actualCount/9*9+9;
    }
}