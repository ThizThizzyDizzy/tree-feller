package com.thizthizzydizzy.simplegui;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
public abstract class Component{
    public final int index;
    public Component(int index){
        this.index = index;
    }
    public abstract ItemStack draw();
    public void onClick(ClickType type){}
}