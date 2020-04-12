package com.thizthizzydizzy.treefeller;
import java.util.ArrayList;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
public class Message{
    public static ArrayList<Message> messages = new ArrayList();
    static{
        addMessage("toggle", "Tree feller is currently toggled off");
        addMessage("checking", "Checking tree #{0} with tool #{1}");
        addMessage("prevent-breakage", "Felling this tree would break the tool");
        addMessage("prevent-breakage-success", "Felling this tree won't break the tool");
        addMessage("durability-low", "Tool durability is too low: {0}<{1}");
        addMessage("partial", "Tool is cutting partial tree");
        addMessage("protected", "This tree is protected by {0} at {1} {2} {3}");
        addMessage("success", "Success! Felling tree...");
        for(Option o : Option.options){
            if(o.hasDebugText())addMessage(o);
        }
    }
    private static void addMessage(Option option){
        if(option.global)addMessage(option.getGlobalName(), option.getGlobalDebugText());
        if(option.tool)addMessage(option.getGlobalName()+"-tool", option.getToolDebugText());
        if(option.tree)addMessage(option.getGlobalName()+"-tree", option.getTreeDebugText());
        addMessage(option.getGlobalName()+"-success", option.getSuccessDebugText());
    }
    private static void addMessage(String name, String debug){
        messages.add(new Message(name, debug));
    }
    public final String name;
    private String actionbar = null,chat = null,debug;
    public Message(String name, String debug){
        this.name = name;
        this.debug = debug;
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
    private void actionbar(Player player, String text){
//        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(text));
//        PacketPlayOutChat packet = actionbar(text);
//        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
    }
//    private PacketPlayOutChat actionbar(String text){
//        PacketPlayOutChat packet = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\":\""+text+"\"}"), ChatMessageType.GAME_INFO);
//        return packet;
//    }
}