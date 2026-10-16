package com.thizthizzydizzy.treefeller.core.message;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.config.structure.message.*;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import com.thizthizzydizzy.treefeller.core.debug.DebuggerContext;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import com.thizthizzydizzy.treefeller.core.config.structure.MessagesConfiguration.*;
import com.thizthizzydizzy.treefeller.core.config.structure.general.BlockFilter;
import com.thizthizzydizzy.treefeller.core.config.structure.general.DualList;
import com.thizthizzydizzy.treefeller.core.connector.item.IItemConnector;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IItemDefinition;
/** Player feedback is independent of diagnostic verbosity and replay. */
public final class TreeFellerMessages{
    private TreeFellerMessages(){}
    public static void send(IPlayerConnector player, SingleMessageConfiguration message, Object... arguments){
        if(player==null||message==null)return;
        if(message.actionbar!=null)player.sendActionbar(format(message.actionbar,arguments));
        if(message.chat!=null)player.sendMessage(format(message.chat,arguments));
    }
    private static String format(String text,Object[] arguments){
        for(int i=0;i<arguments.length;i++)text=text.replace("{"+i+"}",DebuggerContext.describe(arguments[i]));
        return text;
    }
    public static void send(IPlayerConnector player,String name,Object... arguments){
        Object message=message(name);
        if(message instanceof SingleMessageConfiguration)send(player,(SingleMessageConfiguration)message,arguments);
    }
    private static Object message(String name){
        if(TreeFellerCore.config.messages==null)return null;
        Object value=TreeFellerCore.config.messages;
        try{
            for(String part:name.split("\\.")){
                if(value==null)return null;
                value=value.getClass().getField(part).get(value);
            }
            return value;
        }catch(ReflectiveOperationException ex){throw new IllegalArgumentException("Unknown message: "+name,ex);}
    }

    /** Match native definitions one requirement at a time so feedback remains in Core. */
    public static boolean matchesItem(IPlayerConnector player,IItemConnector item,IItemDefinition definition){
        if(player==null||!configured(message("item")))return item.matches(definition);
        if(definition==null)return checks(player,"tool").check("item",item.matches(null),item.getItemId(),null);
        for(Map.Entry<String,IItemDefinition> requirement:definition.requirements().entrySet()){
            String name=requirement.getKey();
            boolean matched=item.matches(requirement.getValue());
            if(!name.isEmpty())checks(player,"tool","item").check(name,matched,item.getItemId(),requirement.getValue());
        }
        return checks(player,"tool").check("item",item.matches(definition),item.getItemId(),definition);
    }

    private static boolean configured(Object config){
        if(config==null)return false;
        if(config instanceof String)return true;
        try{
            for(Field field:config.getClass().getFields())
                if(!java.lang.reflect.Modifier.isStatic(field.getModifiers())&&configured(field.get(config)))return true;
            return false;
        }catch(IllegalAccessException ex){throw new IllegalStateException(ex);}
    }
    public static String scope(String option,Object tree,Object tool){
        try{
            Object treeValue=tree==null?null:tree.getClass().getField(option).get(tree);
            Object toolValue=tool==null?null:tool.getClass().getField(option).get(tool);
            if(Boolean.TRUE.equals(treeValue)&&Boolean.FALSE.equals(toolValue))return "tree";
            return toolValue!=null?"tool":treeValue!=null?"tree":"global";
        }catch(ReflectiveOperationException ex){throw new IllegalArgumentException(option,ex);}
    }
    public static Checks checks(IPlayerConnector player,String scope){return new Checks(player,scope,"");}
    public static Checks checks(IPlayerConnector player,String scope,String section){return new Checks(player,scope,section);}

