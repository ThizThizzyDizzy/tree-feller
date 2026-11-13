package com.thizthizzydizzy.treefeller.platform.bukkit.core.connector;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.BukkitPacketConnector;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_13.BukkitPacketConnectorV1_13;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_13_2.BukkitPacketConnectorV1_13_2;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_16_1.BukkitPacketConnectorV1_16_1;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_16_3.BukkitPacketConnectorV1_16_3;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_16_5.BukkitPacketConnectorV1_16_5;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_17_1.BukkitPacketConnectorV1_17_1;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_18_r1.BukkitPacketConnectorV1_18_R1;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_18_r2.BukkitPacketConnectorV1_18_R2;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_19_r1.BukkitPacketConnectorV1_19_R1;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_19_r1.BukkitPacketConnectorV1_19_R1_1;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_19_r2.BukkitPacketConnectorV1_19_R2;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_19_r3.BukkitPacketConnectorV1_19_R3;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_20_r1.BukkitPacketConnectorV1_20_R1;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_20_r2.BukkitPacketConnectorV1_20_R2;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_20_r3.BukkitPacketConnectorV1_20_R3;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_20_r4.BukkitPacketConnectorV1_20_R4;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_21_r1.BukkitPacketConnectorV1_21_R1;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_21_r2.BukkitPacketConnectorV1_21_R2;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_21_r3.BukkitPacketConnectorV1_21_R3;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_21_r4.BukkitPacketConnectorV1_21_R4;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_21_r5.BukkitPacketConnectorV1_21_R5;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_21_r6.BukkitPacketConnectorV1_21_R6;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_21_r7.BukkitPacketConnectorV1_21_R7;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_8.BukkitPacketConnectorV1_8;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v26_r1.BukkitPacketConnectorV26_R1;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v26_r2.BukkitPacketConnectorV26_R2;

/** Direct construction keeps native access and version selection free of reflection. */
final class BukkitPacketConnectors {
    private BukkitPacketConnectors() {}
    static BukkitPacketConnector create(String version) {
        switch (version) {
            case "1_13":
                return new BukkitPacketConnectorV1_13();
            case "1_13_2":
                return new BukkitPacketConnectorV1_13_2();
            case "1_16_1":
                return new BukkitPacketConnectorV1_16_1();
            case "1_16_3":
                return new BukkitPacketConnectorV1_16_3();
            case "1_16_5":
                return new BukkitPacketConnectorV1_16_5();
            case "1_17_1":
                return new BukkitPacketConnectorV1_17_1();
            case "1_18_R1":
                return new BukkitPacketConnectorV1_18_R1();
            case "1_18_R2":
                return new BukkitPacketConnectorV1_18_R2();
            case "1_19_R1_1":
                return new BukkitPacketConnectorV1_19_R1_1();
            case "1_19_R1":
                return new BukkitPacketConnectorV1_19_R1();
            case "1_19_R2":
                return new BukkitPacketConnectorV1_19_R2();
            case "1_19_R3":
                return new BukkitPacketConnectorV1_19_R3();
            case "1_20_R1":
                return new BukkitPacketConnectorV1_20_R1();
            case "1_20_R2":
                return new BukkitPacketConnectorV1_20_R2();
            case "1_20_R3":
                return new BukkitPacketConnectorV1_20_R3();
            case "1_20_R4":
                return new BukkitPacketConnectorV1_20_R4();
            case "1_21_R1":
                return new BukkitPacketConnectorV1_21_R1();
            case "1_21_R2":
                return new BukkitPacketConnectorV1_21_R2();
            case "1_21_R3":
                return new BukkitPacketConnectorV1_21_R3();
            case "1_21_R4":
                return new BukkitPacketConnectorV1_21_R4();
            case "1_21_R5":
                return new BukkitPacketConnectorV1_21_R5();
            case "1_21_R6":
                return new BukkitPacketConnectorV1_21_R6();
            case "1_21_R7":
                return new BukkitPacketConnectorV1_21_R7();
            case "1_8":
                return new BukkitPacketConnectorV1_8();
            case "26_R2":
                return new BukkitPacketConnectorV26_R2();
            case "26_R1":
                return new BukkitPacketConnectorV26_R1();
            default:
                throw new IllegalArgumentException("Unknown Bukkit packet connector: " + version);
        }
    }
}
