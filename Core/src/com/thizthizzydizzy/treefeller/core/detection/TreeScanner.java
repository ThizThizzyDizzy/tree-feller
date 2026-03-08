package com.thizthizzydizzy.treefeller.core.detection;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeFellerConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.DetectionConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.detection.DecorationConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IBlockDefinition;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockAxis;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
import com.thizthizzydizzy.treefeller.core.connector.world.IWorldConnector;
import com.thizthizzydizzy.treefeller.core.debug.DebuggerContext;
import com.thizthizzydizzy.treefeller.lib.it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;
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
    public static int step(DebuggerContext context, IWorldConnector world, TreeTree tree, TreeConfiguration treeConfig, ScanMode mode, TreeNodeType type, int sectionId, Consumer<TreeNode> nodeHandler, Predicate<Long> posFilter){
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
            case EXTENDED_LEAVES:
                blocks = treeConfig.leaves;
                break;
            case DECORATION:
                blocks = null; // decorations have special handling
                break;
            default:
                throw new AssertionError("Invalid TreeNodeType for scan: "+type.name());
        }

        if(blocks==null&&type!=TreeNodeType.DECORATION)return 0; // There's none, so you aren't going to find any.
        if(type==TreeNodeType.DECORATION&&(config.decorations==null||config.decorations.length==0))
            return 0; // no decorations are defined, so you aren't going to find any

        TreeNodeType baseType = mode.baseType;
        int currentDepth = tree.getScanDepth(baseType, sectionId);
        context.info("BASE TYPE:", baseType);
        context.info("Current Depth: "+currentDepth);
        ReferenceArrayList<TreeNode> lastLayer;
        if(currentDepth==0){
            // Special initial-scan rules
            switch(mode){
                case ROOTS:
                case TRUNK:
                    // scan root node
                    boolean pass = scanNode(world, tree, tree.root, blocks, type, nodeHandler);
                    context.info("Scanned root node: "+pass);
                    return pass?1:0;
                case LEAVES:
                    ReferenceArrayList<TreeNode> trunks = tree.getNodes(TreeNodeType.TRUNK, -1, -1);
                    if(!trunks.isEmpty()&&config.max_leaf_distance_from_top!=null){
                        int highestTrunkY = trunks.stream().mapToInt((n)->BlockPos.getY(n.pos)).max().getAsInt();
                        lastLayer = new ReferenceArrayList<>();
                        lastLayer.addAll(trunks.stream().filter(n -> BlockPos.getY(n.pos)>=highestTrunkY-config.max_leaf_distance_from_top).toList());
                    }else
                        lastLayer = trunks;
                    break;

                case EXTENDED_LEAVES:
                    lastLayer = tree.getNodes(TreeNodeType.LEAVES, -1, -1);
                    break;
                case DECORATION:
                    lastLayer = new ReferenceArrayList<>();
                    lastLayer.addAll(tree.getNodes(TreeNodeType.TRUNK, -1, -1));
                    lastLayer.addAll(tree.getNodes(TreeNodeType.LEAVES, -1, -1));
                    break;
                default:
                    throw new AssertionError("Unknown scan mode: "+mode.toString());
            }
        }else
            lastLayer = tree.getNodes(baseType, sectionId, currentDepth);

        context.info("Last layer: "+lastLayer.size()+" nodes");
        int count = 0;
        for(TreeNode node : lastLayer){
            count += scanNeighbors(world, tree, node, blocks, config, mode, type, nodeHandler, posFilter);
        }
        context.info("Scanned "+count+" blocks");
        return count;
    }
    private static int scanNeighbors(IWorldConnector world, TreeTree tree, TreeNode node, IBlockDefinition[] blocks, DetectionConfiguration config, ScanMode mode, TreeNodeType type, Consumer<TreeNode> nodeHandler, Predicate<Long> posFilter){
        boolean isDecorationColumn = mode==ScanMode.DECORATION&&type==node.type&&node.type==TreeNodeType.DECORATION;
        if(isDecorationColumn&&!node.decoration.column)return 0;

        int x = BlockPos.getX(node.pos);
        int y = BlockPos.getY(node.pos);
        int z = BlockPos.getZ(node.pos);

        boolean includeDirectNeighbors = true;
        boolean includeEdgeNeighbors = mode!=ScanMode.DECORATION&&((mode!=ScanMode.LEAVES&&mode!=ScanMode.EXTENDED_LEAVES)||config.diagonal_leaves);
        boolean includeCornerNeighbors = mode!=ScanMode.DECORATION&&((mode!=ScanMode.LEAVES&&mode!=ScanMode.EXTENDED_LEAVES)||config.diagonal_leaves);
        int len = includeDirectNeighbors?NEIGHBORS_DIRECT_X.length:0;
        if(includeEdgeNeighbors)len += NEIGHBORS_EDGE_X.length;
        if(includeCornerNeighbors)len += NEIGHBORS_CORNER_X.length;

        if(isDecorationColumn)len = 2;

        long[] neighbors = new long[len];
        int idx = 0;

        for(int i = 0; i<NEIGHBORS_DIRECT_X.length; i++){
            if(isDecorationColumn&&NEIGHBORS_DIRECT_Y[i]==0)continue;
            neighbors[idx++] = BlockPos.toPos(x+NEIGHBORS_DIRECT_X[i], y+NEIGHBORS_DIRECT_Y[i], z+NEIGHBORS_DIRECT_Z[i]);
        }
        if(includeEdgeNeighbors){
            for(int i = 0; i<NEIGHBORS_EDGE_X.length; i++){
                neighbors[idx++] = BlockPos.toPos(x+NEIGHBORS_EDGE_X[i], y+NEIGHBORS_EDGE_Y[i], z+NEIGHBORS_EDGE_Z[i]);
            }
        }
        if(includeCornerNeighbors){
            for(int i = 0; i<NEIGHBORS_CORNER_X.length; i++){
                neighbors[idx++] = BlockPos.toPos(x+NEIGHBORS_CORNER_X[i], y+NEIGHBORS_CORNER_Y[i], z+NEIGHBORS_CORNER_Z[i]);
            }
        }

        int count = 0;
        NEIGHBORS:
        for(int i = 0; i<neighbors.length; i++){
            long pos = neighbors[i];
            if(tree.contains(pos))continue; // already scanned that one
            if(posFilter!=null&&!posFilter.test(pos))continue;
            if(mode==ScanMode.TRUNK){
                if(config.block_data_rules.ignore_parallel_trunk_pillars){
                    do{
                        // Check if both are pillars of same orientation
                        BlockAxis axis = world.getBlockAxis(node.pos);
                        if(axis==null)break;
                        if(world.getBlockAxis(pos)!=axis)break;

                        int nx = BlockPos.getX(pos);
                        int ny = BlockPos.getY(pos);
                        int nz = BlockPos.getZ(pos);

                        // Early reject if they are at the same axis position (direct parallel neighbor)
                        if(axis==BlockAxis.X&&x==nx)continue NEIGHBORS;
                        if(axis==BlockAxis.Y&&y==ny)continue NEIGHBORS;
                        if(axis==BlockAxis.Z&&z==nz)continue NEIGHBORS;

                        // Pass if they are aligned on the same axis
                        if(axis==BlockAxis.X&&y==ny&&z==nz)break;
                        if(axis==BlockAxis.Y&&x==nx&&z==nz)break;
                        if(axis==BlockAxis.Z&&x==nx&&y==ny)break;

                        //blocks are diagonal neighbors; reject if either direct axial neighbor is a pillar of same orientation
                        long[] extraPositions = new long[]{
                            BlockPos.toPos(axis==BlockAxis.X?x:nx, axis==BlockAxis.Y?y:ny, axis==BlockAxis.Z?z:nz),
                            BlockPos.toPos(axis==BlockAxis.X?nx:x, axis==BlockAxis.Y?ny:y, axis==BlockAxis.Z?nz:z)
                        };
                        for(long extraPos : extraPositions){
                            Boolean itIsATrunkBlock = tree.contains(extraPos)?tree.nodeMap.get(extraPos).type==TreeNodeType.TRUNK:null;
                            if(itIsATrunkBlock==null){
                                itIsATrunkBlock = false;
                                for(IBlockDefinition block : blocks){
                                    if(world.matches(extraPos, block)){
                                        itIsATrunkBlock = true;
                                        break;
                                    }
                                }
                            }
                            if(itIsATrunkBlock&&world.getBlockAxis(extraPos)==axis)
                                continue NEIGHBORS;
                        }
                    }while(false);
                }
            }
            if(mode==ScanMode.LEAVES){
                if(config.block_data_rules.use_leaf_distance){
                    do{
                        // skip if either block leaf distance is null
                        int d1 = world.getLeafDistance(node.pos);
                        if(d1==-1)break;
                        int d2 = world.getLeafDistance(pos);
                        if(d2==-1)break;
                        // Reject if first block distance is <7 unless second block is greater
                        if(d1<7&&d2<=d1)continue NEIGHBORS;
                    }while(false);
                }
            }
            if(mode==ScanMode.DECORATION){
                for(DecorationConfiguration decoration : config.decorations){
                    if(!isDecorationColumn){
                        int yDiff = BlockPos.getY(pos)-y;
                        switch(decoration.direction){
                            case UP:
                                if(yDiff!=1)continue;
                                break;
                            case DOWN:
                                if(yDiff!=-1)continue;
                                break;
                            case SIDE:
                                if(yDiff!=0)continue;
                                break;
                            case SIDE_AND_DOWN:
                                if(yDiff==1)continue;
                                break;
                        }
                    }
                    if(scanNode(world, tree, new TreeNode(pos, node), decoration.blocks, type, (n) -> {
                        n.decoration = decoration;
                        if(nodeHandler==null)tree.addNode(n);
                    })){
                        count++;
                        break;
                    }
                }
                continue;
            }
            if(scanNode(world, tree, new TreeNode(pos, node), blocks, type, nodeHandler))
                count++;
        }
        return count;
    }
    private static boolean scanNode(IWorldConnector world, TreeTree tree, TreeNode node, IBlockDefinition[] blocks, TreeNodeType type, Consumer<TreeNode> nodeHandler){
        boolean isMatch = false;
        for(IBlockDefinition block : blocks){
            if(isMatch |= world.matches(node.pos, block)){
                node.setType(type);
                break;
            }
        }
        if(!isMatch)return false;
        if(nodeHandler==null)nodeHandler = tree::addNode;
        nodeHandler.accept(node);
        return true;
    }
    public enum ScanMode{
        ROOTS(TreeNodeType.ROOTS),
        TRUNK(TreeNodeType.TRUNK),
        LEAVES(TreeNodeType.LEAVES),
        EXTENDED_LEAVES(TreeNodeType.EXTENDED_LEAVES),
        DECORATION(TreeNodeType.DECORATION);
        public final TreeNodeType baseType;
        private ScanMode(TreeNodeType baseType){
            this.baseType = baseType;

        }
    }
}
