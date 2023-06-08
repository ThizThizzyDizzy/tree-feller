package com.thizthizzydizzy.treefeller;
import com.thizthizzydizzy.treefeller.menu.MenuGlobalConfiguration;
import com.thizthizzydizzy.treefeller.menu.MenuToolConfiguration;
import com.thizthizzydizzy.treefeller.menu.MenuTreeConfiguration;
import com.thizthizzydizzy.treefeller.menu.modify.MenuModifyBoolean;
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
    @Override
    public void setValue(Boolean value){
        if(value==null)value = defaultValue;
        super.setValue(value); //To change body of generated methods, choose Tools | Templates.
    }
    /**
     * Checks if the global value is true.
     * @return <code>true</code> if the global value is <code>true</code>, or <code>false</code> otherwise
     */
    public boolean isTrue(){
        return Objects.equals(getValue(), true);
    }
    @Override
    public void openGlobalModifyMenu(MenuGlobalConfiguration parent){
        parent.open(new MenuModifyBoolean(parent, parent.plugin, parent.player, name, false, globalValue, (value) -> {
            globalValue = value;
        }));
    }
    @Override
    public void openToolModifyMenu(MenuToolConfiguration parent, Tool tool){
        parent.open(new MenuModifyBoolean(parent, parent.plugin, parent.player, name, true, toolValues.get(tool), (value) -> {
            if(value==null)toolValues.remove(tool);
            else toolValues.put(tool, value);
        }));
    }
    @Override
    public void openTreeModifyMenu(MenuTreeConfiguration parent, Tree tree){
        parent.open(new MenuModifyBoolean(parent, parent.plugin, parent.player, name, true, treeValues.get(tree), (value) -> {
            if(value==null)treeValues.remove(tree);
            else treeValues.put(tree, value);
        }));
    }
}