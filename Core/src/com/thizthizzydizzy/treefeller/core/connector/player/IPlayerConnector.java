package com.thizthizzydizzy.treefeller.core.connector.player;
import com.thizthizzydizzy.treefeller.core.connector.item.IItemConnector;
import java.util.Collection;
public interface IPlayerConnector{
    com.thizthizzydizzy.treefeller.core.breaking.FallVector position();

    int inventorySize();
    IItemConnector inventoryItem(int slot);
    void setInventoryItem(int slot, IItemConnector item);

    public void setTool(IItemConnector item);
    /** Returns items that did not fit in the inventory. */
    public Collection<IItemConnector> giveItems(IItemConnector item);
    public void setFoodLevel(int food);
    public void setSaturationLevel(float saturation);
    public float getExhaustion();
    public void setExhaustion(float exhaustion);
    public void setHealth(float health);
    public float getMaxHealth();
    public void damage(float damage);
    public boolean hasPermission(String permission);
    public float getFoodLevel();
    public float getSaturationLevel();
    public float getHealth();
    public PlayerGameMode getGameMode();
    public boolean isSneaking();
    public IItemConnector getTool();
    public void sendMessage(String message);
    public void sendActionbar(String message);
    public String getPlayerName();
    /** Draw a semantic client-only world marker; material is an optional platform hint. */
    public void debugOverlay(com.thizthizzydizzy.treefeller.core.connector.world.IWorldConnector world, long pos,
            com.thizthizzydizzy.treefeller.core.debug.OverlayMark mark, String material);
    /** Restore the current real world state at a displayed position. */
    public void debugRestore(com.thizthizzydizzy.treefeller.core.connector.world.IWorldConnector world, long pos);
}
