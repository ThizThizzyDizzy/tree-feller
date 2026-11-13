package com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_20_r1;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform.*;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.*;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.particles.ParticleParam;
import net.minecraft.core.particles.ParticleParamBlock;
import net.minecraft.core.particles.ParticleParamItem;
import net.minecraft.core.particles.ParticleParamRedstone;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.PacketPlayOutNamedSoundEffect;
import net.minecraft.network.protocol.game.PacketPlayOutWorldParticles;
import net.minecraft.server.MinecraftServer;
import org.bukkit.craftbukkit.v1_20_R1.CraftParticle;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.joml.Vector3f;

/** Direct connection access and native packet decoding for 1_20_R1. */
public final class BukkitPacketConnectorV1_20_R1 implements BukkitPacketConnector {
    @Override
    public Channel channel(Player player) {
        List<NetworkManager> connections = MinecraftServer.getServer().ad().e();
        synchronized (connections) {
            for (NetworkManager connection : connections)
                if (player.getAddress().equals(connection.c()))
                    return connection.m;
        }
        throw new IllegalStateException("No connection for " + player.getName());
    }
    @Override
    public void observe(Object packet, BukkitPacketObserver observer) {
        if (packet instanceof ClientboundBundlePacket) {
            for (Object child : ((ClientboundBundlePacket) packet).a()) observe(child, observer);
            return;
        }
        if (packet instanceof ClientboundSystemChatPacket) {
            ClientboundSystemChatPacket value = (ClientboundSystemChatPacket) packet;
            PacketDataSerializer data = new PacketDataSerializer(Unpooled.buffer());
            try {
                value.a(data);
                String json = BukkitPacketData.string(data);
                String text = CraftChatMessage.fromJSON(json).getString();
                observer.message(new MessageObservation(data.readBoolean(), text));
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
            observer.sound(new SoundObservation(value.d(), value.e(), value.f(), value.g(), value.h(),
                    value.a().a().a().toString(), value.c().name()));
            return;
        }
    }
    private static void particle(PacketPlayOutWorldParticles value, BukkitPacketObserver observer) {
        ParticleParam particle = value.k();
        String description =
                "minecraft:" + CraftParticle.toBukkit(particle.b()).name().toLowerCase(Locale.ROOT) + " " + particle;
        double red = Double.NaN, green = Double.NaN, blue = Double.NaN, size = Double.NaN;
        if (particle instanceof ParticleParamRedstone) {
            ParticleParamRedstone valueDust = (ParticleParamRedstone) particle;
            Vector3f color = valueDust.e();
            red = color.x();
            green = color.y();
            blue = color.z();
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
        ((CraftPlayer) player).getHandle().c.a(new ClientboundSetActionBarTextPacket(component));
    }
}
