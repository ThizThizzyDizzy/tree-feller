package com.thizthizzydizzy.vanillify.version;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
public interface VersionWrapper{
    void actionbar(Player player, String message);
    void modifyEntityNBT(Entity entity, String tag, Object value);
    float getEntityNBTFloat(Entity entity, String tag);
    int getEntityNBTInt(Entity entity, String tag);
}