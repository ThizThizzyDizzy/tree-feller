package com.thizthizzydizzy.treefeller.core.config.structure;
import java.lang.reflect.Field;
public class TreeFellerConfiguration{
    public DebugConfiguration debug = new DebugConfiguration();
    public GlobalConfiguration global = new GlobalConfiguration();
    public ToolConfiguration[] tools;
    public TreeConfiguration[] trees;
    public MessagesConfiguration messages = new MessagesConfiguration();
    
    public static <T> T overlay(T base, T overlay){
        try{
            T combined = (T) base.getClass().getConstructor().newInstance();
            for(Field field : base.getClass().getFields()){
                Object baseValue = field.get(base);
                Object overlayValue = field.get(overlay);
                field.set(combined, overlay==null?baseValue:overlayValue);
            }
            return combined;
        }catch(Exception ex){
            throw new RuntimeException("Could not combine objects of type "+base.getClass().getName()+"!", ex);
        }
    }
}
