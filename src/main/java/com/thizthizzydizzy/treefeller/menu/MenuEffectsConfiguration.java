package com.thizthizzydizzy.treefeller.menu;
import com.thizthizzydizzy.simplegui.Button;
import com.thizthizzydizzy.simplegui.Menu;
import com.thizthizzydizzy.treefeller.Effect;
import com.thizthizzydizzy.treefeller.Effect.EffectType;
import com.thizthizzydizzy.treefeller.TreeFeller;
import com.thizthizzydizzy.treefeller.menu.select.MenuSelectEnum;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;
public class MenuEffectsConfiguration extends Menu{
    private final int page;
    public MenuEffectsConfiguration(Menu parent, Plugin plugin, Player player){
        this(parent, plugin, player, 0);
    }
    private final int EFFECTS_PER_PAGE = 45;
    public MenuEffectsConfiguration(Menu parent, Plugin plugin, Player player, int page){
        super(parent, plugin, player, "Effects Configuration", 54);
        this.page = page;
        refresh();
    }
    @Override
    public void onOpen(){
        refresh();
    }
    private void refresh(){
        int pageMin = page*EFFECTS_PER_PAGE;
        int pageMax = (page+1)*EFFECTS_PER_PAGE;//actually the first index of the next page, but it's used with <
        components.clear();
        add(new Button(size-1, makeItem(Material.BARRIER).setDisplayName("Back"), (click) -> {
            if(click==ClickType.LEFT)open(parent);
        }));
        add(new Button(size-5, makeItem(Material.GREEN_CONCRETE).setDisplayName("New effect"), (click) -> {
            if(click==ClickType.LEFT){
                open(new MenuSelectEnum<EffectType>(this, plugin, player, "New Effect", "EffectType", false, null, EffectType.values(), (menu, value) -> {
                    Effect effect = Effect.newEffect(value);
                    TreeFeller.effects.add(effect);
                    menu.open(new MenuEffectConfiguration(this, plugin, player, effect));
                }){
                    @Override
                    public Material getItem(EffectType value){
                        return value.getItem();
                    }
                });
            }
        }));
        if(page>0){
            add(new Button(size-9, makeItem(Material.PAPER).setDisplayName("Previous Page"), (click) -> {
                if(click==ClickType.LEFT)open(new MenuEffectsConfiguration(parent, plugin, player, page-1));
            }));
        }
        if(pageMax<TreeFeller.effects.size()){
            add(new Button(size-2, makeItem(Material.PAPER).setDisplayName("Next Page"), (click) -> {
                if(click==ClickType.LEFT)open(new MenuEffectsConfiguration(parent, plugin, player, page+1));
            }));
        }
        int index = 0;
        int offset = 0;
        for(int i = 0; i<Math.min(pageMax,TreeFeller.effects.size()-offset); i++){
            Effect effect = TreeFeller.effects.get(i+offset);
            if(i<pageMin)continue;
            add(new Button(index, makeItem(effect.type.getItem()).setDisplayName(effect.name), (click) -> {
                if(click==ClickType.LEFT)open(new MenuEffectConfiguration(this, plugin, player, effect));
            }));
            index++;
        }
        updateInventory();
        add(new Button(size-1, makeItem(Material.BARRIER).setDisplayName("Back"), (click) -> {
            if(click==ClickType.LEFT)open(parent);
        }));
    }
}