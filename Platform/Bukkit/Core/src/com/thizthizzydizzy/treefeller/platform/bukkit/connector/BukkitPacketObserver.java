package com.thizthizzydizzy.treefeller.platform.bukkit.connector;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform.*;
/** Values decoded by a native packet connector. */
public interface BukkitPacketObserver {
    void message(MessageObservation message);
    void particle(ParticleObservation particle);
    void sound(SoundObservation sound);
}
