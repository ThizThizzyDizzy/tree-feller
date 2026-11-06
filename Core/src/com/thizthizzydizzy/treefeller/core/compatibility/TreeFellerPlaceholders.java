package com.thizthizzydizzy.treefeller.core.compatibility;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import com.thizthizzydizzy.treefeller.core.player.PlayerSettings;
import com.thizthizzydizzy.treefeller.core.config.structure.section.TriggerConfiguration;
import java.util.*;
/** Placeholder parsing and cooldown calculations are independent of PlaceholderAPI. */
public final class TreeFellerPlaceholders{
    private TreeFellerPlaceholders(){}
    public static String request(IPlayerConnector player,String params){
        if(params==null)return null;
        if(params.equals("debug"))return TreeFellerCore.config.debug.chat?"ON":"OFF";
        if(params.equals("toggled"))return player==null?null:PlayerSettings.get(player).isToggledOn()?"ON":"OFF";
        if(!params.startsWith("cooldown"))return null;
        List<TriggerConfiguration> configs=new ArrayList<>();configs.add(TreeFellerCore.config.global.trigger);
        for(com.thizthizzydizzy.treefeller.core.config.structure.TreeConfiguration tree:TreeFellerCore.config.trees)configs.add(tree.trigger);
        for(com.thizthizzydizzy.treefeller.core.config.structure.ToolConfiguration tool:TreeFellerCore.config.tools)configs.add(tool.trigger);
        long[] times=configs.stream().mapToLong(config -> player==null?0:PlayerSettings.get(player).remainingCooldown(config)).toArray();
        if(params.equals("cooldown_count"))return Long.toString(Arrays.stream(times).filter(t -> t>0).count());
        int end=params.lastIndexOf('_');if(end<0)return null;
        String key=params.substring(0,end),unit=params.substring(end+1);
        long remaining;
        if(key.equals("cooldown_global"))remaining=times[0];
        else if(key.equals("cooldown_longest"))remaining=Arrays.stream(times).max().orElse(0);
        else if(key.equals("cooldown_shortest"))remaining=Arrays.stream(times).min().orElse(0);
        else try{
            boolean tree=key.startsWith("cooldown_tree_");
            if(!tree&&!key.startsWith("cooldown_tool_"))return null;
            int index=Integer.parseInt(key.substring(key.lastIndexOf('_')+1));
            int length=tree?TreeFellerCore.config.trees.length:TreeFellerCore.config.tools.length;
            if(index<0||index>=length)return null;
            remaining=times[1+(tree?index:TreeFellerCore.config.trees.length+index)];
        }catch(NumberFormatException ex){return null;}
        return unit.equals("ms")?Long.toString(remaining):unit.equals("t")?Long.toString(remaining/50):unit.equals("s")?Long.toString(remaining/1000):null;
    }
}
