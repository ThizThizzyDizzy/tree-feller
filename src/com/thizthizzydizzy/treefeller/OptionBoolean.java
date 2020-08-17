package com.thizthizzydizzy.treefeller;
import java.util.Objects;
public abstract class OptionBoolean extends Option<Boolean>{
    public OptionBoolean(String name, boolean global, boolean tool, boolean tree, Boolean defaultValue){
        super(name, global, tool, tree, defaultValue);
    }
    public OptionBoolean(String name, boolean global, boolean tool, boolean tree, Boolean defaultValue, Object defaultConfigValue){
        super(name, global, tool, tree, defaultValue, defaultConfigValue);
    }
    @Override
    public Boolean load(Object o){
        if(o instanceof String){
            Boolean.valueOf((String)o);
        }
        if(o instanceof Boolean){
            return (Boolean)o;
        }
        if(o instanceof Number){
            return ((Number) o).intValue()>=1;
        }
        return null;
    }
    @Override
    public Boolean get(Tool tool, Tree tree){
        if(toolValues.containsKey(tool)||treeValues.containsKey(tree))return Objects.equals(toolValues.get(tool), true)||Objects.equals(treeValues.get(tree), true);
        return Objects.equals(globalValue, true)||Objects.equals(toolValues.get(tool), true)||Objects.equals(treeValues.get(tree), true);
    }
    /**
     * Checks if the global value is true.
     * @return <code>true</code> if the global value is <code>true</code>, or <code>false</code> otherwise
     */
    public boolean isTrue(){
        return Objects.equals(getValue(), true);
    }
}