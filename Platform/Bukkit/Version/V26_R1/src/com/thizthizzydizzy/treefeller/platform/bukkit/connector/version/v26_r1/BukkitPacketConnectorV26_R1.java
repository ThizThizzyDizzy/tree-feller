package com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v26_r1;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform.*;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.*;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.server.MinecraftServer;
import org.bukkit.craftbukkit.CraftParticle;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.joml.Vector3f;

/** Direct connection access and native packet decoding for 26_R1. */
public class BukkitPacketConnectorV26_R1 implements BukkitPacketConnector {
    @Override
    public Channel channel(Player player) {
        List<Connection> connections = MinecraftServer.getServer().getConnection().getConnections();
        synchronized (connections) {
            for (Connection connection : connections)
                if (player.getAddress().equals(connection.getRemoteAddress()))
                    return connection.channel;
        }
        throw new IllegalStateException("No connection for " + player.getName());
    }
    @Override
    public void observe(Object packet, BukkitPacketObserver observer) {
        if (packet instanceof ClientboundBundlePacket) {
            for (Object child : ((ClientboundBundlePacket) packet).subPackets()) observe(child, observer);
            return;
        }
        if (packet instanceof ClientboundSystemChatPacket) {
            ClientboundSystemChatPacket value = (ClientboundSystemChatPacket) packet;
            RegistryFriendlyByteBuf data =
                    new RegistryFriendlyByteBuf(Unpooled.buffer(), CraftRegistry.getMinecraftRegistry());
            try {
                ClientboundSystemChatPacket.STREAM_CODEC.encode(data, value);
                ClientboundSystemChatPacket decoded = ClientboundSystemChatPacket.STREAM_CODEC.decode(data);
                observer.message(new MessageObservation(decoded.overlay(), decoded.content().getString()));
            } finally {
                data.release();
            }
            return;
        }
        if (packet instanceof ClientboundSetActionBarTextPacket) {
            ClientboundSetActionBarTextPacket value = (ClientboundSetActionBarTextPacket) packet;
            RegistryFriendlyByteBuf data =
                    new RegistryFriendlyByteBuf(Unpooled.buffer(), CraftRegistry.getMinecraftRegistry());
            try {
                ClientboundSetActionBarTextPacket.STREAM_CODEC.encode(data, value);
                ClientboundSetActionBarTextPacket decoded = ClientboundSetActionBarTextPacket.STREAM_CODEC.decode(data);
                observer.message(new MessageObservation(true, decoded.text().getString()));
            } finally {
                data.release();
            }
            return;
        }
        if (packet instanceof ClientboundLevelParticlesPacket) {
            particle((ClientboundLevelParticlesPacket) packet, observer);
            return;
        }
        if (packet instanceof ClientboundSoundPacket) {
            ClientboundSoundPacket value = (ClientboundSoundPacket) packet;
            observer.sound(new SoundObservation(value.getX(), value.getY(), value.getZ(), value.getVolume(),
                    value.getPitch(), value.getSound().value().location().toString(), value.getSource().name()));
            return;
        }
    }
    private static void particle(ClientboundLevelParticlesPacket value, BukkitPacketObserver observer) {
        ParticleOptions particle = value.getParticle();
        String description = "minecraft:"
                + CraftParticle.minecraftToBukkit(particle.getType()).name().toLowerCase(Locale.ROOT) + " " + particle;
        double red = Double.NaN, green = Double.NaN, blue = Double.NaN, size = Double.NaN;
        if (particle instanceof DustParticleOptions) {
            DustParticleOptions valueDust = (DustParticleOptions) particle;
            Vector3f color = valueDust.getColor();
            red = color.x();
            green = color.y();
            blue = color.z();
            size = valueDust.getScale();
        }
        if (particle instanceof ItemParticleOption)
            description += " "
                    + BuiltInRegistries.ITEM.getKey(((ItemParticleOption) particle).getItem().item().value())
                              .toString();
        if (particle instanceof BlockParticleOption)
            description += " " + ((BlockParticleOption) particle).getState();
        observer.particle(
                new ParticleObservation(value.getX(), value.getY(), value.getZ(), value.getXDist(), value.getYDist(),
                        value.getZDist(), value.getMaxSpeed(), value.getCount(), description, red, green, blue, size));
    }
    @Override
    public void actionbar(Player player, String text) {
        Component component = CraftChatMessage.fromString(text)[0];
        ((CraftPlayer) player).getHandle().connection.send(new ClientboundSetActionBarTextPacket(component));
    }
}
