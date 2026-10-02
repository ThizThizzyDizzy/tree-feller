package com.thizthizzydizzy.treefeller.core.config.structure;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.config.structure.section.BreakingConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.CuttingConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.DetectionConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.ResultConfiguration;
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

    /** Tree/tool booleans use OR and numeric values use max. Nested settings merge
     * recursively; arrays accumulate, maps and other scalar conflicts favor the tool.
     * The combined tree/tool settings override globals without mutating any source. */
    private static Object merge(Object base, Object value, boolean combine) throws Exception{
        if(base==null)return copy(value);
        if(value==null)return copy(base);
        Class<?> type = base.getClass();
        if(base instanceof Boolean)return combine?(Boolean)base||(Boolean)value:value;
        if(base instanceof Integer)return combine?Math.max((Integer)base, (Integer)value):value;
        if(base instanceof Float)return combine?Math.max((Float)base, (Float)value):value;
        if(base instanceof Number||base instanceof String||type.isEnum())return value;
        if(type.isArray()){
            if(!combine)return copy(value);
            int first = java.lang.reflect.Array.getLength(base);
            int second = java.lang.reflect.Array.getLength(value);
            Object array = java.lang.reflect.Array.newInstance(type.getComponentType(), first+second);
            for(int i = 0; i<first; i++)java.lang.reflect.Array.set(array, i, copy(java.lang.reflect.Array.get(base, i)));
            for(int i = 0; i<second; i++)java.lang.reflect.Array.set(array, first+i, copy(java.lang.reflect.Array.get(value, i)));
            return array;
        }
        if(base instanceof java.util.Map){
            if(!combine)return copy(value);
            java.util.Map<Object, Object> result = (java.util.Map<Object, Object>)copy(base);
            for(java.util.Map.Entry<?, ?> entry : ((java.util.Map<?, ?>)value).entrySet()){
                // Platform definitions do not necessarily implement value equality.
                result.keySet().removeIf(key -> java.util.Objects.equals(mapKey(key), mapKey(entry.getKey())));
                result.put(copy(entry.getKey()), copy(entry.getValue()));
            }
            return result;
        }
        Object result = type.getConstructor().newInstance();
        for(Field field : type.getFields())
            field.set(result, merge(field.get(base), field.get(value), combine));
        return result;
    }
    private static Object mapKey(Object key){
        return key instanceof com.thizthizzydizzy.treefeller.core.config.ISpecialConfigObject
                ?((com.thizthizzydizzy.treefeller.core.config.ISpecialConfigObject)key).asSimplified():key;
    }
    private static Object copy(Object value) throws Exception{
        if(value==null)return null;
        Class<?> type = value.getClass();
        if(value instanceof Number||value instanceof Boolean||value instanceof String||type.isEnum())return value;
        if(type.isArray()){
            int length = java.lang.reflect.Array.getLength(value);
            Object result = java.lang.reflect.Array.newInstance(type.getComponentType(), length);
            for(int i = 0; i<length; i++)java.lang.reflect.Array.set(result, i, copy(java.lang.reflect.Array.get(value, i)));
            return result;
        }
        if(value instanceof java.util.Map){
            java.util.Map<Object, Object> result = new java.util.LinkedHashMap<>();
            for(java.util.Map.Entry<?, ?> entry : ((java.util.Map<?, ?>)value).entrySet()){
                // Platform definitions do not necessarily implement value equality.
                result.keySet().removeIf(key -> java.util.Objects.equals(mapKey(key), mapKey(entry.getKey())));
                result.put(copy(entry.getKey()), copy(entry.getValue()));
            }
            return result;
        }
        Object result = type.getConstructor().newInstance();
        for(Field field : type.getFields())field.set(result, copy(field.get(value)));
        return result;
    }
    private static <T> T effective(T global, T tree, T tool){
        try{
            return (T)merge(global, merge(tree, tool, true), false);
        }catch(Exception ex){
            throw new IllegalArgumentException("Could not combine configuration", ex);
        }
    }
    public static CuttingConfiguration getCombinedCuttingConfiguration(TreeConfiguration tree, ToolConfiguration tool){
        return effective(TreeFellerCore.config.global.cutting, tree.cutting, tool.cutting);
    }
    public static DetectionConfiguration getCombinedDetectionConfiguration(TreeConfiguration tree){
        return effective(TreeFellerCore.config.global.detection, tree.detection, null);
    }
    public static BreakingConfiguration getCombinedBreakingConfiguration(TreeConfiguration tree, ToolConfiguration tool){
        return effective(TreeFellerCore.config.global.breaking, tree.breaking, tool.breaking);
    }
    public static ResultConfiguration getCombinedResultConfiguration(TreeConfiguration tree, ToolConfiguration tool){
        return effective(TreeFellerCore.config.global.result, tree.result, tool.result);
    }
}
