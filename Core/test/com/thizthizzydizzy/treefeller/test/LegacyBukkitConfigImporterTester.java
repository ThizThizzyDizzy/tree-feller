package com.thizthizzydizzy.treefeller.test;
import com.thizthizzydizzy.treefeller.core.config.legacy.LegacyBukkitConfigImporter;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.Config;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.ConfigFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
/** Approved legacy configuration and focused option-conversion regressions. */
public final class LegacyBukkitConfigImporterTester{
    public static void main(String[] args) throws Exception{
        separateTrunks();
        fellBehaviors();
        effectLists();
        System.out.println("PASS: separate_trunks scopes, fell behaviors, and effect list conversions.");
        if(java.util.Arrays.asList(args).contains("--conversions"))return;
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
    private static Config imported(Map<String,Object> input){
        LegacyBukkitConfigImporter.Result result=LegacyBukkitConfigImporter.importConfiguration(input,name -> name);
        if(!result.warnings.isEmpty())throw new AssertionError(result.report());
        return ConfigFactory.parseString(result.configuration);
    }
    private static void separateTrunks(){
        for(int global:new int[]{1,2}){
            int tree=3-global;
            Config config=imported(Map.of("max-trunks",global,"trees",List.of(List.of("DARK_OAK_LOG","DARK_OAK_LEAVES",Map.of("max-trunks",tree)))));
            equal(global==1,config.getBoolean("global.detection.separate_trunks"));
            equal(tree==1,config.getConfigList("trees").get(0).getBoolean("detection.separate_trunks"));
        }
        equal(false,imported(Map.of("max-trunks",16)).getBoolean("global.detection.separate_trunks"));
        Config inherited=imported(Map.of("max-trunks",1,"trees",List.of(List.of("OAK_LOG","OAK_LEAVES"))));
        equal(false,inherited.getConfigList("trees").get(0).hasPath("detection.separate_trunks"));
        equal(false,imported(Map.of()).hasPath("global.detection.separate_trunks"));
    }
    private static void fellBehaviors(){
        for(String group:new String[]{"log","leaf"})for(String mode:new String[]{"BREAK","INVENTORY","NATURAL","FALL","FALL_HURT","FALL_BREAK","FALL_HURT_BREAK","FALL_INVENTORY","FALL_HURT_INVENTORY","FALL_NATURAL","FALL_NATURAL_HURT","FALL_NATURAL_BREAK","FALL_NATURAL_HURT_BREAK","FALL_NATURAL_INVENTORY","FALL_NATURAL_HURT_INVENTORY"}){
            Config behavior=imported(Map.of(group+"-behavior",mode)).getConfig("global.breaking."+(group.equals("log")?"trunk":"leaves")+"_behavior");
            boolean entity=mode.startsWith("FALL"),natural=mode.contains("NATURAL"),inventory=mode.contains("INVENTORY");
            equal(natural,behavior.getBoolean("natural"));
            equal(entity,behavior.getBoolean("fall_as_entities"));
            equal(inventory?"INVENTORY":(natural||entity)&&!mode.contains("BREAK")?"PLACE":"BREAK",behavior.getString("landing"));
            equal(mode.contains("HURT")?2:0,behavior.getInt("fall_damage_amount"));
        }
    }
    private static void effectLists(){
        for(Object filter:List.of("spruce_log",List.of("birch_log"),"iron_axe")){
            String location=filter.equals("iron_axe")?"TOOL":"LOGS";
            Config effect=imported(Map.of("effects",List.of(Map.of("name","legacy","type","MARKER","location",location,"filter",filter,"tags","legacy")),"global-effects",List.of("legacy"))).getConfigList("global.result.effects").get(0);
            equal(filter instanceof List?filter:List.of(filter),effect.getStringList("filter"));
            equal(List.of("legacy"),effect.getStringList("tags"));
            equal(location.equals("LOGS")?"TRUNK":"TOOL",effect.getString("location"));
        }
    }
    private static void equal(Object expected,Object actual){
        if(!expected.equals(actual))throw new AssertionError("Expected "+expected+", got "+actual);
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
