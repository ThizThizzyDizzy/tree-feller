package com.thizthizzydizzy.treefeller.core.config.structure;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.config.ISpecialConfigObject;
import com.thizthizzydizzy.treefeller.core.config.structure.section.BreakingConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.CuttingConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.DetectionConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.ResultConfiguration;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
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
            int first = Array.getLength(base);
            int second = Array.getLength(value);
            Object array = Array.newInstance(type.getComponentType(), first+second);
            for(int i = 0; i<first; i++)Array.set(array, i, copy(Array.get(base, i)));
            for(int i = 0; i<second; i++)Array.set(array, first+i, copy(Array.get(value, i)));
            return array;
        }
        if(base instanceof Map){
            if(!combine)return copy(value);
            Map<Object, Object> result = (Map<Object, Object>)copy(base);
            for(Map.Entry<?, ?> entry : ((Map<?, ?>)value).entrySet()){
                // Platform definitions do not necessarily implement value equality.
                result.keySet().removeIf(key -> Objects.equals(mapKey(key), mapKey(entry.getKey())));
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
        return key instanceof ISpecialConfigObject
                ?((ISpecialConfigObject)key).asSimplified():key;
    }
    private static Object copy(Object value) throws Exception{
        if(value==null)return null;
        Class<?> type = value.getClass();
        if(value instanceof Number||value instanceof Boolean||value instanceof String||type.isEnum())return value;
        if(type.isArray()){
            int length = Array.getLength(value);
            Object result = Array.newInstance(type.getComponentType(), length);
            for(int i = 0; i<length; i++)Array.set(result, i, copy(Array.get(value, i)));
            return result;
        }
        if(value instanceof Map){
            Map<Object, Object> result = new LinkedHashMap<>();
            for(Map.Entry<?, ?> entry : ((Map<?, ?>)value).entrySet()){
                // Platform definitions do not necessarily implement value equality.
                result.keySet().removeIf(key -> Objects.equals(mapKey(key), mapKey(entry.getKey())));
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
