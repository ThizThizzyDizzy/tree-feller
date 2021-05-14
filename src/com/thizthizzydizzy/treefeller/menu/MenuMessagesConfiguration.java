package com.thizthizzydizzy.treefeller.menu;
import com.thizthizzydizzy.simplegui.Button;
import com.thizthizzydizzy.simplegui.ItemBuilder;
import com.thizthizzydizzy.simplegui.Menu;
import com.thizthizzydizzy.treefeller.Message;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;
public class MenuMessagesConfiguration extends Menu{
    private final int page;
    public MenuMessagesConfiguration(Menu parent, Plugin plugin, Player player){
        this(parent, plugin, player, 0);
    }
    private final int TOOLS_PER_PAGE = 45;
    public MenuMessagesConfiguration(Menu parent, Plugin plugin, Player player, int page){
        super(parent, plugin, player, "Messages Configuration", 54);
        this.page = page;
        refresh();
    }
    @Override
    public void onOpen(){
        refresh();
    }
    private void refresh(){
        int pageMin = page*TOOLS_PER_PAGE;
        int pageMax = (page+1)*TOOLS_PER_PAGE;//actually the first index of the next page, but it's used with <
        components.clear();
        add(new Button(size-1, makeItem(Material.BARRIER).setDisplayName("Back"), (click) -> {
            if(click==ClickType.LEFT)open(parent);
        }));
        if(page>0){
            add(new Button(size-9, makeItem(Material.PAPER).setDisplayName("Previous Page"), (click) -> {
                if(click==ClickType.LEFT)open(new MenuMessagesConfiguration(parent, plugin, player, page-1));
            }));
        }
        if(pageMax<Message.messages.size()){
            add(new Button(size-2, makeItem(Material.PAPER).setDisplayName("Next Page"), (click) -> {
                if(click==ClickType.LEFT)open(new MenuMessagesConfiguration(parent, plugin, player, page+1));
            }));
        }
        int index = 0;
        int offset = 0;
        for(int i = 0; i<Math.min(pageMax,Message.messages.size()-offset); i++){
            Message message = Message.messages.get(i+offset);
            if(i<pageMin)continue;
            add(new Button(index, new ItemBuilder(message.icon).setDisplayName(message.name), (click) -> {
                if(click==ClickType.LEFT)open(new MenuMessageConfiguration(this, plugin, player, message));
            }));
            index++;
        }
        updateInventory();
        add(new Button(size-1, makeItem(Material.BARRIER).setDisplayName("Back"), (click) -> {
            if(click==ClickType.LEFT)open(parent);
        }));
    }
}