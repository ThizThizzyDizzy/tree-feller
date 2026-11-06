package com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.placeholderapi;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.compatibility.BukkitPluginCompatibility;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.TreeFellerBukkit;
import com.thizthizzydizzy.treefeller.core.compatibility.TreeFellerPlaceholders;
public final class PlaceholderAPICompat extends BukkitPluginCompatibility{
    private Expansion expansion;
    public PlaceholderAPICompat(TreeFellerBukkit plugin){super(plugin);}
    @Override public String getPluginName(){return "PlaceholderAPI";}
    @Override public void initialize(){expansion=new Expansion();expansion.register();}
    @Override public String testPlaceholder(com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector player,String parameter){
        return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(((com.thizthizzydizzy.treefeller.platform.bukkit.core.connector.BukkitPlayerConnector)player).getPlayer(),"%treefeller_"+parameter+"%");
    }
    @Override public void close(){if(expansion!=null)expansion.unregister();}
    private final class Expansion extends me.clip.placeholderapi.expansion.PlaceholderExpansion{
        @Override public String getIdentifier(){return "treefeller";}
        @Override public String getAuthor(){return "ThizThizzyDizzy";}
        @Override public String getVersion(){return plugin.getDescription().getVersion();}
        @Override public boolean persist(){return true;}
        @Override public String onRequest(org.bukkit.OfflinePlayer player,String params){
            return TreeFellerPlaceholders.request(player==null?null:plugin.getPlayerConnector(player.getPlayer()),params);
        }
    }
}
