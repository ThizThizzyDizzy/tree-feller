package com.thizthizzydizzy.treefeller.core.trigger;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.config.structure.ToolConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.TriggerConfiguration;
import com.thizthizzydizzy.treefeller.core.connector.item.IItemConnector;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import com.thizthizzydizzy.treefeller.core.connector.world.IWorldConnector;
import com.thizthizzydizzy.treefeller.core.detection.TreeFellerDetection;
import com.thizthizzydizzy.treefeller.core.player.PlayerSettings;
import java.util.ArrayList;
public class TreeFellerTrigger{
    public static void trigger(IPlayerConnector player, IWorldConnector world, long pos){
        PlayerSettings playerSettings = PlayerSettings.get(player);
        if(!playerSettings.isToggledOn())return;

        if(!checkTrigger(player, world, pos, TreeFellerCore.config.global.trigger))
            return;

        IItemConnector item = player.getTool();

        ArrayList<ToolConfiguration> validTools = new ArrayList();
        for(ToolConfiguration tool : TreeFellerCore.config.tools){
            if(!checkTrigger(player, world, pos, tool.trigger))continue;
            if(item.matches(tool.item))validTools.add(tool);
        }

        // check every tool/tree combination
        for(TreeConfiguration tree : TreeFellerCore.config.trees){
            if(!checkTrigger(player, world, pos, tree.trigger))continue;
            for(ToolConfiguration tool : validTools){
                if(TreeFellerDetection.detect(player, world, tree, tool, pos, item)){
                    return;
                }
            }
        }
    }
    private static boolean checkTrigger(IPlayerConnector player, IWorldConnector world, long pos, TriggerConfiguration config){
        // == WORLD CHECKS ==
        if(config.day_time!=null&&!config.day_time.matches(world.getDayTime()))
            return false;
        if(config.moon_phase!=null&&!config.moon_phase.matches(world.getMoonPhase()))
            return false;
        if(config.dimensions!=null&&!config.dimensions.applySingle(world.getDimension()))
            return false;
        if(config.biomes!=null&&!config.biomes.applySingle(world.getBiome(pos)))
            return false;

        // == PLAYER CHECKS ==
        if(player!=null){
            if(PlayerSettings.get(player).isOnCooldown(config))return false;
            if(config.permissions!=null&&!config.permissions.applyMulti(player::hasPermission))
                return false;
            if(config.food!=null&&!config.food.matches(player.getFoodLevel()));
            if(config.saturation!=null&&!config.saturation.matches(player.getSaturationLevel()));
            if(config.health!=null&&!config.health.matches(player.getHealth()));
            switch(player.getGameMode()){
                case ADVENTURE:
                    if(!config.adventure_mode)return false;
                case SURVIVAL:
                    if(!config.survival_mode)return false;
                case CREATIVE:
                    if(!config.creative_mode)return false;
                case SPECTATOR:
                case UNKNOWN:
                    return false;
            }
            if(player.isSneaking()){
                if(!config.with_sneaking)return false;
            }else{
                if(!config.without_sneaking)return false;
            }
        }
        return true;
    }
}