    public static final class Checks{
        private final IPlayerConnector player;
        private final String scope,section;
        private Checks(IPlayerConnector player,String scope,String section){this.player=player;this.scope=scope;this.section=section;}
        /** Failures identify the scope; passing checks use the option's success message, as in v1. */
        public boolean check(String option,boolean passes,Object... arguments){
            if(player==null)return passes;
            String path=section.isEmpty()?option:section+"."+option;
            Object value=message(path);
            if(value instanceof MessageConfiguration){
                MessageConfiguration m=(MessageConfiguration)value;
                send(player,passes?m.success:scope.equals("tool")?m.tool:scope.equals("tree")?m.tree:m.global,arguments);
            }
            if(value instanceof RangeMessages
                    &&arguments.length>=2&&arguments[0] instanceof Number){
                bounds(path,passes,((Number)arguments[0]).floatValue(),arguments[1]);
            }
            if(value instanceof DualListMessages
                    &&arguments.length>=2&&arguments[1] instanceof DualList){
                list(path,arguments[0],(DualList<?>)arguments[1]);
            }
            if(value instanceof BlockFilterMessages){
                if(arguments.length>=2&&arguments[1] instanceof String)
                    checks(player,scope).check(path+"."+arguments[1],passes,arguments[0]);
                else if(passes&&arguments.length>0&&arguments[0] instanceof BlockFilter){
                    BlockFilter filter=(BlockFilter)arguments[0];
                    if(filter.required!=null)checks(player,scope).check(path+".required",true,(Object)filter.required);
                    if(filter.blacklist!=null)checks(player,scope).check(path+".blacklist",true,(Object)filter.blacklist);
                }
            }
            return passes;
        }
        public <T> boolean multi(String option,boolean passes,DualList<T> expected,Predicate<T> predicate){
            check(option,passes,expected);
            if(expected.whitelist!=null){
                boolean allowed=true;
                for(T entry:expected.whitelist)allowed&=predicate.test(entry);
                check(option+".whitelist",allowed,(Object)expected.whitelist);
            }
            if(expected.blacklist!=null){
                boolean allowed=true;
                for(T entry:expected.blacklist)allowed&=!predicate.test(entry);
                check(option+".blacklist",allowed,(Object)expected.blacklist);
            }
            return passes;
        }
        private void list(String path,Object actual,DualList<?> expected){
            if(expected.whitelist!=null){
                boolean found=false;
                for(Object entry:expected.whitelist)found|=Objects.equals(actual,entry);
                checks(player,scope).check(path+".whitelist",found,actual,expected.whitelist);
            }
            if(expected.blacklist!=null){
                boolean found=false;
                for(Object entry:expected.blacklist)found|=Objects.equals(actual,entry);
                checks(player,scope).check(path+".blacklist",!found,actual,expected.blacklist);
            }
        }
        private void bounds(String path,boolean passes,float actual,Object expected){
            try{
                Number min=(Number)expected.getClass().getField("min").get(expected);
                Number max=(Number)expected.getClass().getField("max").get(expected);
                boolean wrapping=min!=null&&max!=null&&min.floatValue()>max.floatValue();
                if(min!=null)checks(player,scope).check(path+".min",wrapping?passes:actual>=min.floatValue(),actual,min);
                if(max!=null)checks(player,scope).check(path+".max",wrapping?passes:actual<=max.floatValue(),actual,max);
            }catch(ReflectiveOperationException ex){throw new IllegalStateException(ex);}
        }
        /** Options that apply behavior rather than refuse felling report their configured value. */
        public void applied(Object config){
            if(player==null||config==null||!configured(message(section)))return;
            for(Field field:config.getClass().getFields()){
                try{
                    Object value=field.get(config);
                    if(value!=null){
                        check(field.getName(),true,value);
                        Object nested=message(section+"."+field.getName());
                        if(nested instanceof MessageConfiguration&&nested.getClass()!=MessageConfiguration.class){
                            Checks child=checks(player,scope,section+"."+field.getName());
                            if(value.getClass().isArray()){
                                for(int i=0;i<java.lang.reflect.Array.getLength(value);i++)child.applied(java.lang.reflect.Array.get(value,i));
                            }else child.applied(value);
                        }
                    }
                }catch(IllegalAccessException ex){throw new IllegalStateException(ex);}
            }
        }
    }
}
