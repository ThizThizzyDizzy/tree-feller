package com.thizthizzydizzy.treefeller;
import net.coreprotect.CoreProtect;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
public class CoreProtectCompat{
    public static void place(Player player, Block block){
        if(Bukkit.getServer().getPluginManager().getPlugin("CoreProtect")!=null){
            try{
            CoreProtect.getInstance().getAPI().logPlacement(player==null?null:player.getName(), block.getLocation(), block.getType(), block.getBlockData());
            }catch(Exception ex){}
        }
    }
    public static void remove(Player player, Block block){
        if(Bukkit.getServer().getPluginManager().getPlugin("CoreProtect")!=null){
            try{
            CoreProtect.getInstance().getAPI().logRemoval(player==null?null:player.getName(), block.getLocation(), block.getType(), block.getBlockData());
            }catch(Exception ex){}
        }
    }
}