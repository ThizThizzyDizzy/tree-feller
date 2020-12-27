package com.thizthizzydizzy.treefeller.menu;
import com.thizthizzydizzy.simplegui.Button;
import com.thizthizzydizzy.simplegui.Menu;
import com.thizthizzydizzy.treefeller.Option;
import java.util.ArrayList;
import java.util.Objects;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;
public class MenuGlobalConfiguration extends Menu{
    public MenuGlobalConfiguration(Menu parent, Plugin plugin, Player player){
        this(parent, plugin, player, 0);
    }
    private final int OPTIONS_PER_PAGE = 45;
    public MenuGlobalConfiguration(Menu parent, Plugin plugin, Player player, int page){
        super(parent, plugin, player, "Global Configuration", Math.max(9,Math.min(54,(getGlobalOptionCount()*9/9)+9)));
        int pageMin = page*OPTIONS_PER_PAGE;
        int pageMax = (page+1)*OPTIONS_PER_PAGE;//actually the first index of the next page, but it's used with <
        add(new Button(size-1, makeItem(Material.BARRIER).setDisplayName("Back"), (click) -> {
            if(click==ClickType.LEFT)open(parent);
        }));
        if(page>0){
            add(new Button(size-9, makeItem(Material.PAPER).setDisplayName("Previous Page"), (click) -> {
                open(new MenuGlobalConfiguration(parent, plugin, player, page-1));
            }));
        }
        if(pageMax<Option.options.size()){
            add(new Button(size-2, makeItem(Material.PAPER).setDisplayName("Next Page"), (click) -> {
                open(new MenuGlobalConfiguration(parent, plugin, player, page+1));
            }));
        }
        int index = 0;
        int offset = 0;
        for(int i = 0; i<Math.min(pageMax,Option.options.size()-offset); i++){
            Option o = Option.options.get(i+offset);
            if(!o.global){
                i--;
                offset++;
                continue;
            }
            if(i<pageMin)continue;
            add(new Button(index, o.getConfigurationDisplayItem().setDisplayName(o.getFriendlyName()).addLore(ChatColor.GRAY+shorten(Objects.toString(o.getValue()), 42)).addLore(shorten(o.getDescription(), 42)), (click) -> {
            }));
            index++;
        }
    }
    private static int getGlobalOptionCount(){
        int total = 0;
        for(Option o : Option.options){
            if(o.global)total++;
        }
        return total;
    }
    private ArrayList<String> shorten(ArrayList<String> description, int chars){
        ArrayList<String> output = new ArrayList<>();
        for(String s : description){
            String[] split = s.split(" ");
            String str = "";
            for(int i = 0; i<split.length; i++){
                str+=" "+split[i];
                if(str.length()>chars){
                    output.add(str.trim());
                    str = "";
                }
            }
            if(!str.trim().isEmpty())output.add(str.trim());
        }
        return output;
    }
    private String shorten(String s, int chars){
        if(s.length()<=chars)return s;
        return s.substring(0, chars-3)+"...";
    }
}