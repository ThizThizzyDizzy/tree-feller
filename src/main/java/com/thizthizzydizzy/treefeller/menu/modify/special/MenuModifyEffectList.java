package com.thizthizzydizzy.treefeller.menu.modify.special;
import com.thizthizzydizzy.simplegui.Button;
import com.thizthizzydizzy.simplegui.ItemBuilder;
import com.thizthizzydizzy.simplegui.Label;
import com.thizthizzydizzy.simplegui.Menu;
import com.thizthizzydizzy.treefeller.Effect;
import com.thizthizzydizzy.treefeller.TreeFeller;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;
public class MenuModifyEffectList extends Menu{
    private ArrayList<Effect> value;
    private final boolean allowNull;
    private final Consumer<ArrayList<Effect>> setFunc;
    public MenuModifyEffectList(Menu parent, Plugin plugin, Player player, String name, boolean allowNull, ArrayList<Effect> defaultValue, Consumer<ArrayList<Effect>> setFunc){
        super(parent, plugin, player, "Modify Effect List ("+name+")", getSize(allowNull));
        this.value = defaultValue;
        this.allowNull = allowNull;
        refresh();
        this.setFunc = setFunc;
    }
    private void refresh(){
        components.clear();
        ItemBuilder builder = makeItem(Material.PAPER).setDisplayName(value==null?"null":value.size()+" effects:");
        if(value!=null){
            HashMap<Effect, Integer> quantities = new HashMap<>();
            for(Effect e : value){
                quantities.put(e, quantities.containsKey(e)?quantities.get(e)+1:1);
            }
            for(Effect e : quantities.keySet()){
                builder.addLore(e.name+" ("+quantities.get(e)+")");
            }
        }
        add(new Label(0, builder));
        for(int i = 0; i<Math.min(allowNull?51:52, TreeFeller.effects.size()); i++){
            Effect effect = TreeFeller.effects.get(i);
            int count = 0;
            if(value!=null){
                for(Effect e : value){
                    if(e==effect)count++;
                }
            }
            add(new Button(i+1, makeItem(effect.type.getItem()).setDisplayName(effect.name).setCount(count).addLore("Left click to add this effect").addLore("Right click to remove this effect").addLore("Currently added: "+count), (click) -> {
                if(click==ClickType.LEFT){
                    if(value==null)value = new ArrayList<>();
                    value.add(effect);
                    setFunc.accept(value);
                }
                if(click==ClickType.RIGHT){
                    if(value==null)value = new ArrayList<>();
                    value.remove(effect);
                    setFunc.accept(value);
                }
                refresh();
            }));
        }
        if(allowNull){
            add(new Button(TreeFeller.effects.size()+1, makeItem(Material.BLACK_CONCRETE).setDisplayName("Set to NULL"), (click) -> {
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
        int actualCount = TreeFeller.effects.size()+2;
        if(allowNull)actualCount++;
        if(actualCount>54)throw new IllegalArgumentException("MenuModifyEffects only supports up to "+(allowNull?51:52)+" values!");//TODO fix
        if(actualCount/9*9==actualCount)return actualCount;
        return actualCount/9*9+9;
    }
}