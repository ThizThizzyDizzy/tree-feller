package com.thizthizzydizzy.treefeller;
import com.thizthizzydizzy.simplegui.ItemBuilder;
import com.thizthizzydizzy.vanillify.Vanillify;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
public class Message{
    public static ArrayList<Message> messages = new ArrayList();
    static{
        addMessage("toggle", "Tree feller is currently toggled off", new ItemBuilder(Material.BLACK_CONCRETE));
        addMessage("checking", "Checking tree #{0} with tool #{1}", new ItemBuilder(Material.YELLOW_CONCRETE));
        addMessage("prevent-breakage", "Felling this tree would break the tool", new ItemBuilder(Material.WOODEN_AXE).setDurability(1));
        addMessage("prevent-breakage-success", "Felling this tree won't break the tool", new ItemBuilder(Material.WOODEN_AXE));
        addMessage("durability-low", "Tool durability is too low: {0}<{1}", new ItemBuilder(Material.GOLDEN_AXE).setDurability(10));
        addMessage("partial", "Tool is cutting partial tree", new ItemBuilder(Material.OAK_PLANKS));
        addMessage("partial-tool", "Tool has partial durability", new ItemBuilder(Material.WOODEN_AXE));
        addMessage("protected", "This tree is protected by {0} at {1} {2} {3}", new ItemBuilder(Material.SHIELD));
        addMessage("success", "Success! Felling tree...", new ItemBuilder(Material.GREEN_CONCRETE));
        for(Option o : Option.options){
            if(o.hasDebugText())addMessage(o);
        }
        addMessage("reload", null, new ItemBuilder(Material.COMMAND_BLOCK)).chat = "Tree Feller reloaded!";
        addMessage("debug-enable", null, new ItemBuilder(Material.REPEATING_COMMAND_BLOCK)).chat = "Debug mode enabled";
        addMessage("debug-disable", null, new ItemBuilder(Material.REPEATING_COMMAND_BLOCK)).chat = "Debug mode disabled";
        addMessage("toggle-on", null, new ItemBuilder(Material.IRON_AXE)).chat = "Tree Feller enabled";
        addMessage("toggle-off", null, new ItemBuilder(Material.WOODEN_AXE)).chat = "Tree Feller disabled";
        addMessage("unknown-command", null, new ItemBuilder(Material.CHAIN_COMMAND_BLOCK)).chat = ChatColor.RED+"Unknown Command";
        addMessage("no-permission", null, new ItemBuilder(Material.BARRIER)).chat = ChatColor.RED+"Unknown Command";
        addMessage("command-usage", null, new ItemBuilder(Material.WRITABLE_BOOK)).chat = "Usage: {0}";
    }
    private static void addMessage(Option option){
        if(option.global)addMessage(option.getGlobalName(), option.getGlobalDebugText(), option.getConfigurationDisplayItem());
        if(option.tool)addMessage(option.getGlobalName()+"-tool", option.getToolDebugText(), option.getConfigurationDisplayItem());
        if(option.tree)addMessage(option.getGlobalName()+"-tree", option.getTreeDebugText(), option.getConfigurationDisplayItem());
        addMessage(option.getGlobalName()+"-success", option.getSuccessDebugText(), option.getConfigurationDisplayItem());
    }
    private static Message addMessage(String name, String debug, ItemBuilder icon){
        Message message = new Message(name, debug, icon);
        messages.add(message);
        return message;
    }
    public final String name;
    public String actionbar = null, chat = null,debug;
    public final ItemBuilder icon;
    public Message(String name, String debug, ItemBuilder icon){
        this.name = name;
        this.debug = debug;
        this.icon = icon;
    }
    public static Message getMessage(String name){
        for(Message m : messages){
            if(m.name.equals(name))return m;
        }
        return null;
    }
    public void load(FileConfiguration f){
        actionbar = f.getString("actionbar-"+name);
        chat = f.getString("chat-"+name);
        String debg = f.getString("debug-"+name);
        if(debg!=null)debug = debg;
    }
    public String getDebugText(){
        return debug;
    }
    public void send(Player player, Object... vars){
        if(actionbar!=null){
            String action = actionbar;
            for(int i = 0; i<vars.length; i++){
                action = action.replace("{"+i+"}", vars[i].toString());
            }
            actionbar(player, action);
        }
        if(chat!=null){
            String cha = chat;
            for(int i = 0; i<vars.length; i++){
                cha = cha.replace("{"+i+"}", vars[i].toString());
            }
            player.sendMessage(cha);
        }
    }
    public void send(CommandSender sender, Object... vars){
        if(chat!=null){
            String cha = chat;
            for(int i = 0; i<vars.length; i++){
                cha = cha.replace("{"+i+"}", vars[i].toString());
            }
            sender.sendMessage(cha);
        }
    }
    private void actionbar(Player player, String text){
        Vanillify.actionbar(player, text);
    }
}