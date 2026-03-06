package com.thizthizzydizzy.treefeller.core.detection.tree;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeFellerConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.DetectionConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IBlockDefinition;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
import com.thizthizzydizzy.treefeller.core.connector.world.IWorldConnector;
import com.thizthizzydizzy.treefeller.core.debug.DebuggerContext;
import com.thizthizzydizzy.treefeller.lib.it.unimi.dsi.fastutil.objects.ReferenceArrayList;
public class TreeScanner{
    private static final byte[] NEIGHBORS_DIRECT_X = {1, -1, 0, 0, 0, 0};
    private static final byte[] NEIGHBORS_DIRECT_Y = {0, 0, 1, -1, 0, 0};
    private static final byte[] NEIGHBORS_DIRECT_Z = {0, 0, 0, 0, 1, -1};

    private static final byte[] NEIGHBORS_EDGE_X = {1, 1, -1, -1, 1, 1, -1, -1, 0, 0, 0, 0};
    private static final byte[] NEIGHBORS_EDGE_Y = {1, -1, 1, -1, 0, 0, 0, 0, 1, 1, -1, -1};
    private static final byte[] NEIGHBORS_EDGE_Z = {0, 0, 0, 0, 1, -1, 1, -1, 1, -1, 1, -1};

    private static final byte[] NEIGHBORS_CORNER_X = {1, 1, 1, 1, -1, -1, -1, -1};
    private static final byte[] NEIGHBORS_CORNER_Y = {1, 1, -1, -1, 1, 1, -1, -1};
    private static final byte[] NEIGHBORS_CORNER_Z = {1, -1, 1, -1, 1, -1, 1, -1};
    public static int step(DebuggerContext context, IWorldConnector world, TreeTree tree, TreeConfiguration treeConfig, ScanMode mode, TreeNodeType type, int sectionId){
        context.info("Step (S:"+sectionId+")", mode, type);
        DetectionConfiguration config = TreeFellerConfiguration.overlay(TreeFellerCore.config.global.detection, treeConfig.detection);

        IBlockDefinition[] blocks;
        switch(type){
            case ROOTS:
                blocks = treeConfig.roots;
                break;
            case TRUNK:
                blocks = treeConfig.trunk;
                break;
            case LEAVES:
                blocks = treeConfig.leaves;
                break;
            case DECORATION:
                blocks = null; // decorations have special handling
                break;
            default:
                throw new AssertionError("Invalid TreeNodeType for scan: "+type.name());
        }
        
        if(blocks==null)return 0; // There's none, so you aren't going to find any.

        int currentDepth = tree.getScanDepth(type, sectionId);
        context.info("Current Depth: "+currentDepth);
        ReferenceArrayList<TreeNode> lastLayer;
        if(currentDepth==0){
            // Special initial-scan rules
            switch(mode){
                case ROOTS:
                case TRUNK:
                    // scan root node
                    boolean pass = scanNode(world, tree, tree.root, blocks, type);
                    context.info("Scanned root node: "+pass);
                    return pass?1:0;
                case LEAVES:
                    lastLayer = tree.getNodes(TreeNodeType.TRUNK, -1, -1);
                    break;
                case DECORATION:
                    lastLayer = new ReferenceArrayList<>();
                    lastLayer.addAll(tree.getNodes(TreeNodeType.TRUNK, -1, -1));
                    lastLayer.addAll(tree.getNodes(TreeNodeType.LEAVES, -1, -1));
                    break;
                default:
                    throw new AssertionError("Unknown scan mode: "+mode.toString());
            }
        }else lastLayer = tree.getNodes(type, sectionId, currentDepth);
        context.info("Last layer: "+lastLayer.size()+" nodes");
        int count = 0;
        for(TreeNode node : lastLayer){
            count += scanNeighbors(world, tree, node, blocks, config, mode, type);
        }
        context.info("Scanned "+count+" blocks");
        return count;
    }
    private static int scanNeighbors(IWorldConnector world, TreeTree tree, TreeNode node, IBlockDefinition[] blocks, DetectionConfiguration config, ScanMode mode, TreeNodeType type){
        int x = BlockPos.getX(node.pos);
        int y = BlockPos.getY(node.pos);
        int z = BlockPos.getZ(node.pos);

        boolean includeDirectNeighbors = true;
        boolean includeEdgeNeighbors = mode!=ScanMode.LEAVES||config.diagonal_leaves;
        boolean includeCornerNeighbors = mode!=ScanMode.LEAVES||config.diagonal_leaves;
        int len = includeDirectNeighbors?NEIGHBORS_DIRECT_X.length:0;
        if(includeEdgeNeighbors)len += NEIGHBORS_EDGE_X.length;
        if(includeCornerNeighbors)len += NEIGHBORS_CORNER_X.length;

        long[] neighbors = new long[len];
        int idx = 0;

        for(int i = 0; i<NEIGHBORS_DIRECT_X.length; i++){
            neighbors[idx++] = BlockPos.toPos(x+NEIGHBORS_DIRECT_X[i], y+NEIGHBORS_DIRECT_Y[i], z+NEIGHBORS_DIRECT_Z[i]);
        }
        for(int i = 0; i<NEIGHBORS_EDGE_X.length; i++){
            neighbors[idx++] = BlockPos.toPos(x+NEIGHBORS_EDGE_X[i], y+NEIGHBORS_EDGE_Y[i], z+NEIGHBORS_EDGE_Z[i]);
        }
        for(int i = 0; i<NEIGHBORS_CORNER_X.length; i++){
            neighbors[idx++] = BlockPos.toPos(x+NEIGHBORS_CORNER_X[i], y+NEIGHBORS_CORNER_Y[i], z+NEIGHBORS_CORNER_Z[i]);
        }

        int count = 0;
        for(int i = 0; i<neighbors.length; i++){
            long pos = neighbors[i];
            if(tree.contains(pos))continue; // already scanned that one
            if(scanNode(world, tree, new TreeNode(pos, node), blocks, type))
                count++;
        }
        return count;
    }
    private static boolean scanNode(IWorldConnector world, TreeTree tree, TreeNode node, IBlockDefinition[] blocks, TreeNodeType type){
        boolean isMatch = false;
        for(IBlockDefinition block : blocks){
            if(isMatch |= world.matches(node.pos, block)){
                node.setType(type);
                break;
            }
        }
        if(!isMatch)return false;
        tree.addNode(node);
        return true;
    }
    public enum ScanMode{
        ROOTS,
        TRUNK,
        LEAVES,
        DECORATION
    }
}
