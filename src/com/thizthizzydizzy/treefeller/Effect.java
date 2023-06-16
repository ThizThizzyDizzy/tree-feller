package com.thizthizzydizzy.treefeller;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
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
        return type.createNewEffect(name);
    }
    public void print(Logger logger){
        logger.log(Level.INFO, "Loaded effect: {0}", name);
        logger.log(Level.INFO, "- Location: {0}", location);
        logger.log(Level.INFO, "- Type: {0}", type);
        logger.log(Level.INFO, "- Chance: {0}", chance);
        type.print(this, logger);
    }
    public void play(Block block){
        type.play(this, block);
    }
    public String writeToConfig(){
        String s = "{name: "+name;
        s+=", chance: "+chance;
        s+=", location: "+location.name();
        s+=", type: "+type.name();
        s+= type.writeToConfig(this);
        return s+"}";
    }
    public static enum EffectLocation{
        LOGS(Material.OAK_WOOD, "The effect will occur at every log in the tree"),
        LEAVES(Material.OAK_LEAVES, "The effect will occur at every block of leaves in the tree"),
        TREE(Material.OAK_SAPLING, "The effect will occur at every block in the tree"),
        DECORATION(Material.SNOW, "The effect will occur at every block of decorations removed from the tree"),
        TOOL(Material.IRON_AXE, "The effect will occur at the block that was cut down"),
        TOOL_BREAK(Material.WOODEN_AXE, "The effect will occur at the block that was cut down, when the tool breaks");
        private final Material item;
        public final String description;
        private EffectLocation(Material item, String description){
            this.item = item;
            this.description = description;
        }
        public Material getItem(){
            return item;
        }
    }
    public static enum EffectType{
        PARTICLE(Material.CAMPFIRE){
            @Override
            Effect createNewEffect(String name){
                return new Effect(name, EffectLocation.TREE, 1, Particle.ASH, 0, 0, 0, 0, 0, 0, 0, 1, null);
            }
            @Override
            void print(Effect effect, Logger logger){
                logger.log(Level.INFO, "- Particle: {0}", effect.particle);
                logger.log(Level.INFO, "- x: {0}", effect.x);
                logger.log(Level.INFO, "- y: {0}", effect.y);
                logger.log(Level.INFO, "- z: {0}", effect.z);
                logger.log(Level.INFO, "- dx: {0}", effect.dx);
                logger.log(Level.INFO, "- dy: {0}", effect.dy);
                logger.log(Level.INFO, "- dz: {0}", effect.dz);
                logger.log(Level.INFO, "- Speed: {0}", effect.speed);
                logger.log(Level.INFO, "- Count: {0}", effect.count);
                logger.log(Level.INFO, "- Extra: {0}", effect.extra);
            }
            @Override
            void play(Effect effect, Block block){
                block.getWorld().spawnParticle(effect.particle, block.getLocation().add(effect.x+.5,effect.y+.5,effect.z+.5), effect.count, effect.dx, effect.dy, effect.dz, effect.speed, effect.extra);
            }
            @Override
            String writeToConfig(Effect effect){
                String s = "";
                s+=", particle: "+effect.particle.name();
                s+=", x: "+effect.x;
                s+=", y: "+effect.y;
                s+=", z: "+effect.z;
                s+=", dx: "+effect.dx;
                s+=", dy: "+effect.dy;
                s+=", dz: "+effect.dz;
                s+=", speed: "+effect.speed;
                s+=", count: "+effect.count;
                switch(effect.particle){
                    case REDSTONE:
                        Particle.DustOptions options = (Particle.DustOptions)effect.extra;
                        Color color = options.getColor();
                        s+=", r: "+color.getRed();
                        s+=", g: "+color.getGreen();
                        s+=", b: "+color.getBlue();
                        s+=", size: "+options.getSize();
                        break;
                    case ITEM_CRACK:
                        s+="item: "+((ItemStack)effect.extra).getType().name();
                        break;
                    case BLOCK_CRACK:
                    case BLOCK_DUST:
                    case FALLING_DUST:
                        s+=", block: "+((BlockData)effect.extra).getMaterial().name();
                        break;
                }
                return s;
            }
            @Override
            Effect loadEffect(String name, EffectLocation location, double chance, LinkedHashMap map){
                Particle particle = TreeFeller.getParticle((String) map.get("particle"));
                double x = 0;
                if(map.containsKey("x")){
                    x = ((Number)map.get("x")).doubleValue();
                }
                double y = 0;
                if(map.containsKey("y")){
                    y = ((Number)map.get("y")).doubleValue();
                }
                double z = 0;
                if(map.containsKey("z")){
                    z = ((Number)map.get("z")).doubleValue();
                }
                double dx = 0;
                if(map.containsKey("dx")){
                    dx = ((Number)map.get("dx")).doubleValue();
                }
                double dy = 0;
                if(map.containsKey("dy")){
                    dy = ((Number)map.get("dy")).doubleValue();
                }
                double dz = 0;
                if(map.containsKey("dz")){
                    dz = ((Number)map.get("dz")).doubleValue();
                }
                double speed = 0;
                if(map.containsKey("speed")){
                    speed = ((Number)map.get("speed")).doubleValue();
                }
                int count = 1;
                if(map.containsKey("count")){
                    count = ((Number)map.get("count")).intValue();
                }
                Object extra = null;
                switch(particle){
                    case REDSTONE:
                        extra = new Particle.DustOptions(Color.fromRGB(((Number)map.get("r")).intValue(), ((Number)map.get("g")).intValue(), ((Number)map.get("b")).intValue()), ((Number)map.get("size")).floatValue());
                        break;
                    case ITEM_CRACK:
                        extra = new ItemStack(Material.matchMaterial((String)map.get("item")));
                        break;
                    case BLOCK_CRACK:
                    case BLOCK_DUST:
                    case FALLING_DUST:
                        extra = Bukkit.createBlockData(Material.matchMaterial((String)map.get("block")));
                        break;
                }
                return new Effect(name, location, chance, particle, x, y, z, dx, dy, dz, speed, count, extra);
            }
        },
        SOUND(Material.NOTE_BLOCK){
            @Override
            Effect createNewEffect(String name){
                return new Effect(name, EffectLocation.TREE, 1, "", 1, 1);
            }
            @Override
            void print(Effect effect, Logger logger){
                logger.log(Level.INFO, "- Sound: {0}", effect.sound);
                logger.log(Level.INFO, "- Volume: {0}", effect.volume);
                logger.log(Level.INFO, "- Pitch: {0}", effect.pitch);
            }
            @Override
            void play(Effect effect, Block block){
                block.getWorld().playSound(block.getLocation().add(0.5,0.5,0.5), effect.sound, SoundCategory.BLOCKS, effect.volume, effect.pitch);
            }
            @Override
            String writeToConfig(Effect effect){
                String s = "";
                s+=", sound: "+effect.sound;
                s+=", volume: "+effect.volume;
                s+=", pitch: "+effect.pitch;
                return s;
            }
            @Override
            Effect loadEffect(String name, EffectLocation location, double chance, LinkedHashMap map){
                String sound = (String)map.get("sound");
                float volume = 1;
                if(map.containsKey("volume")){
                    volume = ((Number)map.get("volume")).floatValue();
                }
                float pitch = 1;
                if(map.containsKey("pitch")){
                    pitch = ((Number)map.get("pitch")).floatValue();
                }
                return new Effect(name, location, chance, sound, volume, pitch);
            }
        },
        EXPLOSION(Material.TNT){
            @Override
            Effect createNewEffect(String name){
                return new Effect(name, EffectLocation.TREE, 1, 0, false);
            }
            @Override
            void print(Effect effect, Logger logger){
                logger.log(Level.INFO, "- Power: {0}", effect.power);
                logger.log(Level.INFO, "- Fire: {0}", effect.fire);
            }
            @Override
            void play(Effect effect, Block block){
                block.getWorld().createExplosion(block.getLocation().add(0.5, 0.5, 0.5), effect.power, effect.fire);
            }
            @Override
            String writeToConfig(Effect effect){
                String s = "";
                s+=", power: "+effect.power;
                s+=", fire: "+effect.fire;
                return s;
            }
            @Override
            Effect loadEffect(String name, EffectLocation location, double chance, LinkedHashMap map){
                float power = ((Number)map.get("power")).floatValue();
                boolean fire = false;
                if(map.containsKey("fire")){
                    fire = (boolean)map.get("fire");
                }
                return new Effect(name, location, chance, power, fire);
            }
        },
        MARKER(Material.ARMOR_STAND){
            @Override
            Effect createNewEffect(String name){
                return new Effect(name, EffectLocation.TREE, 1, false);
            }
            @Override
            void print(Effect effect, Logger logger){
                logger.log(Level.INFO, "- Permanent: {0}", effect.permanent);
                logger.log(Level.INFO, "- Tags: {0}", Arrays.toString(effect.tags));
            }
            @Override
            void play(Effect effect, Block block){
                Entity entity = block.getWorld().spawnEntity(block.getLocation().add(.5,.5,.5), effect.permanent?EntityType.ARMOR_STAND:EntityType.AREA_EFFECT_CLOUD);
                if(effect.permanent){
                    ArmorStand as = (ArmorStand)entity;
                    as.setMarker(true);
                    as.setInvulnerable(true);
                    as.setGravity(false);
                    as.setVisible(false);
                }else{
                    AreaEffectCloud cloud = (AreaEffectCloud)entity;
                    cloud.setReapplicationDelay(0);
                    cloud.setRadius(0);
                    cloud.setDuration(0);
                    cloud.setWaitTime(0);
                }
                entity.addScoreboardTag("tree_feller");
                for(String s : effect.tags)entity.addScoreboardTag(s);
            }
            @Override
            String writeToConfig(Effect effect){
                String s = "";
                s+=", permanent: "+effect.permanent;
                s+=", tags: "+Arrays.toString(effect.tags);
                return s;
            }
            @Override
            Effect loadEffect(String name, EffectLocation location, double chance, LinkedHashMap map){
                boolean permanent = false;
                if(map.containsKey("permanent")){
                    permanent = (boolean)map.get("permanent");
                }
                String[] tags = new String[0];
                if(map.containsKey("tags")){
                    Object ob = map.get(tags);
                    if(ob instanceof ArrayList){
                        ArrayList<String> list = (ArrayList<String>)map.get("tags");
                        tags = list.toArray(new String[list.size()]);
                    }else if(ob instanceof String){
                        tags = new String[]{(String)ob};
                    }else{
                        throw new IllegalArgumentException("Unknown marker tags format: "+ob+"! Please use an array or a String!");
                    }
                }
                return new Effect(name, location, chance, permanent, tags);
            }
        };
        private final Material item;
        private EffectType(Material item){
            this.item = item;
        }
        public Material getItem(){
            return item;
        }
        abstract Effect createNewEffect(String name);
        abstract void print(Effect effect, Logger logger);
        abstract void play(Effect effect, Block block);
        abstract String writeToConfig(Effect effect);
        abstract Effect loadEffect(String name, EffectLocation location, double chance, LinkedHashMap map);
    }
    @Override
    public String toString(){
        return name;
    }
}