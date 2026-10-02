package com.thizthizzydizzy.treefeller.core.config.structure.general;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class Range{
    public Range(){}
    public Range(double value){
        this.min = (float)value;
        this.max = (float)value;
    }
    public Range(String range){
        String number = "[+-]?(?:\\d+(?:\\.\\d*)?|\\.\\d+)(?:[eE][+-]?\\d+)?";
        Matcher matcher = Pattern.compile("^\\s*("+number+")\\s*(?:([+])|[-]\\s*("+number+"))?\\s*$").matcher(range);
        if(!matcher.matches())throw new IllegalArgumentException("Invalid range: "+range);
        min = Float.valueOf(matcher.group(1));
        max = matcher.group(2)!=null?null:matcher.group(3)!=null?Float.valueOf(matcher.group(3)):min;
    }
    public Range(Float min, Float max){
        this.min = min;
        this.max = max;
    }
    public Float min;
    public Float max;
    public boolean matches(float value){
        if(Float.isNaN(value))return false;
        // Reversed bounds describe a wrapping interval (for example, time of day).
        if(min!=null&&max!=null&&min>max)return value>=min||value<=max;
        if(min!=null&&value<min)return false;
        if(max!=null&&value>max)return false;
        return true;
    }
}
