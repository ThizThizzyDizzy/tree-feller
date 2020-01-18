package com.thizthizzydizzy.treefeller;
public enum FellBehavior{
    BREAK,FALL,FALL_HURT,FALL_BREAK,FALL_HURT_BREAK,INVENTORY,FALL_INVENTORY,FALL_HURT_INVENTORY,NATURAL;
    public static FellBehavior match(String s){
        return valueOf(s.toUpperCase().trim().replace("-", "_"));
    }
}