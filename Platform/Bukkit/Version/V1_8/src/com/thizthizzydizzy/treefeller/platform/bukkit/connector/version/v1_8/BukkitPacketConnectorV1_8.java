package com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_8;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform.*;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.*;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import java.util.Locale;
import net.minecraft.server.v1_8_R1.ChatSerializer;
import net.minecraft.server.v1_8_R1.EnumParticle;
import net.minecraft.server.v1_8_R1.IChatBaseComponent;
import net.minecraft.server.v1_8_R1.PacketDataSerializer;
import net.minecraft.server.v1_8_R1.PacketPlayOutChat;
import net.minecraft.server.v1_8_R1.PacketPlayOutNamedSoundEffect;
import net.minecraft.server.v1_8_R1.PacketPlayOutTitle;
import net.minecraft.server.v1_8_R1.PacketPlayOutWorldParticles;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R1.util.CraftChatMessage;
import org.bukkit.entity.Player;

/** Direct connection access and native packet decoding for 1_8. */
public final class BukkitPacketConnectorV1_8 implements BukkitPacketConnector {
    @Override
    public Channel channel(Player player) {
        throw new UnsupportedOperationException("Packet capture requires Minecraft 1.13 or newer");
    }
    @Override
    public void observe(Object packet, BukkitPacketObserver observer) {
        if (packet instanceof PacketPlayOutChat) {
            PacketPlayOutChat value = (PacketPlayOutChat) packet;
            PacketDataSerializer data = new PacketDataSerializer(Unpooled.buffer());
            try {
                value.b(data);
                String json = BukkitPacketData.string(data);
                String text = ChatSerializer.a(json).c();
                observer.message(new MessageObservation(data.readUnsignedByte() == 2, text));
            } finally {
                data.release();
            }
            return;
        }
        if (packet instanceof PacketPlayOutTitle) {
            PacketPlayOutTitle value = (PacketPlayOutTitle) packet;
            PacketDataSerializer data = new PacketDataSerializer(Unpooled.buffer());
            try {
                value.b(data);
                if (BukkitPacketData.varInt(data) != 2)
                    return;
                String json = BukkitPacketData.string(data);
                String text = ChatSerializer.a(json).c();
                observer.message(new MessageObservation(true, text));
            } finally {
                data.release();
            }
            return;
        }
        if (packet instanceof PacketPlayOutWorldParticles) {
            PacketPlayOutWorldParticles value = (PacketPlayOutWorldParticles) packet;
            PacketDataSerializer data = new PacketDataSerializer(Unpooled.buffer());
            try {
                value.b(data);
                int id = data.readInt();
                data.readBoolean();
                double x = data.readFloat(), y = data.readFloat(), z = data.readFloat();
                float dx = data.readFloat(), dy = data.readFloat(), dz = data.readFloat(), speed = data.readFloat();
                int count = data.readInt();
                double red = Double.NaN, green = Double.NaN, blue = Double.NaN, size = Double.NaN;
                String description;
                description = "minecraft:" + EnumParticle.a(id).name().toLowerCase(Locale.ROOT);
                observer.particle(new ParticleObservation(
                        x, y, z, dx, dy, dz, speed, count, description, red, green, blue, size));
            } finally {
                data.release();
            }
            return;
        }
        if (packet instanceof PacketPlayOutNamedSoundEffect) {
            PacketPlayOutNamedSoundEffect value = (PacketPlayOutNamedSoundEffect) packet;
            PacketDataSerializer data = new PacketDataSerializer(Unpooled.buffer());
            try {
                value.b(data);
                String sound = BukkitPacketData.string(data);
                String category = "MASTER";
                double x = data.readInt() / 8d, y = data.readInt() / 8d, z = data.readInt() / 8d;
                float volume = data.readFloat(), pitch = data.readUnsignedByte() / 63f;
                observer.sound(new SoundObservation(x, y, z, volume, pitch, sound, category));
            } finally {
                data.release();
            }
            return;
        }
    }

    @Override
    public void actionbar(Player player, String text) {
        IChatBaseComponent component = CraftChatMessage.fromString(text)[0];
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(component, (byte) 2));
    }
}
