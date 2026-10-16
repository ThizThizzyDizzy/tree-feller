package com.thizthizzydizzy.treefeller.core.test.options.result;
import com.thizthizzydizzy.treefeller.core.test.*;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform.*;
import java.util.*;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
/** Individually selectable MCAutoTester suites for every effect field. */
public final class EffectsTests{
    private static final String MARKER="type=MARKER, location=TOOL, permanent=true";
    private static final String PARTICLE="type=PARTICLE, location=TOOL, particle=FLAME";
    private static final String SOUND="type=SOUND, location=TOOL, sound=\"minecraft:block.note_block.pling\"";
    private static String effect(String fields){return "{"+fields+"}";}
    public static void register(ITestPlatform platform){
        new EffectTestSuite("")
                .add("empty","",(c,x) -> count(c.markers().size(),0))
                .add("enabled",effect(MARKER),(c,x) -> marker(c,1,x))
                .add("refused-fell",effect(MARKER),"global.criteria.required-trunk.min=9","iron_axe",false,(c,x) -> count(c.markers().size(),0))
                .add("tree-tool-merge",effect(MARKER+", tags=[global]"),
                        "trees = [{trunk=[spruce_log], leaves=[spruce_leaves], result.effects=[{"+MARKER+", tags=[tree]}]}]\n"
                        +"tools = [{item=iron_axe, result.effects=[{"+MARKER+", tags=[tool]}]}]", "iron_axe",(c,x) -> {
                    marker(c,2,x);
                    Set<String> tags=new HashSet<>();for(MarkerObservation m:c.markers())tags.addAll(m.tags);
                    if(!tags.containsAll(Arrays.asList("tree","tool"))||tags.contains("global"))throw new AssertionError("Incorrect effect inheritance: "+tags);
                }).register(platform);
        new EffectTestSuite("chance")
                .add("zero",effect(MARKER+", chance=0"),(c,x) -> count(c.markers().size(),0))
                .add("one",effect(MARKER+", chance=1"),(c,x) -> marker(c,1,x)).register(platform);
        EffectTestSuite location=new EffectTestSuite("location");
        for(String loc:new String[]{"TRUNK","LEAVES","DECORATION","TOOL"}){
            location.add(loc.toLowerCase(Locale.ROOT),effect("type=MARKER, permanent=true, location="+loc),(c,x) -> {
                List<MarkerObservation> markers=c.markers();
                int expected=loc.equals("TRUNK")?8:loc.equals("LEAVES")?(int)location.tree.blocks.values().stream().filter(b -> b.axis==null).count():1;
                count(markers.size(),expected);
                Set<Long> positions=new HashSet<>();
                for(MarkerObservation m:markers){
                    long relative=BlockPos.toPos((int)Math.floor(m.x)-x,(int)Math.floor(m.y)-100,(int)Math.floor(m.z)-2);
                    if(!positions.add(relative))throw new AssertionError("Duplicate effect position");
                    if(loc.equals("DECORATION")){near(m.x,x+1.5);near(m.y,100.5);near(m.z,2.5);}
                    else if(loc.equals("TOOL")){near(m.x,x+.5);near(m.y,100.5);near(m.z,2.5);}
                    else{
                        TestTrees.Block block=location.tree.blocks.get(relative);
                        if(block==null||(block.axis!=null)!=loc.equals("TRUNK"))throw new AssertionError("Effect at incorrect tree block");
                    }
                }
            });
        }
        location.add("tool-break-refused",effect("type=MARKER, permanent=true, location=TOOL_BREAK"),(c,x) -> count(c.markers().size(),0))
                .add("tool-break",effect("type=MARKER, permanent=true, location=TOOL_BREAK"),"global.cutting.allow-partial-tool=true","iron_axe[damage=247]",(c,x) -> marker(c,1,x))
                .add("animated",effect("type=MARKER, permanent=true, location=TRUNK"),"global.cutting.cutting-animation=true\nglobal.cutting.animation-delay=2","iron_axe",(c,x) -> count(c.markers().size(),8))
                .register(platform);
        new EffectTestSuite("filter")
                .add("block-match",effect("type=MARKER, permanent=true, location=TRUNK, filter=[\"minecraft:spruce_log\"]"),(c,x) -> count(c.markers().size(),8))
                .add("block-refused",effect("type=MARKER, permanent=true, location=TRUNK, filter=[birch_log]"),(c,x) -> count(c.markers().size(),0))
                .add("tool-match",effect(MARKER+", filter=[IRON_AXE]"),(c,x) -> marker(c,1,x))
                .add("tool-refused",effect(MARKER+", filter=[diamond_axe]"),(c,x) -> count(c.markers().size(),0))
                .add("tool-break-match",effect("type=MARKER, permanent=true, location=TOOL_BREAK, filter=[iron_axe]"),"global.cutting.allow-partial-tool=true","iron_axe[damage=247]",(c,x) -> marker(c,1,x))
                .add("tool-break-refused",effect("type=MARKER, permanent=true, location=TOOL_BREAK, filter=[diamond_axe]"),"global.cutting.allow-partial-tool=true","iron_axe[damage=247]",(c,x) -> count(c.markers().size(),0))
                .register(platform);
        new EffectTestSuite("type")
                .add("particle",effect(PARTICLE),(c,x) -> {particle(c,x);count(c.markers().size(),0);})
                .add("sound",effect(SOUND),(c,x) -> {sound(c,x);count(c.markers().size(),0);})
                .add("explosion",effect("type=EXPLOSION,location=TOOL,power=1"),(c,x) -> {count(c.explosions().size(),1);count(c.markers().size(),0);})
                .add("marker",effect(MARKER),(c,x) -> marker(c,1,x)).register(platform);
        EffectTestSuite particle=new EffectTestSuite("particle");
        for(String name:new String[]{"FLAME","HEART"})particle.add(name.toLowerCase(Locale.ROOT),effect("type=PARTICLE,location=TOOL,particle="+name),(c,x) -> {
            ParticleObservation p=particle(c,x);if(!p.data.contains("minecraft:"+name.toLowerCase(Locale.ROOT)))throw new AssertionError(p.data);
        });
        particle.register(platform);
        for(String field:new String[]{"x","y","z","dx","dy","dz","speed","count"}){
            EffectTestSuite tests=new EffectTestSuite(field);
            for(int value:field.equals("speed")?new int[]{0,2,-1}:new int[]{0,2})tests.add("value-"+value,effect(PARTICLE+", "+field+"="+value),(c,x) -> {
                ParticleObservation p=onlyParticle(c);
                near(p.x,x+.5+(field.equals("x")?value:0));near(p.y,100.5+(field.equals("y")?value:0));near(p.z,2.5+(field.equals("z")?value:0));
                near(p.dx,field.equals("dx")?value:0);near(p.dy,field.equals("dy")?value:0);near(p.dz,field.equals("dz")?value:0);
                near(p.speed,field.equals("speed")?value:0);count(p.count,field.equals("count")?value:1);
            });
            tests.register(platform);
        }
        for(String field:new String[]{"r","g","b","size"}){
            EffectTestSuite tests=new EffectTestSuite(field);
            for(int value:field.equals("size")?new int[]{1,3}:new int[]{0,255})tests.add("value-"+value,
                    effect("type=PARTICLE,location=TOOL,particle=REDSTONE, "+field+"="+value),(c,x) -> {
                ParticleObservation p=particle(c,x);
                near(p.r,field.equals("r")?value/255.0:0);near(p.g,field.equals("g")?value/255.0:0);near(p.b,field.equals("b")?value/255.0:0);
                near(p.size,field.equals("size")?value:1);
            });
            tests.register(platform);
        }
        for(String field:new String[]{"item","block"}){
            EffectTestSuite tests=new EffectTestSuite(field);
            for(String material:field.equals("item")?new String[]{"diamond","apple"}:new String[]{"stone","oak_log[axis=x]"})
                tests.add(material.replaceAll("[^a-z]","-"),effect("type=PARTICLE,location=TOOL,particle="+(field.equals("item")?"ITEM_CRACK":"BLOCK_CRACK")+", "+field+"=\""+material+"\""),(c,x) -> {
                    ParticleObservation p=particle(c,x);
                    String base=material.split("\\[")[0];
                    if(!p.data.contains("minecraft:"+base)||(material.contains("axis=x")&&!p.data.contains("axis=x")))throw new AssertionError(p.data);
                });
            tests.register(platform);
        }
        EffectTestSuite sounds=new EffectTestSuite("sound");
        for(String name:new String[]{"block.note_block.pling","entity.experience_orb.pickup"})sounds.add(name.replace('.','-'),effect("type=SOUND,location=TOOL,sound=\"minecraft:"+name+"\""),(c,x) -> {
            SoundObservation s=sound(c,x);if(!s.sound.contains(name))throw new AssertionError(s.sound);
        });
        sounds.register(platform);
        for(String field:new String[]{"volume","pitch"}){
            EffectTestSuite tests=new EffectTestSuite(field);
            for(double value:new double[]{0.5,1.5})tests.add("value-"+String.valueOf(value).replace('.','-'),effect(SOUND+", "+field+"="+value),(c,x) -> {
                SoundObservation s=sound(c,x);near(s.volume,field.equals("volume")?value:1);near(s.pitch,field.equals("pitch")?value:1);
            });
            tests.register(platform);
        }
        EffectTestSuite power=new EffectTestSuite("power");
        for(int value:new int[]{0,3})power.add("value-"+value,effect("type=EXPLOSION,location=TOOL,power="+value),(c,x) -> {
            count(c.explosions().size(),value==0?0:1);
            int blocks=c.explosions().isEmpty()?0:c.explosions().get(0).blocks;
            if(value==0?blocks!=0:blocks==0)throw new AssertionError("Unexpected explosion damage: "+blocks);
        });
        power.register(platform);
        EffectTestSuite fire=new EffectTestSuite("fire");
        fire.foundationMaterial="bedrock";
        for(boolean value:new boolean[]{false,true})fire.add(String.valueOf(value),effect("type=EXPLOSION,location=TOOL,power=3,fire="+value),(c,x) -> {
            count(c.explosions().size(),1);
            int flames=fire.arena.world.fireCount(x-6,99,-4,x+6,110,8);
            if(value?flames==0:flames!=0)throw new AssertionError("Expected fire="+value+", got "+flames+" fire blocks");
        });
        fire.register(platform);
        new EffectTestSuite("permanent")
                .add("false",effect("type=MARKER,location=TOOL,permanent=false"),(c,x) -> {
                    count(c.markers().size(),1);MarkerObservation m=c.markers().get(0);
                    if(m.permanent||m.alive)throw new AssertionError("Temporary marker did not expire");
                })
                .add("true",effect(MARKER),(c,x) -> {
                    marker(c,1,x);MarkerObservation m=c.markers().get(0);
                    if(!m.permanent||!m.alive||!m.invisible||!m.invulnerable||m.gravity||!m.marker)throw new AssertionError("Incorrect permanent marker properties");
                }).register(platform);
        new EffectTestSuite("tags")
                .add("empty",effect(MARKER+",tags=[]"),(c,x) -> {marker(c,1,x);if(!c.markers().get(0).tags.equals(Collections.singleton("tree_feller")))throw new AssertionError("Unexpected marker tags");})
                .add("custom",effect(MARKER+",tags=[first,second]"),(c,x) -> {marker(c,1,x);if(!c.markers().get(0).tags.equals(new HashSet<>(Arrays.asList("tree_feller","first","second"))))throw new AssertionError("Missing marker tags");})
                .register(platform);
    }
    private static void marker(EffectCapture c,int count,int x){count(c.markers().size(),count);for(MarkerObservation m:c.markers()){near(m.x,x+.5);near(m.y,100.5);near(m.z,2.5);}}
    private static ParticleObservation onlyParticle(EffectCapture c){count(c.particles().size(),1);return c.particles().get(0);}
    private static ParticleObservation particle(EffectCapture c,int x){ParticleObservation p=onlyParticle(c);near(p.x,x+.5);near(p.y,100.5);near(p.z,2.5);return p;}
    private static SoundObservation sound(EffectCapture c,int x){
        List<SoundObservation> sounds=new ArrayList<>();for(SoundObservation s:c.sounds())if(s.sound.contains("note_block")||s.sound.contains("experience_orb"))sounds.add(s);
        count(sounds.size(),1);SoundObservation s=sounds.get(0);near(s.x,x+.5);near(s.y,100.5);near(s.z,2.5);
        if(!s.category.equalsIgnoreCase("blocks"))throw new AssertionError("Incorrect sound category: "+s.category);return s;
    }
    private static void count(int actual,int expected){if(actual!=expected)throw new AssertionError("Expected "+expected+", got "+actual);}
    private static void near(double actual,double expected){if(!Double.isFinite(actual)||Math.abs(actual-expected)>.0001)throw new AssertionError("Expected "+expected+", got "+actual);}
}
