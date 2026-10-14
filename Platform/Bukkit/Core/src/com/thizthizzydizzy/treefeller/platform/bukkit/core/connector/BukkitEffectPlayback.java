package com.thizthizzydizzy.treefeller.platform.bukkit.core.connector;
import com.thizthizzydizzy.treefeller.core.config.structure.section.result.EffectConfiguration;
import java.util.function.Consumer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ArmorStand;
/** Native effect construction only. Core chooses where and when to play. */
final class BukkitEffectPlayback{
    static Consumer<Location> prepare(EffectConfiguration e){
        switch(e.type){
            case PARTICLE: return BukkitConnector.blockData.prepareParticle(e);
            case SOUND:
                if(e.sound==null||e.sound.isEmpty())throw new IllegalArgumentException("Sound name is required");
                try{
                    Class category=Class.forName("org.bukkit.SoundCategory");
                    Object blocks=Enum.valueOf(category,"BLOCKS");
                    java.lang.reflect.Method play=World.class.getMethod("playSound",Location.class,String.class,category,float.class,float.class);
                    return location -> invoke(play,location.getWorld(),location,e.sound,blocks,e.volume,e.pitch);
                }catch(ClassNotFoundException|NoSuchMethodException ex){
                    try{
                        String version=org.bukkit.Bukkit.getServer().getClass().getPackage().getName();
                        version=version.substring(version.lastIndexOf('.')+1);
                        String nms="net.minecraft.server."+version+".";
                        java.lang.reflect.Constructor<?> packet=Class.forName(nms+"PacketPlayOutNamedSoundEffect").getConstructor(String.class,double.class,double.class,double.class,float.class,float.class);
                        Class<?> packetType=Class.forName(nms+"Packet");
                        return location -> {
                            try{
                                Object sound=packet.newInstance(e.sound,location.getX(),location.getY(),location.getZ(),e.volume,e.pitch);
                                for(org.bukkit.entity.Player player : location.getWorld().getPlayers()){
                                    Object handle=player.getClass().getMethod("getHandle").invoke(player);
                                    Object connection=handle.getClass().getField("playerConnection").get(handle);
                                    invoke(connection.getClass().getMethod("sendPacket",packetType),connection,sound);
                                }
                            }catch(ReflectiveOperationException failure){throw new IllegalStateException(failure);}
                        };
                    }catch(ReflectiveOperationException failure){throw new IllegalArgumentException("Could not prepare legacy sound",failure);}
                }
            case EXPLOSION: return location -> location.getWorld().createExplosion(location,e.power,e.fire);
            case MARKER:
                try{
                    EntityType type=e.permanent?EntityType.ARMOR_STAND:EntityType.valueOf("AREA_EFFECT_CLOUD");
                    java.lang.reflect.Method tag=Entity.class.getMethod("addScoreboardTag",String.class);
                    java.lang.reflect.Method marker=ArmorStand.class.getMethod("setMarker",boolean.class);
                    java.lang.reflect.Method invulnerable=Entity.class.getMethod("setInvulnerable",boolean.class);
                    Class<?> cloud=e.permanent?null:Class.forName("org.bukkit.entity.AreaEffectCloud");
                    return location -> {
                        Entity entity=location.getWorld().spawnEntity(location,type);
                        if(e.permanent){
                            ArmorStand stand=(ArmorStand)entity;
                            invoke(marker,stand,true);
                            invoke(invulnerable,stand,true);
                            stand.setGravity(false);
                            stand.setVisible(false);
                        }else{
                            call(cloud,entity,"setReapplicationDelay",int.class,0);
                            call(cloud,entity,"setRadius",float.class,0f);
                            call(cloud,entity,"setDuration",int.class,0);
                            call(cloud,entity,"setWaitTime",int.class,0);
                        }
                        invoke(tag,entity,"tree_feller");
                        for(String value : e.tags)invoke(tag,entity,value);
                    };
                }catch(ReflectiveOperationException ex){throw new IllegalArgumentException("Markers require scoreboard tags and area effect clouds",ex);}
            default: throw new AssertionError(e.type);
        }
    }
    private static void call(Class<?> type,Object target,String name,Class<?> argument,Object value){
        try{invoke(type.getMethod(name,argument),target,value);}
        catch(NoSuchMethodException ex){throw new IllegalStateException(ex);}
    }
    private static void invoke(java.lang.reflect.Method method,Object target,Object... args){
        try{method.invoke(target,args);}
        catch(ReflectiveOperationException ex){throw new IllegalStateException(ex);}
    }
}
