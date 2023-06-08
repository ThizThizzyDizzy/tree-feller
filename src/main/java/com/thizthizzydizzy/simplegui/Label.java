package com.thizthizzydizzy.simplegui;
import org.bukkit.inventory.ItemStack;
public class Label extends Component{
    public ItemStack label;
    public Label(int index, ItemStack label){
        super(index);
        this.label = label;
    }
    public Label(int index, ItemBuilder label){
        this(index, label.build());
    }
    @Override
    public ItemStack draw(){
        return label;
    }
}