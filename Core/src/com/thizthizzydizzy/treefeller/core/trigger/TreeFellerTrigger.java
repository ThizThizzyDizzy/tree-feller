package com.thizthizzydizzy.treefeller.core.trigger;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.message.TreeFellerMessages;
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
        if(!context.checkTrue("Is Toggled On", playerSettings.isToggledOn())){
            TreeFellerMessages.send(player,"toggle");
            return null;
        }

        if(!context.checkTrue("Global Trigger", checkTrigger(context, player, world, pos, TreeFellerCore.config.global.trigger, "global", secondary)))
            return null;

        context.mark(DebugLevel.USER,pos,OverlayMark.CANDIDATE);
        IItemConnector item = player.getTool();
        context.info(item);

        ArrayList<ToolConfiguration> validTools = new ArrayList();
        for(ToolConfiguration tool : TreeFellerCore.config.tools){
            context.info(tool);
            if(!context.checkTrue("Tool Trigger", checkTrigger(context, player, world, pos, tool.trigger, "tool", secondary)))
                continue;
            if(context.match("Tool matches", TreeFellerMessages.matchesItem(secondary?null:player,item,tool.item)))
                validTools.add(tool);
        }

        context.info(validTools.size()+"/"+TreeFellerCore.config.tools.length+" tools");
        context.info(TreeFellerCore.config.trees.length+" trees");

        // check every tool/tree combination
        for(int treeIndex = 0; treeIndex<TreeFellerCore.config.trees.length; treeIndex++){
            TreeConfiguration tree = TreeFellerCore.config.trees[treeIndex];
            if(allowedTrees!=null&&!allowedTrees.contains(tree))continue;
            context.info(tree);
            if(!context.checkTrue("Tree Trigger", checkTrigger(context, player, world, pos, tree.trigger, "tree", secondary)))
                continue;
            for(ToolConfiguration tool : validTools){
                if(tool.trees!=null&&!TreeFellerMessages.checks(secondary?null:player,"tool").check("trigger.allowed_trees",tool.trees.applySingle(treeIndex),treeIndex,tool.trees))continue;
                if(!secondary)TreeFellerMessages.send(player,"checking",treeIndex,TreeFellerCore.config.tools.length==0?-1:java.util.Arrays.asList(TreeFellerCore.config.tools).indexOf(tool));
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
    private static boolean checkTrigger(DebuggerContext context, IPlayerConnector player, IWorldConnector world, long pos, TriggerConfiguration config, String scope, boolean secondary){
        if(config==null)return true;
        context.values("Trigger settings",config);
        TreeFellerMessages.Checks messages=TreeFellerMessages.checks(secondary?null:player,scope,"trigger");
        // == WORLD CHECKS ==
        if(config.day_time!=null&&!messages.check("day_time", context.check("Time of Day",world.getDayTime(),config.day_time,config.day_time.matches(world.getDayTime())), world.getDayTime(), config.day_time))
            return false;
        if(config.moon_phase!=null&&!messages.check("moon_phase", context.check("Moon Phase",world.getMoonPhase(),config.moon_phase,config.moon_phase.matches(world.getMoonPhase())), world.getMoonPhase(), config.moon_phase))
            return false;
        if(config.dimensions!=null&&!messages.check("dimensions", context.check("Dimensions",world.getDimension(),config.dimensions,config.dimensions.applySingle(world.getDimension())), world.getDimension(), config.dimensions))
            return false;
        if(config.biomes!=null&&!messages.check("biomes", context.check("Biomes",world.getBiome(pos),config.biomes,config.biomes.applySingle(world.getBiome(pos))), world.getBiome(pos), config.biomes))
            return false;

        // == PLAYER CHECKS ==
        if(player!=null){
            if(!messages.check("cooldown", !context.checkFalse("On Cooldown", PlayerSettings.get(player).isOnCooldown(config)), config.cooldown))
                return false;
            if(config.permissions!=null&&!messages.multi("permissions", context.checkTrue("Permissions", config.permissions.applyMulti(player::hasPermission)), config.permissions,player::hasPermission))
                return false;
            if(config.food!=null&&!messages.check("food", context.check("Food",player.getFoodLevel(),config.food,config.food.matches(player.getFoodLevel())), player.getFoodLevel(), config.food))return false;
            if(config.saturation!=null&&!messages.check("saturation", context.check("Saturation",player.getSaturationLevel(),config.saturation,config.saturation.matches(player.getSaturationLevel())), player.getSaturationLevel(), config.saturation))return false;
            if(config.health!=null&&!messages.check("health", context.check("Health",player.getHealth(),config.health,config.health.matches(player.getHealth())), player.getHealth(), config.health))return false;

            PlayerGameMode gameMode = player.getGameMode();
            context.info(gameMode);
            switch(gameMode){
                case ADVENTURE:
                    if(!messages.check("adventure_mode", context.checkTrue("Enabled in adventure mode", !Boolean.FALSE.equals(config.adventure_mode)), !Boolean.FALSE.equals(config.adventure_mode)))
                        return false;
                    break;
                case SURVIVAL:
                    if(!messages.check("survival_mode", context.checkTrue("Enabled in survival mode", !Boolean.FALSE.equals(config.survival_mode)), !Boolean.FALSE.equals(config.survival_mode)))
                        return false;
                    break;
                case CREATIVE:
                    if(!messages.check("creative_mode", context.checkTrue("Enabled in creative mode", !Boolean.FALSE.equals(config.creative_mode)), !Boolean.FALSE.equals(config.creative_mode)))
                        return false;
                    break;
                case SPECTATOR:
                case UNKNOWN:
                    context.info("Gamemode "+gameMode+" not supported.");
                    return false;
            }
            context.info("Is Sneaking: "+player.isSneaking());
            if(player.isSneaking()){
                if(!messages.check("with_sneaking", context.checkTrue("Enabled when sneaking", !Boolean.FALSE.equals(config.with_sneaking)), !Boolean.FALSE.equals(config.with_sneaking)))
                    return false;
            }else{
                if(!messages.check("without_sneaking", context.checkTrue("Enabled without sneaking", !Boolean.FALSE.equals(config.without_sneaking)), !Boolean.FALSE.equals(config.without_sneaking)))
                    return false;
            }
        }
        return true;
    }
}
