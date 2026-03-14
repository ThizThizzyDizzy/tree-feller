package com.thizthizzydizzy.treefeller.core.config.structure.general;

public class IntegerRange{
    public IntegerRange(){
    }
    public IntegerRange(String range){
        if(range.endsWith("+")){
            min = Integer.valueOf(range.substring(0, range.length()-1));
        }
        if(range.substring(1).contains("-")){
            min = Integer.valueOf(range.substring(0, range.indexOf("-", 1)));
            max = Integer.valueOf(range.substring(range.indexOf("-", 1)+1));
        }
    }
    public IntegerRange(Integer min, Integer max){
        this.min = min;
        this.max = max;
    }
    public Integer min;
    public Integer max;
    
    public boolean matches(float value){
        if(min!=null&&max!=null&&min>max)return value>=min||value<=max;
        if(min!=null&&value<min)return false;
        if(max!=null&&value<max)return false;
        return true;
    }
}
