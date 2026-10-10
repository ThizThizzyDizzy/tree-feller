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
import com.thizthizzydizzy.treefeller.core.debug.DebuggerContext;
import com.thizthizzydizzy.treefeller.core.debug.TreeFellerDebugger;
import com.thizthizzydizzy.treefeller.core.detection.DetectionResult;
import com.thizthizzydizzy.treefeller.core.detection.TreeNode;
import com.thizthizzydizzy.treefeller.core.detection.TreeNodeType;
import com.thizthizzydizzy.treefeller.core.player.PlayerSettings;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
public class TreeFellerResult{
    public static List<IItemConnector> collectDrops(IWorldConnector world, IPlayerConnector player, TreeNode node, IItemConnector tool, ResultConfiguration config){
        List<IItemConnector> drops = new ArrayList<>();
        if(player.getGameMode()==PlayerGameMode.CREATIVE)return drops;
        boolean trunk = node.type==TreeNodeType.TRUNK;
        boolean leaves = node.type==TreeNodeType.LEAVES;
        boolean fortune = Boolean.TRUE.equals(trunk?config.trunk_fortune:leaves?config.leaves_fortune:config.decoration_fortune);
        boolean silk = Boolean.TRUE.equals(trunk?config.trunk_silk_touch:leaves?config.leaves_silk_touch:config.decoration_silk_touch);
        Float chance = trunk?config.trunk_drop_chance:leaves?config.leaves_drop_chance:config.decorations_drop_chance;
        double multiplier = chance==null?1:Math.max(0, chance);
        int rolls = (int)Math.floor(multiplier);
        if(ThreadLocalRandom.current().nextDouble()<multiplier-rolls)rolls++;
        IItemConnector dropTool = tool.copyForDrops(fortune, silk);
        for(int i = 0; i<rolls; i++){
            for(IItemConnector item : world.getDrops(node.pos, dropTool, player)){
                if(config.drop_conversions!=null){
                    for(Map.Entry<IItemDefinition, IItemDefinition> entry : config.drop_conversions.entrySet()){
                        if(item.matches(entry.getKey())){
                            item = item.convert(entry.getValue());
                            break;
                        }
                    }
                }
                if(item.getCount()>0)drops.add(item);
            }
        }
        return drops;
    }
    public static void complete(IWorldConnector world, IPlayerConnector player, DetectionResult detected, BreakingResult result){
        DebuggerContext context = TreeFellerDebugger.begin("Result", player, world);
        ResultConfiguration config = TreeFellerConfiguration.getCombinedResultConfiguration(detected.tree, detected.tool);
        int items = 0;
        for(Map.Entry<Long, List<IItemConnector>> entry : result.drops.entrySet()){
            for(IItemConnector item : entry.getValue()){
                items += item.getCount();
                context.info("Drop: "+item.getItemId()+" x"+item.getCount(), entry.getKey());
                if(Boolean.TRUE.equals(config.drop_to_inventory)){
                    for(IItemConnector overflow : player.giveItems(item))world.dropItem(entry.getKey(), overflow);
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
            float food = value(config.consumed_food_base)+trunks*value(config.consumed_food_trunk)
                    +leaves*value(config.consumed_food_leaves)+decorations*value(config.consumed_food_decoration);
            float health = value(config.consumed_health_base)+trunks*value(config.consumed_health_trunk)
                    +leaves*value(config.consumed_health_leaves)+decorations*value(config.consumed_health_decoration);
            consumeFood(player, food);
            if(health>0)player.damage(health);
            else if(health<0)player.setHealth(Math.min(player.getMaxHealth(), player.getHealth()-health));
            context.info("Food cost: "+food+", health cost: "+health);
        }
        PlayerSettings settings = PlayerSettings.get(player);
        settings.startCooldown(TreeFellerCore.config.global.trigger);
        settings.startCooldown(detected.tree.trigger);
        settings.startCooldown(detected.tool.trigger);
        context.pass("Produced "+items+" items; started configured cooldowns");
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
