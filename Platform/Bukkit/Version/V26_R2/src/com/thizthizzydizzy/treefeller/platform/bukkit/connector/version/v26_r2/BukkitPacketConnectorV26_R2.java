package com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v26_r2;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform.*;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.*;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v26_r1.BukkitPacketConnectorV26_R1;
import java.util.Locale;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import org.bukkit.craftbukkit.CraftParticle;
import org.joml.Vector3f;

/** 26.3 replaced the particle packet with a record. Other packet APIs are unchanged. */
public final class BukkitPacketConnectorV26_R2 extends BukkitPacketConnectorV26_R1 {
    @Override
    public void observe(Object packet, BukkitPacketObserver observer) {
        if (packet instanceof ClientboundLevelParticlesPacket)
            particle((ClientboundLevelParticlesPacket) packet, observer);
        else
            super.observe(packet, observer);
    }
    private static void particle(ClientboundLevelParticlesPacket value, BukkitPacketObserver observer) {
        ParticleOptions particle = value.particle();
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
        observer.particle(new ParticleObservation(value.x(), value.y(), value.z(), value.xDist(), value.yDist(),
                value.zDist(), value.xMaxSpeed(), value.count(), description, red, green, blue, size));
    }
}
