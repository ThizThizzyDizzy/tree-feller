package com.thizthizzydizzy.vanillify;
import com.thizthizzydizzy.vanillify.version.VersionMatcher;
import com.thizthizzydizzy.vanillify.version.VersionWrapper;
import org.bukkit.entity.Player;
public class Vanillify{
    private static VersionWrapper WRAPPER = new VersionMatcher().match();
    public static void actionbar(Player player, String text){
        WRAPPER.actionbar(player, text);
    }
}