package com.thizthizzydizzy.vanillify;
import com.thizthizzydizzy.vanillify.version.VersionMatcher;
import com.thizthizzydizzy.vanillify.version.VersionWrapper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
public class Vanillify{
    private static VersionWrapper WRAPPER = new VersionMatcher().match();
    public static void actionbar(Player player, String text){
        WRAPPER.actionbar(player, text);
    }
    public static void modifyEntityNBT(Entity entity, String tag, Object value){
        WRAPPER.modifyEntityNBT(entity, tag, value);
    }
    public static Object getEntityNBTFloat(Entity entity, String tag){
        return WRAPPER.getEntityNBTFloat(entity, tag);
    }
    public static Object getEntityNBTInt(Entity entity, String tag){
        return WRAPPER.getEntityNBTInt(entity, tag);
    }
}