package com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_20_r3;
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
import org.bukkit.craftbukkit.v1_20_R3.CraftParticle;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R3.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.joml.Vector3f;

/** Direct connection access and native packet decoding for 1_20_R3. */
public final class BukkitPacketConnectorV1_20_R3 implements BukkitPacketConnector {
    @Override
    public Channel channel(Player player) {
        List<NetworkManager> connections = MinecraftServer.getServer().af().e();
        synchronized (connections) {
            for (NetworkManager connection : connections)
                if (player.getAddress().equals(connection.f()))
                    return connection.n;
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
                ClientboundSystemChatPacket decoded = new ClientboundSystemChatPacket(data);
                observer.message(new MessageObservation(decoded.d(), decoded.a().getString()));
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
                ClientboundSetActionBarTextPacket decoded = new ClientboundSetActionBarTextPacket(data);
                observer.message(new MessageObservation(true, decoded.a().getString()));
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
            observer.sound(new SoundObservation(value.e(), value.f(), value.g(), value.h(), value.i(),
                    value.a().a().a().toString(), value.d().name()));
            return;
        }
    }
    private static void particle(PacketPlayOutWorldParticles value, BukkitPacketObserver observer) {
        ParticleParam particle = value.l();
        String description = "minecraft:"
                + CraftParticle.minecraftToBukkit(particle.b()).name().toLowerCase(Locale.ROOT) + " " + particle;
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
        observer.particle(new ParticleObservation(value.d(), value.e(), value.f(), value.g(), value.h(), value.i(),
                value.j(), value.k(), description, red, green, blue, size));
    }
    @Override
    public void actionbar(Player player, String text) {
        IChatBaseComponent component = CraftChatMessage.fromString(text)[0];
        ((CraftPlayer) player).getHandle().c.b(new ClientboundSetActionBarTextPacket(component));
    }
}
