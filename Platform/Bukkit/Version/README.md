# Bukkit version connectors

Core owns typed NBT matching, list subset matching, recursive merging, and protection of block entity identity/coordinates. NBT version classes only parse SNBT, serialize native data, and apply the merged result. Block-data hooks independently choose the existing 1.8 or 1.13 implementation; NBT selection does not duplicate those hooks. Connector construction is direct and does not use reflection.

The builder compiles Bukkit Core, then the version projects, then the small direct-construction bootstrap in Core. This order lets the connector interfaces live in Core without a separate API project. Native connector compilation failures stop the build. All connector bytecode targets Java 8 so the selector can load on older servers; building the complete set requires a current JDK capable of reading the newest server classes (JDK 25 or newer).

Like the existing Bukkit libraries, server jars and their dependencies are local build inputs and are excluded from Git. Place them under `Libraries/Bukkit/<connector suffix>/`. The two existing baselines retain their `Libraries/Bukkit/1.8` and `Libraries/Bukkit/1.13` locations.

| Connector suffix | Compile server |
| --- | --- |
| 1_8 | 1.8 |
| 1_13 | 1.13 |
| 1_13_2 | 1.13.2 |
| 1_16_1 | 1.16.1 |
| 1_16_3 | 1.16.3 |
| 1_16_5 | 1.16.5 |
| 1_17_1 | 1.17.1 |
| 1_18_R1 / 1_18_R2 | 1.18.1 / 1.18.2 |
| 1_19_R1 / 1_19_R2 / 1_19_R3 | 1.19 / 1.19.3 / 1.19.4 |
| 1_20_R1 / 1_20_R2 / 1_20_R3 / 1_20_R4 | 1.20.1 / 1.20.2 / 1.20.4 / 1.20.6 |
| 1_21_R1 / 1_21_R2 / 1_21_R3 / 1_21_R4 | 1.21 / 1.21.3 / 1.21.4 / 1.21.5 |
| 1_21_R5 / 1_21_R6 / 1_21_R7 | 1.21.6 / 1.21.10 / 1.21.11 |
| 26_R1 | 26.1.1 (Mojang names, unversioned CraftBukkit packages) |

Use the unbundled CraftBukkit server jar and its dependency jars for modern versions. The version selector groups patch releases by their native integration version; the documented supported range starts at 1.16.
