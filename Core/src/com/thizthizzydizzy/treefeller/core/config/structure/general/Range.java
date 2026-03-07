package com.thizthizzydizzy.treefeller.core.config.structure.general;

public class Range{
    public Range(){
    }
    public Range(String range){
        if(range.endsWith("+")){
            min = Float.valueOf(range.substring(0, range.length()-1));
        }
        if(range.substring(1).contains("-")){
            min = Float.valueOf(range.substring(0, range.indexOf("-", 1)));
            max = Float.valueOf(range.substring(range.indexOf("-", 1)+1));
        }
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
