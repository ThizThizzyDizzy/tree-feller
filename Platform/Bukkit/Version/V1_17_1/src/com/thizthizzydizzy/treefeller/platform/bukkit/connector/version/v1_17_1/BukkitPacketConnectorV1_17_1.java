package com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_17_1;
import com.mojang.math.Vector3fa;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform.*;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.*;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import java.util.Locale;
import net.minecraft.core.particles.ParticleParam;
import net.minecraft.core.particles.ParticleParamBlock;
import net.minecraft.core.particles.ParticleParamItem;
import net.minecraft.core.particles.ParticleParamRedstone;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.PacketPlayOutChat;
import net.minecraft.network.protocol.game.PacketPlayOutCustomSoundEffect;
import net.minecraft.network.protocol.game.PacketPlayOutNamedSoundEffect;
import net.minecraft.network.protocol.game.PacketPlayOutWorldParticles;
import org.bukkit.craftbukkit.v1_17_R1.CraftParticle;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftChatMessage;
import org.bukkit.entity.Player;

/** Direct connection access and native packet decoding for 1_17_1. */
public final class BukkitPacketConnectorV1_17_1 implements BukkitPacketConnector {
    @Override
    public Channel channel(Player player) {
        return ((CraftPlayer) player).getHandle().b.a.k;
    }
    @Override
    public void observe(Object packet, BukkitPacketObserver observer) {
        if (packet instanceof PacketPlayOutChat) {
            PacketPlayOutChat value = (PacketPlayOutChat) packet;
            PacketDataSerializer data = new PacketDataSerializer(Unpooled.buffer());
            try {
                value.a(data);
                String json = BukkitPacketData.string(data);
                String text = CraftChatMessage.fromJSON(json).getString();
                observer.message(new MessageObservation(data.readUnsignedByte() == 2, text));
            } finally {
                data.release();
            }
            return;
        }
        if (packet instanceof ClientboundSetActionBarTextPacket) {
            ClientboundSetActionBarTextPacket value = (ClientboundSetActionBarTextPacket) packet;
            PacketDataSerializer data = new PacketDataSerializer(Unpooled.buffer());
            try {
                value.a(data);
                String json = BukkitPacketData.string(data);
                String text = CraftChatMessage.fromJSON(json).getString();
                observer.message(new MessageObservation(true, text));
            } finally {
                data.release();
            }
            return;
        }
        if (packet instanceof PacketPlayOutWorldParticles) {
            particle((PacketPlayOutWorldParticles) packet, observer);
            return;
        }
        if (packet instanceof PacketPlayOutNamedSoundEffect) {
            PacketPlayOutNamedSoundEffect value = (PacketPlayOutNamedSoundEffect) packet;
            observer.sound(new SoundObservation(
                    value.d(), value.e(), value.f(), value.g(), value.h(), value.b().a().toString(), value.c().name()));
            return;
        }
        if (packet instanceof PacketPlayOutCustomSoundEffect) {
            PacketPlayOutCustomSoundEffect value = (PacketPlayOutCustomSoundEffect) packet;
            observer.sound(new SoundObservation(
                    value.d(), value.e(), value.f(), value.g(), value.h(), value.b().toString(), value.c().name()));
            return;
        }
    }
    private static void particle(PacketPlayOutWorldParticles value, BukkitPacketObserver observer) {
        ParticleParam particle = value.k();
        String description =
                "minecraft:" + CraftParticle.toBukkit(particle).name().toLowerCase(Locale.ROOT) + " " + particle;
        double red = Double.NaN, green = Double.NaN, blue = Double.NaN, size = Double.NaN;
        if (particle instanceof ParticleParamRedstone) {
            ParticleParamRedstone valueDust = (ParticleParamRedstone) particle;
            Vector3fa color = valueDust.e();
            red = color.a();
            green = color.b();
            blue = color.c();
            size = valueDust.f();
        }
        if (particle instanceof ParticleParamItem)
            description += " "
                    + "minecraft:"
                    + CraftItemStack.asBukkitCopy(((ParticleParamItem) particle).c())
                              .getType()
                              .name()
                              .toLowerCase(Locale.ROOT);
        if (particle instanceof ParticleParamBlock)
            description += " " + ((ParticleParamBlock) particle).c();
        observer.particle(new ParticleObservation(value.c(), value.d(), value.e(), value.f(), value.g(), value.h(),
                value.i(), value.j(), description, red, green, blue, size));
    }
    @Override
    public void actionbar(Player player, String text) {
        IChatBaseComponent component = CraftChatMessage.fromString(text)[0];
        ((CraftPlayer) player).getHandle().b.sendPacket(new ClientboundSetActionBarTextPacket(component));
    }
}
