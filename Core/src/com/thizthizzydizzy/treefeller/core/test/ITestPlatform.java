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
    void setPlayerLevels(String player, int food, float health, float saturation);
    void prepareCamera(String player);
    /** Announce the current test in chat. */
    void showTestStatus(String player, String message);
    interface ITestWorld{
        int maxHeight(); // Exclusive build ceiling; arenas deliberately use positive Y.
        boolean supports(String material);
        void setBlock(int x, int y, int z, String material, BlockAxis axis, int leafDistance);
        String blockState(int x, int y, int z);
        boolean isMaterial(int x, int y, int z, String material);
        void loadChunk(int x, int z);
        void daylight();
    }
}
