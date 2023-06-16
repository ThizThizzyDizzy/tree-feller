package com.thizthizzydizzy.treefeller.compat;
import com.thizthizzydizzy.treefeller.Cooldown;
import com.thizthizzydizzy.treefeller.TreeFeller;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
public class PlaceholderAPICompat extends InternalCompatibility{
    private boolean initialized = false;
    @Override
    public String getPluginName(){
        return "PlaceholderAPI";
    }
    @Override
    public void init(TreeFeller treeFeller){
        if(initialized)return;
        new TreeFellerExpansion(treeFeller).register();
        initialized = true;
    }
    public class TreeFellerExpansion extends me.clip.placeholderapi.expansion.PlaceholderExpansion{
        private final TreeFeller treefeller;
        private TreeFellerExpansion(TreeFeller treeFeller){
            this.treefeller = treeFeller;
        }
        @Override
        public String getIdentifier(){
            return "treefeller";
        }
        @Override
        public String getAuthor(){
            return "ThizThizzyDizzy";
        }
        @Override
        public String getVersion(){
            return "1.0.0";
        }
        @Override
        public boolean persist(){
            return true;
        }
        @Override
        public String onRequest(OfflinePlayer oplayer, String params){
            Player player = null;
            for(Player p : Bukkit.getOnlinePlayers()){
                if(p.getUniqueId().equals(oplayer.getUniqueId())){
                    player = p;
                }
            }
            if(params.equals("debug"))return treefeller.debug?"ON":"OFF";
            if(player!=null){
                if(params.equals("toggled"))return treefeller.isToggledOn(player)?"ON":"OFF";
            }
            if(oplayer!=null){
                if(params.startsWith("cooldown")){
                    Cooldown cooldown = TreeFeller.cooldowns.get(oplayer.getUniqueId());
                    if(params.startsWith("cooldown_global")){
                        long ms = cooldown.getGlobal();
                        if(params.equals("cooldown_global_ms"))return ms+"";
                        if(params.equals("cooldown_global_t"))return ms/50+"";
                        if(params.equals("cooldown_global_s"))return ms/1000+"";
                    }
                    if(params.startsWith("cooldown_tree_")){
                        int tree = Integer.parseInt(params.split("_")[2]);
                        long ms = cooldown.get(TreeFeller.trees.get(tree));
                        if(params.equals("cooldown_tree_"+tree+"_ms"))return ms+"";
                        if(params.equals("cooldown_tree_"+tree+"_t"))return ms/50+"";
                        if(params.equals("cooldown_tree_"+tree+"_s"))return ms/1000+"";
                    }
                    if(params.startsWith("cooldown_tool_")){
                        int tool = Integer.parseInt(params.split("_")[2]);
                        long ms = cooldown.get(TreeFeller.trees.get(tool));
                        if(params.equals("cooldown_tool_"+tool+"_ms"))return ms+"";
                        if(params.equals("cooldown_tool_"+tool+"_t"))return ms/50+"";
                        if(params.equals("cooldown_tool_"+tool+"_s"))return ms/1000+"";
                    }
                    if(params.startsWith("cooldown_longest")){
                        long ms = 0;
                        for(long l : cooldown.getCooldowns())if(l>ms)ms = l;
                        if(params.equals("cooldown_longest_ms"))return ms+"";
                        if(params.equals("cooldown_longest_t"))return ms/50+"";
                        if(params.equals("cooldown_longest_s"))return ms/1000+"";
                    }
                    if(params.startsWith("cooldown_shortest")){
                        long ms = -1;
                        for(long l : cooldown.getCooldowns())if(ms==-1||l<ms)ms = l;
                        if(params.equals("cooldown_shortest_ms"))return ms+"";
                        if(params.equals("cooldown_shortest_t"))return ms/50+"";
                        if(params.equals("cooldown_shortest_s"))return ms/1000+"";
                    }
                    if(params.equals("cooldown_count")){
                        int count = 0;
                        for(long l : cooldown.getCooldowns())if(l>0)count++;
                        if(params.equals("cooldown_count"))return count+"";
                    }
                }
            }
            return null;
        }
    }
}