package com.thizthizzydizzy.treefeller.core.result;
import com.thizthizzydizzy.treefeller.core.config.structure.section.result.*;
import com.thizthizzydizzy.treefeller.core.connector.item.IItemConnector;
import com.thizthizzydizzy.treefeller.core.connector.world.IWorldConnector;
import com.thizthizzydizzy.treefeller.core.detection.*;
import com.thizthizzydizzy.treefeller.core.debug.*;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.LongConsumer;
/** A fell owns its effect settings, including throughout delayed cutting layers. */
public final class TreeFellerEffects{
    private final List<Entry> effects = new ArrayList<>();
    private final Map<Long, List<Entry>> blocks = new HashMap<>();
    private final long origin;
    private final DebuggerContext context;
    public TreeFellerEffects(IWorldConnector world,IPlayerConnector player, long origin, List<TreeNode> nodes, IItemConnector tool, EffectConfiguration[] configured){
        this.origin = origin;
        context=TreeFellerDebugger.begin(DebugPhase.EFFECTS,player,world);
        context.values("Effects",configured);
        if(configured==null){context.summary("No configured effects");return;}
        context.user("Configured "+configured.length+" effects");
        context.mark(DebugLevel.USER,origin,OverlayMark.EFFECT);
        for(EffectConfiguration effect : configured){
            if(effect==null||effect.type==null||effect.location==null)
                throw new IllegalArgumentException("Effects require a type and location");
            validate(effect);
            Entry entry = new Entry(effect, world.prepareEffect(effect));
            if(effect.location==EffectLocation.TOOL||effect.location==EffectLocation.TOOL_BREAK){
                if(matches(effect,definition -> tool.matches((com.thizthizzydizzy.treefeller.core.config.structure.special.IItemDefinition)definition)))effects.add(entry);
                else context.trace("Tool effect filter rejected "+tool.getItemId());
            }
            for(TreeNode node : nodes){
                EffectLocation location = node.type==TreeNodeType.TRUNK?EffectLocation.TRUNK
                        :node.type==TreeNodeType.LEAVES?EffectLocation.LEAVES:EffectLocation.DECORATION;
                if(effect.location==location){
                    if(matches(effect,definition -> world.matches(node.pos,(com.thizthizzydizzy.treefeller.core.config.structure.special.IBlockDefinition)definition))){blocks.computeIfAbsent(node.pos, ignored -> new ArrayList<>()).add(entry);context.mark(DebugLevel.DETAIL,node.pos,OverlayMark.SELECTED);}
                    else context.decision(DebugLevel.TRACE,node.pos,OverlayMark.REJECTED,"Effect material filter rejected "+world.material(node.pos));
                }
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
    private static boolean matches(EffectConfiguration effect,java.util.function.Predicate<com.thizthizzydizzy.treefeller.core.config.ISpecialConfigObject> matcher){
        if(effect.filter==null||effect.filter.length==0)return true;
        for(com.thizthizzydizzy.treefeller.core.config.ISpecialConfigObject filter:effect.filter)if(matcher.test(filter))return true;
        return false;
    }
    private static void validate(EffectConfiguration e){
        if(e.type==EffectType.PARTICLE&&(e.particle==null||e.particle.isEmpty()))throw new IllegalArgumentException("Particle name is required");
        if(e.filter!=null)for(com.thizthizzydizzy.treefeller.core.config.ISpecialConfigObject definition:e.filter)if(definition==null)throw new IllegalArgumentException("Effect filters must not be null");
        for(float value : new float[]{e.chance,e.x,e.y,e.z,e.dx,e.dy,e.dz,e.speed,e.r,e.g,e.b,e.size,e.volume,e.pitch,e.power})
            if(!Float.isFinite(value))throw new IllegalArgumentException("Effect values must be finite");
        if(e.count<0||e.power<0)throw new IllegalArgumentException("Particle count and explosion power must be nonnegative");
        if(e.tags==null)throw new IllegalArgumentException("Marker tags must be a list");
        for(String tag : e.tags)if(tag==null||tag.isEmpty())throw new IllegalArgumentException("Marker tags must not be empty");
    }
    private final class Entry{
        final EffectConfiguration config;
        final LongConsumer playback;
        Entry(EffectConfiguration config, LongConsumer playback){this.config=config;this.playback=playback;}
        void play(long pos){
            double roll=ThreadLocalRandom.current().nextDouble();
            context.trace("Effect "+config.type+" at "+DebuggerContext.position(pos)+": roll="+roll+", chance="+config.chance);
            if(roll<config.chance){playback.accept(pos);context.decision(DebugLevel.DETAIL,pos,OverlayMark.EFFECT,"Played "+config.type+" at "+config.location);}
            else context.decision(DebugLevel.TRACE,pos,OverlayMark.REJECTED,"Effect chance refused playback");
        }
    }
}
