package com.thizthizzydizzy.treefeller.core.config.structure.general;
public class VariableRange{
    public Range value;
    public Range percent;
    
    public boolean matches(float min, float max, float val){
        float percnt = (val-min)/(max-min);
        if(value!=null&&!value.matches(val))return false;
        if(percent!=null&&!percent.matches(percnt))return false;
        return true;
    }
}
