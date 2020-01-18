package com.thizthizzydizzy.treefeller;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Particle;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
public class Effect{
    public final String name;
    public final EffectLocation location;
    public final EffectType type;
    public final double chance;
    private Particle particle;
    private double x;
    private double y;
    private double z;
    private double dx;
    private double dy;
    private double dz;
    private double speed;
    private int count;
    private Object extra;
    private String sound;
    private float volume;
    private float pitch;
    private float power;
    private boolean fire;
    private Effect(String name, EffectLocation location, EffectType type, double chance){
        this.name = name;
        this.location = location;
        this.type = type;
        this.chance = chance;
    }
    public Effect(String name, EffectLocation location, double chance, Particle particle, double x, double y, double z, double dx, double dy, double dz, double speed, int count, Object extra){
        this(name, location, EffectType.PARTICLE, chance);
        this.particle = particle;
        this.x = x;
        this.y = y;
        this.z = z;
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        this.speed = speed;
        this.count = count;
        this.extra = extra;
    }
    public Effect(String name, EffectLocation location, double chance, String sound, float volume, float pitch){
        this(name, location, EffectType.SOUND, chance);
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }
    public Effect(String name, EffectLocation location, double chance, float power, boolean fire){
        this(name, location, EffectType.EXPLOSION, chance);
        this.power = power;
        this.fire = fire;
    }
    public void print(Logger logger){
        logger.log(Level.INFO, "Loaded effect: {0}", name);
        logger.log(Level.INFO, "- Location: {0}", location);
        logger.log(Level.INFO, "- Type: {0}", type);
        logger.log(Level.INFO, "- Chance: {0}", chance);
        switch(type){
            case PARTICLE:
                logger.log(Level.INFO, "- Particle: {0}", particle);
                logger.log(Level.INFO, "- x: {0}", x);
                logger.log(Level.INFO, "- y: {0}", y);
                logger.log(Level.INFO, "- z: {0}", z);
                logger.log(Level.INFO, "- dx: {0}", dx);
                logger.log(Level.INFO, "- dy: {0}", dy);
                logger.log(Level.INFO, "- dz: {0}", dz);
                logger.log(Level.INFO, "- Speed: {0}", speed);
                logger.log(Level.INFO, "- Count: {0}", count);
                logger.log(Level.INFO, "- Extra: {0}", extra);
                break;
            case SOUND:
                logger.log(Level.INFO, "- Sound: {0}", sound);
                logger.log(Level.INFO, "- Volume: {0}", volume);
                logger.log(Level.INFO, "- Pitch: {0}", pitch);
                break;
            case EXPLOSION:
                logger.log(Level.INFO, "- Power: {0}", power);
                logger.log(Level.INFO, "- Fire: {0}", fire);
                break;
        }
    }
    public void play(Block block){
        switch(type){
            case EXPLOSION:
                block.getWorld().createExplosion(block.getLocation().add(0.5, 0.5, 0.5), power, fire);
                break;
            case SOUND:
                block.getWorld().playSound(block.getLocation().add(0.5,0.5,0.5), sound, SoundCategory.BLOCKS, volume, pitch);
                break;
            case PARTICLE:
                block.getWorld().spawnParticle(particle, block.getLocation().add(x+.5,y+.5,z+.5), count, dx, dy, dz, speed, extra);
                break;
        }
    }
    public static enum EffectLocation{
        LOGS,LEAVES,TREE,TOOL;
    }
    public static enum EffectType{
        PARTICLE,SOUND,EXPLOSION;
    }
}