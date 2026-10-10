package com.thizthizzydizzy.treefeller.core.breaking;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeFellerConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.BreakingConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.ResultConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.breaking.FellBehavior;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IBlockDefinition;
import com.thizthizzydizzy.treefeller.core.connector.item.IItemConnector;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import com.thizthizzydizzy.treefeller.core.connector.world.IWorldConnector;
import com.thizthizzydizzy.treefeller.core.debug.DebuggerContext;
import com.thizthizzydizzy.treefeller.core.debug.TreeFellerDebugger;
import com.thizthizzydizzy.treefeller.core.detection.DetectionResult;
import com.thizthizzydizzy.treefeller.core.detection.TreeNode;
import com.thizthizzydizzy.treefeller.core.detection.TreeNodeType;
import com.thizthizzydizzy.treefeller.core.result.TreeFellerResult;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
public class TreeFellerBreaking{
    public static BreakingResult breakTree(IWorldConnector world, IPlayerConnector player, DetectionResult detected, List<TreeNode> nodes, IItemConnector tool){
        DebuggerContext context = TreeFellerDebugger.begin("Breaking", player, world);
        BreakingConfiguration config = TreeFellerConfiguration.getCombinedBreakingConfiguration(detected.tree, detected.tool);
        ResultConfiguration resultConfig = TreeFellerConfiguration.getCombinedResultConfiguration(detected.tree, detected.tool);
        Map<Long, IBlockDefinition> conversions = new LinkedHashMap<>();
        for(TreeNode node : nodes){
            FellBehavior behavior = node.type==TreeNodeType.TRUNK?config.trunk_behavior
                    :node.type==TreeNodeType.LEAVES?config.leaves_behavior:config.decoration_behavior;
            if(behavior!=null&&(Boolean.TRUE.equals(behavior.natural)||Boolean.TRUE.equals(behavior.fall_as_entities))){
                context.fail("Falling behavior is not implemented yet");
                return null;
            }
            if(config.block_conversions!=null){
                for(Map.Entry<IBlockDefinition, IBlockDefinition> entry : config.block_conversions.entrySet()){
                    if(world.matches(node.pos, entry.getKey())){
                        conversions.put(node.pos, entry.getValue());
                        break;
                    }
                }
            }
        }
        Map<Long, List<IItemConnector>> drops = new LinkedHashMap<>();
        // Capture drops while blocks and tool metadata still exist, before removal or tool damage.
        for(TreeNode node : nodes){
            if(!conversions.containsKey(node.pos))
                drops.put(node.pos, TreeFellerResult.collectDrops(world, player, node, tool, resultConfig));
        }
        for(TreeNode node : nodes){
            if(conversions.containsKey(node.pos)){
                world.setBlock(node.pos, conversions.get(node.pos));
                context.info("Converted "+node.type, node.pos, conversions.get(node.pos));
            }else{
                world.removeBlock(node.pos);
                context.info("Broke "+node.type, node.pos);
            }
        }
        context.pass("Processed "+nodes.size()+" blocks, including "+conversions.size()+" conversions");
        return new BreakingResult(new ArrayList<>(nodes), drops);
    }
}
