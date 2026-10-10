package com.thizthizzydizzy.treefeller.core.cutting;
import com.thizthizzydizzy.treefeller.core.breaking.BreakingResult;
import com.thizthizzydizzy.treefeller.core.breaking.TreeFellerBreaking;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeFellerConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.CuttingConfiguration;
import com.thizthizzydizzy.treefeller.core.connector.item.IItemConnector;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import com.thizthizzydizzy.treefeller.core.connector.player.PlayerGameMode;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
import com.thizthizzydizzy.treefeller.core.connector.world.IWorldConnector;
import com.thizthizzydizzy.treefeller.core.debug.DebuggerContext;
import com.thizthizzydizzy.treefeller.core.debug.TreeFellerDebugger;
import com.thizthizzydizzy.treefeller.core.detection.DetectionResult;
import com.thizthizzydizzy.treefeller.core.detection.TreeNode;
import com.thizthizzydizzy.treefeller.core.detection.TreeNodeType;
import com.thizthizzydizzy.treefeller.core.result.TreeFellerResult;
import java.util.ArrayList;
import java.util.List;
public class TreeFellerCutting{
    public static boolean cut(IWorldConnector world, IPlayerConnector player, DetectionResult detected){
        DebuggerContext context = TreeFellerDebugger.begin("Cutting", player, world);
        CuttingConfiguration config = TreeFellerConfiguration.getCombinedCuttingConfiguration(detected.tree, detected.tool);

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
                totalTrimmed += detected.detected.trim(node);
            }
            context.info("Leave Stump - Trimmed "+totalTrimmed+" nodes from "+toTrim.size()+" branches");
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
                totalTrimmed += detected.detected.trim(node);
            }
            context.info("Leaf Break Range "+config.leaf_break_range+" - Trimmed "+totalTrimmed+" nodes from "+toTrim.size()+" branches");
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
                totalTrimmed += detected.detected.trim(node);
            }
            context.info("Partial Trunk Limit "+limit+" - Trimmed "+totalTrimmed+" nodes from "+toTrim.size()+" branches");
        }

        if(Boolean.TRUE.equals(config.cutting_animation)){
            context.fail("Cutting animation is not implemented yet");
            return false;
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
        context.info("Final cut selection", detected.detected);
        BreakingResult result = TreeFellerBreaking.breakTree(world, player, detected, nodes, item);
        if(result==null)return false;
        damageTool(item, player, cost, Boolean.TRUE.equals(config.stacked_tools), context);
        TreeFellerResult.complete(world, player, detected, result);
        context.pass("Felled "+result.nodes.size()+" blocks; charged "+cost+" durability");
        return true;
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
    private static void damageTool(IItemConnector item, IPlayerConnector player, int cost, boolean stacked, DebuggerContext context){
        if(cost<=0)return;
        int remaining = item.getCurrentDurability();
        int count = item.getCount();
        while(cost>=remaining&&count>0){
            cost -= remaining;
            count = stacked?count-1:0;
            remaining = item.getMaxDurability();
            context.info("Consumed one tool");
        }
        item.setCount(count);
        if(count>0)item.setDamage(item.getMaxDurability()-remaining+cost);
        player.setTool(item);
    }
}
