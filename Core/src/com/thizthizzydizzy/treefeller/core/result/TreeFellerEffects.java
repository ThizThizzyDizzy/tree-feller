package com.thizthizzydizzy.treefeller.core.result;
import com.thizthizzydizzy.treefeller.core.config.structure.section.result.*;
import com.thizthizzydizzy.treefeller.core.connector.item.IItemConnector;
import com.thizthizzydizzy.treefeller.core.connector.world.IWorldConnector;
import com.thizthizzydizzy.treefeller.core.detection.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.LongConsumer;
/** A fell owns its effect settings, including throughout delayed cutting layers. */
public final class TreeFellerEffects{
    private final List<Entry> effects = new ArrayList<>();
    private final Map<Long, List<Entry>> blocks = new HashMap<>();
    private final long origin;
    public TreeFellerEffects(IWorldConnector world, long origin, List<TreeNode> nodes, IItemConnector tool, EffectConfiguration[] configured){
        this.origin = origin;
        if(configured==null)return;
        for(EffectConfiguration effect : configured){
            if(effect==null||effect.type==null||effect.location==null)
                throw new IllegalArgumentException("Effects require a type and location");
            validate(effect);
            Entry entry = new Entry(effect, world.prepareEffect(effect));
            if(matches(effect,String.valueOf(tool.getItemId())))effects.add(entry);
            for(TreeNode node : nodes){
                EffectLocation location = node.type==TreeNodeType.TRUNK?EffectLocation.TRUNK
                        :node.type==TreeNodeType.LEAVES?EffectLocation.LEAVES:EffectLocation.DECORATION;
                if(effect.location==location&&matches(effect,world.material(node.pos)))blocks.computeIfAbsent(node.pos, ignored -> new ArrayList<>()).add(entry);
            }
        }
    }
    public void block(long pos){
        List<Entry> selected = blocks.remove(pos);
        if(selected!=null)for(Entry entry : selected)entry.play(pos);
    }
    public void tool(boolean broken){
        for(Entry entry : effects)if(entry.config.location==(broken?EffectLocation.TOOL_BREAK:EffectLocation.TOOL))entry.play(origin);
    }
    private static boolean matches(EffectConfiguration effect, String material){
        if(effect.filter==null||effect.filter.length==0)return true;
        for(String filter : effect.filter)if(normalize(filter).equals(normalize(material)))return true;
        return false;
    }
    private static String normalize(String value){
        return value.toLowerCase(Locale.ROOT).replace("minecraft:", "");
    }
    private static void validate(EffectConfiguration e){
        if(e.type==EffectType.PARTICLE&&(e.particle==null||e.particle.isEmpty()))throw new IllegalArgumentException("Particle name is required");
        if(e.filter!=null)for(String material : e.filter)if(material==null||material.isEmpty())throw new IllegalArgumentException("Effect filter materials must not be empty");
        for(float value : new float[]{e.chance,e.x,e.y,e.z,e.dx,e.dy,e.dz,e.speed,e.r,e.g,e.b,e.size,e.volume,e.pitch,e.power})
            if(!Float.isFinite(value))throw new IllegalArgumentException("Effect values must be finite");
        if(e.count<0||e.power<0)throw new IllegalArgumentException("Particle count and explosion power must be nonnegative");
        if(e.tags==null)throw new IllegalArgumentException("Marker tags must be a list");
        for(String tag : e.tags)if(tag==null||tag.isEmpty())throw new IllegalArgumentException("Marker tags must not be empty");
    }
    private static final class Entry{
        final EffectConfiguration config;
        final LongConsumer playback;
        Entry(EffectConfiguration config, LongConsumer playback){this.config=config;this.playback=playback;}
        void play(long pos){if(ThreadLocalRandom.current().nextDouble()<config.chance)playback.accept(pos);}
    }
}
