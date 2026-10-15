package com.thizthizzydizzy.treefeller.core.debug;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
/** Platforms supply their own invocation and permission hook. */
public final class DebugCommands{
    private DebugCommands(){}
    private static String position(IPlayerConnector player){
        TreeFellerDebugger.ReplayStep step=TreeFellerDebugger.peek(player);
        return step==null?"end of trace":step.toString();
    }
    public static boolean execute(IPlayerConnector player,String[] args){
        if(args.length>=2&&args[0].equalsIgnoreCase("debug")){
            if(args[1].equalsIgnoreCase("clear")){TreeFellerDebugger.clear(player);player.sendMessage("[TreeFeller] Debug trace and overlay cleared.");return true;}
            if(args[1].equalsIgnoreCase("status")){player.sendMessage("[TreeFeller] "+TreeFellerDebugger.pending(player)+" debug steps pending; next: "+position(player));return true;}
            if(args[1].equalsIgnoreCase("next")){
                if(args.length>2){
                    TreeFellerDebugger.Advance tier=null;
                    try{tier=TreeFellerDebugger.Advance.valueOf(args[2].toUpperCase(java.util.Locale.ROOT));}
                    catch(IllegalArgumentException ex){/* Numeric compatibility below. */}
                    if(tier!=null){
                        int advanced=TreeFellerDebugger.advance(player,tier);
                        player.sendMessage("[TreeFeller] Advanced "+advanced+" events; next: "+position(player));
                        return true;
                    }
                }
                int count=1;
                try{if(args.length>2)count=Integer.parseInt(args[2]);}
                catch(NumberFormatException ex){player.sendMessage("Choose event, layer, phase, all, or a count of 1..1000.");return true;}
                if(count<1||count>1000){player.sendMessage("Choose event, layer, phase, all, or a count of 1..1000.");return true;}
                boolean advanced=false;
                for(int i=0;i<count;i++){if(!TreeFellerDebugger.next(player))break;advanced=true;}
                if(!advanced)player.sendMessage("[TreeFeller] No debug steps pending.");
                return true;
            }
        }
        player.sendMessage("Usage: /treefeller debug <next [event|layer|phase|all|count]|clear|status>");return true;
    }
}
