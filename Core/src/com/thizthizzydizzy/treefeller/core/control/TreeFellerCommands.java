package com.thizthizzydizzy.treefeller.core.control;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.debug.DebugCommands;
import java.util.*;
/** Optional text adapter over the public actions. No platform command types are used. */
public final class TreeFellerCommands{
    private TreeFellerCommands(){}
    private static final String[] ROOT={"help","toggle","on","off","reload","debug","config"};
    private static String permission(String action){return action.equals("on")||action.equals("off")?"toggle":action;}
    private static String usage(String action){
        if(action.equals("toggle"))return "toggle [on|off]";
        if(action.equals("debug"))return "debug [on|off|next [event|layer|phase|all|count]|clear|status]";
        return action;
    }
    public static void help(ControlAudience source){
        if(!TreeFellerControls.permitted(source,"help"))return;
        for(String action:ROOT)if(source.hasPermission("treefeller."+permission(action)))source.sendMessage("/treefeller "+usage(action));
    }
    private static void usage(ControlAudience source){
        TreeFellerControls.message(source,TreeFellerCore.config.messages.command_usage);
    }
    public static boolean execute(ControlAudience source,String[] args){
        if(args.length==0){usage(source);return true;}
        String action=args[0].toLowerCase(Locale.ROOT);
        if(!Arrays.asList(ROOT).contains(action)){
            TreeFellerControls.message(source,TreeFellerCore.config.messages.unknown_command);usage(source);return true;
        }
        if(!TreeFellerControls.permitted(source,permission(action)))return true;
        if(action.equals("debug")){
            if(args.length==1||args.length==2&&(args[1].equalsIgnoreCase("on")||args[1].equalsIgnoreCase("off"))){
                TreeFellerControls.debug(source,args.length==1?null:args[1].equalsIgnoreCase("on"));return true;
            }
            boolean valid=args.length==2&&(args[1].equalsIgnoreCase("clear")||args[1].equalsIgnoreCase("status"))
                    ||(args.length==2||args.length==3)&&args[1].equalsIgnoreCase("next");
            if(!valid){source.sendMessage("Usage: /treefeller "+usage("debug"));return true;}
            if(TreeFellerControls.requirePlayer(source))DebugCommands.execute(source.player(),args);
            return true;
        }
        if(action.equals("toggle")){
            if(args.length==1)TreeFellerControls.toggle(source,null);
            else if(args.length==2&&(args[1].equalsIgnoreCase("on")||args[1].equalsIgnoreCase("off")))TreeFellerControls.toggle(source,args[1].equalsIgnoreCase("on"));
            else source.sendMessage("Usage: /treefeller toggle [on|off]");
            return true;
        }
        if(args.length!=1){source.sendMessage("Usage: /treefeller "+usage(action));return true;}
        switch(action){
            case "help":help(source);break;
            case "on":TreeFellerControls.toggle(source,true);break;
            case "off":TreeFellerControls.toggle(source,false);break;
            case "reload":TreeFellerControls.reload(source);break;
            case "config":ConfigurationEditor.open(source);break;
        }
        return true;
    }
    public static List<String> complete(ControlAudience source,String[] args){
        List<String> result=new ArrayList<>();
        if(args.length==0)return result;
        String action=args[0].toLowerCase(Locale.ROOT);
        Collection<String> candidates=Collections.emptyList();
        if(args.length==1){for(String candidate:ROOT)if(source.hasPermission("treefeller."+permission(candidate)))result.add(candidate);}
        else if(source.hasPermission("treefeller."+permission(action))){
            if(args.length==2&&action.equals("toggle"))candidates=Arrays.asList("on","off");
            if(args.length==2&&action.equals("debug"))candidates=Arrays.asList("on","off","next","clear","status");
            if(args.length==3&&action.equals("debug")&&args[1].equalsIgnoreCase("next"))candidates=Arrays.asList("event","layer","phase","all");
            result.addAll(candidates);
        }
        String prefix=args[args.length-1].toLowerCase(Locale.ROOT);result.removeIf(value -> !value.startsWith(prefix));return result;
    }
}
