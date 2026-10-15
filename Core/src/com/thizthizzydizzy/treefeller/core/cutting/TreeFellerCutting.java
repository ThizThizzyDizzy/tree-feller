package com.thizthizzydizzy.treefeller.core.cutting;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.config.structure.section.ResultConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.TriggerConfiguration;
import com.thizthizzydizzy.treefeller.core.breaking.BreakingResult;
import com.thizthizzydizzy.treefeller.core.breaking.TreeFellerBreaking;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeFellerConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.CuttingConfiguration;
import com.thizthizzydizzy.treefeller.core.connector.item.IItemConnector;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import com.thizthizzydizzy.treefeller.core.connector.player.PlayerGameMode;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
import com.thizthizzydizzy.treefeller.core.connector.world.IWorldConnector;
import com.thizthizzydizzy.treefeller.core.debug.*;
import com.thizthizzydizzy.treefeller.core.detection.DetectionResult;
import com.thizthizzydizzy.treefeller.core.detection.TreeNode;
import com.thizthizzydizzy.treefeller.core.detection.TreeNodeType;
import com.thizthizzydizzy.treefeller.core.result.TreeFellerResult;
import com.thizthizzydizzy.treefeller.core.result.TreeFellerCascade;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
public class TreeFellerCutting{
    public static boolean cut(IWorldConnector world, IPlayerConnector player, DetectionResult detected){
        return cut(world, player, detected, null);
    }
    public static boolean cut(IWorldConnector world, IPlayerConnector player, DetectionResult detected,
            TreeFellerCascade.Chain chain){
        DebuggerContext context = TreeFellerDebugger.begin("Cutting", player, world);
        context.at(detected.detected.root.pos);
        List<Long> sourceTrunks = detected.detected.getNodes(TreeNodeType.TRUNK, -1, -1).stream().map(node -> node.pos).toList();
        CuttingConfiguration config = TreeFellerConfiguration.getCombinedCuttingConfiguration(detected.tree, detected.tool);

        context.values("Cutting settings",config);
        if(config.leave_stump!=null&&config.leave_stump){
            int rootY = BlockPos.getY(detected.detected.root.pos);
            ArrayList<TreeNode> toTrim = new ArrayList<>();
            detected.detected.walk((node) -> {
                if(node.type!=TreeNodeType.TRUNK)return false;
                int y = BlockPos.getY(node.pos);
                if(y>rootY+1)return false; //TODO remove magic number! (This `1` exists to account for disconnected trunk sections in a partial cross-section cut)
                if(y<rootY){
                    toTrim.add(node);
                    return false;
                }
                return true; // keep walking
            });
            int totalTrimmed = 0;
            for(TreeNode node : toTrim){
                context.decision(DebugLevel.DETAIL,node.pos,OverlayMark.RETAINED,"Retain branch: leave stump below cut height");
                totalTrimmed += detected.detected.trim(node);
            }
            context.info("Leave Stump - Trimmed "+totalTrimmed+" nodes from "+toTrim.size()+" branches");
            context.snapshot(detected.detected,DebugLevel.SUMMARY,OverlayMark.SELECTED);
        }
        if(config.leaf_break_range!=null){
            ArrayList<TreeNode> toTrim = new ArrayList<>();
            detected.detected.walk((node) -> {
                if(node.type==TreeNodeType.TRUNK)return true;
                if(node.type!=TreeNodeType.LEAVES)return false;
                if(node.distance>config.leaf_break_range){
                    toTrim.add(node);
                    return false;
                }
                return true;
            });
            int totalTrimmed = 0;
            for(TreeNode node : toTrim){
                context.decision(DebugLevel.DETAIL,node.pos,OverlayMark.RETAINED,"Retain branch: leaf distance exceeds break range");
                totalTrimmed += detected.detected.trim(node);
            }
            context.info("Leaf Break Range "+config.leaf_break_range+" - Trimmed "+totalTrimmed+" nodes from "+toTrim.size()+" branches");
            context.snapshot(detected.detected,DebugLevel.SUMMARY,OverlayMark.SELECTED);
        }
        if(config.partial_trunk_limit!=null){
            int limit = config.partial_trunk_limit;
            int[] found = new int[1];
            ArrayList<TreeNode> toTrim = new ArrayList<>();
            detected.detected.walk((node) -> {
                if(node.type!=TreeNodeType.TRUNK)return node.type==TreeNodeType.LEAVES;
                found[0]++;
                if(found[0]>limit){
                    toTrim.add(node);
                    return false;
                }
                return true;
            });
            int totalTrimmed = 0;
            for(TreeNode node : toTrim){
                context.decision(DebugLevel.DETAIL,node.pos,OverlayMark.RETAINED,"Retain branch: partial trunk limit exceeded");
                totalTrimmed += detected.detected.trim(node);
            }
            context.info("Partial Trunk Limit "+limit+" - Trimmed "+totalTrimmed+" nodes from "+toTrim.size()+" branches");
            context.snapshot(detected.detected,DebugLevel.SUMMARY,OverlayMark.SELECTED);
        }

        IItemConnector item = player.getTool();
        boolean skipDamage = item.getMaxDurability()==0
                ||player.getGameMode()==PlayerGameMode.CREATIVE
                ||Boolean.TRUE.equals(config.respect_unbreakable)&&item.isUnbreakable();
        long available = item.getCurrentDurability();
        if(Boolean.TRUE.equals(config.stacked_tools))
            available += (long)item.getMaxDurability()*Math.max(0, item.getCount()-1);
        long budget = Math.max(0, available-(Boolean.TRUE.equals(config.prevent_breakage)?1:0));
        int divisor = Boolean.TRUE.equals(config.respect_unbreaking)?Math.max(1, item.getUnbreakingLevel()+1):1;
        context.info("Tool damage exempt="+skipDamage+", unbreaking divisor="+divisor+", stacked tools="+config.stacked_tools+", prevent breakage="+config.prevent_breakage);
        List<TreeNode> nodes = selectedNodes(detected);
        int cost = skipDamage?0:cost(nodes, config, divisor);
        context.info("Selected blocks: "+nodes.size()+", durability cost: "+cost+", available: "+available+", budget: "+budget);
        if(cost>budget){
            if(Boolean.TRUE.equals(config.allow_partial_tool)&&!Boolean.TRUE.equals(config.prevent_breakage)){
                cost = (int)Math.min(Integer.MAX_VALUE, available);
                context.info("Partial tool: felling the full selection with remaining durability");
            }else if(Boolean.TRUE.equals(config.allow_partial)){
                double accumulated = 0;
                for(TreeNode node : nodes){
                    if(!isCuttable(node))continue; // An earlier branch may already have been trimmed.
                    double next = accumulated+weight(node, config);
                    context.trace("Durability at "+DebuggerContext.position(node.pos)+": weight="+weight(node,config)+", cumulative="+next+", rounded="+roundedCost(next,divisor)+", budget="+budget);
                    if(roundedCost(next, divisor)>budget){
                        int trimmed = detected.detected.trim(node);
                        context.info("Durability limit: trimmed "+trimmed+" nodes", node.pos);
                    }else accumulated = next;
                }
                nodes = selectedNodes(detected);
                cost = cost(nodes, config, divisor);
                context.info("Partial cut: "+nodes.size()+" blocks, durability cost: "+cost);
            }else{
                context.fail("Insufficient durability"+(Boolean.TRUE.equals(config.prevent_breakage)?" while preventing tool breakage":""));
                return false;
            }
        }
        if(nodes.isEmpty()||nodes.stream().noneMatch(n -> n.type==TreeNodeType.TRUNK)){
            context.fail("No trunk remains in the cut selection");
            return false;
        }
        context.user("Cutting "+nodes.size()+" blocks; tool durability cost "+cost);
        context.snapshot(detected.detected,DebugLevel.USER,OverlayMark.SELECTED);
        TreeFellerBreaking.PreparedBreak prepared = TreeFellerBreaking.prepare(world, player, detected, nodes, item);
        if(prepared==null)return false;
        Runnable cascadeComplete = TreeFellerCascade.enqueue(world, player, detected, nodes, sourceTrunks, chain);
        boolean animationScheduled = false;
        try{
            int brokenTools = damageTool(item, player, cost, Boolean.TRUE.equals(config.stacked_tools), context);
            if(brokenTools>0)prepared.toolEffects(true);
            if(Boolean.TRUE.equals(config.cutting_animation)&&(config.animation_delay==null||config.animation_delay>0)){
                Map<Integer, List<TreeNode>> layers = new TreeMap<>();
                for(TreeNode node : nodes){
                    TreeNode trunk = node;
                    while(trunk.parent!=null&&trunk.type!=TreeNodeType.TRUNK)trunk = trunk.parent;
                    layers.computeIfAbsent(trunk.distance, ignored -> new ArrayList<>()).add(node);
                }
                int delay = config.animation_delay==null?1:Math.max(1, config.animation_delay);
                List<List<TreeNode>> orderedLayers = new ArrayList<>(layers.values());
                // Break the initiating layer during the original event, avoiding a second vanilla
                // drop and durability charge for the player's target block.
                BreakingResult first = prepared.apply(orderedLayers.get(0));
                prepared.toolEffects(false);
                ResultConfiguration resultConfig = TreeFellerConfiguration.getCombinedResultConfiguration(detected.tree, detected.tool);
                TreeFellerResult.complete(world, player, detected, first, resultConfig, TreeFellerCore.config.global.trigger);
                if(orderedLayers.size()>1)animate(world, player, detected, prepared, orderedLayers, 1, delay,
                        resultConfig, TreeFellerCore.config.global.trigger, cascadeComplete);
                else cascadeComplete.run();
                animationScheduled = orderedLayers.size()>1;
                context.user("Felling in "+layers.size()+" layers, "+delay+" ticks apart; charged "+cost+" durability");
            }else{
                BreakingResult result = prepared.apply(nodes);
                prepared.toolEffects(false);
                TreeFellerResult.complete(world, player, detected, result);
                cascadeComplete.run();
                context.user("Felled "+result.nodes.size()+" blocks; charged "+cost+" durability");
            }
            return true;
        }finally{
            if(!animationScheduled)cascadeComplete.run();
        }
    }
    private static void animate(IWorldConnector world, IPlayerConnector player, DetectionResult detected,
            TreeFellerBreaking.PreparedBreak prepared, List<List<TreeNode>> layers, int index, int delay, ResultConfiguration resultConfig, TriggerConfiguration globalTrigger, Runnable cascadeComplete){
        TreeFellerCore.connector.schedule(delay, () -> {
            boolean continued = false;
            try{
                TreeFellerDebugger.begin(DebugPhase.CUTTING,player,world).summary("Animation layer "+(index+1)+"/"+layers.size()+", "+layers.get(index).size()+" blocks");
                BreakingResult result = prepared.apply(layers.get(index));
                TreeFellerResult.complete(world, player, detected, result, resultConfig, globalTrigger, false);
                if(index+1<layers.size())animate(world, player, detected, prepared, layers, index+1, delay, resultConfig, globalTrigger, cascadeComplete);
                else cascadeComplete.run();
                continued = index+1<layers.size();
            }finally{if(!continued)cascadeComplete.run();}
        });
    }
    private static boolean isCuttable(TreeNode node){
        return node.type==TreeNodeType.TRUNK||node.type==TreeNodeType.LEAVES||node.type==TreeNodeType.DECORATION;
    }
    private static List<TreeNode> selectedNodes(DetectionResult detected){
        List<TreeNode> nodes = new ArrayList<>();
        detected.detected.walk(node -> {
            if(isCuttable(node))nodes.add(node);
            return true;
        });
        return nodes;
    }
    private static double weight(TreeNode node, CuttingConfiguration config){
        Float multiplier = node.type==TreeNodeType.TRUNK?config.trunk_damage_mult
                :node.type==TreeNodeType.LEAVES?config.leaves_damage_mult:config.decorations_damage_mult;
        return multiplier==null?0:Math.max(0, multiplier);
    }
    private static int roundedCost(double cost, int divisor){
        if(cost<=0)return 0;
        return (int)Math.min(Integer.MAX_VALUE, Math.ceil(Math.max(1, Math.floor(cost))/divisor));
    }
    private static int cost(List<TreeNode> nodes, CuttingConfiguration config, int divisor){
        double total = 0;
        for(TreeNode node : nodes)total += weight(node, config);
        return roundedCost(total, divisor);
    }
    private static int damageTool(IItemConnector item, IPlayerConnector player, int cost, boolean stacked, DebuggerContext context){
        if(cost<=0)return 0;
        int broken = 0;
        int remaining = item.getCurrentDurability();
        int count = item.getCount();
        while(cost>=remaining&&count>0){
            cost -= remaining;
            count = stacked?count-1:0;
            remaining = item.getMaxDurability();
            broken++;
            context.info("Consumed one tool");
        }
        item.setCount(count);
        if(count>0)item.setDamage(item.getMaxDurability()-remaining+cost);
        player.setTool(item);
        return broken;
    }
}
