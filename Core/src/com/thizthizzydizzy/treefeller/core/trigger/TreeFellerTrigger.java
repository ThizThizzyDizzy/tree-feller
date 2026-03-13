package com.thizthizzydizzy.treefeller.core.trigger;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.config.structure.ToolConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.TriggerConfiguration;
import com.thizthizzydizzy.treefeller.core.connector.item.IItemConnector;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import com.thizthizzydizzy.treefeller.core.connector.player.PlayerGameMode;
import com.thizthizzydizzy.treefeller.core.connector.world.IWorldConnector;
import com.thizthizzydizzy.treefeller.core.criteria.TreeFellerCriteria;
import com.thizthizzydizzy.treefeller.core.debug.DebuggerContext;
import com.thizthizzydizzy.treefeller.core.debug.TreeFellerDebugger;
import com.thizthizzydizzy.treefeller.core.detection.DetectionResult;
import com.thizthizzydizzy.treefeller.core.detection.TreeFellerDetection;
import com.thizthizzydizzy.treefeller.core.detection.TreeTree;
import com.thizthizzydizzy.treefeller.core.player.PlayerSettings;
import java.util.ArrayList;
public class TreeFellerTrigger{
    public static void trigger(IPlayerConnector player, IWorldConnector world, long pos){
        DetectionResult detected = detect(player, world, pos, false);
        if(detected==null)return;
        if(!TreeFellerCriteria.check(world, detected.detected, TreeFellerCore.config.global.criteria))return;
        if(!TreeFellerCriteria.check(world, detected.detected, detected.tree.criteria))return;
        if(!TreeFellerCriteria.check(world, detected.detected, detected.tool.criteria))return;
        //TODO cutting
        //TODO breaking
        //TODO result
    }
    public static DetectionResult detect(IPlayerConnector player, IWorldConnector world, long pos, boolean secondary){
        DebuggerContext context = TreeFellerDebugger.begin("Trigger", player, world, pos);
        PlayerSettings playerSettings = PlayerSettings.get(player);
        if(!context.checkTrue("Is Toggled On", playerSettings.isToggledOn()))
            return null;

        if(!context.checkTrue("Global Trigger", checkTrigger(context, player, world, pos, TreeFellerCore.config.global.trigger)))
            return null;

        IItemConnector item = player.getTool();
        context.info(item);

        ArrayList<ToolConfiguration> validTools = new ArrayList();
        for(ToolConfiguration tool : TreeFellerCore.config.tools){
            context.info(tool);
            if(!context.checkTrue("Tool Trigger", checkTrigger(context, player, world, pos, tool.trigger)))
                continue;
            if(context.checkTrue("Tool matches", item.matches(tool.item)))
                validTools.add(tool);
        }

        context.info(validTools.size()+"/"+TreeFellerCore.config.tools.length+" tools");
        context.info(TreeFellerCore.config.trees.length+" trees");

        // check every tool/tree combination
        for(TreeConfiguration tree : TreeFellerCore.config.trees){
            context.info(tree);
            if(!context.checkTrue("Tree Trigger", checkTrigger(context, player, world, pos, tree.trigger)))
                continue;
            for(ToolConfiguration tool : validTools){
                TreeTree detected = TreeFellerDetection.detect(player, world, tree, tool, pos, item, secondary);
                if(detected==null)continue;
                return new DetectionResult(detected, tree, tool);
            }
        }
        return null;
    }
    private static boolean checkTrigger(DebuggerContext context, IPlayerConnector player, IWorldConnector world, long pos, TriggerConfiguration config){
        if(config==null)return true;
        // == WORLD CHECKS ==
        if(config.day_time!=null&&!context.checkTrue("Time of Day", config.day_time.matches(world.getDayTime())))
            return false;
        if(config.moon_phase!=null&&!context.checkTrue("Moon Phase", config.moon_phase.matches(world.getMoonPhase())))
            return false;
        if(config.dimensions!=null&&!context.checkTrue("Dimensions", config.dimensions.applySingle(world.getDimension())))
            return false;
        if(config.biomes!=null&&!context.checkTrue("Biomes", config.biomes.applySingle(world.getBiome(pos))))
            return false;

        // == PLAYER CHECKS ==
        if(player!=null){
            if(context.checkFalse("On Cooldown", PlayerSettings.get(player).isOnCooldown(config)))
                return false;
            if(config.permissions!=null&&!context.checkTrue("Permissions", config.permissions.applyMulti(player::hasPermission)))
                return false;
            if(config.food!=null&&!context.checkTrue("Food", config.food.matches(player.getFoodLevel())));
            if(config.saturation!=null&&!context.checkTrue("Saturation", config.saturation.matches(player.getSaturationLevel())));
            if(config.health!=null&&!context.checkTrue("Health", config.health.matches(player.getHealth())));

            PlayerGameMode gameMode = player.getGameMode();
            context.info(gameMode);
            switch(gameMode){
                case ADVENTURE:
                    if(!context.checkTrue("Enabled in adventure mode", config.adventure_mode))
                        return false;
                    break;
                case SURVIVAL:
                    if(!context.checkTrue("Enabled in survival mode", config.survival_mode))
                        return false;
                    break;
                case CREATIVE:
                    if(!context.checkTrue("Enabled in creative mode", config.creative_mode))
                        return false;
                    break;
                case SPECTATOR:
                case UNKNOWN:
                    context.info("Gamemode "+gameMode+" not supported.");
                    return false;
            }
            context.info("Is Sneaking: "+player.isSneaking());
            if(player.isSneaking()){
                if(!context.checkTrue("Enabled when sneaking", config.with_sneaking))
                    return false;
            }else{
                if(!context.checkTrue("Enabled without sneaking", config.without_sneaking))
                    return false;
            }
        }
        return true;
    }
}
