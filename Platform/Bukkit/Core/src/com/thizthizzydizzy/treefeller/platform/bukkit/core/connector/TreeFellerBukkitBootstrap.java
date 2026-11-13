package com.thizthizzydizzy.treefeller.platform.bukkit.core.connector;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.BukkitApiConnector;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.BukkitNbtConnector;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.BukkitPacketConnector;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.TreeFellerBukkit;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.compatibility.BukkitCompatibilities;

/** Compiled after version projects so Core can construct their hooks directly. */
public final class TreeFellerBukkitBootstrap extends TreeFellerBukkit{
    @Override public void registerCompatibilities(){BukkitCompatibilities.register(this);}
    @Override public BukkitPacketConnector createPacketConnector(String version){return BukkitPacketConnectors.create(version);}
    @Override public BukkitApiConnector createApiConnector(String version){return BukkitApiConnectors.create(version);}
    @Override public BukkitNbtConnector createNbtConnector(String version){return BukkitNbtConnectors.create(version);}
}
