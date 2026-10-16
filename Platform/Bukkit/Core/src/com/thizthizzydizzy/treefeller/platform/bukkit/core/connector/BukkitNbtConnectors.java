package com.thizthizzydizzy.treefeller.platform.bukkit.core.connector;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.BukkitNbtConnector;
/** Direct construction keeps native access and version selection free of reflection. */
final class BukkitNbtConnectors{
    private BukkitNbtConnectors(){}
    static BukkitNbtConnector create(String version){
        switch(version){
            case "1_13":return new com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_13.BukkitNbtConnectorV1_13();
            case "1_13_2":return new com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_13_2.BukkitNbtConnectorV1_13_2();
            case "1_16_1":return new com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_16_1.BukkitNbtConnectorV1_16_1();
            case "1_16_3":return new com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_16_3.BukkitNbtConnectorV1_16_3();
            case "1_16_5":return new com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_16_5.BukkitNbtConnectorV1_16_5();
            case "1_17_1":return new com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_17_1.BukkitNbtConnectorV1_17_1();
            case "1_18_R1":return new com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_18_r1.BukkitNbtConnectorV1_18_R1();
            case "1_18_R2":return new com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_18_r2.BukkitNbtConnectorV1_18_R2();
            case "1_19_R1":return new com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_19_r1.BukkitNbtConnectorV1_19_R1();
            case "1_19_R2":return new com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_19_r2.BukkitNbtConnectorV1_19_R2();
            case "1_19_R3":return new com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_19_r3.BukkitNbtConnectorV1_19_R3();
            case "1_20_R1":return new com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_20_r1.BukkitNbtConnectorV1_20_R1();
            case "1_20_R2":return new com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_20_r2.BukkitNbtConnectorV1_20_R2();
            case "1_20_R3":return new com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_20_r3.BukkitNbtConnectorV1_20_R3();
            case "1_20_R4":return new com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_20_r4.BukkitNbtConnectorV1_20_R4();
            case "1_21_R1":return new com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_21_r1.BukkitNbtConnectorV1_21_R1();
            case "1_21_R2":return new com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_21_r2.BukkitNbtConnectorV1_21_R2();
            case "1_21_R3":return new com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_21_r3.BukkitNbtConnectorV1_21_R3();
            case "1_21_R4":return new com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_21_r4.BukkitNbtConnectorV1_21_R4();
            case "1_21_R5":return new com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_21_r5.BukkitNbtConnectorV1_21_R5();
            case "1_21_R6":return new com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_21_r6.BukkitNbtConnectorV1_21_R6();
            case "1_21_R7":return new com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_21_r7.BukkitNbtConnectorV1_21_R7();
            case "1_8":return new com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_8.BukkitNbtConnectorV1_8();
            case "26_R1":return new com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v26_r1.BukkitNbtConnectorV26_R1();
            default:throw new IllegalArgumentException("Unknown Bukkit NBT connector: "+version);
        }
    }
}
