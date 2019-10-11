package com.thizthizzydizzy.treefeller;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
public class CommandTreeFeller implements TabExecutor{
    private final TreeFeller plugin;
    public CommandTreeFeller(TreeFeller plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(!sender.hasPermission("treefeller.reload")&&!sender.hasPermission("treefeller.debug")){
            sender.sendMessage(ChatColor.RED+"Insufficient permission!");
            return true;
        }
        if(args.length<1){
            sender.sendMessage("Usage: /treefeller reload|debug [on|off]");
            return true;
        }
        if(args[0].equals("reload")){
            if(!sender.hasPermission("treefeller.reload")){
                sender.sendMessage(ChatColor.RED+"Insufficient permission!");
                return true;
            }
            if(args.length!=1){
                sender.sendMessage("Usage: /treefeller reload");
                return true;
            }
            plugin.reloadConfig();
            plugin.reload();
            sender.sendMessage("Tree Feller reloaded!");
            return true;
        }
        if(args[0].equals("debug")){
            if(!sender.hasPermission("treefeller.debug")){
                sender.sendMessage(ChatColor.RED+"Insufficient permission!");
                return true;
            }
            if(args.length>2){
                sender.sendMessage("Usage: /treefeller debug [on|off]");
                return true;
            }
            if(args.length==1){
                plugin.debug = !plugin.debug;
                sender.sendMessage("Debug mode "+(plugin.debug?"enabled":"disabled"));
            }else{
                if(args[1].equals("on")){
                    plugin.debug = true;
                    sender.sendMessage("Debug mode enabled");
                    return true;
                }else if(args[1].equals("off")){
                    plugin.debug = false;
                    sender.sendMessage("Debug mode disabled");
                    return true;
                }else{
                    sender.sendMessage("Usage: /treefeller debug [on|off]");
                    return true;
                }
            }
        }
        sender.sendMessage("Usage: /treefeller reload|debug [on|off]");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args){
        ArrayList<String> strs = new ArrayList<>();
        if(args.length==1){
            if("reloa".startsWith(args[0]))strs.add("reload");
            if("debu".startsWith(args[0]))strs.add("debug");
        }
        if(args.length==2){
            if(args[0].equals("debug")){
                if("of".startsWith(args[1]))strs.add("off");
                if("o".startsWith(args[1]))strs.add("on");
            }
        }
        return strs;
    }
}