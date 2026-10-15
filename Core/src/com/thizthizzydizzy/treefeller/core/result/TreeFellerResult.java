package com.thizthizzydizzy.treefeller.core.result;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.breaking.BreakingResult;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeFellerConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.ResultConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IItemDefinition;
import com.thizthizzydizzy.treefeller.core.connector.item.IItemConnector;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import com.thizthizzydizzy.treefeller.core.connector.player.PlayerGameMode;
import com.thizthizzydizzy.treefeller.core.connector.world.IWorldConnector;
import com.thizthizzydizzy.treefeller.core.debug.*;
import com.thizthizzydizzy.treefeller.core.detection.DetectionResult;
import com.thizthizzydizzy.treefeller.core.detection.TreeNode;
import com.thizthizzydizzy.treefeller.core.detection.TreeNodeType;
import com.thizthizzydizzy.treefeller.core.player.PlayerSettings;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
public class TreeFellerResult{
    /** Shared immediate/impact delivery; overflow remains at the delivery location. */
    public static void deliverDrops(IWorldConnector world, IPlayerConnector player, long pos, List<IItemConnector> drops, boolean inventory){
        DebuggerContext context=TreeFellerDebugger.begin(DebugPhase.RESULT,player,world);
        for(IItemConnector item : drops){
            if(item.getCount()<=0)continue;
            context.decision(DebugLevel.DETAIL,pos,OverlayMark.DROP,"Deliver "+item.getItemId()+" x"+item.getCount()+", inventory="+inventory);
            if(inventory)for(IItemConnector overflow : player.giveItems(item)){
                context.info("Inventory overflow: "+overflow.getItemId()+" x"+overflow.getCount());world.dropItem(pos,overflow);
            }else world.dropItem(pos,item);
        }
    }
    public static List<IItemConnector> collectDrops(IWorldConnector world, IPlayerConnector player, TreeNode node, IItemConnector tool, ResultConfiguration config){
        DebuggerContext context=TreeFellerDebugger.begin(DebugPhase.RESULT,player,world);
        List<IItemConnector> drops = new ArrayList<>();
        if(player.getGameMode()==PlayerGameMode.CREATIVE){context.trace("No drops in creative at "+DebuggerContext.position(node.pos));return drops;}
        boolean trunk = node.type==TreeNodeType.TRUNK;
        boolean leaves = node.type==TreeNodeType.LEAVES;
        boolean fortune = Boolean.TRUE.equals(trunk?config.trunk_fortune:leaves?config.leaves_fortune:config.decoration_fortune);
        boolean silk = Boolean.TRUE.equals(trunk?config.trunk_silk_touch:leaves?config.leaves_silk_touch:config.decoration_silk_touch);
        Float chance = trunk?config.trunk_drop_chance:leaves?config.leaves_drop_chance:config.decorations_drop_chance;
        double multiplier = chance==null?1:Math.max(0, chance);
        int rolls = (int)Math.floor(multiplier);
        double roll=ThreadLocalRandom.current().nextDouble();
        if(roll<multiplier-rolls)rolls++;
        context.decision(DebugLevel.DETAIL,node.pos,OverlayMark.CANDIDATE,"Drop rolls="+rolls+", multiplier="+multiplier+", fortune="+fortune+", silk touch="+silk);
        context.trace("Fractional drop roll="+roll+", threshold="+(multiplier-Math.floor(multiplier)));
        IItemConnector dropTool = tool.copyForDrops(fortune, silk);
        for(int i = 0; i<rolls; i++){
            for(IItemConnector item : world.getDrops(node.pos, dropTool, player)){
                if(config.drop_conversions!=null){
                    for(Map.Entry<IItemDefinition, IItemDefinition> entry : config.drop_conversions.entrySet()){
                        if(item.matches(entry.getKey())){
                            context.info("Drop conversion "+item.getItemId()+" -> "+DebuggerContext.describe(entry.getValue()));
                            item = item.convert(entry.getValue());
                            break;
                        }
                    }
                }
                if(item.getCount()>0){drops.add(item);context.decision(DebugLevel.DETAIL,node.pos,OverlayMark.DROP,"Collected "+item.getItemId()+" x"+item.getCount());}
            }
        }
        return drops;
    }
    public static void complete(IWorldConnector world, IPlayerConnector player, DetectionResult detected, BreakingResult result){
        complete(world, player, detected, result, TreeFellerConfiguration.getCombinedResultConfiguration(detected.tree, detected.tool),
                TreeFellerCore.config.global.trigger);
    }
    /** The caller can retain the initiating settings while an animation is in progress. */
    public static void complete(IWorldConnector world, IPlayerConnector player, DetectionResult detected, BreakingResult result,
            ResultConfiguration config, com.thizthizzydizzy.treefeller.core.config.structure.section.TriggerConfiguration globalTrigger){
        complete(world, player, detected, result, config, globalTrigger, true);
    }
    /** Each batch delivers its own drops and block costs; per-fell effects belong to the first batch. */
    public static void complete(IWorldConnector world, IPlayerConnector player, DetectionResult detected, BreakingResult result,
            ResultConfiguration config, com.thizthizzydizzy.treefeller.core.config.structure.section.TriggerConfiguration globalTrigger, boolean first){
        DebuggerContext context = TreeFellerDebugger.begin("Result", player, world);
        context.values("Result settings",config);
        int items = 0;
        for(Map.Entry<Long, List<IItemConnector>> entry : result.drops.entrySet()){
            for(IItemConnector item : entry.getValue()){
                if(item.getCount()<=0)continue;
                items += item.getCount();
                context.decision(DebugLevel.DETAIL,entry.getKey(),OverlayMark.DROP,"Drop: "+item.getItemId()+" x"+item.getCount()+", inventory="+config.drop_to_inventory);
                if(Boolean.TRUE.equals(config.drop_to_inventory)){
                    for(IItemConnector overflow : player.giveItems(item)){context.info("Inventory overflow: "+overflow.getItemId()+" x"+overflow.getCount());world.dropItem(entry.getKey(), overflow);}
                }else world.dropItem(entry.getKey(), item);
            }
        }
        if(player.getGameMode()!=PlayerGameMode.CREATIVE){
            int trunks = 0, leaves = 0, decorations = 0;
            for(TreeNode node : result.nodes){
                if(node.type==TreeNodeType.TRUNK)trunks++;
                else if(node.type==TreeNodeType.LEAVES)leaves++;
                else if(node.type==TreeNodeType.DECORATION)decorations++;
            }
            float food = (first?value(config.consumed_food_base):0)+trunks*value(config.consumed_food_trunk)
                    +leaves*value(config.consumed_food_leaves)+decorations*value(config.consumed_food_decoration);
            float health = (first?value(config.consumed_health_base):0)+trunks*value(config.consumed_health_trunk)
                    +leaves*value(config.consumed_health_leaves)+decorations*value(config.consumed_health_decoration);
            consumeFood(player, food);
            if(health>0)player.damage(health);
            else if(health<0)player.setHealth(Math.min(player.getMaxHealth(), player.getHealth()-health));
            context.info("Food cost: "+food+", health cost: "+health);
        }
        if(first){
            PlayerSettings settings = PlayerSettings.get(player);
            settings.startCooldown(globalTrigger);
            settings.startCooldown(detected.tree.trigger);
            settings.startCooldown(detected.tool.trigger);
        }
        context.user("Delivered "+items+" immediate items"+(first?"; started configured cooldowns":""));
        context.mark(DebugLevel.USER,detected.detected.root.pos,OverlayMark.DROP);
        if(first){context.values("Global cooldown",globalTrigger.cooldown);context.values("Tree cooldown",detected.tree.trigger==null?null:detected.tree.trigger.cooldown);context.values("Tool cooldown",detected.tool.trigger==null?null:detected.tool.trigger.cooldown);}
    }
    private static float value(Float value){
        return value==null?0:value;
    }
    private static void consumeFood(IPlayerConnector player, float cost){
        if(cost==0)return;
        float food = player.getFoodLevel();
        float saturation = player.getSaturationLevel();
        if(cost>0){
            float used = Math.min(saturation, cost);
            saturation -= used;
            float remaining = cost-used;
            float whole = (float)Math.floor(remaining);
            food = Math.max(0, food-whole);
            // Minecraft stores fractional hunger consumption as exhaustion:
            // four exhaustion points consume one saturation or food point.
            float fraction = remaining-whole;
            if(fraction>0)player.setExhaustion(player.getExhaustion()+fraction*4);
        }else{
            float added = Math.min(20-food, (float)Math.floor(-cost));
            food += added;
            saturation = Math.min(food, saturation-cost-added);
        }
        player.setFoodLevel((int)food);
        player.setSaturationLevel(saturation);
    }
}
