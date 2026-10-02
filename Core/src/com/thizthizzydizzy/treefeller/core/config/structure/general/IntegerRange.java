package com.thizthizzydizzy.treefeller.core.config.structure.general;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class IntegerRange{
    public IntegerRange(){}
    public IntegerRange(int value){
        this.min = value;
        this.max = value;
    }
    public IntegerRange(String range){
        String number = "[+-]?\\d+";
        Matcher matcher = Pattern.compile("^\\s*("+number+")\\s*(?:([+])|[-]\\s*("+number+"))?\\s*$").matcher(range);
        if(!matcher.matches())throw new IllegalArgumentException("Invalid range: "+range);
        min = Integer.valueOf(matcher.group(1));
        max = matcher.group(2)!=null?null:matcher.group(3)!=null?Integer.valueOf(matcher.group(3)):min;
    }
    public IntegerRange(Integer min, Integer max){
        this.min = min;
        this.max = max;
    }
    public Integer min;
    public Integer max;
    public boolean matches(float value){
        if(Float.isNaN(value))return false;
        // Reversed bounds describe a wrapping interval (for example, time of day).
        if(min!=null&&max!=null&&min>max)return value>=min||value<=max;
        if(min!=null&&value<min)return false;
        if(max!=null&&value>max)return false;
        return true;
    }
}
