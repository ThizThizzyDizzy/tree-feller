package com.thizthizzydizzy.treefeller.core.debug;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import com.thizthizzydizzy.treefeller.core.connector.world.IWorldConnector;
import java.util.*;
import java.util.function.Consumer;
/** Server-thread diagnostics. Recorded steps never retain live tree nodes or block snapshots. */
public class TreeFellerDebugger{
    public static final Object ADMIN_BROADCAST = new Object();
    private static final Map<IPlayerConnector, Session> sessions = new HashMap<>();
    private static final List<Consumer<Output>> observers = new ArrayList<>();
    public static final class Output{
        public final IPlayerConnector player;
        public final DebugPhase phase;
        public final DebugLevel level;
        public final DebugLayer layer;
        public final String text;
        public final Long pos;
        public final OverlayMark mark;
        Output(IPlayerConnector player,DebugPhase phase,DebugLevel level,String text,Long pos,OverlayMark mark){
            this(player,phase,level,text,pos,mark,null);
        }
        Output(IPlayerConnector player,DebugPhase phase,DebugLevel level,String text,Long pos,OverlayMark mark,DebugLayer layer){
            this.player=player;this.phase=phase;this.level=level;this.text=text;this.pos=pos;this.mark=mark;this.layer=layer;
        }
    }
    /** Observe diagnostic events without changing output; periodic redraws are not new events. */
    public interface Capture extends AutoCloseable{@Override void close();}
    public static Capture observe(Consumer<Output> observer){observers.add(observer);return () -> observers.remove(observer);}
    private static void observed(Output output){for(Consumer<Output> observer : new ArrayList<>(observers))observer.accept(output);}
    public static DebuggerContext begin(Object... log){
        DebuggerContext context = new DebuggerContext();
        context.info(log);
        return context;
    }
    public static DebuggerContext begin(DebugPhase phase,IPlayerConnector player,IWorldConnector world){
        DebuggerContext context = new DebuggerContext();
        context.phase=phase;
        context.attach(player,world);
        return context;
    }
    static void text(IPlayerConnector player,DebugPhase phase,DebugLayer layer,DebugLevel level,String line,boolean chat,boolean console){
        Runnable send=() -> {if(console)TreeFellerCore.connector.log(line);if(chat)player.sendMessage(line);observed(new Output(player,phase,level,line,null,null,layer));};
        dispatch(player,phase,layer,send);
    }
    static void mark(IPlayerConnector player,IWorldConnector world,DebugPhase phase,DebugLayer layer,DebugLevel level,long pos,OverlayMark mark){
        String material=TreeFellerCore.config.debug.overlay_palette==null?null:TreeFellerCore.config.debug.overlay_palette.get(mark.name());
        int duration=Math.max(0,TreeFellerCore.config.debug.overlay_duration);
        int limit=Math.max(1,TreeFellerCore.config.debug.overlay_limit);
        int refresh=Math.max(0,TreeFellerCore.config.debug.overlay_refresh);
        dispatch(player,phase,layer,() -> {
            Session session=sessions.computeIfAbsent(player,k -> new Session());
            Position key=new Position(world,pos);
            if(!session.overlay.containsKey(key)&&session.overlay.size()>=limit)return;
            session.overlay.put(key,new Overlay(duration==0?Long.MAX_VALUE:session.age+duration,phase,level,mark,material,refresh,session.age+refresh));
            player.debugOverlay(world,pos,mark,material);
            observed(new Output(player,phase,level,null,pos,mark,layer));
            if(duration>0||refresh>0)scheduleOverlay(player,session);
        });
    }
    private static void scheduleOverlay(IPlayerConnector player,Session session){
        if(session.overlayScheduled)return;
        session.overlayScheduled=true;
        TreeFellerCore.connector.schedule(1,() -> {
            session.overlayScheduled=false;
            if(sessions.get(player)!=session)return;
            session.age++;
            boolean timed=false;
            for(Iterator<Map.Entry<Position,Overlay>> it=session.overlay.entrySet().iterator();it.hasNext();){
                Map.Entry<Position,Overlay> entry=it.next();Overlay overlay=entry.getValue();
                if(overlay.expires<=session.age){
                    Position key=entry.getKey();it.remove();player.debugRestore(key.world,key.pos);
                    observed(new Output(player,overlay.phase,overlay.level,null,key.pos,null));
                }else{
                    if(overlay.refresh>0&&session.age>=overlay.nextRefresh){
                        Position key=entry.getKey();player.debugOverlay(key.world,key.pos,overlay.mark,overlay.material);
                        overlay.nextRefresh=session.age+overlay.refresh;
                    }
                    if(overlay.expires!=Long.MAX_VALUE||overlay.refresh>0)timed=true;
                }
            }
            if(timed)scheduleOverlay(player,session);
        });
    }
    private static void dispatch(IPlayerConnector player,DebugPhase phase,DebugLayer layer,Runnable output){
        if(!TreeFellerCore.config.debug.step_through){output.run();return;}
        Session session=sessions.computeIfAbsent(player,k -> new Session());
        if(session.steps.size()<Math.max(1,TreeFellerCore.config.debug.step_limit)){
            if(!session.announced){session.announced=true;player.sendMessage("[TreeFeller] Trace recorded. Use /treefeller debug next [event|layer|phase|all] to advance; debug clear to discard.");}
            session.steps.add(new ReplayStep(phase,layer,output));
        }
        else if(!session.truncated){session.truncated=true;player.sendMessage("[TreeFeller] Debug step limit reached; remaining output omitted.");}
    }
    public static int overlaySize(IPlayerConnector player){Session s=sessions.get(player);return s==null?0:s.overlay.size();}
    public static int pending(IPlayerConnector player){Session s=sessions.get(player);return s==null?0:s.steps.size();}
    public static boolean next(IPlayerConnector player){
        Session session=sessions.get(player);
        if(session==null||session.steps.isEmpty())return false;
        session.steps.remove().action.run();
        return true;
    }
    /** Immutable recorded boundaries, independent of message wording and current configuration. */
    public static final class ReplayStep{
        public final DebugPhase phase;
        public final DebugLayer layer;
        private final Runnable action;
        private ReplayStep(DebugPhase phase,DebugLayer layer,Runnable action){this.phase=phase;this.layer=layer;this.action=action;}
        @Override public String toString(){return phase+(layer==null?"":" / "+layer.label);}
    }
    public enum Advance{EVENT,LAYER,PHASE,ALL}
    public static ReplayStep peek(IPlayerConnector player){Session s=sessions.get(player);return s==null?null:s.steps.peek();}
    public static int advance(IPlayerConnector player,Advance tier){
        ReplayStep first=peek(player);
        if(first==null)return 0;
        DebugLayer layer=first.layer;
        int count=0;
        while(peek(player)!=null){
            ReplayStep step=peek(player);
            if(tier!=Advance.ALL&&step.phase!=first.phase)break;
            if(tier==Advance.LAYER){
                if(layer!=null&&step.layer!=layer)break;
                if(layer==null)layer=step.layer;
            }
            next(player);count++;
            if(tier==Advance.EVENT)break;
        }
        return count;
    }
    public static void clearOverlay(IPlayerConnector player){
        Session session=sessions.get(player);
        if(session==null)return;
        for(Position key : session.overlay.keySet()){player.debugRestore(key.world,key.pos);observed(new Output(player,DebugPhase.EVENTS,DebugLevel.USER,null,key.pos,null));}
        session.overlay.clear();
    }
    public static void clear(IPlayerConnector player){clearOverlay(player);sessions.remove(player);}
    public static void clearAll(){for(IPlayerConnector player : new ArrayList<>(sessions.keySet()))clear(player);}
    private static final class Session{
        final ArrayDeque<ReplayStep> steps=new ArrayDeque<>();
        final Map<Position,Overlay> overlay=new LinkedHashMap<>();
        long age;
        boolean overlayScheduled;
        boolean truncated;
        boolean announced;
    }
    private static final class Overlay{
        final long expires;final DebugPhase phase;final DebugLevel level;
        final OverlayMark mark;final String material;final int refresh;long nextRefresh;
        Overlay(long expires,DebugPhase phase,DebugLevel level,OverlayMark mark,String material,int refresh,long nextRefresh){
            this.expires=expires;this.phase=phase;this.level=level;this.mark=mark;this.material=material;
            this.refresh=refresh;this.nextRefresh=nextRefresh;
        }
    }
    private static final class Position{
        final IWorldConnector world;final long pos;
        Position(IWorldConnector world,long pos){this.world=world;this.pos=pos;}
        @Override public int hashCode(){return 31*System.identityHashCode(world)+Long.hashCode(pos);}
        @Override public boolean equals(Object object){return object instanceof Position&&((Position)object).world==world&&((Position)object).pos==pos;}
    }
}
