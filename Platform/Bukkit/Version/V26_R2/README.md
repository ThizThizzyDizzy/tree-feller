This module compiles against `Libraries/Bukkit/26_R2/paper-26.3.jar`, with shared native dependencies from `Libraries/Bukkit/26_R1`.

The particle connector handles the 26.3 packet record on all Bukkit server families. The NBT connector handles Paper/Purpur’s widened `ItemInstance` copy signature; CraftBukkit/Spigot retain the existing 26_R1 NBT connector.

Java 8 class-file output is intentional: older Paper remappers scan every bundled connector, including versions they do not load. The compiler JDK still supplies the native API’s record types.
