package com.thizthizzydizzy.treefeller.platform.bukkit.core.connector;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockAxis;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
/** Bukkit API translation only; fixture construction lives in Core. */
public final class BukkitTestPlatform implements ITestPlatform{
    private Player player(String name){
        Player player = Bukkit.getPlayerExact(name);
        if(player==null)throw new IllegalArgumentException("Test player is offline: "+name);
        return player;
    }
    @Override
    public ITestWorld world(String player){
        return new TestWorld(player(player).getWorld());
    }
    @Override
    public void preparePlayer(String name, boolean survival){
        Player player = player(name);
        player.setGameMode(survival?GameMode.SURVIVAL:GameMode.CREATIVE);
        player.getInventory().clear();
        player.setItemInHand(new ItemStack(Material.IRON_AXE));
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setFallDistance(0);
    }
    @Override
    public void setPlayerLevels(String name, int food, float health, float saturation){
        Player player = player(name);
        player.setFoodLevel(food);
        player.setHealth(health);
        player.setSaturation(saturation);
        player.setExhaustion(0);
    }
    @Override
    public void prepareCamera(String name){
        preparePlayer(name, false);
        player(name).setFlying(true);
    }
    @Override
    public void showTestStatus(String name, String message){
        player(name).sendMessage("\u00a7e[TreeFeller test] \u00a7f"+message);
    }
    private static final class TestWorld implements ITestWorld{
        private final World world;
        TestWorld(World world){this.world = world;}
        @Override public int maxHeight(){return world.getMaxHeight();}
        @Override public boolean supports(String material){return BukkitConnector.blockData.supportsTestMaterial(material);}
        @Override public void setBlock(int x, int y, int z, String material, BlockAxis axis, int leafDistance){
            BukkitConnector.blockData.setTestBlock(world.getBlockAt(x, y, z), material, axis, leafDistance);
        }
        @Override public String blockState(int x, int y, int z){return BukkitConnector.blockData.testBlockState(world.getBlockAt(x, y, z));}
        @Override public boolean isMaterial(int x, int y, int z, String material){
            return BukkitConnector.blockData.isTestMaterial(world.getBlockAt(x, y, z), material);
        }
        @Override public void loadChunk(int x, int z){world.loadChunk(x, z);}
        @Override public void daylight(){world.setTime(6000); world.setStorm(false); world.setThundering(false);}
    }
}
