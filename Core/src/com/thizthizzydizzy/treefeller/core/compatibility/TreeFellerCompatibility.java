package com.thizthizzydizzy.treefeller.core.compatibility;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.config.structure.*;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import java.util.*;
import java.util.function.*;
/** Core owns compatibility enablement, error isolation and per-fell lifecycle. */
public final class TreeFellerCompatibility{
    private static final Map<String,PluginCompatibility> hooks=new LinkedHashMap<>();
    private static final Set<String> initialized=new HashSet<>();
    private TreeFellerCompatibility(){}
    public static PluginCompatibility hook(String id){
        PluginCompatibility hook=hooks.get(id);
        if(hook==null||!hook.installed())throw new IllegalStateException("Required test plugin unavailable: "+id);
        return hook;
    }
    public static void register(PluginCompatibility hook){
        PluginCompatibility previous=hooks.put(hook.id(),hook);
        if(previous!=null&&initialized.remove(hook.id()))previous.close();
    }
    public static BukkitConfiguration defaultConfiguration(){
        BukkitConfiguration config=new BukkitConfiguration();
        try{GlobalConfiguration.applyGlobalConfiguration(BukkitConfiguration.class,config);}
        catch(Exception ex){throw new IllegalStateException("Could not apply Bukkit defaults",ex);}
        return config;
    }
    public static BukkitConfiguration defaults(){
        BukkitConfiguration config=defaultConfiguration();
        // Global settings override defaults through the same merge semantics as tree/tool settings.
        BukkitConfiguration supplied=TreeFellerCore.config.bukkit;
        if(supplied!=null){
            if(supplied.send_block_break_events!=null)config.send_block_break_events=supplied.send_block_break_events;
            if(supplied.compatibility!=null)for(java.lang.reflect.Field f:config.compatibility.getClass().getFields())try{
                Object value=f.get(supplied.compatibility);if(value!=null)f.set(config.compatibility,value);
            }catch(ReflectiveOperationException ex){throw new IllegalStateException(ex);}
        }
        return config;
    }
    public static BukkitCompatibilityConfiguration settings(CompatibilityContext context){return context.config.compatibility;}
    private static boolean enabled(PluginCompatibility hook,BukkitCompatibilityConfiguration config){
        try{return Boolean.TRUE.equals(BukkitCompatibilityConfiguration.class.getField(hook.id().replace('-','_').replace('.','_').replace("+","_plus")).get(config));}
        catch(ReflectiveOperationException ex){return true;}
    }
    private static void failure(PluginCompatibility hook,Throwable ex){
        java.io.StringWriter trace=new java.io.StringWriter();ex.printStackTrace(new java.io.PrintWriter(trace));
        TreeFellerCore.connector.log("Compatibility "+hook.id()+" failed: "+trace);
    }
    private static void visit(Consumer<PluginCompatibility> action){
        BukkitCompatibilityConfiguration configuration=defaults().compatibility;
        for(PluginCompatibility hook:hooks.values())try{
            if(!enabled(hook,configuration)||!hook.installed())continue;
            if(initialized.add(hook.id())){try{hook.initialize();hook.reload();}catch(RuntimeException|LinkageError ex){initialized.remove(hook.id());throw ex;}}
            action.accept(hook);
        }catch(RuntimeException|LinkageError ex){failure(hook,ex);}
    }
    public static void reload(){
        BukkitCompatibilityConfiguration configuration=defaults().compatibility;
        for(PluginCompatibility hook:hooks.values())if(initialized.contains(hook.id())&&(!enabled(hook,configuration)||!hook.installed())){
            try{hook.close();}catch(RuntimeException|LinkageError ex){failure(hook,ex);}initialized.remove(hook.id());
        }
        visit(PluginCompatibility::reload);
    }
    public static boolean check(CompatibilityContext context){
        boolean[] allowed={true};
        com.thizthizzydizzy.treefeller.core.debug.DebuggerContext debug=com.thizthizzydizzy.treefeller.core.debug.TreeFellerDebugger.begin(
            com.thizthizzydizzy.treefeller.core.debug.DebugPhase.CRITERIA,context.player,context.world);
        visit(hook -> {
            if(!hook.requirements(context))allowed[0]=false;
            if(hook.id().equals("mmocore"))for(Map.Entry<String,Integer> requirement:settings(context).mmocore_required_profession_level.entrySet())
                if(hook.professionLevel(context,requirement.getKey())<requirement.getValue()){
                    debug.fail("MMOCore requires "+requirement.getKey()+" level "+requirement.getValue());allowed[0]=false;
                }
            for(com.thizthizzydizzy.treefeller.core.detection.TreeNode node:context.nodes)if(!hook.canBreak(context,node.pos)){
                debug.at(node.pos);debug.fail(hook.id()+" protects block "+com.thizthizzydizzy.treefeller.core.debug.DebuggerContext.position(node.pos));allowed[0]=false;break;
            }
        });
        return allowed[0];
    }
    public static void fell(CompatibilityContext context){visit(hook -> {hook.fell(context);award(hook,context,"tree");});}
    public static double breakBlock(CompatibilityContext context,long pos){double[] multiplier={1};visit(hook -> {
        multiplier[0]*=hook.breakBlock(context,pos);
        if(hook.id().equals("mcmmo")&&Boolean.TRUE.equals(settings(context).mcmmo_double_drops))multiplier[0]*=hook.bonusDrops(context,pos);
        if(hook.id().equals("mmocore")&&Boolean.TRUE.equals(settings(context).mmocore_emulate_regen))hook.regenerate(context,pos);
        com.thizthizzydizzy.treefeller.core.detection.TreeNodeType type=context.type(pos);
        if(type==com.thizthizzydizzy.treefeller.core.detection.TreeNodeType.TRUNK)award(hook,context,"trunk");
        if(type==com.thizthizzydizzy.treefeller.core.detection.TreeNodeType.LEAVES)award(hook,context,hook.id().equals("ecojobs")?"leaf":"leaves");
    });return multiplier[0];}
    public static void removeBlock(CompatibilityContext context,long pos){visit(hook -> hook.removeBlock(context,pos));}
    public static Map<PluginCompatibility,Object> capture(CompatibilityContext context,long pos){
        Map<PluginCompatibility,Object> states=new LinkedHashMap<>();visit(hook -> states.put(hook,hook.capture(context.world,pos)));return states;
    }
    public static void addBlock(CompatibilityContext context,long pos,Map<PluginCompatibility,Object> states){states.forEach((hook,state) -> {try{hook.addBlock(context,pos,state);}catch(RuntimeException|LinkageError ex){failure(hook,ex);}});}
    public static void dropItem(IPlayerConnector player,Object item){visit(hook -> hook.dropItem(player,item));}
    private static void award(PluginCompatibility hook,CompatibilityContext context,String kind){
        BukkitCompatibilityConfiguration config=settings(context);
        String id=hook.id();
        Map<String,Double> xp;
        try{
            java.lang.reflect.Field field=BukkitCompatibilityConfiguration.class.getField(id+"_"+kind+"_xp");
            xp=(Map<String,Double>)field.get(config);
        }catch(NoSuchFieldException ex){return;}catch(IllegalAccessException ex){throw new IllegalStateException(ex);}
        if(xp==null)return;
        boolean apply=id.equals("auraskills")?Boolean.TRUE.equals(config.auraskills_apply_modifiers)
                :id.equals("aureliumskills")?Boolean.TRUE.equals(config.aureliumskills_apply_modifiers)
                :id.equals("ecojobs")?Boolean.TRUE.equals(config.ecojobs_apply_multipliers):true;
        for(Map.Entry<String,Double> entry:xp.entrySet()){
            double amount=entry.getValue();
            if(!Double.isFinite(amount)||amount<=0)continue;
            if(id.equals("mmocore"))amount=Math.floor(amount)+(Math.random()<amount-Math.floor(amount)?1:0);
            if(id.equals("auraskills")||id.equals("aureliumskills"))queue(hook,context,entry.getKey(),amount,apply);
            else hook.experience(context,entry.getKey(),amount,apply);
        }
    }
    private static final Map<ExperienceKey,Map<String,Double>> pending=new LinkedHashMap<>();
    private static boolean pendingTask;
    private static void queue(PluginCompatibility hook,CompatibilityContext context,String skill,double amount,boolean apply){
        ExperienceKey key=new ExperienceKey(hook,context,apply);
        pending.computeIfAbsent(key,k -> new LinkedHashMap<>()).merge(skill,amount,Double::sum);
        if(!pendingTask){
            pendingTask=true;
            int delay=Boolean.TRUE.equals(TreeFellerConfiguration.getCombinedCuttingConfiguration(context.detected.tree,context.detected.tool).cutting_animation)?10:1;
            TreeFellerCore.connector.schedule(delay,() -> {
                pendingTask=false;
                Map<ExperienceKey,Map<String,Double>> batch=new LinkedHashMap<>(pending);pending.clear();
                batch.forEach((k,values) -> values.forEach((name,xp) -> {try{k.hook.experience(k.context,name,xp,k.apply);}catch(RuntimeException|LinkageError ex){failure(k.hook,ex);}}));
            });
        }
    }
    private static final class ExperienceKey{
        final PluginCompatibility hook;final CompatibilityContext context;final boolean apply;
        ExperienceKey(PluginCompatibility hook,CompatibilityContext context,boolean apply){this.hook=hook;this.context=context;this.apply=apply;}
        @Override public int hashCode(){return Objects.hash(hook,context.player,apply);}
        @Override public boolean equals(Object other){if(!(other instanceof ExperienceKey))return false;ExperienceKey k=(ExperienceKey)other;return hook==k.hook&&Objects.equals(context.player,k.context.player)&&apply==k.apply;}
    }
    public static void close(){pending.clear();pendingTask=false;hooks.values().forEach(PluginCompatibility::close);hooks.clear();initialized.clear();}
}
