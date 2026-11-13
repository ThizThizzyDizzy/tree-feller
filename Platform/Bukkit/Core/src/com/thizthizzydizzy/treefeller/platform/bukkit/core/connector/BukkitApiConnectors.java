package com.thizthizzydizzy.treefeller.platform.bukkit.core.connector;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.BukkitApiConnector;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_13.BukkitApiConnectorV1_13;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_16_1.BukkitApiConnectorV1_16_1;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_20_r4.BukkitApiConnectorV1_20_R4;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_21_r2.BukkitApiConnectorV1_21_R2;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_21_r4.BukkitApiConnectorV1_21_R4;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_21_r7.BukkitApiConnectorV1_21_R7;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_8.BukkitApiConnectorV1_8;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v26_r1.BukkitApiConnectorV26_R1;

/** Direct API construction at the Bukkit API change boundaries. */
final class BukkitApiConnectors {
    static BukkitApiConnector create(String version) {
        switch (version) {
            case "1_13":
                return new BukkitApiConnectorV1_13();
            case "1_16_1":
                return new BukkitApiConnectorV1_16_1();
            case "1_20_R4":
                return new BukkitApiConnectorV1_20_R4();
            case "1_21_R2":
                return new BukkitApiConnectorV1_21_R2();
            case "1_21_R4":
                return new BukkitApiConnectorV1_21_R4();
            case "1_21_R7":
                return new BukkitApiConnectorV1_21_R7();
            case "1_8":
                return new BukkitApiConnectorV1_8();
            case "26_R1":
                return new BukkitApiConnectorV26_R1();
            default:
                throw new IllegalArgumentException("Unknown Bukkit API connector: " + version);
        }
    }
}
