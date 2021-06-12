package com.thizthizzydizzy.vanillify.version;
import java.util.UUID;
import net.minecraft.network.chat.ChatMessageType;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutChat;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
public class Wrapper1_17_R1 implements VersionWrapper{
    @Override
    public void actionbar(Player player, String text){
        PacketPlayOutChat packet = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\":\""+text+"\"}"), ChatMessageType.c, new UUID(0L,0L));
        ((CraftPlayer)player).getHandle().b.sendPacket(packet);
    }
}