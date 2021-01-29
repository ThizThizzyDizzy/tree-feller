package com.thizthizzydizzy.treefeller;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
public class Effect{
    public String name;
    public EffectLocation location;
    public EffectType type;
    public double chance;
    public Particle particle;
    public double x;
    public double y;
    public double z;
    public double dx;
    public double dy;
    public double dz;
    public double speed;
    public int count;
    public Object extra;
    public String sound;
    public float volume;
    public float pitch;
    public float power;
    public boolean fire;
    public boolean permanent;
    public String[] tags;
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
    public Effect(String name, EffectLocation location, double chance, boolean permanent, String... tags){
        this(name, location, EffectType.MARKER, chance);
        this.permanent = permanent;
        this.tags = tags;
    }
    public static Effect newEffect(EffectType type){
        String nam = "new_effect";
        String name = nam;
        boolean matches;
        int i = 0;
        do{
            matches = false;
            for(Effect e : TreeFeller.effects){
                if(e.name.equalsIgnoreCase(name))matches = true;
            }
            if(matches)name = nam+i;
            i++;
        }while(matches);
        switch(type){
            case EXPLOSION:
                return new Effect(name, EffectLocation.TREE, 1, 0, false);
            case MARKER:
                return new Effect(name, EffectLocation.TREE, 1, false);
            case PARTICLE:
                return new Effect(name, EffectLocation.TREE, 1, Particle.ASH, 0, 0, 0, 0, 0, 0, 0, 1, null);
            case SOUND:
                return new Effect(name, EffectLocation.TREE, 1, "", 1, 1);
            default:
                throw new IllegalArgumentException("Unknown EffectType: "+type+"! This is a bug!");
        }
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
            case MARKER:
                logger.log(Level.INFO, "- Permanent: {0}", permanent);
                logger.log(Level.INFO, "- Tags: {0}", Arrays.toString(tags));
                break;
            default:
                throw new IllegalArgumentException("Unknown EffectType: "+type+"! This is a bug!");
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
            case MARKER:
                Entity entity = block.getWorld().spawnEntity(block.getLocation().add(.5,.5,.5), permanent?EntityType.ARMOR_STAND:EntityType.AREA_EFFECT_CLOUD);
                if(permanent){
                    ArmorStand as = (ArmorStand)entity;
                    as.setMarker(true);
                    as.setInvulnerable(true);
                    as.setGravity(false);
                    as.setVisible(false);
                    as.addScoreboardTag("tree_feller");
                    for(String s : tags)as.addScoreboardTag(s);
                }else{
                    AreaEffectCloud cloud = (AreaEffectCloud)entity;
                    cloud.setReapplicationDelay(0);
                    cloud.setRadius(0);
                    cloud.setDuration(0);
                    cloud.setWaitTime(0);
                    cloud.addScoreboardTag("tree_feller");
                    for(String s : tags)cloud.addScoreboardTag(s);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown EffectType: "+type+"! This is a bug!");
        }
    }
    public String writeToConfig(){
        String s = "{name: "+name;
        s+=", chance: "+chance;
        s+=", location: "+location.name();
        s+=", type: "+type.name();
        switch(type){
            case EXPLOSION:
                s+=", power: "+power;
                s+=", fire: "+fire;
                break;
            case MARKER:
                s+=", permanent: "+permanent;
                s+=", tags: "+Arrays.toString(tags);
                break;
            case PARTICLE:
                s+=", particle: "+particle.name();
                s+=", x: "+x;
                s+=", y: "+y;
                s+=", z: "+z;
                s+=", dx: "+dx;
                s+=", dy: "+dy;
                s+=", dz: "+dz;
                s+=", speed: "+speed;
                s+=", count: "+count;
                switch(particle){
                    case REDSTONE:
                        Particle.DustOptions options = (Particle.DustOptions)extra;
                        Color color = options.getColor();
                        s+=", r: "+color.getRed();
                        s+=", g: "+color.getGreen();
                        s+=", b: "+color.getBlue();
                        s+=", size: "+options.getSize();
                        break;
                    case ITEM_CRACK:
                        s+="item: "+((ItemStack)extra).getType().name();
                        break;
                    case BLOCK_CRACK:
                    case BLOCK_DUST:
                    case FALLING_DUST:
                        s+=", block: "+((BlockData)extra).getMaterial().name();
                        break;
                }
                break;
            case SOUND:
                s+=", sound: "+sound;
                s+=", volume: "+volume;
                s+=", pitch: "+pitch;
                break;
            default:
                throw new IllegalArgumentException("Unknown EffectType: "+type+"! This is a bug!");
        }
        return s+"}";
    }
    public static enum EffectLocation{
        LOGS(Material.OAK_WOOD),
        LEAVES(Material.OAK_LEAVES),
        TREE(Material.OAK_SAPLING),
        TOOL(Material.IRON_AXE);
        private final Material item;
        private EffectLocation(Material item){
            this.item = item;
        }
        public Material getItem(){
            return item;
        }
    }
    public static enum EffectType{
        PARTICLE(Material.CAMPFIRE),
        SOUND(Material.NOTE_BLOCK),
        EXPLOSION(Material.TNT),
        MARKER(Material.ARMOR_STAND);
        private final Material item;
        private EffectType(Material item){
            this.item = item;
        }
        public Material getItem(){
            return item;
        }
    }
    @Override
    public String toString(){
        return name;
    }
}