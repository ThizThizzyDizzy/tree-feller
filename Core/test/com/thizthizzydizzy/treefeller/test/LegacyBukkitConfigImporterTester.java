package com.thizthizzydizzy.treefeller.test;
import com.thizthizzydizzy.treefeller.core.config.legacy.LegacyBukkitConfigImporter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
/** Explicitly approved default-config regression; field variants are ephemeral. */
public final class LegacyBukkitConfigImporterTester{
    public static void main(String[] args) throws Exception{
        Path test = Path.of("test");
        String yaml = new String(Files.readAllBytes(test.resolve("legacy-default.yml")), StandardCharsets.UTF_8);
        // Decode the actual master YAML with Bukkit's parser, without adding a Bukkit
        // runtime dependency to Core. The test classpath supplies the bundled API jar.
        Object parser = Class.forName("org.bukkit.configuration.file.YamlConfiguration").getConstructor().newInstance();
        parser.getClass().getMethod("loadFromString", String.class).invoke(parser, yaml);
        Map<String, Object> input = (Map<String, Object>)yamlValue(parser);
        LegacyBukkitConfigImporter.Result result = LegacyBukkitConfigImporter.importConfiguration(input);
        if(!result.warnings.isEmpty())
            throw new AssertionError("Legacy Bukkit import produced "+result.warnings.size()+" warnings:\n"+String.join("\n",result.warnings));
        System.out.println("Default legacy Bukkit configuration imported without warnings.");
    }
    private static Object yamlValue(Object value) throws Exception{
        if(Class.forName("org.bukkit.configuration.ConfigurationSection").isInstance(value)){
            Map<String, Object> result = new LinkedHashMap<>();
            Set<String> keys = (Set<String>)value.getClass().getMethod("getKeys", boolean.class).invoke(value, false);
            for(String key : keys)result.put(key, yamlValue(value.getClass().getMethod("get", String.class).invoke(value, key)));
            return result;
        }
        if(value instanceof Map){
            Map<String, Object> result = new LinkedHashMap<>();
            for(Map.Entry<?, ?> entry : ((Map<?, ?>)value).entrySet())result.put(String.valueOf(entry.getKey()), yamlValue(entry.getValue()));
            return result;
        }
        if(value instanceof List){
            List<Object> result = new ArrayList<>();
            for(Object entry : (List<?>)value)result.add(yamlValue(entry));
            return result;
        }
        return value;
    }
}
