package com.thizthizzydizzy.treefeller.core.config;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeFellerConfiguration;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.Config;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.ConfigFactory;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.ConfigList;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.ConfigObject;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.ConfigValue;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class ConfigReader{
    public static TreeFellerConfiguration read(Path path){
        TreeFellerConfiguration config = new TreeFellerConfiguration();
        File file = path.toFile();
        if(!file.exists()){
            file.getParentFile().mkdirs();
            try{
                ConfigWriter.write(path, config);
            }catch(IOException ex){
                throw new RuntimeException(ex);
            }
            return config;
        }
        Config rawConfig = ConfigFactory.parseFile(file);
        try{
            parseInto(rawConfig.root(), config);
        }catch(Exception ex){
            // Exception handling varies by platform. Failing to parse the config is generally a critical error, and treefeller should not try to keep loading.
            throw new RuntimeException(ex);
        }
        return config;
    }
    private static void parseInto(ConfigObject config, Object object) throws Exception{
        for(Field field : object.getClass().getFields()){
            for(String key : config.keySet()){
                if(keyMatches(key, field.getName())){
                    parseFieldInto(config.toConfig(), key, config.get(key), field, object);
                }
            }
        }
    }
    private static void parseFieldInto(Config rawConfig, String key, ConfigValue value, Field field, Object object) throws Exception{
        Object parsedValue = parseConfigValue(rawConfig, key, value, field.getType(), field.getGenericType());
        field.set(object, parsedValue);
    }
    private static Object parseConfigValue(Config rawConfig, String key, ConfigValue value, Class<?> targetType, Type genericType) throws Exception{
        switch(value.valueType()){
            case OBJECT:
                if(Map.class.isAssignableFrom(targetType)){
                    if(genericType instanceof ParameterizedType){
                        ParameterizedType paramType = (ParameterizedType)genericType;
                        Class<?> keyType = (Class<?>)paramType.getActualTypeArguments()[0];
                        if(keyType!=String.class)throw new IllegalArgumentException("Invalid key type in Map! Expected String, found "+keyType.getName());
                        Class<?> valueType = (Class<?>)paramType.getActualTypeArguments()[1];
                        Type valueGenericType = paramType.getActualTypeArguments()[1];
                        Map<String, Object> result = new HashMap<>();
                        ConfigObject mapObject = rawConfig.getObject(key);
                        for(String entryKey : mapObject.keySet()){
                            ConfigValue entryValue = mapObject.get(entryKey);
                            Object parsedValue = parseConfigValue(rawConfig, key+"."+entryKey, entryValue, valueType, valueGenericType);
                            result.put(entryKey, parsedValue);
                        }
                        return result;
                    }
                }
                if(targetType.isMemberClass()&&!Modifier.isStatic(targetType.getModifiers()))
                    throw new IllegalArgumentException("Cannot create an instance of a non-static inner class: "+targetType.getName());
                Object instance = targetType.getDeclaredConstructor().newInstance();
                parseInto(rawConfig.getObject(key), instance);
                return instance;
            case LIST:
                ConfigList configList = rawConfig.getList(key);
                if(List.class.isAssignableFrom(targetType)){
                    if(genericType instanceof ParameterizedType){
                        ParameterizedType paramType = (ParameterizedType)genericType;
                        Class<?> elementType = (Class<?>)paramType.getActualTypeArguments()[0];
                        Type elementGenericType = paramType.getActualTypeArguments()[0];
                        List<Object> result = new ArrayList<>();
                        for(int i = 0; i<configList.size(); i++){
                            ConfigValue elementValue = configList.get(i);
                            String elementKey = key+"."+i;
                            result.add(parseConfigValue(rawConfig, elementKey, elementValue, elementType, elementGenericType));
                        }
                        return result;
                    }
                }else if(targetType.isArray()){
                    Class<?> componentType = targetType.getComponentType();
                    Object array = Array.newInstance(componentType, configList.size());
                    for(int i = 0; i<configList.size(); i++){
                        ConfigValue elementValue = configList.get(i);
                        String elementKey = key+"."+i;
                        Array.set(array, i, parseConfigValue(rawConfig, elementKey, elementValue, componentType, componentType));
                    }
                    return array;
                }
                break;
            case STRING:
                if(targetType==String.class)return rawConfig.getString(key);
                break;
            case NUMBER:
                if(targetType==Integer.class||targetType==int.class)
                    return rawConfig.getInt(key);
                if(targetType==Double.class||targetType==double.class)
                    return rawConfig.getDouble(key);
                if(targetType==Long.class||targetType==long.class)
                    return rawConfig.getLong(key);
                break;
            case BOOLEAN:
                if(targetType==Boolean.class||targetType==boolean.class)
                    return rawConfig.getBoolean(key);
                break;
            case NULL:
                return null;
        }
        if(targetType.isEnum()){
            return rawConfig.getEnum((Class<Enum>)targetType, key);
        }
        throw new IllegalArgumentException("Cannot parse "+value.valueType().toString()+" into type "+targetType.getName());
    }
    private static boolean keyMatches(String key, String name){
        key = key.replace('-', '_').replace(' ', '_').replace("_", "");
        name = name.replace('-', '_').replace(' ', '_').replace("_", "");
        return key.equalsIgnoreCase(name);
    }
}
