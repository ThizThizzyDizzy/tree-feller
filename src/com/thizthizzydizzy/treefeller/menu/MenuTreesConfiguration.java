package com.thizthizzydizzy.treefeller.menu;
import com.thizthizzydizzy.simplegui.Button;
import com.thizthizzydizzy.simplegui.Menu;
import com.thizthizzydizzy.treefeller.Tree;
import com.thizthizzydizzy.treefeller.TreeFeller;
import com.thizthizzydizzy.treefeller.menu.modify.MenuModifyMaterialSet;
import com.thizthizzydizzy.treefeller.menu.select.MenuSelectMaterialSet;
import java.util.ArrayList;
import java.util.HashSet;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;
public class MenuTreesConfiguration extends Menu{
    private final int page;
    public MenuTreesConfiguration(Menu parent, Plugin plugin, Player player){
        this(parent, plugin, player, 0);
    }
    private final int TREES_PER_PAGE = 45;
    public MenuTreesConfiguration(Menu parent, Plugin plugin, Player player, int page){
        super(parent, plugin, player, "Trees Configuration", 54);
        this.page = page;
        refresh();
    }
    @Override
    public void onOpen(){
        refresh();
    }
    private void refresh(){
        int pageMin = page*TREES_PER_PAGE;
        int pageMax = (page+1)*TREES_PER_PAGE;//actually the first index of the next page, but it's used with <
        components.clear();
        add(new Button(size-1, makeItem(Material.BARRIER).setDisplayName("Back"), (click) -> {
            if(click==ClickType.LEFT)open(parent);
        }));
        add(new Button(size-5, makeItem(Material.GREEN_CONCRETE).setDisplayName("New tree"), (click) -> {
            if(click==ClickType.LEFT){
                open(new MenuSelectMaterialSet(this, plugin, player, "New Tree Trunk", false, "block", new HashSet<>(), (mat) -> {
                    return mat.isItem();
                }, (menu, trunk) -> {
                    if(trunk.isEmpty())return;
                    menu.open(new MenuSelectMaterialSet(this, plugin, player, "New Tree Leaves", false, "block", new HashSet<>(), (mat) -> {
                        return mat.isItem();
                    }, (menu2, leaves) -> {
                        if(leaves.isEmpty())return;
                        Tree tree = new Tree(new ArrayList<>(trunk), new ArrayList<>(leaves));
                        TreeFeller.trees.add(tree);
                        menu2.open(new MenuTreeConfiguration(this, plugin, player, tree));
                    }));
                }));
            }
        }));
        if(page>0){
            add(new Button(size-9, makeItem(Material.PAPER).setDisplayName("Previous Page"), (click) -> {
                if(click==ClickType.LEFT)open(new MenuTreesConfiguration(parent, plugin, player, page-1));
            }));
        }
        if(pageMax<TreeFeller.trees.size()){
            add(new Button(size-2, makeItem(Material.PAPER).setDisplayName("Next Page"), (click) -> {
                if(click==ClickType.LEFT)open(new MenuTreesConfiguration(parent, plugin, player, page+1));
            }));
        }
        int index = 0;
        int offset = 0;
        for(int i = 0; i<Math.min(pageMax,TreeFeller.trees.size()-offset); i++){
            Tree t = TreeFeller.trees.get(i+offset);
            if(i<pageMin)continue;
            add(new Button(index, makeItem(t.getDisplayMaterial()).setDisplayName("Tree #"+i), (click) -> {
                if(click==ClickType.LEFT)open(new MenuTreeConfiguration(this, plugin, player, t));
            }));
            index++;
        }
        updateInventory();
        add(new Button(size-1, makeItem(Material.BARRIER).setDisplayName("Back"), (click) -> {
            if(click==ClickType.LEFT)open(parent);
        }));
    }
}