package com.thizthizzydizzy.treefeller.core.test;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockAxis;
/** Integration hooks only. All calls are made on the server thread. */
public interface ITestPlatform{
    ITestWorld world(String player);
    void preparePlayer(String player, boolean survival);
    void setPlayerGameMode(String player, com.thizthizzydizzy.treefeller.core.connector.player.PlayerGameMode mode);
    void setPlayerSneaking(String player, boolean sneaking);
    /** Equip an item using Minecraft's item definition syntax. */
    void equipItem(String player, String definition);
    void setToolCount(String player, int count);
    void setPlayerLevels(String player, int food, float health, float saturation);
    com.thizthizzydizzy.treefeller.core.connector.item.IItemConnector tool(String player);
    com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector playerState(String player);
    int inventoryItemCount(String player, String material);
    void setInventoryItem(String player, int slot, String material, int count);
    void prepareCamera(String player);
    /** Announce the current test in chat. */
    void showTestStatus(String player, String message);
    final class FallingObservation{
        public final double x,y,z,vx,vy,vz;
        public final String material;
        public FallingObservation(double x,double y,double z,double vx,double vy,double vz,String material){
            this.x=x;this.y=y;this.z=z;this.vx=vx;this.vy=vy;this.vz=vz;this.material=material;
        }
    }
    interface TestTarget{
        float health();
        void remove();
    }
    interface ITestWorld{
        java.util.List<FallingObservation> fallingBlocks();
        void removeFallingBlocks();
        TestTarget spawnTarget(double x,double y,double z);

        int maxHeight(); // Exclusive build ceiling; arenas deliberately use positive Y.
        boolean supports(String material);
        void setBlock(int x, int y, int z, String material, BlockAxis axis, int leafDistance);
        String blockState(int x, int y, int z);
        boolean isMaterial(int x, int y, int z, String material);
        int droppedItemCount(String material, int minX, int minY, int minZ, int maxX, int maxY, int maxZ);
        void removeDroppedItems(int minX, int minY, int minZ, int maxX, int maxY, int maxZ);
        void loadChunk(int x, int z);
        void daylight();
        long fullTime();
        void setFullTime(long ticks);
        boolean daylightCycle();
        void setDaylightCycle(boolean enabled);
        String dimension();
        String biome(int x, int z);
        void setBiome(int x, int z, String biome);
    }
}
