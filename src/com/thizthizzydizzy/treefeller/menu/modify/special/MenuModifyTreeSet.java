package com.thizthizzydizzy.treefeller.menu.modify.special;
import com.thizthizzydizzy.simplegui.Button;
import com.thizthizzydizzy.simplegui.ItemBuilder;
import com.thizthizzydizzy.simplegui.Label;
import com.thizthizzydizzy.simplegui.Menu;
import com.thizthizzydizzy.treefeller.Tree;
import com.thizthizzydizzy.treefeller.TreeFeller;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.Consumer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;
public class MenuModifyTreeSet extends Menu{
    private HashSet<Tree> value;
    private final boolean allowNull;
    private final Consumer<HashSet<Tree>> setFunc;
    public MenuModifyTreeSet(Menu parent, Plugin plugin, Player player, String name, boolean allowNull, Collection<Tree> defaultValue, Consumer<HashSet<Tree>> setFunc){
        super(parent, plugin, player, "Modify Tree Set ("+name+")", getSize(allowNull));
        this.value = defaultValue==null?null:new HashSet<>(defaultValue);
        this.allowNull = allowNull;
        refresh();
        this.setFunc = setFunc;
    }
    private void refresh(){
        components.clear();
        ItemBuilder builder = makeItem(Material.PAPER).setDisplayName(value==null?"null":value.size()+" trees:");
        if(value!=null){
            for(Tree t : TreeFeller.trees){
                if(value.contains(t)){
                    builder.addLore("Tree #"+TreeFeller.trees.indexOf(t));
                }
            }
        }
        add(new Label(0, builder));
        for(int i = 0; i<Math.min(allowNull?51:52, TreeFeller.trees.size()); i++){
            Tree tree = TreeFeller.trees.get(i);
            boolean added = false;
            if(value!=null){
                added = value.contains(tree);
            }
            add(new Button(i+1, makeItem(tree.getDisplayMaterial()).setDisplayName("Tree #"+i).addLore("Left click to add this tree").addLore("Right click to remove this tree").addLore(added?"Added":"Not Added"), (click) -> {
                if(click==ClickType.LEFT){
                    if(value==null)value = new HashSet<>();
                    value.add(tree);
                    setFunc.accept(value);
                }
                if(click==ClickType.RIGHT){
                    if(value==null)value = new HashSet<>();
                    value.remove(tree);
                    setFunc.accept(value);
                }
                refresh();
            }));
        }
        if(allowNull){
            add(new Button(TreeFeller.trees.size()+1, makeItem(Material.BLACK_CONCRETE).setDisplayName("Set to NULL"), (click) -> {
                if(click!=ClickType.LEFT)return;
                value = null;
                setFunc.accept(value);
                refresh();
            }));
        }
        add(new Button(size-1, makeItem(Material.BARRIER).setDisplayName("Back"), (click) -> {
            if(click!=ClickType.LEFT)return;
            open(parent);
        }));
        updateInventory();
    }
    private static int getSize(boolean allowNull){
        int actualCount = TreeFeller.trees.size()+2;
        if(allowNull)actualCount++;
        if(actualCount>54)throw new IllegalArgumentException("MenuModifyTrees only supports up to "+(allowNull?51:52)+" values!");//TODO fix
        if(actualCount/9*9==actualCount)return actualCount;
        return actualCount/9*9+9;
    }
}