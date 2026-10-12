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
        PreparedBreak prepared = prepare(world, player, detected, nodes, tool);
        return prepared==null?null:prepared.apply(nodes);
    }
    /** Validate the selection and retain the original tool and settings for delayed batches. */
    public static PreparedBreak prepare(IWorldConnector world, IPlayerConnector player, DetectionResult detected, List<TreeNode> nodes, IItemConnector tool){
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
        return new PreparedBreak(world, player, tool.copyForDrops(true, true), resultConfig, context, conversions);
    }
    public static final class PreparedBreak{
        private final IWorldConnector world;
        private final DebuggerContext context;
        private final Map<Long, IBlockDefinition> conversions;
        private final IPlayerConnector player;
        private final IItemConnector tool;
        private final ResultConfiguration resultConfig;
        private PreparedBreak(IWorldConnector world, IPlayerConnector player, IItemConnector tool, ResultConfiguration resultConfig, DebuggerContext context, Map<Long, IBlockDefinition> conversions){
            this.world = world;
            this.context = context;
            this.conversions = conversions;
            this.player = player;
            this.tool = tool;
            this.resultConfig = resultConfig;
        }
        public BreakingResult apply(List<TreeNode> nodes){
            Map<Long, List<IItemConnector>> selectedDrops = new LinkedHashMap<>();
            // Capture this batch's drops before any of its blocks or decorations are removed.
            for(TreeNode node : nodes)if(!conversions.containsKey(node.pos))
                selectedDrops.put(node.pos, TreeFellerResult.collectDrops(world, player, node, tool, resultConfig));
            int converted = 0;
            for(TreeNode node : nodes){
                if(conversions.containsKey(node.pos)){
                    converted++;
                    world.setBlock(node.pos, conversions.get(node.pos));
                    context.info("Converted "+node.type, node.pos, conversions.get(node.pos));
                }else{
                    world.removeBlock(node.pos);
                    context.info("Broke "+node.type, node.pos);
                }
            }
            context.pass("Processed "+nodes.size()+" blocks, including "+converted+" conversions");
            return new BreakingResult(new ArrayList<>(nodes), selectedDrops);
        }
    }
}
