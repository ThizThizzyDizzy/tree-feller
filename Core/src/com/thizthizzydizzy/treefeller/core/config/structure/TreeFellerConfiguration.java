package com.thizthizzydizzy.treefeller.core.config.structure;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import java.lang.reflect.Field;
public class TreeFellerConfiguration{
    public DebugConfiguration debug = new DebugConfiguration();
    public GlobalConfiguration global = new GlobalConfiguration();
    public ToolConfiguration[] tools = new ToolConfiguration[0];
    public TreeConfiguration[] trees = new TreeConfiguration[0];
    public MessagesConfiguration messages = new MessagesConfiguration();

    public TreeFellerConfiguration(){
        if(TreeFellerCore.connector==null){
            throw new IllegalStateException("TreeFellerConfiguration CANNOT be instantiated before TreeFeller has initialized!");
        }
        TreeFellerCore.connector.buildDefaultConfig(this);
    }

    public static <T> T overlay(T base, T overlay){
        if(overlay==null)return base;
        try{
            T combined = (T)base.getClass().getConstructor().newInstance();
            for(Field field : base.getClass().getFields()){
                Object baseValue = field.get(base);
                Object overlayValue = field.get(overlay);
                field.set(combined, overlayValue==null?baseValue:overlayValue);
            }
            return combined;
        }catch(Exception ex){
            throw new RuntimeException("Could not combine objects of type "+base.getClass().getName()+"!", ex);
        }
    }
}
