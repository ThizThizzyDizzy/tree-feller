package com.thizthizzydizzy.treefeller.core.cutting;
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
import java.util.ArrayList;
public class TreeFellerCutting{
    public static void cut(IWorldConnector world, IPlayerConnector player, DetectionResult detected){
        DebuggerContext context = TreeFellerDebugger.begin("Cutting");
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
                if(node.type!=TreeNodeType.TRUNK)return false;
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

        IItemConnector item = player.getTool();
        boolean skipDurabilityChecks = false;
        if(config.respect_unbreakable!=null&&config.respect_unbreakable&&item.isUnbreakable())
            skipDurabilityChecks = true;
        if(item.getMaxDurability()==0)skipDurabilityChecks = true;
        if(player.getGameMode()==PlayerGameMode.CREATIVE)
            skipDurabilityChecks = true;
        int durabilityCost;
        if(!skipDurabilityChecks){
            float durabilityCostF = 0;
            if(config.trunk_damage_mult!=null)
                durabilityCostF += detected.detected.count(TreeNodeType.TRUNK)*config.trunk_damage_mult;
            if(config.leaves_damage_mult!=null)
                durabilityCostF += detected.detected.count(TreeNodeType.LEAVES)*config.leaves_damage_mult;
            if(config.decorations_damage_mult!=null)
                durabilityCostF += detected.detected.count(TreeNodeType.DECORATION)*config.decorations_damage_mult;

            // round down, but ensure it is at least 1 if durability cost is positive
            durabilityCost = (int)durabilityCostF;
            if(durabilityCostF>0&&durabilityCost==0)durabilityCost = 1;

            int durability = item.getCurrentDurability();
            if(config.stacked_tools!=null&&config.stacked_tools)
                durability += item.getMaxDurability()*(item.getCount()-1);
            if(config.respect_unbreaking!=null&&config.respect_unbreaking){
                int level = item.getUnbreakingLevel();
                if(level>0){
                    durabilityCost /= (level+1);
                }
            }
            if(config.prevent_breakage!=null&&config.prevent_breakage)
                durability--;

            if(durabilityCost>durability){
                if(config.allow_partial_tool!=null&&config.allow_partial_tool){
                    context.info("Partial Tool");
                    durabilityCost = durability;
                }else if(config.allow_partial!=null&&!config.allow_partial){
                    context.fail("Partial");
                    return;
                }
            }
        }
        
        //TODO cutting_animation
        //TODO animation_delay
        
        //TODO damage the tool

        //TODO breaking
        //TODO result
    }
}
