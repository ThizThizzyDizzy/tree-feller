package com.thizthizzydizzy.treefeller.menu;
import com.thizthizzydizzy.simplegui.Button;
import com.thizthizzydizzy.simplegui.ItemBuilder;
import com.thizthizzydizzy.simplegui.Label;
import com.thizthizzydizzy.simplegui.Menu;
import com.thizthizzydizzy.treefeller.Message;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;
public class MenuMessageConfiguration extends Menu{
    private final Message message;
    public MenuMessageConfiguration(Menu parent, Plugin plugin, Player player, Message message){
        super(parent, plugin, player, "Message Configuration: "+message.name, 9);
        this.message = message;
        refresh();
    }
    @Override
    public void onOpen(){
        refresh();
    }
    private void refresh(){
        components.clear();
        add(new Label(0, new ItemBuilder(message.icon).setDisplayName(message.name)));
        add(new Button(3, makeItem(Material.DEBUG_STICK).setDisplayName("Debug").addLore(message.debug==null?"null":message.debug), (click) -> {
            if(click==ClickType.LEFT)openAnvilGUI(message.debug==null?"null":message.debug, "Set Debug: "+message.name, (player,str)->{
                message.debug = str;
            });
        }));
        add(new Button(4, makeItem(Material.PAPER).setDisplayName("Chat").addLore(message.chat==null?"null":message.chat), (click) -> {
            if(click==ClickType.LEFT)openAnvilGUI(message.chat==null?"null":message.chat, "Set Chat: "+message.name, (player,str)->{
                message.chat = str.equalsIgnoreCase("null")?null:str;
            });
        }));
        add(new Button(5, makeItem(Material.IRON_SWORD).setDisplayName("Actionbar").addLore(message.actionbar==null?"null":message.actionbar), (click) -> {
            if(click==ClickType.LEFT)openAnvilGUI(message.actionbar==null?"null":message.actionbar, "Set Actionbar: "+message.name, (player,str)->{
                message.actionbar = str.equalsIgnoreCase("null")?null:str;
            });
        }));
        add(new Button(8, makeItem(Material.BARRIER).setDisplayName("Back"), (click) -> {
            if(click==ClickType.LEFT)open(parent);
        }));
        updateInventory();
    }
}