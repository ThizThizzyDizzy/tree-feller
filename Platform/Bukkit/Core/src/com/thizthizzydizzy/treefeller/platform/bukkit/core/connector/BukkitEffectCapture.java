package com.thizthizzydizzy.treefeller.platform.bukkit.core.connector;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform.*;
import io.netty.channel.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.plugin.Plugin;
/** Native output observation for MCAutoTester; no scenario decisions or assertions. */
final class BukkitEffectCapture implements EffectCapture, Listener{
    private final List<MessageObservation> messages=new CopyOnWriteArrayList<>();
    private final List<ParticleObservation> particles=new CopyOnWriteArrayList<>();
    private final List<SoundObservation> sounds=new CopyOnWriteArrayList<>();
    private final List<Entity> markers=new ArrayList<>();
    private final List<ExplosionObservation> explosions=new ArrayList<>();
    private ChannelPipeline pipeline;
    private final String handler="treefeller-effects-"+UUID.randomUUID();
    private volatile RuntimeException failure;
    BukkitEffectCapture(Player player){
        try{
            Object handle=player.getClass().getMethod("getHandle").invoke(player);
            Object listener=field(handle,"connection");
            Object connection=field(listener,"connection");
            Channel channel=(Channel)field(connection,"channel");
            pipeline=channel.pipeline();
            pipeline.addBefore("packet_handler",handler,new ChannelOutboundHandlerAdapter(){
                @Override public void write(ChannelHandlerContext ctx,Object packet,ChannelPromise promise) throws Exception{
                    try{observe(packet);}catch(RuntimeException ex){failure=ex;}
                    super.write(ctx,packet,promise);
                }
            });
            Plugin plugin=org.bukkit.plugin.java.JavaPlugin.getPlugin(com.thizthizzydizzy.treefeller.platform.bukkit.core.TreeFellerBukkit.class);
            Class<? extends Event> spawn=(Class<? extends Event>)Class.forName("org.bukkit.event.entity.EntitySpawnEvent");
            Bukkit.getPluginManager().registerEvent(spawn,this,EventPriority.MONITOR,(ignored,event) -> {
                try{
                    Entity entity=(Entity)spawn.getMethod("getEntity").invoke(event);
                    if(entity instanceof ArmorStand||entity.getType().name().equals("AREA_EFFECT_CLOUD"))markers.add(entity);
                }catch(ReflectiveOperationException ex){throw new IllegalStateException(ex);}
            },plugin);
            Class<? extends Event> explosion=(Class<? extends Event>)Class.forName("org.bukkit.event.block.BlockExplodeEvent");
            Bukkit.getPluginManager().registerEvent(explosion,this,EventPriority.MONITOR,(ignored,event) -> {
                org.bukkit.block.Block block=(org.bukkit.block.Block)get(event,"getBlock");
                Location p=block.getLocation();
                explosions.add(new ExplosionObservation(p.getX(),p.getY(),p.getZ(),((List<?>)get(event,"blockList")).size()));
            },plugin);
        }catch(ReflectiveOperationException|RuntimeException ex){
            close();
            throw new IllegalStateException("Cannot observe server effect output",ex);
        }
    }
    private void observe(Object packet){
        String name=packet.getClass().getSimpleName();
        if(name.equals("ClientboundBundlePacket")){
            for(Object child:(Iterable<?>)get(packet,"subPackets"))observe(child);
            return;
        }
        if(name.equals("ClientboundSystemChatPacket")||name.equals("ClientboundSetActionBarTextPacket")){
            boolean actionbar=name.equals("ClientboundSetActionBarTextPacket")||(Boolean)get(packet,"overlay");
            Object component=get(packet,name.equals("ClientboundSetActionBarTextPacket")?"text":"content");
            messages.add(new MessageObservation(actionbar,(String)get(component,"getString")));
        }else if(name.equals("ClientboundLevelParticlesPacket")){
            Object particle=get(packet,"getParticle");
            particles.add(new ParticleObservation(number(packet,"getX"),number(packet,"getY"),number(packet,"getZ"),
                    number(packet,"getXDist"),number(packet,"getYDist"),number(packet,"getZDist"),number(packet,"getMaxSpeed"),
                    ((Number)get(packet,"getCount")).intValue(),particleData(particle),color(particle,"x"),color(particle,"y"),color(particle,"z"),optionalNumber(particle,"getScale")));
        }else if(name.equals("ClientboundSoundPacket")){
            sounds.add(new SoundObservation(number(packet,"getX"),number(packet,"getY"),number(packet,"getZ"),
                    number(packet,"getVolume"),number(packet,"getPitch"),get(packet,"getSound").toString(),get(packet,"getSource").toString()));
        }
    }
    private static double color(Object particle,String component){
        try{return number(particle.getClass().getMethod("getColor").invoke(particle),component);}
        catch(NoSuchMethodException ex){return Double.NaN;}
        catch(ReflectiveOperationException ex){throw new IllegalStateException(ex);}
    }
    private static double optionalNumber(Object object,String method){
        try{return ((Number)object.getClass().getMethod(method).invoke(object)).doubleValue();}
        catch(NoSuchMethodException ex){return Double.NaN;}
        catch(ReflectiveOperationException ex){throw new IllegalStateException(ex);}
    }
    private static String particleData(Object particle){
        StringBuilder result=new StringBuilder(particle.toString());
        try{
            Object registry=Class.forName("net.minecraft.core.registries.BuiltInRegistries").getField("PARTICLE_TYPE").get(null);
            result.append(" type=").append(registry.getClass().getMethod("getKey",Object.class).invoke(registry,get(particle,"getType")));
        }catch(ReflectiveOperationException ex){throw new IllegalStateException(ex);}
        for(String method : new String[]{"getColor","getScale","getItem","getState"}){
            try{result.append(" ").append(method).append("=").append(particle.getClass().getMethod(method).invoke(particle));}
            catch(NoSuchMethodException ignored){}
            catch(ReflectiveOperationException ex){throw new IllegalStateException(ex);}
        }
        return result.toString();
    }
    private void check(){if(failure!=null)throw failure;}
    @Override public List<MessageObservation> messages(){check();return new ArrayList<>(messages);}
    @Override public List<ParticleObservation> particles(){check();return new ArrayList<>(particles);}
    @Override public List<SoundObservation> sounds(){check();return new ArrayList<>(sounds);}
    @Override public List<ExplosionObservation> explosions(){return new ArrayList<>(explosions);}
    @Override public List<MarkerObservation> markers(){
        List<MarkerObservation> result=new ArrayList<>();
        for(Entity entity : markers){
            Location p=entity.getLocation();
            Set<String> tags=new HashSet<>((Set<String>)get(entity,"getScoreboardTags"));
            if(!tags.contains("tree_feller"))continue;
            boolean stand=entity instanceof ArmorStand;
            result.add(new MarkerObservation(p.getX(),p.getY(),p.getZ(),stand,entity.isValid(),stand&&!((ArmorStand)entity).isVisible(),
                    (Boolean)get(entity,"isInvulnerable"),(Boolean)get(entity,"hasGravity"),stand&&(Boolean)get(entity,"isMarker"),tags));
        }
        return result;
    }
    @Override public void close(){
        HandlerList.unregisterAll(this);
        if(pipeline!=null&&pipeline.get(handler)!=null)pipeline.remove(handler);
        for(Entity entity : markers)entity.remove();
    }
    private static Object field(Object object,String name) throws ReflectiveOperationException{
        for(Class<?> type=object.getClass();type!=null;type=type.getSuperclass()){
            try{Field field=type.getDeclaredField(name);field.setAccessible(true);return field.get(object);}
            catch(NoSuchFieldException ignored){}
        }
        throw new NoSuchFieldException(name);
    }
    private static Object get(Object object,String method){
        try{return object.getClass().getMethod(method).invoke(object);}
        catch(ReflectiveOperationException ex){throw new IllegalStateException(ex);}
    }
    private static double number(Object object,String method){return ((Number)get(object,method)).doubleValue();}
}
