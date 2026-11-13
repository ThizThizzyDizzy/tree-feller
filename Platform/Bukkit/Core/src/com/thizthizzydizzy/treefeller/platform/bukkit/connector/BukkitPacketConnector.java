package com.thizthizzydizzy.treefeller.platform.bukkit.connector;
import io.netty.channel.Channel;
import org.bukkit.entity.Player;
/** Typed access to a version's connection and outgoing packets. */
public interface BukkitPacketConnector {
    void actionbar(Player player, String text);
    Channel channel(Player player);
    void observe(Object packet, BukkitPacketObserver observer);
}
