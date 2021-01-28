package com.thizthizzydizzy.treefeller.menu;
import com.thizthizzydizzy.simplegui.Button;
import com.thizthizzydizzy.simplegui.Menu;
import com.thizthizzydizzy.treefeller.Message;
import com.thizthizzydizzy.treefeller.TreeFeller;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
public class MenuConfiguration extends Menu{
    public MenuConfiguration(Menu parent, TreeFeller plugin, Player player){
        super(parent, plugin, player, "TreeFeller Configuration", 9);
        refresh();
    }
    @Override
    public void onOpen(){
        refresh();
    }
    private void refresh(){
        components.clear();
        add(new Button(0, makeItem(Material.GRASS_BLOCK).setDisplayName("Global"), (click) -> {
            if(click==ClickType.LEFT)open(new MenuGlobalConfiguration(this, plugin, player));
        }));
        add(new Button(2, makeItem(Material.IRON_AXE).setDisplayName("Tools").addFlag(ItemFlag.HIDE_ATTRIBUTES).setCount(Math.max(1,TreeFeller.tools.size())), (click) -> {
            if(click==ClickType.LEFT)open(new MenuToolsConfiguration(this, plugin, player));
        }));
        add(new Button(4, makeItem(Material.OAK_SAPLING).setDisplayName("Trees").setCount(TreeFeller.trees.size()), (click) -> {
            if(click==ClickType.LEFT)open(new MenuTreesConfiguration(this, plugin, player));
        }));
        add(new Button(6, makeItem(Material.POTION).setDisplayName("Effects").setCount(TreeFeller.effects.size()).addFlag(ItemFlag.HIDE_POTION_EFFECTS), (click) -> {
            if(click==ClickType.LEFT)open(new MenuEffectsConfiguration(this, plugin, player));
        }));
        add(new Button(8, makeItem(Material.PAPER).setDisplayName("Messages").setCount(Message.messages.size()), (click) -> {
            if(click==ClickType.LEFT)open(new MenuMessagesConfiguration(this, plugin, player));
        }));
    }
}