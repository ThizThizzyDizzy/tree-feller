package com.thizthizzydizzy.treefeller.menu;
import com.thizthizzydizzy.simplegui.Button;
import com.thizthizzydizzy.simplegui.Label;
import com.thizthizzydizzy.simplegui.Menu;
import com.thizthizzydizzy.treefeller.Message;
import com.thizthizzydizzy.treefeller.TreeFeller;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
public class MenuConfiguration extends Menu{
    public MenuConfiguration(Menu parent, TreeFeller plugin, Player player){
        super(parent, plugin, player, "TreeFeller Configuration", 54);
        refresh();
    }
    @Override
    public void onOpen(){
        refresh();
    }
    private void refresh(){
        components.clear();
        for(int i = 0; i<size-18; i++){
            add(new Label(i, makeItem(Material.RED_STAINED_GLASS_PANE).setDisplayName(ChatColor.DARK_RED+"WARNING").addLore("All changes made are applied immediately!").addLore("To revert changes, run /trefeller reload").addLore("To save changes, click the save button below")));
        }
        for(int i = size-18; i<size-9; i++){
            add(new Label(i, makeItem(Material.RED_STAINED_GLASS_PANE).setDisplayName(ChatColor.DARK_RED+"WARNING").addLore("All changes made are applied immediately!").addLore("To revert changes, run /trefeller reload").addLore("To save changes, click the save button")));
        }
        for(int i = size-9; i<size; i++){
            add(new Label(i, makeItem(Material.RED_STAINED_GLASS_PANE).setDisplayName(ChatColor.DARK_RED+"WARNING").addLore("All changes made are applied immediately!").addLore("To revert changes, run /trefeller reload").addLore("To save changes, click the save button above")));
        }
        add(new Button(11, makeItem(Material.GRASS_BLOCK).setDisplayName("Global"), (click) -> {
            if(click==ClickType.LEFT)open(new MenuGlobalConfiguration(this, plugin, player));
        }));
        add(new Button(13, makeItem(Material.IRON_AXE).setDisplayName("Tools").addFlag(ItemFlag.HIDE_ATTRIBUTES).setCount(Math.max(1,TreeFeller.tools.size())), (click) -> {
            if(click==ClickType.LEFT)open(new MenuToolsConfiguration(this, plugin, player));
        }));
        add(new Button(15, makeItem(Material.OAK_SAPLING).setDisplayName("Trees").setCount(TreeFeller.trees.size()), (click) -> {
            if(click==ClickType.LEFT)open(new MenuTreesConfiguration(this, plugin, player));
        }));
        add(new Button(21, makeItem(Material.POTION).setDisplayName("Effects").setCount(TreeFeller.effects.size()).addFlag(ItemFlag.HIDE_POTION_EFFECTS), (click) -> {
            if(click==ClickType.LEFT)open(new MenuEffectsConfiguration(this, plugin, player));
        }));
        add(new Button(23, makeItem(Material.PAPER).setDisplayName("Messages").setCount(Message.messages.size()), (click) -> {
            if(click==ClickType.LEFT)open(new MenuMessagesConfiguration(this, plugin, player));
        }));
        add(new Button(size-14, makeItem(Material.GREEN_CONCRETE).setDisplayName("Save Configuration").addLore("This will overwrite the config.yml file!"), (click) -> {
            save(theConfig).makeItHappen(hurryUp);
        }));
    }
}