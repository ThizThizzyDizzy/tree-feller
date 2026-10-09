package com.thizthizzydizzy.treefeller.core.config.legacy;
import com.thizthizzydizzy.treefeller.core.config.structure.MessagesConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeFellerConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.result.EffectConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IBlockDefinition;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.ConfigRenderOptions;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.ConfigValueFactory;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
/** Imports a legacy Bukkit configuration into TreeFeller 2.0's review format.
 * No Bukkit dependency: parsing YAML is an integration hook; all importer logic is here.
 * Only supported schema matches are written as active settings. Unmatched and
 * failed conversions produce warnings; the input configuration is not modified.
 */
public final class LegacyBukkitConfigImporter{
    private static final Map<String, Field> FIELDS = loadFields();
    private LegacyBukkitConfigImporter(){}
    public static final class Result{
        public final String configuration;
        /** Every unrecognized, failed, or uncertain conversion is a warning,
         * including known legacy settings without a v2 equivalent. */
        public final List<String> warnings;
        public final List<String> translated;
        private Result(String configuration, List<String> warnings, List<String> translated){
            this.configuration = configuration;
            this.warnings = Collections.unmodifiableList(new ArrayList<>(warnings));
            this.translated = Collections.unmodifiableList(new ArrayList<>(translated));
        }
        public String report(){
            StringBuilder out = new StringBuilder("# TreeFeller 2.0 legacy Bukkit configuration import\n\n"
                    +"Matches refer to the intended v2 schema, regardless of implementation status.\n"
                    +"Unsupported values are omitted from the candidate and listed below.\n\n## Translated fields\n\n");
            for(String line : translated)out.append("- ").append(line).append('\n');
            out.append("\n## Fields with no match or requiring explanation\n\n");
            if(warnings.isEmpty())out.append("None.\n");
            for(String line : warnings)out.append("- ").append(line).append('\n');
            return out.toString();
        }
        /** CREATE_NEW keeps reruns from replacing an administrator's reviewed draft. */
        public void write(Path config, Path report) throws IOException{
            // Verify both names before writing either; originals and active configs are never touched.
            if(Files.exists(config)||Files.exists(report))throw new IOException("Import output already exists: "+config+" or "+report);
            Files.write(config, configuration.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);
            try{
                Files.write(report, report().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);
            }catch(IOException ex){
                Files.delete(config);
                throw ex;
            }
        }
    }
    private static final class Field{
        final String name, scopes, kind, target;
        Field(LegacyBukkitOption option, String path, java.lang.reflect.Field member){
            name=option.value();
            List<String> scopeNames=new ArrayList<>();
            for(LegacyBukkitOption.Scope scope:option.scopes())scopeNames.add(scope.name().toLowerCase(Locale.ROOT));
            scopes=String.join(",",scopeNames);
            LegacyBukkitOption.Conversion conversion=option.conversion();
            if(conversion==LegacyBukkitOption.Conversion.AUTO)conversion=inferConversion(member,option.suffix());
            kind=conversion.name().toLowerCase(Locale.ROOT).replace('_','-');
            target=path+option.suffix();
            if(name.isEmpty()||scopeNames.isEmpty())throw new IllegalStateException("Incomplete legacy option annotation on "+path);
        }
        boolean sameMapping(Field other){
            return scopes.equals(other.scopes)&&kind.equals(other.kind)&&target.equals(other.target);
        }
    }
    /** The supplied map must contain YAML values, not Bukkit MemorySection objects. */
    public static Result importConfiguration(Map<String, ?> original){
        return importConfiguration(original, name -> {
            String normalized=name.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
            return normalized.startsWith("MINECRAFT:")?normalized.substring(10):normalized;
        });
    }
    /** Bukkit supplies its material registry; all filtering and selector numbering stay in Core. */
    public static Result importConfiguration(Map<String, ?> original, Function<String,String> materialResolver){
        Map<String, Object> effective = restoreBukkitPaths(original);
        Map<String, Object> output = new LinkedHashMap<>();
        List<String> issues = new ArrayList<>(), translated = new ArrayList<>();
        Map<String, Object> global = new LinkedHashMap<>();
        output.put("global", global);
        List<Object> trees = new ArrayList<>(), tools = new ArrayList<>();
        List<Map<String,Object>> treeOptions=new ArrayList<>();
        output.put("trees", trees);output.put("tools", tools);
        int declarationIndex=0;
        for(Object tree : list(effective.get("trees"))){
            String path="trees["+(declarationIndex++)+"]";
            Map<String,Object> result=new LinkedHashMap<>(), options=new LinkedHashMap<>();
            Object trunks, leaves;
            if(tree instanceof String){
                String trunk=(String)tree;
                trunks=trunk;
                leaves=trunk.replace("STRIPPED_", "").replace("LOG", "LEAVES").replace("WOOD", "LEAVES");
            }else if(tree instanceof List&&((List<?>)tree).size()>=2){
                List<?> values=(List<?>)tree;trunks=values.get(0);leaves=values.get(1);
                if(values.size()>2&&values.get(2) instanceof Map)options=copyMap((Map<?,?>)values.get(2));
                else if(values.size()>2)issue(issues,path+"[2]","Expected a tree option map; value retained without importing it.");
                if(values.size()>3)issue(issues,path,"Extra declaration elements have no v2 match; values skipped.");
            }else{issue(issues,path,"V1 ignores this unrecognized tree declaration; original retained.");continue;}
            List<Object> trunkMaterials=resolveMaterials(treeMaterials(trunks,path+".trunk",issues),materialResolver,path+".trunk",issues);
            List<Object> leafMaterials=resolveMaterials(treeMaterials(leaves,path+".leaves",issues),materialResolver,path+".leaves",issues);
            if(trunkMaterials.isEmpty()||leafMaterials.isEmpty()){
                issue(issues,path,"V1 ignores trees with no available trunk or leaf materials; selector numbering uses the remaining trees.");continue;
            }
            result.put("trunk",trunkMaterials);result.put("leaves",leafMaterials);
            trees.add(result);treeOptions.add(options);
            translated.add(path+" → trees["+(trees.size()-1)+"].trunk, leaves (loaded order retained)");
        }
        Map<String,Object> context=copyMap(effective);
        context.put("trees",trees);
        convertScope(effective,global,"global","global",context,issues,translated,output,materialResolver);
        for(int i=0;i<trees.size();i++)convertScope(treeOptions.get(i),(Map<String,Object>)trees.get(i),"tree","trees["+i+"]",context,issues,translated,output,materialResolver);
        declarationIndex=0;
        for(Object tool : list(effective.get("tools"))){
            String path="tools["+(declarationIndex++)+"]";
            Map<String, Object> result=new LinkedHashMap<>();
            Map<String, Object> options=new LinkedHashMap<>();
            Object material=tool;
            if(tool instanceof Map){options=copyMap((Map<?, ?>)tool);material=options.remove("type");}
            if(!(material instanceof String)){issue(issues,path,"Tool type has no clear material match.");continue;}
            String resolved=materialResolver.apply(((String)material).trim());
            if(resolved==null){issue(issues,path,"V1 ignores unavailable tool material: "+material);continue;}
            tools.add(result);
            // V1 AIR is an all-item wildcard, not a test for an empty hand.
            Map<String,Object> item=new LinkedHashMap<>();result.put("item",item);
            if(!"AIR".equals(resolved))item.put("material",resolved);
            translated.add(path+".type → item.material"+(item.isEmpty()?" (AIR wildcard)":""));
            convertScope(options,result,"tool",path,context,issues,translated,output,materialResolver);
        }
        checkScopeInteractions(trees, tools, issues);
        checkRanges(global,"global",issues);
        for(int i=0;i<trees.size();i++)checkRanges((Map<String,Object>)trees.get(i),"trees["+i+"]",issues);
        for(int i=0;i<tools.size();i++)checkRanges((Map<String,Object>)tools.get(i),"tools["+i+"]",issues);
        return new Result(ConfigValueFactory.fromMap(output).render(ConfigRenderOptions.defaults().setJson(false).setOriginComments(false)),issues,translated);
    }
    private static void convertScope(Map<String,Object> raw, Map<String,Object> out, String scope, String path,
            Map<String,Object> global, List<String> issues, List<String> translated, Map<String,Object> root, Function<String,String> materialResolver){
        Map<String,Object> local=new LinkedHashMap<>();
        for(Map.Entry<String,Object> e:raw.entrySet()){
            String name=scope.equals("global")?e.getKey():canonical(e.getKey());
            if(!scope.equals("global")&&local.containsKey(name))issue(issues,path+"."+name,"Duplicate normalized v1 option names; last declaration retained.");
            local.put(name,e.getValue());
        }
        for(Map.Entry<String,Object> entry:local.entrySet()){
            String name=entry.getKey(),at=path+"."+name;Object value=entry.getValue();
            if(scope.equals("global")&&(name.equals("trees")||name.equals("tools")))continue;
            if(scope.equals("global")&&name.equals("effects")){checkEffectDefinitions(value,issues);continue;}
            if(scope.equals("global")&&message(name,value,root,translated,issues))continue;
            Field field=FIELDS.get(name);
            if(field==null){issue(issues,at,"No supported legacy import for this field; value skipped.");continue;}
            if(!Arrays.asList(field.scopes.split(",")).contains(scope)){
                issue(issues,at,"V1 does not apply this option at "+scope+" scope; retained without giving it new behavior.");continue;
            }
            if(value==null){
                if(!scope.equals("global"))issue(issues,at,"Explicit local null can suppress a v1 global fallback; v2 null inherits. Original scope retained for review.");
                continue;
            }
            if(scope.equals("tool")&&field.target.startsWith("detection.")){
                issue(issues,at,"V1 supports per-tool detection, but v2 ToolConfiguration has no detection section.");continue;
            }
            if(!scope.equals("tool")&&field.target.startsWith("item.")){
                issue(issues,at,"V2 has item predicates only on tools; global/tree item requirements need an explicit distribution rule.");continue;
            }
            if(!scope.equals("global")){
                if(name.endsWith("fall-velocity"))issue(issues,at,"V1 adds global/tool/tree velocities; v2 numeric combination does not describe this sum. Candidate retains the scoped value.");
                if(name.equals("log-drop-chance")||name.equals("leaf-drop-chance"))issue(issues,at,"V1 multiplies global/tool/tree drop chances; confirm scoped v2 combination. Candidate retains the scoped multiplier.");
                if((field.target.startsWith("trigger.")||field.target.startsWith("criteria."))&&global.get(name)!=null&&!Objects.equals(global.get(name),value))
                    issue(issues,at,"V1 suppresses this global requirement when a local requirement exists. The v2 schema does not specify that fallback; confirm before applying both requirements.");
            }
            try{
                switch(field.kind){
                    case "boolean":value=booleanValue(value);break;
                    case "integer":value=integerValue(value,name.contains("durability")&&!name.contains("percent"));break;
                    case "float32":value=number(value).floatValue();break;
                    case "float":
                        Number number=number(value);
                        // V1 Double settings cannot be exactly represented by a Float for arbitrary inputs.
                        if(number instanceof Double&&Double.compare((double)number.floatValue(),number.doubleValue())!=0)
                            issue(issues,at,"V2 uses Float; the candidate value is rounded and cannot preserve the original double precision.");
                        value=number.floatValue();break;
                    case "list":
                        value=name.equals("custom-model-data")?Collections.singletonList(integerValue(entry.getValue(),false)):
                                name.equals("required-name")?Collections.singletonList(value.toString()):stringList(value);
                        if(name.equals("roots")||name.equals("grass")||name.equals("overridables")||name.equals("banned-logs")||name.equals("banned-leaves"))
                            value=resolveMaterials((List<Object>)value,materialResolver,at,issues);
                        break;
                    case "conversions":
                        if(value instanceof List)throw new IllegalArgumentException("V1 list-form conversions load MemorySection entries, not ordinary YAML map entries; no clear list-form conversion");
                        value=conversionMap(value);
                        for(Object target:((Map<?,?>)value).values())if(target instanceof String&&((String)target).contains("{"))
                            throw new IllegalArgumentException("NBT-bearing conversion targets have no v2 item/block data match");
                        Map<String,Object> resolvedConversions=new LinkedHashMap<>();
                        for(Map.Entry<String,Object> conversion:((Map<String,Object>)value).entrySet()){
                            String from=materialResolver.apply(conversion.getKey()), to=conversion.getValue() instanceof String?materialResolver.apply((String)conversion.getValue()):null;
                            if(from==null||to==null)issue(issues,at,"Unavailable conversion material skipped: "+conversion);
                            else resolvedConversions.put(from,to);
                        }
                        value=resolvedConversions;
                        break;
                    case "inverse":value=!booleanValue(value);break;
                    case "spawn":
                        int mode=integerValue(value,false);
                        if(mode!=0&&mode!=1)throw new IllegalArgumentException("spawn-saplings=2 always creates new saplings; v2 boolean only describes fallback spawning");
                        value=mode==1;break;
                    case "sapling":
                        List<Object> saplings=stringList(value);
                        if(saplings.size()!=1)throw new IllegalArgumentException("V1 accepts multiple sapling materials; v2 has one sapling_block and one sapling_item");
                        saplings=resolveMaterials(saplings,materialResolver,at,issues);
                        if(saplings.isEmpty())continue;
                        put(out,"sapling_block",saplings.get(0));put(out,"sapling_item",saplings.get(0));
                        translated.add(at+" → sapling_block, sapling_item");continue;
                    case "tree-selector":
                        value=treeSelectors(value,global);
                        if(scope.equals("tree")&&name.equals("cascade-trees")){
                            int index=Integer.parseInt(path.substring(6,path.length()-1));
                            List<Object> loaded=new ArrayList<>();
                            for(Object selected:(List<?>)value){
                                if((Integer)selected<index)loaded.add(selected);
                                else issue(issues,at,"Master loads cascade selectors before adding the current tree; inactive self/forward references are skipped. Confirm intended selector behavior.");
                            }
                            value=loaded;
                        }
                        break;
                    case "direction":direction(out,value);translated.add(at+" → "+field.target+" + breaking.fall_direction_yaw");continue;
                    case "behavior":behavior(out,field.target,value,local,global,at,issues);translated.add(at+" → "+field.target);continue;
                    case "hurt":
                        if(!scope.equals("global"))issue(issues,at,"V1 selects hurt settings independently of log/leaf behavior. Cross-scope HURT behavior needs a joint conversion.");
                        translated.add(at+" → damage field on HURT behaviors; otherwise inactive");continue; // applied only by a HURT behavior; unused values do not create active settings
                    case "effects":
                        put(out,field.target,effects(value,global,at,issues));translated.add(at+" → "+field.target);continue;
                    case "decorations":
                        List<Object> decorationRules=decorations(value,materialResolver,at,issues);
                        if(!scope.equals("global")&&global.get("decorations")!=null){
                            List<Object> allRules=decorations(global.get("decorations"),materialResolver,at,issues);
                            allRules.addAll(decorationRules);decorationRules=allRules;
                        }
                        value=decorationRules;break;
                    case "biomes":
                        if(name.equals("biome-blacklist")){translated.add(at+" → biome list polarity (inactive without a local biome list)");continue;}
                        Object biomeBlacklist=local.get("biome-blacklist");
                        boolean blacklist=biomeBlacklist!=null&&booleanValue(biomeBlacklist);
                        put(out,field.target+"."+(blacklist?"blacklist":"whitelist"),stringList(value));
                        translated.add(at+" → "+field.target);continue;
                    default:break;
                }
                if(name.equals("startup-logs"))put(root,field.target,value);else put(out,field.target,value);
                translated.add(at+" → "+field.target);
            }catch(IllegalArgumentException ex){issue(issues,at,ex.getMessage());}
        }
        // Each v1 range endpoint is its own option. Setting one endpoint does not
        // discard the global fallback for the other endpoint.
        if(!scope.equals("global"))for(Field field:FIELDS.values()){
            if(field.target.endsWith(".min")||field.target.endsWith(".max")){
                String opposite=field.target.substring(0,field.target.lastIndexOf('.'));
                if(has(out,opposite)&&!has(out,field.target)&&global.get(field.name)!=null){
                    try{
                        Object fallback=global.get(field.name);
                        if(field.kind.equals("integer"))fallback=integerValue(fallback,field.name.contains("durability")&&!field.name.contains("percent"));
                        else if(field.kind.equals("float")||field.kind.equals("float32"))fallback=number(fallback).floatValue();
                        if(!field.target.startsWith("item."))put(out,field.target,fallback);
                    }catch(IllegalArgumentException ex){issue(issues,path+"."+field.name,"Global range fallback requires review: "+ex.getMessage());}
                }
            }
        }
    }
    private static void behavior(Map<String,Object> out,String target,Object value,Map<String,Object> local,Map<String,Object> global,String at,List<String> issues){
        String mode=value.toString().trim().toUpperCase(Locale.ROOT).replace('-','_');
        List<String> valid=Arrays.asList("BREAK","INVENTORY","NATURAL","FALL","FALL_HURT","FALL_BREAK","FALL_HURT_BREAK","FALL_INVENTORY","FALL_HURT_INVENTORY","FALL_NATURAL","FALL_NATURAL_HURT","FALL_NATURAL_BREAK","FALL_NATURAL_HURT_BREAK","FALL_NATURAL_INVENTORY","FALL_NATURAL_HURT_INVENTORY");
        if(!valid.contains(mode))throw new IllegalArgumentException("Unknown fell behavior, retained without guessing");
        if(mode.contains("BREAK")&&!mode.equals("BREAK"))throw new IllegalArgumentException("V2 FellBehavior has no break-on-landing switch");
        String otherKey=target.contains("trunk")?"leaf-behavior":"log-behavior";
        String otherMode=String.valueOf(fallback(local,global,otherKey,"BREAK")).trim().toUpperCase(Locale.ROOT).replace('-','_');
        if(mode.contains("INVENTORY")||otherMode.contains("INVENTORY")){
            if(mode.equals("INVENTORY")&&otherMode.equals("INVENTORY")){
                put(out,"result.drop_to_inventory",true);mode="BREAK";
            }else throw new IllegalArgumentException("V1 has separate log/leaf inventory and landing behavior; v2 has a shared drop_to_inventory flag without a landing-action switch");
        }else put(out,"result.drop_to_inventory",false);
        put(out,target+".natural",mode.contains("NATURAL"));put(out,target+".fall_as_entities",mode.startsWith("FALL"));
        boolean hurt=mode.contains("HURT");
        put(out,target+".fall_damage_amount",hurt?number(fallback(local,global,"fall-hurt-amount",2)).floatValue():0f);
        int maximum=integerValue(fallback(local,global,"fall-hurt-max",40),false);
        if((double)(float)maximum!=(double)maximum)issue(issues,at+".fall-hurt-max","V1 integer damage cap cannot be represented exactly by the v2 Float; original retained.");
        put(out,target+".fall_damage_maximum",(float)maximum);
    }
    private static Object fallback(Map<String,Object> local,Map<String,Object> global,String key,Object fallback){
        Object value=local.get(key);if(value==null)value=global.get(key);return value==null?fallback:value;
    }
    private static void direction(Map<String,Object> out,Object value){
        String dir=value.toString().trim().toUpperCase(Locale.ROOT).replace('-','_');
        if(dir.equals("RANDOM")){put(out,"breaking.fall_direction","RANDOM");put(out,"breaking.fall_direction_yaw",0);return;}
        String[] compass={"NORTH","NORTH_EAST","EAST","SOUTH_EAST","SOUTH","SOUTH_WEST","WEST","NORTH_WEST"};
        for(int i=0;i<compass.length;i++)if(dir.equals(compass[i])){
            put(out,"breaking.fall_direction","FIXED");put(out,"breaking.fall_direction_yaw",i*45);return;
        }
        throw new IllegalArgumentException("V1 "+dir+" is relative to the player-to-cut-block vector; v2 RELATIVE does not specify which player vector it uses");
    }
    /** Check all templates, including ones no selector currently uses. */
    private static void checkEffectDefinitions(Object value,List<String> issues){
        int index=0;
        for(Object declaration:list(value)){
            String path="effects["+(index++)+"]";
            if(!(declaration instanceof Map)){issue(issues,path,"Expected an effect definition map; value retained without importing it.");continue;}
            for(Object key:((Map<?,?>)declaration).keySet()){
                if("name".equals(key))continue;
                try{EffectConfiguration.class.getField(String.valueOf(key));}
                catch(NoSuchFieldException ex){issue(issues,path+"."+key,"No v2 effect field; original retained.");}
            }
        }
    }
    private static List<Object> effects(Object selected,Map<String,Object> global,String at,List<String> issues){
        List<Object> result=new ArrayList<>();
        for(Object reference:flatten(selected)){
            if(!(reference instanceof String)){issue(issues,at,"Effect reference is not a name; preserved.");continue;}
            boolean found=false;
            for(Object declaration:list(global.get("effects"))){
                if(!(declaration instanceof Map))continue;
                Map<String,Object> effect=copyMap((Map<?,?>)declaration);
                if(!reference.equals("ALL")&&!reference.toString().equalsIgnoreCase(String.valueOf(effect.get("name"))))continue;
                found=true;
                if(effect.containsKey("filter")){issue(issues,at+"["+effect.get("name")+"]","V2 EffectConfiguration has no material filter.");continue;}
                String label=String.valueOf(effect.remove("name"));
                Set<String> allowed=new HashSet<>(Arrays.asList("chance","location","type","particle","x","y","z","dx","dy","dz","speed","count","r","g","b","size","item","block","sound","volume","pitch","power","fire","permanent","tags"));
                for(String key:new ArrayList<>(effect.keySet()))if(!allowed.contains(key)){
                    issue(issues,at+"["+label+"]."+key,"No v2 effect field; original retained.");effect.remove(key);
                }
                String location=String.valueOf(effect.get("location")).toUpperCase(Locale.ROOT);
                String type=String.valueOf(effect.get("type")).toUpperCase(Locale.ROOT);
                List<String> locations=location.equals("TREE")?Arrays.asList("TRUNK","LEAVES","DECORATION"):Arrays.asList(location.equals("LOGS")?"TRUNK":location);
                effect.put("type",type);effect.putIfAbsent("chance",1);
                if(type.equals("PARTICLE")){effect.putIfAbsent("count",1);}
                if(type.equals("SOUND")){effect.putIfAbsent("volume",1);effect.putIfAbsent("pitch",1);}
                for(String numeric:Arrays.asList("chance","x","y","z","dx","dy","dz","speed")){
                    Object raw=effect.get(numeric);
                    if(raw instanceof Number&&Double.compare(((Number)raw).doubleValue(),(double)((Number)raw).floatValue())!=0)
                        issue(issues,at+"["+label+"]."+numeric,"V1 double cannot be represented exactly by the v2 Float field; original precision retained.");
                }
                for(String loc:locations){Map<String,Object> expanded=new LinkedHashMap<>(effect);expanded.put("location",loc);result.add(expanded);}
            }
            if(!found&&!reference.equals("ALL"))issue(issues,at,"Unknown effect reference "+reference+"; v1 selects no effect for this name.");
        }
        return result;
    }
    private static boolean message(String name,Object value,Map<String,Object> root,List<String> translated,List<String> issues){
        String channel=null;
        for(String prefix:Arrays.asList("debug","chat","actionbar"))if(name.startsWith(prefix+"-"))channel=prefix;
        if(channel==null)return false;
        String suffix=name.substring(channel.length()+1),branch="global";
        List<String> singles=Arrays.asList("toggle","checking","prevent-breakage","prevent-breakage-success","durability-low","partial","partial-tool","protected","success","reload","debug-enable","debug-disable","toggle-on","toggle-off","unknown-command","no-permission","command-usage");
        boolean single=singles.contains(suffix);
        if(!single)for(String end:Arrays.asList("tool","tree","success"))if(suffix.endsWith("-"+end)){
            suffix=suffix.substring(0,suffix.length()-end.length()-1);branch=end;break;
        }
        String target=suffix.equals("protected")?"tree_protected":suffix.replace('-','_');
        try{MessagesConfiguration.class.getField(target);}
        catch(NoSuchFieldException ex){issue(issues,name,"No corresponding v2 message field.");return true;}
        put(root,"messages."+target+(single?"":"."+branch)+"."+channel,value);
        translated.add(name+" → messages."+target+(single?"":"."+branch)+"."+channel);return true;
    }
    private static void checkScopeInteractions(List<Object> trees,List<Object> tools,List<String> issues){
        for(int t=0;t<trees.size();t++)for(int u=0;u<tools.size();u++){
            Map<String,Object> tree=flattenPaths((Map<String,Object>)trees.get(t),"");
            Map<String,Object> tool=flattenPaths((Map<String,Object>)tools.get(u),"");
            for(String path:tree.keySet())if(tool.containsKey(path)&&!Objects.equals(tree.get(path),tool.get(path)))
                issue(issues,"trees["+t+"] + tools["+u+"]."+path,"Both scopes configure different values. V1 uses option-specific reducers; confirm the v2 combined value before activation.");
        }
    }
    private static void checkRanges(Map<String,Object> map,String path,List<String> issues){
        Object min=map.get("min"),max=map.get("max");
        if(min instanceof Number&&max instanceof Number&&((Number)min).doubleValue()>((Number)max).doubleValue())
            issue(issues,path,"V1's separate bounds reject every value when min > max; v2 Range treats reversed bounds as wrapping. No lossless range match.");
        for(Map.Entry<String,Object> entry:map.entrySet())if(entry.getValue() instanceof Map)
            checkRanges((Map<String,Object>)entry.getValue(),path+"."+entry.getKey(),issues);
    }
    private static Map<String,Object> flattenPaths(Map<String,Object> source,String prefix){
        Map<String,Object> result=new LinkedHashMap<>();
        for(Map.Entry<String,Object> e:source.entrySet()){
            String path=prefix.isEmpty()?e.getKey():prefix+"."+e.getKey();
            if(e.getValue() instanceof Map)result.putAll(flattenPaths((Map<String,Object>)e.getValue(),path));
            else result.put(path,e.getValue());
        }
        return result;
    }
    private static boolean has(Map<String,Object> map,String path){
        String[] keys=path.split("\\.");
        Object value=map;
        for(String key:keys){if(!(value instanceof Map)||!((Map<?,?>)value).containsKey(key))return false;value=((Map<?,?>)value).get(key);}
        return true;
    }
    private static int integerValue(Object value,boolean shortValue){
        if(value instanceof Number)return shortValue?((Number)value).shortValue():((Number)value).intValue();
        if(value instanceof String)try{return shortValue?Short.parseShort((String)value):Integer.parseInt((String)value);}catch(NumberFormatException ex){}
        throw new IllegalArgumentException("V1 does not load this value as an integer; retained without coercion: "+value);
    }
    private static List<Object> treeSelectors(Object value,Map<String,Object> global){
        List<Object> selected=new ArrayList<>(),trees=list(global.get("trees"));
        for(Object selector:flatten(value)){
            if(selector instanceof Integer){int i=(Integer)selector;if(i>=0&&i<trees.size())selected.add(i);}
            else if(selector instanceof String){
                String name=((String)selector).toUpperCase(Locale.ROOT).replace(" ","_");
                for(int i=0;i<trees.size();i++){
                    Object tree=trees.get(i),trunk=tree instanceof Map?((Map<?,?>)tree).get("trunk"):tree;
                    for(Object material:list(trunk))if(material instanceof String&&material.toString().toUpperCase(Locale.ROOT).contains(name)){selected.add(i);break;}
                }
            }else throw new IllegalArgumentException("No valid v1 tree selector for "+selector);
        }
        return selected;
    }
    private static List<Object> decorations(Object value,Function<String,String> materialResolver,String path,List<String> issues){
        List<Object> result=new ArrayList<>();
        for(Object name:flatten(value)){
            String key=name.toString().toLowerCase(Locale.ROOT);
            String[] blocks;String direction;boolean column=false;
            switch(key){
                case "snow":blocks=new String[]{"SNOW"};direction="UP";break;
                case "moss":blocks=new String[]{"MOSS_CARPET"};direction="UP";break;
                case "cocoa":blocks=new String[]{"COCOA"};direction="SIDE";break;
                case "pale moss carpet":blocks=new String[]{"PALE_MOSS_CARPET"};direction="SIDE";break;
                case "resin clump":blocks=new String[]{"RESIN_CLUMP"};direction="ALL";break;
                case "vines":blocks=new String[]{"VINE"};direction="SIDE";column=true;break;
                case "weeping vines":blocks=new String[]{"WEEPING_VINES","WEEPING_VINES_PLANT"};direction="DOWN";column=true;break;
                case "pale hanging moss":blocks=new String[]{"PALE_HANGING_MOSS"};direction="DOWN";column=true;break;
                default:blocks=new String[]{name.toString()};direction="ALL";break;
            }
            List<Object> resolvedBlocks=resolveMaterials(new ArrayList<Object>(Arrays.asList(blocks)),materialResolver,path,issues);
            if(resolvedBlocks.isEmpty())continue;
            Map<String,Object> rule=new LinkedHashMap<>();rule.put("blocks",resolvedBlocks);rule.put("direction",direction);rule.put("column",column);result.add(rule);
        }
        return result;
    }
    private static String canonical(String name){
        String normalized=normalize(name);
        for(String key:FIELDS.keySet())if(normalize(key).equals(normalized)&&!key.equals("global-effects"))return key;
        return name;
    }
    private static String normalize(String name){return name.toLowerCase(Locale.ROOT).replace("-","").replace("_","").replace(" ","");}
    private static Number number(Object value){
        if(value instanceof Number)return (Number)value;
        if(value instanceof String)try{return Double.valueOf((String)value);}catch(NumberFormatException ex){}
        throw new IllegalArgumentException("Numeric value has no clear conversion: "+value);
    }
    private static boolean booleanValue(Object value){
        if(value instanceof Boolean)return (Boolean)value;
        if(value instanceof Number)return ((Number)value).intValue()>=1;
        // V1 OptionBoolean.load does not return Boolean.valueOf for strings.
        throw new IllegalArgumentException("V1 does not load quoted booleans as booleans; retained without changing behavior");
    }
    private static List<Object> list(Object value){
        if(value==null)return Collections.emptyList();
        if(value instanceof List)return (List<Object>)value;
        if(value instanceof Iterable){List<Object> result=new ArrayList<>();for(Object item:(Iterable<?>)value)result.add(item);return result;}
        return Collections.singletonList(value);
    }
    private static List<Object> treeMaterials(Object value,String path,List<String> issues){
        List<Object> result=new ArrayList<>();
        for(Object material:list(value)){
            if(material instanceof String)result.add(material);
            else issue(issues,path,"V1 ignores non-string material-list entries; original retained without adding new trunk/leaves.");
        }
        if(result.isEmpty())issue(issues,path,"No v1 material strings in this declaration; retained for review.");
        return result;
    }
    private static List<Object> resolveMaterials(List<Object> values,Function<String,String> resolver,String path,List<String> issues){
        List<Object> result=new ArrayList<>();
        for(Object value:values){
            String material=value instanceof String?resolver.apply((String)value):null;
            if(material==null)issue(issues,path,"V1 ignores unavailable material: "+value+"; value skipped.");
            else if(!result.contains(material))result.add(material);
        }
        return result;
    }
    private static List<Object> stringList(Object value){
        List<Object> values=flatten(value);
        for(Object item:values)if(!(item instanceof String))throw new IllegalArgumentException("V1 requires string values here; original retained without assigning a different meaning");
        return values;
    }
    private static List<Object> flatten(Object value){
        List<Object> result=new ArrayList<>();
        for(Object item:list(value))if(item instanceof Iterable)result.addAll(flatten(item));else result.add(item);
        return result;
    }
    private static Map<String,Object> conversionMap(Object value){
        Map<String,Object> result=new LinkedHashMap<>();
        for(Object entry:list(value)){
            if(!(entry instanceof Map))throw new IllegalArgumentException("Expected map or list of maps");
            result.putAll(copyMap((Map<?,?>)entry));
        }
        return result;
    }
    private static Map<String,Object> restoreBukkitPaths(Map<String,?> source){
        Map<String,Object> restored=copyMap(source);
        // Bukkit represents dotted YAML keys as nested ConfigurationSections.
        // Rejoin only known global option names; arbitrary unknown maps remain intact.
        for(String key:FIELDS.keySet())if(key.contains(".")&&!restored.containsKey(key)){
            String[] parts=key.split("\\.");
            Map<String,Object> map=restored;
            List<Map<String,Object>> parents=new ArrayList<>();
            boolean found=true;
            for(int i=0;i<parts.length-1;i++){
                parents.add(map);
                Object next=map.get(parts[i]);
                if(!(next instanceof Map)){found=false;break;}
                map=(Map<String,Object>)next;
            }
            if(found&&map.containsKey(parts[parts.length-1])){
                restored.put(key,map.remove(parts[parts.length-1]));
                for(int i=parts.length-2;i>=0&&map.isEmpty();i--){
                    map=parents.get(i);map.remove(parts[i]);
                }
            }
        }
        return restored;
    }
    private static Map<String,Object> copyMap(Map<?,?> map){
        Map<String,Object> copy=new LinkedHashMap<>();
        for(Map.Entry<?,?> entry:map.entrySet()){
            if(!(entry.getKey() instanceof String))throw new IllegalArgumentException("Configuration map keys must be strings");
            copy.put((String)entry.getKey(),deepCopy(entry.getValue()));
        }
        return copy;
    }
    private static Object deepCopy(Object value){
        if(value instanceof Map)return copyMap((Map<?,?>)value);
        if(value instanceof List){List<Object> list=new ArrayList<>();for(Object item:(List<?>)value)list.add(deepCopy(item));return list;}
        if(value instanceof Set){Set<Object> set=new LinkedHashSet<>();for(Object item:(Set<?>)value)set.add(deepCopy(item));return set;}
        return value;
    }
    private static void put(Map<String,Object> map,String path,Object value){
        String[] keys=path.split("\\.");
        for(int i=0;i<keys.length-1;i++){
            Object next=map.get(keys[i]);
            if(!(next instanceof Map)){next=new LinkedHashMap<String,Object>();map.put(keys[i],next);}
            map=(Map<String,Object>)next;
        }
        map.put(keys[keys.length-1],value);
    }
    private static void issue(List<String> issues,String path,String reason){String issue="`"+path+"`: "+reason;if(!issues.contains(issue))issues.add(issue);}
    private static Map<String,Field> loadFields(){
        // Stable ordering, independent of the JVM's reflection field order.
        Map<String,Field> fields=new TreeMap<>();
        collectFields(TreeFellerConfiguration.class,"",fields,new ArrayList<>());
        return Collections.unmodifiableMap(fields);
    }
    /** Inspect types only: import metadata must not depend on initialized platform
     * connectors, instantiated config defaults, or optional sections being present.
     */
    private static void collectFields(Class<?> type, String path, Map<String,Field> fields, List<Class<?>> ancestors){
        if(!type.getName().startsWith("com.thizthizzydizzy.treefeller.core.config.structure.")||ancestors.contains(type))return;
        ancestors.add(type);
        for(java.lang.reflect.Field member:type.getFields()){
            if(Modifier.isStatic(member.getModifiers()))continue;
            String memberPath=path.isEmpty()?member.getName():path+"."+member.getName();
            String destination=memberPath.replaceFirst("^(global|tools|trees)\\.","");
            for(LegacyBukkitOption option:member.getAnnotationsByType(LegacyBukkitOption.class)){
                Field field=new Field(option,destination,member);
                Field previous=fields.putIfAbsent(field.name,field);
                // Shared sections appear beneath global, tools and trees. They
                // must describe the same relative destination at each occurrence.
                if(previous!=null&&!previous.sameMapping(field))
                    throw new IllegalStateException("Conflicting legacy option annotations: "+field.name+" at "+memberPath);
            }
            Class<?> nested=member.getType();
            if(nested.isArray())nested=nested.getComponentType();
            collectFields(nested,memberPath,fields,ancestors);
        }
        ancestors.remove(ancestors.size()-1);
    }
    private static LegacyBukkitOption.Conversion inferConversion(java.lang.reflect.Field member,String suffix){
        Type destination=member.getGenericType();
        try{
            if(!suffix.isEmpty())for(String key:suffix.substring(1).split("\\.")){
                Class<?> container=destination instanceof Class?(Class<?>)destination:
                        (Class<?>)((ParameterizedType)destination).getRawType();
                Type nested=container.getField(key).getGenericType();
                // DualList<String>.whitelist is declared as T[], but its owning
                // field supplies the concrete element type needed for inference.
                if(nested instanceof GenericArrayType&&destination instanceof ParameterizedType){
                    Type element=((GenericArrayType)nested).getGenericComponentType();
                    int parameter=Arrays.asList(container.getTypeParameters()).indexOf(element);
                    if(parameter>=0){
                        Type bound=((ParameterizedType)destination).getActualTypeArguments()[parameter];
                        if(bound instanceof Class)nested=Array.newInstance((Class<?>)bound,0).getClass();
                    }
                }
                destination=nested;
            }
        }catch(ReflectiveOperationException|ClassCastException ex){
            throw new IllegalStateException("Specify a legacy conversion for "+member+suffix,ex);
        }
        if(destination==Boolean.class||destination==boolean.class)return LegacyBukkitOption.Conversion.BOOLEAN;
        if(destination==Integer.class||destination==int.class)return LegacyBukkitOption.Conversion.INTEGER;
        if(destination==Float.class||destination==float.class)return LegacyBukkitOption.Conversion.FLOAT;
        if(destination instanceof Class&&((Class<?>)destination).isArray()){
            Class<?> element=((Class<?>)destination).getComponentType();
            if(element==String.class||IBlockDefinition.class.isAssignableFrom(element))
                return LegacyBukkitOption.Conversion.LIST;
        }
        throw new IllegalStateException("Specify a legacy conversion for ambiguous destination "+member+suffix);
    }
}
