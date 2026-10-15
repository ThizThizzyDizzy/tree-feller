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
import com.thizthizzydizzy.treefeller.core.cutting.TreeFellerCutting;
import com.thizthizzydizzy.treefeller.core.debug.*;
import com.thizthizzydizzy.treefeller.core.detection.DetectionResult;
import com.thizthizzydizzy.treefeller.core.detection.TreeFellerDetection;
import com.thizthizzydizzy.treefeller.core.detection.TreeTree;
import com.thizthizzydizzy.treefeller.core.player.PlayerSettings;
import java.util.ArrayList;
public class TreeFellerTrigger{
    public static void trigger(IPlayerConnector player, IWorldConnector world, long pos){
        DetectionResult detected = detect(player, world, pos, false);
        if(detected==null)return;
        TreeFellerCutting.cut(world, player, detected);
    }
    public static DetectionResult detect(IPlayerConnector player, IWorldConnector world, long pos, boolean secondary){
        return detect(player, world, pos, secondary, null, false);
    }
    /** Cascade selection is explicit; secondary verification still examines all trees. */
    public static DetectionResult detect(IPlayerConnector player, IWorldConnector world, long pos, boolean secondary,
            java.util.Collection<TreeConfiguration> allowedTrees, boolean cascading){
        DebuggerContext context = TreeFellerDebugger.begin("Trigger", player, world, pos);
        PlayerSettings playerSettings = PlayerSettings.get(player);
        if(!context.checkTrue("Is Toggled On", playerSettings.isToggledOn()))
            return null;

        if(!context.checkTrue("Global Trigger", checkTrigger(context, player, world, pos, TreeFellerCore.config.global.trigger)))
            return null;

        context.mark(DebugLevel.USER,pos,OverlayMark.CANDIDATE);
        IItemConnector item = player.getTool();
        context.info(item);

        ArrayList<ToolConfiguration> validTools = new ArrayList();
        for(ToolConfiguration tool : TreeFellerCore.config.tools){
            context.info(tool);
            if(!context.checkTrue("Tool Trigger", checkTrigger(context, player, world, pos, tool.trigger)))
                continue;
            if(context.match("Tool matches", item.matches(tool.item)))
                validTools.add(tool);
        }

        context.info(validTools.size()+"/"+TreeFellerCore.config.tools.length+" tools");
        context.info(TreeFellerCore.config.trees.length+" trees");

        // check every tool/tree combination
        for(int treeIndex = 0; treeIndex<TreeFellerCore.config.trees.length; treeIndex++){
            TreeConfiguration tree = TreeFellerCore.config.trees[treeIndex];
            if(allowedTrees!=null&&!allowedTrees.contains(tree))continue;
            context.info(tree);
            if(!context.checkTrue("Tree Trigger", checkTrigger(context, player, world, pos, tree.trigger)))
                continue;
            for(ToolConfiguration tool : validTools){
                if(tool.trees!=null&&!tool.trees.applySingle(treeIndex))continue;
                TreeTree detected = TreeFellerDetection.detect(player, world, tree, tool, pos, item, secondary);
                if(detected==null)continue;
                DetectionResult result = new DetectionResult(detected, tree, tool);
                if(!secondary&&!TreeFellerCriteria.check(world, result, cascading,player))continue;
                context.user("Tree and tool matched");
                context.mark(DebugLevel.USER,pos,OverlayMark.ACCEPTED);
                return result;
            }
        }
        context.user(validTools.isEmpty()?"This item does not match any configured tool and its trigger requirements":"No eligible tree matched this block and tool");
        context.mark(DebugLevel.USER,pos,OverlayMark.REJECTED);
        return null;
    }
    private static boolean checkTrigger(DebuggerContext context, IPlayerConnector player, IWorldConnector world, long pos, TriggerConfiguration config){
        if(config==null)return true;
        context.values("Trigger settings",config);
        // == WORLD CHECKS ==
        if(config.day_time!=null&&!context.check("Time of Day",world.getDayTime(),config.day_time,config.day_time.matches(world.getDayTime())))
            return false;
        if(config.moon_phase!=null&&!context.check("Moon Phase",world.getMoonPhase(),config.moon_phase,config.moon_phase.matches(world.getMoonPhase())))
            return false;
        if(config.dimensions!=null&&!context.check("Dimensions",world.getDimension(),config.dimensions,config.dimensions.applySingle(world.getDimension())))
            return false;
        if(config.biomes!=null&&!context.check("Biomes",world.getBiome(pos),config.biomes,config.biomes.applySingle(world.getBiome(pos))))
            return false;

        // == PLAYER CHECKS ==
        if(player!=null){
            if(context.checkFalse("On Cooldown", PlayerSettings.get(player).isOnCooldown(config)))
                return false;
            if(config.permissions!=null&&!context.checkTrue("Permissions", config.permissions.applyMulti(player::hasPermission)))
                return false;
            if(config.food!=null&&!context.check("Food",player.getFoodLevel(),config.food,config.food.matches(player.getFoodLevel())))return false;
            if(config.saturation!=null&&!context.check("Saturation",player.getSaturationLevel(),config.saturation,config.saturation.matches(player.getSaturationLevel())))return false;
            if(config.health!=null&&!context.check("Health",player.getHealth(),config.health,config.health.matches(player.getHealth())))return false;

            PlayerGameMode gameMode = player.getGameMode();
            context.info(gameMode);
            switch(gameMode){
                case ADVENTURE:
                    if(!context.checkTrue("Enabled in adventure mode", !Boolean.FALSE.equals(config.adventure_mode)))
                        return false;
                    break;
                case SURVIVAL:
                    if(!context.checkTrue("Enabled in survival mode", !Boolean.FALSE.equals(config.survival_mode)))
                        return false;
                    break;
                case CREATIVE:
                    if(!context.checkTrue("Enabled in creative mode", !Boolean.FALSE.equals(config.creative_mode)))
                        return false;
                    break;
                case SPECTATOR:
                case UNKNOWN:
                    context.info("Gamemode "+gameMode+" not supported.");
                    return false;
            }
            context.info("Is Sneaking: "+player.isSneaking());
            if(player.isSneaking()){
                if(!context.checkTrue("Enabled when sneaking", !Boolean.FALSE.equals(config.with_sneaking)))
                    return false;
            }else{
                if(!context.checkTrue("Enabled without sneaking", !Boolean.FALSE.equals(config.without_sneaking)))
                    return false;
            }
        }
        return true;
    }
}
