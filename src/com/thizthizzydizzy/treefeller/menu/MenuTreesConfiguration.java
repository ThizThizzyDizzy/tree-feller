package com.thizthizzydizzy.treefeller.menu;
import com.thizthizzydizzy.simplegui.Button;
import com.thizthizzydizzy.simplegui.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;
public class MenuTreesConfiguration extends Menu{
    public MenuTreesConfiguration(Menu parent, Plugin plugin, Player player){
        super(parent, plugin, player, "Trees Configuration", 54);
        add(new Button(size-1, makeItem(Material.BARRIER).setDisplayName("Back"), (click) -> {
            if(click==ClickType.LEFT)open(parent);
        }));
    }
}