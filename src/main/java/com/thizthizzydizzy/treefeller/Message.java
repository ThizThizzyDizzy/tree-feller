package com.thizthizzydizzy.treefeller;
import com.thizthizzydizzy.simplegui.ItemBuilder;
import com.thizthizzydizzy.vanillify.Vanillify;
import java.util.ArrayList;
import org.bukkit.Material;
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
        addMessage("protected", "This tree is protected by {0} at {1} {2} {3}", new ItemBuilder(Material.SHIELD));
        addMessage("success", "Success! Felling tree...", new ItemBuilder(Material.GREEN_CONCRETE));
        for(Option o : Option.options){
            if(o.hasDebugText())addMessage(o);
        }
    }
    private static void addMessage(Option option){
        if(option.global)addMessage(option.getGlobalName(), option.getGlobalDebugText(), option.getConfigurationDisplayItem());
        if(option.tool)addMessage(option.getGlobalName()+"-tool", option.getToolDebugText(), option.getConfigurationDisplayItem());
        if(option.tree)addMessage(option.getGlobalName()+"-tree", option.getTreeDebugText(), option.getConfigurationDisplayItem());
        addMessage(option.getGlobalName()+"-success", option.getSuccessDebugText(), option.getConfigurationDisplayItem());
    }
    private static void addMessage(String name, String debug, ItemBuilder icon){
        messages.add(new Message(name, debug, icon));
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
    private void actionbar(Player player, String text){
        Vanillify.actionbar(player, text);
    }
}