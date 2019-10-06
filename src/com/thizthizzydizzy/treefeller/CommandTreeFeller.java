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
        if(!sender.hasPermission("treefeller.reload")){
            sender.sendMessage(ChatColor.RED+"Insufficient permission!");
            return true;
        }
        if(args.length!=1){
            sender.sendMessage("Usage: /treefeller reload");
            return true;
        }
        if(args[0].equals("reload")){
            plugin.reloadConfig();
            plugin.reload();
            sender.sendMessage("Tree Feller reloaded!");
            return true;
        }
        sender.sendMessage("Usage: /treefeller reload");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args){
        ArrayList<String> strs = new ArrayList<>();
        if(args.length==1){
            if("reloa".startsWith(args[0]))strs.add("reload");
        }
        return strs;
    }
}