package com.thizthizzydizzy.treefeller;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
public class CommandTreeFeller implements TabExecutor{
    private final TreeFeller plugin;
    public CommandTreeFeller(TreeFeller plugin){
        this.plugin = plugin;
    }
    private final ArrayList<TreeFellerCommand> commands = new ArrayList<>();
    {
        commands.add(new TreeFellerCommand("reload"){
            @Override
            protected boolean run(CommandSender sender, Command command, String label, String[] args){
                plugin.reloadConfig();
                plugin.reload();
                sender.sendMessage("Tree Feller reloaded!");
                return true;
            }
            @Override
            protected String getUsage(){
                return "/treefeller reload";
            }
        });
        commands.add(new TreeFellerCommand("help"){
            @Override
            protected boolean run(CommandSender sender, Command command, String label, String[] args){
                for(TreeFellerCommand cmd : commands){
                    if(cmd.hasPermission(sender)){
                        String s = cmd.getUsage();
                        if(s!=null)sender.sendMessage(s);
                    }
                }
                return true;
            }
            @Override
            protected String getUsage(){
                return "/treefeller help";
            }
        });
        TreeFellerCommand debugOn = new TreeFellerCommand("on"){
            @Override
            protected boolean run(CommandSender sender, Command command, String label, String[] args){
                plugin.debug = true;
                sender.sendMessage("Debug mode enabled");
                return true;
            }
            @Override
            protected String getUsage(){
                return "/treefeller debug on";
            }
        };
        TreeFellerCommand debugOff = new TreeFellerCommand("off"){
            @Override
            protected boolean run(CommandSender sender, Command command, String label, String[] args){
                plugin.debug = false;
                sender.sendMessage("Debug mode disabled");
                return true;
            }
            @Override
            protected String getUsage(){
                return "/treefeller debug off";
            }
        };
        commands.add(new TreeFellerCommand("debug", debugOn, debugOff) {
            @Override
            protected boolean run(CommandSender sender, Command command, String label, String[] args){
                plugin.debug = !plugin.debug;
                sender.sendMessage("Debug mode "+(plugin.debug?"enabled":"disabled"));
                return true;
            }
            @Override
            protected String getUsage(){
                return "/treefeller debug [on|off]";
            }
        });
        TreeFellerCommand toggleOn = new TreeFellerCommand("on"){
            @Override
            protected boolean run(CommandSender sender, Command command, String label, String[] args){
                plugin.disabledPlayers.remove(((Player)sender).getUniqueId());
                sender.sendMessage("Tree Feller enabled");
                return true;
            }
            @Override
            protected String getUsage(){
                return null;
            }
        };
        TreeFellerCommand toggleOff = new TreeFellerCommand("off"){
            @Override
            protected boolean run(CommandSender sender, Command command, String label, String[] args){
                plugin.disabledPlayers.add(((Player)sender).getUniqueId());
                sender.sendMessage("Tree Feller disabled");
                return true;
            }
            @Override
            protected String getUsage(){
                return null;
            }
        };
        commands.add(toggleOn);
        commands.add(toggleOff);
        commands.add(new TreeFellerCommand("toggle", toggleOn, toggleOff) {
            @Override
            protected boolean run(CommandSender sender, Command command, String label, String[] args){
                if(plugin.disabledPlayers.contains(((Player)sender).getUniqueId())){
                    plugin.disabledPlayers.remove(((Player)sender).getUniqueId());
                }else{
                    plugin.disabledPlayers.add(((Player)sender).getUniqueId());
                }
                sender.sendMessage("Tree Feller "+(plugin.disabledPlayers.contains(((Player)sender).getUniqueId())?"enabled":"disabled"));
                return true;
            }
            @Override
            protected String getUsage(){
                return "/treefeller on|off|toggle [on|off]";
            }
        });
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(args.length<1){
            sender.sendMessage("Usage: /treefeller help | toggle [on|off] | on | off | reload | debug [on|off]");
            return true;
        }
        for(TreeFellerCommand cmd : commands){
            if(args[0].equals(cmd.command)){
                return cmd.onCommand(sender, command, label, trim(args, 1), args);
            }
        }
        sender.sendMessage("Usage: /treefeller help | toggle [on|off] | on | off | reload | debug [on|off]");
        return true;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args){
        ArrayList<String> strs = new ArrayList<>();
        if(args.length==1){
            for(TreeFellerCommand cmd : commands){
                if(cmd.command.substring(0, cmd.command.length()-1).startsWith(args[0])&&cmd.hasPermission(sender))strs.add(cmd.command);
            }
        }
        if(args.length>1){
            for(TreeFellerCommand cmd : commands){
                if(args[0].equals(cmd.command))return cmd.onTabComplete(sender, command, label, trim(args, 1));
            }
        }
        return strs;
    }
    public String[] trim(String[] data, int beginning){
        if(data==null)return null;
        String[] newData = new String[Math.max(0,data.length-beginning)];
        for(int i = 0; i<newData.length; i++){
            newData[i] = data[i+beginning];
        }
        return newData;
    }
}