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
    /** Validate and capture every drop before an animation changes blocks or damages the tool. */
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
        Map<Long, List<IItemConnector>> drops = new LinkedHashMap<>();
        // Capture drops while blocks and tool metadata still exist, before removal or tool damage.
        for(TreeNode node : nodes){
            if(!conversions.containsKey(node.pos))
                drops.put(node.pos, TreeFellerResult.collectDrops(world, player, node, tool, resultConfig));
        }
        return new PreparedBreak(world, context, conversions, drops);
    }
    public static final class PreparedBreak{
        private final IWorldConnector world;
        private final DebuggerContext context;
        private final Map<Long, IBlockDefinition> conversions;
        private final Map<Long, List<IItemConnector>> drops;
        private PreparedBreak(IWorldConnector world, DebuggerContext context, Map<Long, IBlockDefinition> conversions, Map<Long, List<IItemConnector>> drops){
            this.world = world;
            this.context = context;
            this.conversions = conversions;
            this.drops = drops;
        }
        public BreakingResult apply(List<TreeNode> nodes){
            Map<Long, List<IItemConnector>> selectedDrops = new LinkedHashMap<>();
            int converted = 0;
            for(TreeNode node : nodes){
                if(conversions.containsKey(node.pos)){
                    converted++;
                    world.setBlock(node.pos, conversions.get(node.pos));
                    context.info("Converted "+node.type, node.pos, conversions.get(node.pos));
                }else{
                    world.removeBlock(node.pos);
                    selectedDrops.put(node.pos, drops.get(node.pos));
                    context.info("Broke "+node.type, node.pos);
                }
            }
            context.pass("Processed "+nodes.size()+" blocks, including "+converted+" conversions");
            return new BreakingResult(new ArrayList<>(nodes), selectedDrops);
        }
    }
}
