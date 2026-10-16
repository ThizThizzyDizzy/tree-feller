package com.thizthizzydizzy.treefeller.platform.bukkit.core.connector;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.TreeFellerBukkit;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.BukkitNbtConnector;
/** Compiled after version projects so Core can construct their hooks directly. */
public final class TreeFellerBukkitBootstrap extends TreeFellerBukkit{
    @Override public BukkitNbtConnector createNbtConnector(String version){return BukkitNbtConnectors.create(version);}
}
