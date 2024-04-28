package com.thizthizzydizzy.treefeller;
import com.thizthizzydizzy.treefeller.menu.MenuConfiguration;
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
        commands.add(new TreeFellerCommand("help"){
            @Override
            protected boolean run(CommandSender sender, Command command, String label, String[] args){
                for(TreeFellerCommand cmd : commands){
                    if(cmd.hasPermission(sender)){
                        String s = cmd.getFullUsage();
                        if(s!=null)sender.sendMessage("/treefeller "+s);
                    }
                }
                return true;
            }
        });
        TreeFellerCommand toggleOn = new TreeFellerCommand("on", "toggle"){
            @Override
            protected boolean run(CommandSender sender, Command command, String label, String[] args){
                plugin.toggle((Player)sender, true);
                return true;
            }
        };
        TreeFellerCommand toggleOff = new TreeFellerCommand("off", "toggle"){
            @Override
            protected boolean run(CommandSender sender, Command command, String label, String[] args){
                plugin.toggle((Player)sender, false);
                return true;
            }
        };
        commands.add(new TreeFellerCommand("toggle", toggleOn, toggleOff) {
            @Override
            protected boolean run(CommandSender sender, Command command, String label, String[] args){
                plugin.toggle((Player)sender);
                return true;
            }
        });
        commands.add(toggleOn);
        commands.add(toggleOff);
        commands.add(new TreeFellerCommand("reload"){
            @Override
            protected boolean run(CommandSender sender, Command command, String label, String[] args){
                plugin.reloadConfig();
                plugin.reload(sender);
                Message.getMessage("reload").send(sender);
                return true;
            }
        });
        TreeFellerCommand debugOn = new TreeFellerCommand("on", "debug"){
            @Override
            protected boolean run(CommandSender sender, Command command, String label, String[] args){
                plugin.debug = true;
                Message.getMessage("debug-enable").send(sender);
                return true;
            }
        };
        TreeFellerCommand debugOff = new TreeFellerCommand("off", "debug"){
            @Override
            protected boolean run(CommandSender sender, Command command, String label, String[] args){
                plugin.debug = false;
                Message.getMessage("debug-disable").send(sender);
                return true;
            }
        };
        commands.add(new TreeFellerCommand("debug", debugOn, debugOff) {
            @Override
            protected boolean run(CommandSender sender, Command command, String label, String[] args){
                plugin.debug = !plugin.debug;
                Message.getMessage("debug-"+(plugin.debug?"enable":"disable")).send(sender);
                return true;
            }
        });
        commands.add(new TreeFellerCommand("config"){
            @Override
            protected boolean run(CommandSender sender, Command command, String label, String[] args){
                if(!(sender instanceof Player)){
                    sender.sendMessage("You're not a player!");
                    return false;
                }
                new MenuConfiguration(null, plugin, (Player)sender).openInventory();
                return true;
            }
        });
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(args.length<1){
            sendUsageResponse(sender);
            return true;
        }
        for(TreeFellerCommand cmd : commands){
            if(args[0].equals(cmd.command)){
                return cmd.onCommand(sender, command, label, trim(args, 1), args);
            }
        }
        sendUsageResponse(sender);
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
    private String getFullUsage(CommandSender sender){
        String usage = "/treefeller ";
        boolean foundValidCommand = false;
        String subUsage = "";
        for(TreeFellerCommand cmd : commands){
            if(cmd.hasPermission(sender)){
                subUsage+="|"+cmd.getFullUsage();
                foundValidCommand = true;
            }
        }
        if(!foundValidCommand)return null;
        usage+=subUsage.substring(1);
        return usage;
    }
    private void sendUsageResponse(CommandSender sender){
        String usage = getFullUsage(sender);
        if(usage==null)Message.getMessage("unknown-command").send(sender);
        else Message.getMessage("command-usage").send(sender);
    }
}
//treefeller (help|toggle [on|off]|on|off|reload|debug [on|off]|config)