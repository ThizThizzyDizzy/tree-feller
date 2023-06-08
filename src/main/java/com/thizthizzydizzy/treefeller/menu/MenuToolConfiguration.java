package com.thizthizzydizzy.treefeller.menu;
import com.thizthizzydizzy.simplegui.Button;
import com.thizthizzydizzy.simplegui.Menu;
import com.thizthizzydizzy.treefeller.Option;
import com.thizthizzydizzy.treefeller.Tool;
import com.thizthizzydizzy.treefeller.TreeFeller;
import com.thizthizzydizzy.treefeller.menu.modify.MenuModifyMaterial;
import java.util.ArrayList;
import java.util.Objects;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;
public class MenuToolConfiguration extends Menu{
    private final Tool tool;
    private final int page;
    public MenuToolConfiguration(Menu parent, Plugin plugin, Player player, Tool tool){
        this(parent, plugin, player, tool, 0);
    }
    private final int OPTIONS_PER_PAGE = 45;
    public MenuToolConfiguration(Menu parent, Plugin plugin, Player player, Tool tool, int page){
        super(parent, plugin, player, "Tool Configuration", 54);
        this.tool = tool;
        this.page = page;
        refresh();
    }
    @Override
    public void onOpen(){
        refresh();
    }
    private void refresh(){
        int pageMin = page*OPTIONS_PER_PAGE;
        int pageMax = (page+1)*OPTIONS_PER_PAGE;//actually the first index of the next page, but it's used with <
        components.clear();
        add(new Button(size-1, makeItem(Material.BARRIER).setDisplayName("Back").addLore("Shift-right click to delete tool"), (click) -> {
            if(click==ClickType.LEFT)open(parent);
            if(click==ClickType.SHIFT_RIGHT){
                TreeFeller.tools.remove(tool);
                open(parent);
            }
        }));
        add(new Button(size-5, makeItem(tool.material).addLore("Click to modify material"), (click) -> {
            if(click==ClickType.LEFT)open(new MenuModifyMaterial(this, plugin, player, "Modify Tool Material", false, "item", tool.material, (material) -> {
                return material.isItem();
            }, (value) -> {
                tool.material = value;
            }));
        }));
        if(page>0){
            add(new Button(size-9, makeItem(Material.PAPER).setDisplayName("Previous Page"), (click) -> {
                if(click==ClickType.LEFT)open(new MenuToolConfiguration(parent, plugin, player, tool, page-1));
            }));
        }
        if(pageMax<Option.options.size()){
            add(new Button(size-2, makeItem(Material.PAPER).setDisplayName("Next Page"), (click) -> {
                if(click==ClickType.LEFT)open(new MenuToolConfiguration(parent, plugin, player, tool, page+1));
            }));
        }
        int index = 0;
        int offset = 0;
        for(int i = 0; i<Math.min(pageMax,Option.options.size()-offset); i++){
            Option o = Option.options.get(i+offset);
            if(!o.tool){
                i--;
                offset++;
                continue;
            }
            if(i<pageMin)continue;
            add(new Button(index, o.getConfigurationDisplayItem(tool).setDisplayName(o.getFriendlyName()).addLore(ChatColor.GRAY+shorten(Objects.toString(o.getValue(tool)), 42)).addLore(shorten(o.getDescription(true), 42)), (click) -> {
                if(click==ClickType.LEFT)o.openToolModifyMenu(this, tool);
            }));
            index++;
        }
        updateInventory();
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