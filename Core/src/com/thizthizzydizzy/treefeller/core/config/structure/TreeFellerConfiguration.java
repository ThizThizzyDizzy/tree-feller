package com.thizthizzydizzy.treefeller.core.config.structure;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.config.structure.section.CuttingConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.DetectionConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.ResultConfiguration;
import java.lang.reflect.Field;
import java.util.ArrayList;
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

    private static <T> T overlay(T base, T overlay){
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
            throw new RuntimeException("Could not overlay objects of type "+base.getClass().getName()+"!", ex);
        }
    }
    private static <T> T combine(T... objects){
        ArrayList<T> actualObjects = new ArrayList();
        for(T t : objects){
            if(t!=null)actualObjects.add(t);
        }
        if(actualObjects.isEmpty())return null;
        try{
            T combined = (T)actualObjects.getFirst().getClass().getConstructor().newInstance();
            for(T t : objects){
                for(Field field : combined.getClass().getFields()){
                    Object current = field.get(combined);
                    Object value = field.get(t);
                    if(value==null)continue;
                    if(current==null){
                        field.set(combined, value);
                        continue;
                    }
                    if(field.getType()==Boolean.class){
                        field.set(combined, (Boolean)current||(Boolean)value);
                    }else if(field.getType()==Integer.class){
                        field.set(combined, (Integer)Math.max((Integer)current, (Integer)value));
                    }else if(field.getType()==Float.class){
                        field.set(combined, (Float)Math.max((Float)current, (Float)value));
                    }
                }
            }
            return combined;
        }catch(Exception ex){
            throw new RuntimeException("Could not combine objects of type "+actualObjects.getFirst().getClass().getName()+"!", ex);
        }
    }
    public static CuttingConfiguration getCombinedCuttingConfiguration(TreeConfiguration tree, ToolConfiguration tool){
        return overlay(TreeFellerCore.config.global.cutting, combine(tree.cutting, tool.cutting));
    }
    public static DetectionConfiguration getCombinedDetectionConfiguration(TreeConfiguration tree){
        return overlay(TreeFellerCore.config.global.detection, tree.detection);
    }
    public static ResultConfiguration getCombinedResultConfiguration(TreeConfiguration tree, ToolConfiguration tool){
        return combine(TreeFellerCore.config.global.result, tree.result, tool.result);
    }
}
