package com.thizthizzydizzy.treefeller.core.control;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
/** An invocation source: a player, server console, or another platform UI. */
public interface ControlAudience{
    boolean hasPermission(String permission);
    void sendMessage(String message);
    /** Null for sources without a player. */
    IPlayerConnector player();
    void show(Screen screen);
    void requestText(String title, String initialValue, java.util.function.Consumer<String> submit, Runnable cancel);
}
