package com.thizthizzydizzy.treefeller.core.config;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeFellerConfiguration;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.Config;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.ConfigFactory;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.ConfigList;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.ConfigObject;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.ConfigValue;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.ConfigValueType;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
public class ConfigReader{
    public static TreeFellerConfiguration readFromString(String str){
        TreeFellerConfiguration config = new TreeFellerConfiguration();
        Config rawConfig = ConfigFactory.parseString(str).resolve();
        try{
            parseInto(rawConfig.root(), config, TreeFellerConfiguration.class, "");
        }catch(Exception ex){
            // Exception handling varies by platform. Failing to parse the config is generally a critical error, and treefeller should not try to keep loading.
            throw new IllegalArgumentException("Could not read Tree Feller configuration: "+ex.getMessage(), ex);
        }
        return config;
    }
    public static TreeFellerConfiguration read(Path path){
        TreeFellerConfiguration config = new TreeFellerConfiguration();
        File file = path.toFile();
        if(!file.exists()){
            try{
                File parent = file.getAbsoluteFile().getParentFile();
                if(parent!=null)Files.createDirectories(parent.toPath());
                ConfigWriter.write(path, config);
            }catch(IOException ex){
                throw new RuntimeException(ex);
            }
            return config;
        }
        Config rawConfig = ConfigFactory.parseFile(file).resolve();
        try{
            parseInto(rawConfig.root(), config, TreeFellerConfiguration.class, "");
        }catch(Exception ex){
            // Exception handling varies by platform. Failing to parse the config is generally a critical error, and treefeller should not try to keep loading.
            throw new IllegalArgumentException("Could not read Tree Feller configuration: "+ex.getMessage(), ex);
        }
        return config;
    }
    private static void parseInto(ConfigObject config, Object object, Type genericType, String path) throws Exception{
        Set<String> matchedKeys = new HashSet<>();
        for(Field field : object.getClass().getFields()){
            boolean assigned = false;
            for(String key : config.keySet()){
                if(keyMatches(key, field.getName())){
                    String fieldPath = path.isEmpty()?key:path+"."+key;
                    if(assigned)throw new IllegalArgumentException("Duplicate configuration field: "+fieldPath);
                    assigned = true;
                    matchedKeys.add(key);
                    parseFieldInto(config.toConfig(), key, config.get(key), field, object, genericType, fieldPath);
                }
            }
        }
        for(String key : config.keySet()){
            if(!matchedKeys.contains(key))
                throw new IllegalArgumentException("Unknown configuration key: "+(path.isEmpty()?key:path+"."+key));
        }
    }
    private static void parseFieldInto(Config rawConfig, String key, ConfigValue value, Field field, Object object, Type genericType, String path) throws Exception{
        try{
            Type fieldType = resolveType(field.getGenericType(), genericType);
            Object parsedValue = parseConfigValue(rawConfig, key, value, rawType(fieldType), fieldType, field.get(object), path);
            if(parsedValue==null&&field.getType().isPrimitive())
                throw new IllegalArgumentException("Cannot set a primitive field to null");
            field.set(object, parsedValue);
        }catch(Exception ex){
            throw new IllegalArgumentException("Invalid configuration at "+path+": "+ex.getMessage(), ex);
        }
    }
    private static Object parseConfigValue(Config rawConfig, String key, ConfigValue value, Class<?> targetType, Type genericType, Object existing, String path) throws Exception{
        if(value.valueType()==ConfigValueType.NULL)return null;
        if(ISpecialConfigObject.class.isAssignableFrom(targetType)){
            targetType = TreeFellerCore.mapSpecialConfigObject(rawConfig, key, value, (Class<? extends ISpecialConfigObject>)targetType);
        }
        if(targetType.isEnum()){
            return rawConfig.getEnum((Class<Enum>)targetType, key);
        }
        switch(value.valueType()){
            case OBJECT:
                if(Map.class.isAssignableFrom(targetType)){
                    if(genericType instanceof ParameterizedType){
                        ParameterizedType paramType = (ParameterizedType)genericType;
                        Type keyGenericType = paramType.getActualTypeArguments()[0];
                        Class<?> keyType = rawType(keyGenericType);
                        Type valueGenericType = paramType.getActualTypeArguments()[1];
                        Class<?> valueType = rawType(valueGenericType);
                        Map<Object, Object> result = new LinkedHashMap<>();
                        ConfigObject mapObject = rawConfig.getObject(key);
                        for(String entryKey : mapObject.keySet()){
                            ConfigValue keyValue = ConfigFactory.parseMap(java.util.Collections.singletonMap("v", entryKey)).getValue("v");
                            Object parsedKey = parseConfigValue(keyValue.atPath("v"), "v", keyValue, keyType, keyGenericType, null, path+"[key]");
                            ConfigValue entryValue = mapObject.get(entryKey);
                            Object parsedValue = parseConfigValue(entryValue.atPath("v"), "v", entryValue, valueType, valueGenericType, null, path+"["+entryKey+"]");
                            result.put(parsedKey, parsedValue);
                        }
                        return result;
                    }
                }
                if(targetType.isMemberClass()&&!Modifier.isStatic(targetType.getModifiers()))
                    throw new IllegalArgumentException("Cannot create an instance of a non-static inner class: "+targetType.getName());
                // Retain initialized defaults when only part of a section is supplied.
                Object instance = existing!=null?existing:targetType.getDeclaredConstructor().newInstance();
                parseInto(rawConfig.getObject(key), instance, genericType, path);
                return instance;
            case LIST:
                ConfigList configList = rawConfig.getList(key);
                if(List.class.isAssignableFrom(targetType)){
                    if(genericType instanceof ParameterizedType){
                        ParameterizedType paramType = (ParameterizedType)genericType;
                        Class<?> elementType = rawType(paramType.getActualTypeArguments()[0]);
                        Type elementGenericType = paramType.getActualTypeArguments()[0];
                        List<Object> result = new ArrayList<>();
                        for(int i = 0; i<configList.size(); i++){
                            ConfigValue elementValue = configList.get(i);
                            result.add(parseConfigValue(elementValue.atPath("v"), "v", elementValue, elementType, elementGenericType, null, path+"["+i+"]"));
                        }
                        return result;
                    }
                }else if(targetType.isArray()){
                    Class<?> componentType = targetType.getComponentType();
                    Object array = Array.newInstance(componentType, configList.size());
                    for(int i = 0; i<configList.size(); i++){
                        ConfigValue elementValue = configList.get(i);
                        Array.set(array, i, parseConfigValue(elementValue.atPath("v"), "v", elementValue, componentType, componentType, null, path+"["+i+"]"));
                    }
                    return array;
                }
                break;
            case STRING:
                if(targetType==String.class)return rawConfig.getString(key);
                return tryBasicConstructors(targetType, rawConfig.getString(key), String.class);
            case NUMBER:
                if(targetType==Integer.class||targetType==int.class)
                    return rawConfig.getInt(key);
                if(targetType==Float.class||targetType==float.class)
                    return (float)rawConfig.getDouble(key);
                if(targetType==Double.class||targetType==double.class)
                    return rawConfig.getDouble(key);
                if(targetType==Long.class||targetType==long.class)
                    return rawConfig.getLong(key);

                // Numeric range shorthand must use the same validation as string shorthand.
                try{
                    return targetType.getConstructor(String.class).newInstance(value.unwrapped().toString());
                }catch(NoSuchMethodException ex){
                    return tryBasicConstructors(targetType, rawConfig.getDouble(key), double.class);
                }
            case BOOLEAN:
                if(targetType==Boolean.class||targetType==boolean.class)
                    return rawConfig.getBoolean(key);
                break;
            case NULL:
                return null;
        }
        throw new IllegalArgumentException("Cannot parse "+value.valueType().toString()+" into type "+targetType.getName());
    }
    private static Object tryBasicConstructors(Class<?> targetType, Object val, Class<?>... argTypes) throws Exception{
        for(Class<?> argType : argTypes){
            Constructor<?> constructor = Arrays.stream(targetType.getConstructors())
                .filter(c -> c.getParameterCount()==1&&(c.getParameterTypes()[0].isAssignableFrom(argType)
                ||(argType==boolean.class&&c.getParameterTypes()[0]==Boolean.class)
                ||(argType==int.class&&c.getParameterTypes()[0]==Integer.class)
                ||(argType==double.class&&c.getParameterTypes()[0]==Double.class)))
                .findFirst()
                .orElse(null);
            if(constructor!=null)return constructor.newInstance(val);
        }
        throw new IllegalArgumentException("Cannot parse "+targetType.toString()+" as any of "+Arrays.toString(argTypes)+", as it has no matching constructors!");
    }
    private static Class<?> rawType(Type type){
        if(type instanceof Class)return (Class<?>)type;
        if(type instanceof ParameterizedType)return (Class<?>)((ParameterizedType)type).getRawType();
        throw new IllegalArgumentException("Unresolved configuration type: "+type);
    }
    private static Type resolveType(Type field, Type context){
        if(field instanceof TypeVariable&&context instanceof ParameterizedType){
            TypeVariable<?>[] variables = rawType(context).getTypeParameters();
            Type[] arguments = ((ParameterizedType)context).getActualTypeArguments();
            for(int i = 0; i<variables.length; i++)if(variables[i].equals(field))return arguments[i];
        }
        if(field instanceof java.lang.reflect.GenericArrayType){
            Type component = resolveType(((java.lang.reflect.GenericArrayType)field).getGenericComponentType(), context);
            return Array.newInstance(rawType(component), 0).getClass();
        }
        return field;
    }
    private static boolean keyMatches(String key, String name){
        key = key.replace('-', '_').replace(' ', '_').replace("_", "");
        name = name.replace('-', '_').replace(' ', '_').replace("_", "");
        return key.equalsIgnoreCase(name);
    }
}
