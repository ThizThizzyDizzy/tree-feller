package com.thizthizzydizzy.treefeller.core.control;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.config.*;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.*;
import java.util.*;
/** Platform-independent navigation and validated configuration editing. */
public final class ConfigurationEditor{
    private final ControlAudience source;
    private ConfigurationEditor(ControlAudience source){this.source=source;}
    public static void open(ControlAudience source){
        if(TreeFellerControls.permitted(source,"config")&&TreeFellerControls.requirePlayer(source))new ConfigurationEditor(source).browse(Collections.emptyList());
    }
    private ConfigObject root(){return ConfigFactory.parseString(ConfigWriter.writeToString(TreeFellerCore.config)).resolve().root();}
    private Object at(Object value,List<Object> path){
        for(Object part:path){
            if(value instanceof ConfigObject)value=((ConfigObject)value).get((String)part);
            else value=((ConfigList)value).get((Integer)part);
        }
        return value;
    }
    private String label(List<Object> path){StringBuilder name=new StringBuilder("Configuration");for(Object part:path)name.append(part instanceof Integer?"["+part+"]":" / "+part);return name.toString();}
    private List<Object> child(List<Object> path,Object key){List<Object> next=new ArrayList<>(path);next.add(key);return next;}
    private String display(ConfigValue value){return value==null?"null":value.render(ConfigRenderOptions.concise());}
    private void browse(List<Object> path){
        if(!TreeFellerControls.permitted(source,"config"))return;
        Object value;
        try{value=at(root(),path);}catch(RuntimeException ex){browse(Collections.emptyList());return;}
        List<Screen.Entry> entries=new ArrayList<>();
        if(!path.isEmpty())entries.add(new Screen.Entry("Back","Return to the parent section",() -> browse(path.subList(0,path.size()-1)),Screen.Kind.BACK));
        if(value instanceof ConfigObject){
            // Include unset fields as well as serialized values, so nullable overrides can be created.
            Object model=modelAt(path);
            Set<String> keys=new LinkedHashSet<>();
            if(model!=null&&! (model instanceof ISpecialConfigObject)&&!(model instanceof Map)){
                for(java.lang.reflect.Field field:model.getClass().getFields())if(!java.lang.reflect.Modifier.isStatic(field.getModifiers()))keys.add(field.getName());
            }
            keys.addAll(((ConfigObject)value).keySet());
            for(String key:keys){
                ConfigValue entry=((ConfigObject)value).get(key);
                List<Object> next=child(path,key);
                entries.add(new Screen.Entry(key,display(entry),() -> select(next),entry instanceof ConfigObject||entry instanceof ConfigList?Screen.Kind.SECTION:entry!=null&&entry.valueType()==ConfigValueType.BOOLEAN?Screen.Kind.TOGGLE:Screen.Kind.VALUE));
            }
        }else if(value instanceof ConfigList){
            ConfigList list=(ConfigList)value;
            for(int i=0;i<list.size();i++){
                final int index=i;
                entries.add(new Screen.Entry("["+i+"]",display(list.get(i)),() -> select(child(path,index)),Screen.Kind.SECTION));
            }
            entries.add(new Screen.Entry("Add entry","Enter a new value or object in configuration syntax",() -> input(path,true),Screen.Kind.ADD));
        }
        if(!path.isEmpty()&&path.get(path.size()-1) instanceof Integer){
            int index=(Integer)path.get(path.size()-1);
            List<Object> parent=path.subList(0,path.size()-1);
            ConfigList siblings=(ConfigList)at(root(),parent);
            if(index>0)entries.add(new Screen.Entry("Move earlier","Move this entry before the previous entry",() -> move(path,-1),Screen.Kind.MOVE));
            if(index+1<siblings.size())entries.add(new Screen.Entry("Move later","Move this entry after the next entry",() -> move(path,1),Screen.Kind.MOVE));
        }
        if(!path.isEmpty()){
            entries.add(new Screen.Entry("Edit value","Replace this value in configuration syntax",() -> input(path,false)));
            entries.add(new Screen.Entry("Reset / inherit","Remove this field or list entry",() -> reset(path),Screen.Kind.REMOVE));
        }
        entries.add(new Screen.Entry("Save configuration","Changes apply immediately. Save writes config.conf.",() -> {TreeFellerControls.save(source);browse(path);},Screen.Kind.SAVE));
        com.thizthizzydizzy.treefeller.core.config.structure.TreeFellerConfiguration shown=TreeFellerCore.config;
        entries.replaceAll(entry -> new Screen.Entry(entry.title,entry.description,() -> {
            if(!TreeFellerControls.permitted(source,"config"))return;
            if(TreeFellerCore.config!=shown){source.sendMessage("Configuration changed; the editor has been refreshed. Choose the action again.");browse(Collections.emptyList());return;}
            entry.activate.run();
        },entry.kind));
        source.show(new Screen(label(path),entries));
    }
    private Object modelAt(List<Object> path){
        Object value=TreeFellerCore.config;
        try{for(Object part:path){if(value==null)return null;if(part instanceof Integer)value=java.lang.reflect.Array.get(value,(Integer)part);else if(value instanceof Map)value=((Map<?,?>)value).get(part);else value=value.getClass().getField((String)part).get(value);}return value;}
        catch(ReflectiveOperationException|RuntimeException ex){return null;}
    }
    private void select(List<Object> path){
        ConfigValue value=(ConfigValue)at(root(),path);
        if(value instanceof ConfigObject||value instanceof ConfigList)browse(path);
        else{
            Object model=modelAt(path);
            if(model instanceof Boolean){apply(path,ConfigValueFactory.fromAnyRef(!((Boolean)model)),false);browse(path.subList(0,path.size()-1));}
            else input(path,false);
        }
    }
    private void input(List<Object> path,boolean append){
        ConfigValue current=(ConfigValue)at(root(),path);
        com.thizthizzydizzy.treefeller.core.config.structure.TreeFellerConfiguration editing=TreeFellerCore.config;
        source.requestText((append?"Add entry: ":"Edit: ")+label(path),append?"{}":display(current),text -> {
            if(TreeFellerCore.config!=editing){source.sendMessage("Configuration changed; reopen the value before editing it.");browse(Collections.emptyList());return;}
            try{
                Config parsed=ConfigFactory.parseString("value = "+text).resolve();
                if(!parsed.root().containsKey("value")||parsed.root().size()!=1)throw new IllegalArgumentException("Enter exactly one value");
                apply(path,parsed.root().get("value"),append);
            }catch(RuntimeException ex){source.sendMessage("Invalid configuration value: "+ex.getMessage());}
            browse(append?path:path.subList(0,path.size()-1));
        },() -> browse(append?path:path.subList(0,path.size()-1)));
    }
    /** Return a new immutable config tree with a nested edit. */
    private ConfigValue replace(ConfigValue node,List<Object> path,int depth,ConfigValue replacement,boolean append,boolean remove){
        if(depth==path.size()){
            if(append){List<ConfigValue> list=new ArrayList<>((ConfigList)node);list.add(replacement);return ConfigValueFactory.fromIterable(list);}
            return replacement;
        }
        Object key=path.get(depth);
        if(key instanceof String){
            ConfigObject object=(ConfigObject)node;
            if(remove&&depth==path.size()-1)return object.withoutKey((String)key);
            return object.withValue((String)key,replace(object.get(key),path,depth+1,replacement,append,remove));
        }
        List<ConfigValue> list=new ArrayList<>((ConfigList)node);int index=(Integer)key;
        if(remove&&depth==path.size()-1)list.remove(index);
        else list.set(index,replace(list.get(index),path,depth+1,replacement,append,remove));
        return ConfigValueFactory.fromIterable(list);
    }
    private void move(List<Object> path,int direction){
        if(!TreeFellerControls.permitted(source,"config"))return;
        List<Object> parent=path.subList(0,path.size()-1);
        List<ConfigValue> entries=new ArrayList<>((ConfigList)at(root(),parent));
        int index=(Integer)path.get(path.size()-1),target=index+direction;
        if(target<0||target>=entries.size())return;
        Collections.swap(entries,index,target);
        commit(replace(root(),parent,0,ConfigValueFactory.fromIterable(entries),false,false));
        browse(parent);
    }
    private void reset(List<Object> path){
        if(!TreeFellerControls.permitted(source,"config"))return;
        try{commit(replace(root(),path,0,null,false,true));}catch(RuntimeException ex){source.sendMessage("Could not reset: "+ex.getMessage());}
        browse(path.subList(0,path.size()-1));
    }
    private void apply(List<Object> path,ConfigValue value,boolean append){
        if(!TreeFellerControls.permitted(source,"config"))return;
        commit(replace(root(),path,0,value,append,false));
    }
    private void commit(ConfigValue candidate){
        // Parse the entire candidate before changing the live configuration.
        TreeFellerCore.config=ConfigReader.readFromString(candidate.render());
        source.sendMessage("Configuration updated. Save to keep changes; reload to discard them.");
    }
}
