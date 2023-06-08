package com.thizthizzydizzy.treefeller.menu;
import com.thizthizzydizzy.simplegui.Button;
import com.thizthizzydizzy.simplegui.Menu;
import com.thizthizzydizzy.treefeller.Option;
import com.thizthizzydizzy.treefeller.Tree;
import com.thizthizzydizzy.treefeller.TreeFeller;
import com.thizthizzydizzy.treefeller.menu.modify.MenuModifyMaterialSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;
public class MenuTreeConfiguration extends Menu{
    private final Tree tree;
    private final int page;
    public MenuTreeConfiguration(Menu parent, Plugin plugin, Player player, Tree tree){
        this(parent, plugin, player, tree, 0);
    }
    private final int OPTIONS_PER_PAGE = 45;
    public MenuTreeConfiguration(Menu parent, Plugin plugin, Player player, Tree tree, int page){
        super(parent, plugin, player, "Tree Configuration", 54);
        this.tree = tree;
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
        add(new Button(size-1, makeItem(Material.BARRIER).setDisplayName("Back").addLore("Shift-right click to delete tree"), (click) -> {
            if(click==ClickType.LEFT)open(parent);
            if(click==ClickType.SHIFT_RIGHT){
                TreeFeller.trees.remove(tree);
                open(parent);
            }
        }));
        add(new Button(size-6, makeItem(tree.trunk.isEmpty()?Material.OAK_LOG:tree.trunk.get(0)).setDisplayName("Click to modify Trunk"), (click) -> {
            if(click==ClickType.LEFT)open(new MenuModifyMaterialSet(this, plugin, player, "Modify Tree Trunk", false, "block", new HashSet<>(tree.trunk), (material) -> {
                return material.isBlock();
            }, (value) -> {
                tree.trunk.clear();
                tree.trunk.addAll(value);
            }));
        }));
        add(new Button(size-4, makeItem(tree.leaves.isEmpty()?Material.OAK_LEAVES:tree.leaves.get(0)).setDisplayName("Click to modify Leaves"), (click) -> {
            if(click==ClickType.LEFT)open(new MenuModifyMaterialSet(this, plugin, player, "Modify Tree Leaves", false, "block", new HashSet<>(tree.leaves), (material) -> {
                return material.isBlock();
            }, (value) -> {
                tree.leaves.clear();
                tree.leaves.addAll(value);
            }));
        }));
        if(page>0){
            add(new Button(size-9, makeItem(Material.PAPER).setDisplayName("Previous Page"), (click) -> {
                if(click==ClickType.LEFT)open(new MenuTreeConfiguration(parent, plugin, player, tree, page-1));
            }));
        }
        if(pageMax<Option.options.size()){
            add(new Button(size-2, makeItem(Material.PAPER).setDisplayName("Next Page"), (click) -> {
                if(click==ClickType.LEFT)open(new MenuTreeConfiguration(parent, plugin, player, tree, page+1));
            }));
        }
        int index = 0;
        int offset = 0;
        for(int i = 0; i<Math.min(pageMax,Option.options.size()-offset); i++){
            Option o = Option.options.get(i+offset);
            if(!o.tree){
                i--;
                offset++;
                continue;
            }
            if(i<pageMin)continue;
            add(new Button(index, o.getConfigurationDisplayItem(tree).setDisplayName(o.getFriendlyName()).addLore(ChatColor.GRAY+shorten(Objects.toString(o.getValue(tree)), 42)).addLore(shorten(o.getDescription(true), 42)), (click) -> {
                if(click==ClickType.LEFT)o.openTreeModifyMenu(this, tree);
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