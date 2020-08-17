package com.thizthizzydizzy.treefeller;
public class Modifier{
    public final Type type;
    public final double value;
    public Modifier(Type type, double value){
        this.type = type;
        this.value = value;
    }
    public static enum Type{
        LOG_MULT,LEAF_MULT,DROPS_MULT;
    }
}