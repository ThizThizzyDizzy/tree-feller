package com.thizthizzydizzy.vanillify.version;
import java.util.UUID;
import net.minecraft.server.v1_16_R1.ChatMessageType;
import net.minecraft.server.v1_16_R1.IChatBaseComponent;
import net.minecraft.server.v1_16_R1.PacketPlayOutChat;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
public class Wrapper1_16_R1 implements VersionWrapper{
    @Override
    public void actionbar(Player player, String text){
        PacketPlayOutChat packet = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\":\""+text+"\"}"), ChatMessageType.GAME_INFO, new UUID(0L,0L));
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
    }
}