package com.thizthizzydizzy.treefeller.platform.bukkit.core.connector;
import java.lang.reflect.Array;
import org.bukkit.entity.Player;
/** Native actionbar transport, including the Bukkit 1.8 baseline. */
final class BukkitActionbar{
    static void send(Player player,String text){
        try{
            player.getClass().getMethod("sendActionBar",String.class).invoke(player,text);
            return;
        }catch(NoSuchMethodException ex){
            // Spigot and the Bukkit 1.8 baseline use the transports below.
        }catch(ReflectiveOperationException ex){throw new IllegalStateException("Could not send actionbar",ex);}
        try{
            Class<?> component=Class.forName("net.md_5.bungee.api.chat.BaseComponent");
            Class<?> type=Class.forName("net.md_5.bungee.api.ChatMessageType");
            Object components=Class.forName("net.md_5.bungee.api.chat.TextComponent").getMethod("fromLegacyText",String.class).invoke(null,text);
            Object spigot=player.getClass().getMethod("spigot").invoke(player);
            spigot.getClass().getMethod("sendMessage",type,Array.newInstance(component,0).getClass())
                    .invoke(spigot,Enum.valueOf((Class)type,"ACTION_BAR"),components);
        }catch(ClassNotFoundException|NoSuchMethodException ex){
            try{
                String version=org.bukkit.Bukkit.getServer().getClass().getPackage().getName();
                String nms="net.minecraft.server."+version.substring(version.lastIndexOf('.')+1)+".";
                Class<?> component=Class.forName(nms+"IChatBaseComponent");
                Object chat=Class.forName(nms+"ChatComponentText").getConstructor(String.class).newInstance(text);
                Object packet=Class.forName(nms+"PacketPlayOutChat").getConstructor(component,byte.class).newInstance(chat,(byte)2);
                Object handle=player.getClass().getMethod("getHandle").invoke(player);
                Object connection=handle.getClass().getField("playerConnection").get(handle);
                connection.getClass().getMethod("sendPacket",Class.forName(nms+"Packet")).invoke(connection,packet);
            }catch(ReflectiveOperationException failure){throw new IllegalStateException("Could not send actionbar",failure);}
        }catch(ReflectiveOperationException ex){throw new IllegalStateException("Could not send actionbar",ex);}
    }
}
