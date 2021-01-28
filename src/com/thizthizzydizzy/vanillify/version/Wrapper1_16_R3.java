package com.thizthizzydizzy.vanillify.version;
import java.util.UUID;
import net.minecraft.server.v1_16_R3.ChatMessageType;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.PacketPlayOutChat;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
public class Wrapper1_16_R3 implements VersionWrapper{
    @Override
    public void actionbar(Player player, String text){
        PacketPlayOutChat packet = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\":\""+text+"\"}"), ChatMessageType.GAME_INFO, new UUID(0L,0L));
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
    }
}