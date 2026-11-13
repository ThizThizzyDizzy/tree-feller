package com.thizthizzydizzy.treefeller.platform.bukkit.core.connector;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform.*;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.*;
import io.netty.channel.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.*;
/**
 * Test-only recorder of output sent to a player and effect events in the world.
 * MCAutoTester uses the snapshots to assert actual messages, particles, sounds,
 * marker entities, and explosions. Closing removes listeners and test markers.
 */
final class BukkitTestOutputRecorder implements TestOutputRecorder, Listener, BukkitPacketObserver {
    private final List<MessageObservation> messages = new CopyOnWriteArrayList<>();
    private final List<ParticleObservation> particles = new CopyOnWriteArrayList<>();
    private final List<SoundObservation> sounds = new CopyOnWriteArrayList<>();
    private final List<Entity> markers = new ArrayList<>();
    private final List<ExplosionObservation> explosions = new ArrayList<>();
    private final ChannelPipeline pipeline;
    private final String handler = "treefeller-effects-" + UUID.randomUUID();
    private volatile RuntimeException failure;
    BukkitTestOutputRecorder(Player player) {
        BukkitPacketConnector packets = BukkitConnector.packets;
        pipeline = packets.channel(player).pipeline();
        pipeline.addBefore("packet_handler", handler, new ChannelOutboundHandlerAdapter() {
            @Override
            public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
                try {
                    packets.observe(packet, BukkitTestOutputRecorder.this);
                } catch (RuntimeException ex) {
                    failure = ex;
                }
                super.write(ctx, packet, promise);
            }
        });
        BukkitConnector.api.registerOutputListeners(this, markers, explosions);
    }
    @Override
    public void message(MessageObservation value) {
        messages.add(value);
    }
    @Override
    public void particle(ParticleObservation value) {
        particles.add(value);
    }
    @Override
    public void sound(SoundObservation value) {
        sounds.add(value);
    }
    private void check() {
        if (failure != null)
            throw failure;
    }
    @Override
    public List<MessageObservation> messages() {
        check();
        return new ArrayList<>(messages);
    }
    @Override
    public List<ParticleObservation> particles() {
        check();
        return new ArrayList<>(particles);
    }
    @Override
    public List<SoundObservation> sounds() {
        check();
        return new ArrayList<>(sounds);
    }
    @Override
    public List<ExplosionObservation> explosions() {
        return new ArrayList<>(explosions);
    }
    @Override
    public List<MarkerObservation> markers() {
        List<MarkerObservation> result = new ArrayList<>();
        for (Entity entity : markers) {
            Location p = entity.getLocation();
            Set<String> tags = new HashSet<>(BukkitConnector.api.markerTags(entity));
            if (!tags.contains("tree_feller"))
                continue;
            boolean stand = entity instanceof ArmorStand;
            result.add(new MarkerObservation(p.getX(), p.getY(), p.getZ(), stand, entity.isValid(),
                    stand && !((ArmorStand) entity).isVisible(), BukkitConnector.api.invulnerable(entity),
                    BukkitConnector.api.gravity(entity), stand && BukkitConnector.api.marker(entity), tags));
        }
        return result;
    }
    @Override
    public void close() {
        HandlerList.unregisterAll(this);
        if (pipeline != null && pipeline.get(handler) != null)
            pipeline.remove(handler);
        for (Entity entity : markers) entity.remove();
    }
}
