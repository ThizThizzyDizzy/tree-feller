package com.thizthizzydizzy.simplegui;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
public class Button extends Component{
    public ItemStack label;
    private final ClickListener listener;
    public Button(int index, ItemStack label, ClickListener listener){
        super(index);
        this.label = label;
        this.listener = listener;
    }
    public Button(int index, ItemBuilder label, ClickListener listener){
        this(index, label.build(), listener);
    }
    @Override
    public ItemStack draw(){
        return label;
    }
    @Override
    public final void onClick(ClickType type){
        listener.onClick(type);
    }
}