package com.thizthizzydizzy.treefeller.core.config.structure.general;

public class Range{
    public Range(){
    }
    public Range(Float min, Float max){
        this.min = min;
        this.max = max;
    }
    public Float min;
    public Float max;
    
    public boolean matches(float value){
        if(min!=null&&max!=null&&min>max)return value>=min||value<=max;
        if(min!=null&&value<min)return false;
        if(max!=null&&value<max)return false;
        return true;
    }
}
