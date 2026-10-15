package com.thizthizzydizzy.treefeller.core.debug;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.config.structure.ToolConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeConfiguration;
import com.thizthizzydizzy.treefeller.core.connector.item.IItemConnector;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
import com.thizthizzydizzy.treefeller.core.connector.world.IWorldConnector;
import com.thizthizzydizzy.treefeller.core.detection.TreeNode;
import com.thizthizzydizzy.treefeller.core.detection.TreeNodeType;
import com.thizthizzydizzy.treefeller.core.detection.TreeTree;
import com.thizthizzydizzy.treefeller.core.event.EventListener;
import com.thizthizzydizzy.treefeller.core.event.TreeFellerEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
public class DebuggerContext{
    private DebugLayer layer;
    public final class LayerScope implements AutoCloseable{
        private final DebugLayer previous;
        private LayerScope(String label){previous=layer;layer=new DebugLayer(label);}
        @Override public void close(){layer=previous;}
    }
    public LayerScope layer(String label){return new LayerScope(label);}

    private final HashSet<IPlayerConnector> players = new HashSet<>();
    DebugPhase phase=DebugPhase.EVENTS;
    private IWorldConnector world;
    private Long anchor;
    public void at(long pos){anchor=pos;}
    public void attach(IPlayerConnector player,IWorldConnector world){if(player!=null)players.add(player);this.world=world;}
    public boolean enabled(DebugLevel level){
        return TreeFellerCore.config!=null&&((TreeFellerCore.config.debug.chat||TreeFellerCore.config.debug.console)
                &&TreeFellerCore.config.debug.level(phase,false).includes(level));
    }
    public boolean active(DebugLevel level){
        return enabled(level)||(TreeFellerCore.config!=null&&TreeFellerCore.config.debug.block_changes&&TreeFellerCore.config.debug.level(phase,true).includes(level));
    }
    public static String position(long pos){return BlockPos.getX(pos)+","+BlockPos.getY(pos)+","+BlockPos.getZ(pos);}
    public static String describe(Object value){
        if(value==null)return "unset";
        if(value instanceof com.thizthizzydizzy.treefeller.core.config.ISpecialConfigObject){
            Object simplified=((com.thizthizzydizzy.treefeller.core.config.ISpecialConfigObject)value).asSimplified();
            if(simplified==null)return "any";
            if(simplified!=value)return describe(simplified);
        }
        if(value instanceof Number||value instanceof Boolean||value instanceof String||value instanceof Enum)return String.valueOf(value);
        if(value.getClass().isArray()){
            java.util.List<String> entries=new java.util.ArrayList<>();
            for(int i=0;i<java.lang.reflect.Array.getLength(value);i++)entries.add(describe(java.lang.reflect.Array.get(value,i)));
            return entries.toString();
        }
        if(value instanceof java.util.Map){
            java.util.List<String> entries=new java.util.ArrayList<>();
            ((java.util.Map<?,?>)value).forEach((k,v) -> entries.add(describe(k)+"="+describe(v)));
            return entries.toString();
        }
        java.util.List<String> fields=new java.util.ArrayList<>();
        for(java.lang.reflect.Field field : value.getClass().getFields()){
            if(java.lang.reflect.Modifier.isStatic(field.getModifiers()))continue;
            try{Object v=field.get(value);if(v!=null)fields.add(field.getName()+"="+describe(v));}
            catch(IllegalAccessException ex){throw new IllegalStateException(ex);}
        }
        return fields.isEmpty()?String.valueOf(value):String.join(", ",fields);
    }
    public void values(String label,Object value){if(enabled(DebugLevel.DETAIL))info(label+": "+describe(value));}
    public boolean check(String label,Object actual,Object expected,boolean passes){
        String line=label+": "+describe(actual)+" (required "+describe(expected)+")";
        if(passes)pass(line);else fail(line);
        return passes;
    }
    public void decision(DebugLevel level,long pos,OverlayMark mark,String reason){
        print(level,"INFO",position(pos)+": "+reason);mark(level,pos,mark);
    }
    public void summary(String line){print(DebugLevel.SUMMARY,"INFO",line);}
    public void user(String line){print(DebugLevel.USER,"INFO",line);}
    public void trace(String line){print(DebugLevel.TRACE,"INFO",line);}
    public void mark(DebugLevel level,long pos,OverlayMark mark){
        if(world==null||TreeFellerCore.config==null||!TreeFellerCore.config.debug.block_changes
                ||!TreeFellerCore.config.debug.level(phase,true).includes(level))return;
        for(IPlayerConnector player : players)TreeFellerDebugger.mark(player,world,phase,layer,level,pos,mark);
    }
    public void snapshot(TreeTree tree,DebugLevel level,OverlayMark override){
        if(world==null||TreeFellerCore.config==null||!TreeFellerCore.config.debug.block_changes
                ||!TreeFellerCore.config.debug.level(phase,true).includes(level))return;
        for(TreeNode node : tree.nodeMap.values()){
            OverlayMark mark=override;
            if(node.type==TreeNodeType.NONE
                    ||override==OverlayMark.SELECTED&&node.type!=TreeNodeType.TRUNK
                    &&node.type!=TreeNodeType.LEAVES
                    &&node.type!=TreeNodeType.DECORATION)mark=OverlayMark.RETAINED;
            else if(mark==null)switch(node.type){
                case ROOTS: mark=OverlayMark.ROOT;break;
                case TRUNK: mark=node.secondary?OverlayMark.SECONDARY_TRUNK:node.sectionId>0?OverlayMark.SECTION_TRUNK:OverlayMark.TRUNK;break;
                case LEAVES: mark=node.extendedDistance>0?(node.sectionId>0?OverlayMark.SECTION_OWNED_LEAVES:OverlayMark.OWNED_LEAVES)
                        :node.secondary?OverlayMark.SECONDARY_LEAVES:node.sectionId>0?OverlayMark.SECTION_LEAVES:OverlayMark.LEAVES;break;
                case EXTENDED_LEAVES: mark=OverlayMark.EXTENDED_LEAVES;break;
                case DECORATION: mark=node.sectionId>0?OverlayMark.SECTION_DECORATION:OverlayMark.DECORATION;break;
                default: continue;
            }
            mark(level,node.pos,mark);
        }
    }
    public void info(Object... objects){
        // collect all logs before printing, to allow the player (or log context) to not be the very first log item
        ArrayList<String> logs = new ArrayList<>();
        for(Object object : objects){
            if(object==null){
                logs.add("null");
                continue;
            }
            if(object==TreeFellerDebugger.ADMIN_BROADCAST){
                players.addAll(TreeFellerCore.connector.getAdminPlayers());
                logs.add("== DEBUGGER BROADCAST TO ALL ONLINE ADMINS ==");
                continue;
            }

            if(object instanceof String){
                String str = (String)object;
                if(phase==DebugPhase.EVENTS){
                    if(str.equals("Detect"))phase=DebugPhase.DETECTION;
                    else try{phase=DebugPhase.valueOf(str.toUpperCase(java.util.Locale.ROOT));}catch(IllegalArgumentException ignored){}
                }
                logs.add(str);
                continue;
            }
            if(object instanceof Enum){
                logs.add(object.getClass().getSimpleName()+": "+object.toString());
                continue;
            }

            if(object instanceof Long){
                long l = (long)object;
                if(anchor==null)anchor=l;
                int x = BlockPos.getX(l);
                int y = BlockPos.getY(l);
                int z = BlockPos.getZ(l);
                logs.add("Position: "+x+" "+y+" "+z);
                continue;
            }

            if(object instanceof IPlayerConnector){
                IPlayerConnector player = (IPlayerConnector)object;
                players.add(player);
                logs.add("Player: "+player.getPlayerName());
                continue;
            }
            if(object instanceof IWorldConnector){
                IWorldConnector world = (IWorldConnector)object;
                this.world=world;
                logs.add("World: "+world.getDimension());
                continue;
            }
            if(object instanceof IItemConnector){
                IItemConnector item = (IItemConnector)object;
                logs.add("Item: "+item.getItemId());
                continue;
            }

            if(object instanceof ToolConfiguration){
                ToolConfiguration tool = (ToolConfiguration)object;
                logs.add("Tool: "+describe(tool.item));
                continue;
            }
            if(object instanceof TreeConfiguration){
                TreeConfiguration tree = (TreeConfiguration)object;
                String[] trunks = new String[tree.trunk==null?0:tree.trunk.length];
                for(int i = 0; i<trunks.length; i++)
                    trunks[i] = Objects.toString(tree.trunk[i].asSimplified());
                String[] leaves = new String[tree.leaves==null?0:tree.leaves.length];
                for(int i = 0; i<leaves.length; i++)
                    leaves[i] = Objects.toString(tree.leaves[i].asSimplified());
                logs.add("Tree: "+String.join(", ", trunks)+" | "+String.join(", ", leaves));
                continue;
            }

            if(object instanceof TreeFellerEvent){
                logs.add("Event: "+object.getClass().getSimpleName());
                continue;
            }
            if(object instanceof EventListener){
                String simpleName = object.getClass().getSimpleName();
                if(simpleName.contains("$$Lambda"))simpleName = simpleName.substring(0, simpleName.indexOf("$$Lambda")+8);
                logs.add("Listener: "+simpleName);
                continue;
            }
            if(object instanceof Throwable){
                Throwable t = (Throwable)object;
                boolean first = true;
                while(t!=null){
                    logs.add((first?"Exception":"Cause")+": "+t.getClass().getName());
                    logs.add(t.getMessage());
                    for(StackTraceElement element : t.getStackTrace()){
                        logs.add(element.toString());
                    }
                    t = t.getCause();
                    first = false;
                }
                continue;
            }
            
            if(object instanceof TreeTree){
                snapshot((TreeTree)object,DebugLevel.USER,null);
                continue;
            }

            String unknown = object.getClass().getName()+": "+object.toString();
            logs.add(unknown);
        }
        for(String line : logs){
            print("INFO", line);
        }
    }
    public void pass(String line){print(DebugLevel.SUMMARY,"PASS",line);}
    public void fail(String line){print(DebugLevel.USER,"FAIL",line);if(anchor!=null)mark(DebugLevel.USER,anchor,OverlayMark.REJECTED);}
    private void print(String type,String line){print(DebugLevel.DETAIL,type,line);}
    private void print(DebugLevel level,String type,String line){
        if(TreeFellerCore.config==null||(!TreeFellerCore.config.debug.chat&&!TreeFellerCore.config.debug.console)
                ||!TreeFellerCore.config.debug.level(phase,false).includes(level))return;
        String message="[TreeFeller]["+phase.name().toLowerCase(java.util.Locale.ROOT)+"]["+type+"] "+line;
        boolean deferredConsole=TreeFellerCore.config.debug.console&&TreeFellerCore.config.debug.step_through&&!players.isEmpty();
        if(TreeFellerCore.config.debug.console&&!deferredConsole)TreeFellerCore.connector.log(message);
        for(IPlayerConnector player : players){
            if(TreeFellerCore.config.debug.chat||deferredConsole)TreeFellerDebugger.text(player,phase,layer,level,message,TreeFellerCore.config.debug.chat,deferredConsole);
            deferredConsole=false;
        }
    }
    public boolean match(String label,boolean value){print(DebugLevel.SUMMARY,value?"PASS":"FAIL",label+" = "+value);return value;}
    public boolean checkTrue(String label,boolean value){
        if(value)pass(label+" = true");else fail(label+" = false");
        return value;
    }
    public boolean checkFalse(String label,boolean value){
        if(value)fail(label+" = true");else pass(label+" = false");
        return value;
    }
}
