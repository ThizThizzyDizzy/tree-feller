package com.thizthizzydizzy.treefeller.core.connector.player;
import com.thizthizzydizzy.treefeller.core.connector.item.IItemConnector;
public interface IPlayerConnector{
    public boolean hasPermission(String permission);
    public float getFoodLevel();
    public float getSaturationLevel();
    public float getHealth();
    public PlayerGameMode getGameMode();
    public boolean isSneaking();
    public IItemConnector getTool();
}
