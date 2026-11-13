package com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_13;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform.*;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.*;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import java.io.IOException;
import java.util.Locale;
import net.minecraft.server.v1_13_R1.ChatMessageType;
import net.minecraft.server.v1_13_R1.IChatBaseComponent;
import net.minecraft.server.v1_13_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_13_R1.PacketDataSerializer;
import net.minecraft.server.v1_13_R1.PacketPlayOutChat;
import net.minecraft.server.v1_13_R1.PacketPlayOutCustomSoundEffect;
import net.minecraft.server.v1_13_R1.PacketPlayOutNamedSoundEffect;
import net.minecraft.server.v1_13_R1.PacketPlayOutTitle;
import net.minecraft.server.v1_13_R1.PacketPlayOutWorldParticles;
import net.minecraft.server.v1_13_R1.Particle;
import net.minecraft.server.v1_13_R1.ParticleParam;
import net.minecraft.server.v1_13_R1.ParticleParamItem;
import net.minecraft.server.v1_13_R1.ParticleParamRedstone;
import net.minecraft.server.v1_13_R1.SoundCategory;
import net.minecraft.server.v1_13_R1.SoundEffect;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_13_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_13_R1.util.CraftChatMessage;
import org.bukkit.entity.Player;

/** Direct connection access and native packet decoding for 1_13. */
public final class BukkitPacketConnectorV1_13 implements BukkitPacketConnector {
    @Override
    public Channel channel(Player player) {
        return ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel;
    }
    @Override
    public void observe(Object packet, BukkitPacketObserver observer) {
        try {
            if (packet instanceof PacketPlayOutChat) {
                PacketPlayOutChat value = (PacketPlayOutChat) packet;
                PacketDataSerializer data = new PacketDataSerializer(Unpooled.buffer());
                try {
                    value.b(data);
                    String json = BukkitPacketData.string(data);
                    String text = ChatSerializer.a(json).getString();
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
                    String text = ChatSerializer.a(json).getString();
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
                    Particle type = Particle.REGISTRY.getId(id);
                    int payload = data.readerIndex();
                    ParticleParam particle = (ParticleParam) type.f().b(type, data);
                    description = particle.a();
                    if (particle instanceof ParticleParamItem) {
                        data.readerIndex(payload);
                        description += " minecraft:"
                                + CraftItemStack.asBukkitCopy(data.k()).getType().name().toLowerCase(Locale.ROOT);
                    }
                    if (particle instanceof ParticleParamRedstone) {
                        red = data.getFloat(payload);
                        green = data.getFloat(payload + 4);
                        blue = data.getFloat(payload + 8);
                        size = data.getFloat(payload + 12);
                    }
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
                    String sound = SoundEffect.a.b(SoundEffect.a.getId(BukkitPacketData.varInt(data))).toString();
                    String category = SoundCategory.values()[BukkitPacketData.varInt(data)].name();
                    double x = data.readInt() / 8d, y = data.readInt() / 8d, z = data.readInt() / 8d;
                    float volume = data.readFloat(), pitch = data.readFloat();
                    observer.sound(new SoundObservation(x, y, z, volume, pitch, sound, category));
                } finally {
                    data.release();
                }
                return;
            }
            if (packet instanceof PacketPlayOutCustomSoundEffect) {
                PacketPlayOutCustomSoundEffect value = (PacketPlayOutCustomSoundEffect) packet;
                PacketDataSerializer data = new PacketDataSerializer(Unpooled.buffer());
                try {
                    value.b(data);
                    String sound = BukkitPacketData.string(data);
                    String category = SoundCategory.values()[BukkitPacketData.varInt(data)].name();
                    double x = data.readInt() / 8d, y = data.readInt() / 8d, z = data.readInt() / 8d;
                    float volume = data.readFloat(), pitch = data.readFloat();
                    observer.sound(new SoundObservation(x, y, z, volume, pitch, sound, category));
                } finally {
                    data.release();
                }
                return;
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot decode native packet", ex);
        }
    }

    @Override
    public void actionbar(Player player, String text) {
        IChatBaseComponent component = CraftChatMessage.fromString(text)[0];
        ((CraftPlayer) player)
                .getHandle()
                .playerConnection.sendPacket(new PacketPlayOutChat(component, ChatMessageType.GAME_INFO));
    }
}
