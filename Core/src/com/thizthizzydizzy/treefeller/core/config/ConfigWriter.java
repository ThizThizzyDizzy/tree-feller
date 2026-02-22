package com.thizthizzydizzy.treefeller.core.config;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeFellerConfiguration;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
public class ConfigWriter{
    public static void write(Path path, TreeFellerConfiguration config) throws IOException{
        try(BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)){
            writer.write(writeToString(config));
        }
    }
    public static String writeToString(TreeFellerConfiguration config){
        StringBuilder sb = new StringBuilder();
        writeObject(sb, config, 0);
        return sb.toString();
    }
    private static void writeObject(StringBuilder sb, Object obj, int indent){
        if(obj==null)return;
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getFields();
        for(int i = 0; i<fields.length; i++){
            Field field = fields[i];
            try{
                Object value = field.get(obj);
                ConfigComment comment = field.getAnnotation(ConfigComment.class);
                if(comment!=null){
                    if(i>0)sb.append("\n");
                    writeIndent(sb, indent);
                    sb.append("// ");
                    StringBuilder replacement = new StringBuilder().append("\n");
                    for(int ind = 0; ind<indent; ind++)
                        replacement.append("    ");
                    sb.append(comment.value().replace("\n", replacement.append("// ").toString()));
                    sb.append("\n");
                }
                writeIndent(sb, indent);
                if(value==null)sb.append("#");
                sb.append(field.getName());
                sb.append(isNestedObject(value)?" ":" = ");
                if(value!=null)writeValue(sb, value, indent);
                sb.append("\n");
                if(comment!=null)sb.append("\n");
            }catch(IllegalAccessException ex){
                throw new RuntimeException(ex);
            }
        }
    }
    private static void writeValue(StringBuilder sb, Object value, int indent){
        if(value==null){
            return;
        }else if(value instanceof String){
            sb.append('"');
            sb.append(escapeString((String)value));
            sb.append('"');
        }else if(value instanceof Number||value instanceof Boolean){
            sb.append(value.toString());
        }else if(value.getClass().isEnum()){
            sb.append(((Enum<?>)value).name());
        }else if(value.getClass().isArray()){
            int length = java.lang.reflect.Array.getLength(value);
            if(length==0){
                sb.append("[]");
            }else{
                sb.append("[\n");
                for(int i = 0; i<length; i++){
                    Object element = java.lang.reflect.Array.get(value, i);
                    writeIndent(sb, indent+1);
                    writeValue(sb, element, indent+1);
                    if(i<length-1){
                        sb.append(",");
                    }
                    sb.append("\n");
                }
                writeIndent(sb, indent);
                sb.append("]");
            }
        }else if(value instanceof List){
            List<?> list = (List<?>)value;
            if(list.isEmpty()){
                sb.append("[]");
            }else{
                sb.append("[\n");
                for(int i = 0; i<list.size(); i++){
                    Object element = list.get(i);
                    writeIndent(sb, indent+1);
                    writeValue(sb, element, indent+1);
                    if(i<list.size()-1){
                        sb.append(",");
                    }
                    sb.append("\n");
                }
                writeIndent(sb, indent);
                sb.append("]");
            }
        }else if(value instanceof Map){
            Map<?, ?> map = (Map<?, ?>)value;
            if(map.isEmpty()){
                sb.append("{}");
            }else{
                sb.append("{\n");
                int i = 0;
                for(Map.Entry<?, ?> entry : map.entrySet()){
                    writeIndent(sb, indent+1);
                    sb.append('"');
                    sb.append(escapeString(entry.getKey().toString()));
                    sb.append("\" = ");
                    writeValue(sb, entry.getValue(), indent+1);
                    if(i<map.size()-1){
                        sb.append(",");
                    }
                    sb.append("\n");
                    i++;
                }
                writeIndent(sb, indent);
                sb.append("}");
            }
        }else{
            sb.append("{\n");
            writeObject(sb, value, indent+1);
            writeIndent(sb, indent);
            sb.append("}");
        }
    }
    private static void writeIndent(StringBuilder sb, int indent){
        for(int i = 0; i<indent; i++){
            sb.append("    ");
        }
    }
    private static String escapeString(String s){
        return s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }
    private static boolean isNestedObject(Object value){
        if(value==null)return false;
        if(value instanceof String)return false;
        if(value instanceof Number)return false;
        if(value instanceof Boolean)return false;
        if(value.getClass().isEnum())return false;
        if(value.getClass().isArray())return false;
        if(value instanceof List)return false;
        if(value instanceof Map)return true;
        return true;
    }
}
