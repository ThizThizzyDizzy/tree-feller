package com.thizthizzydizzy.treefeller.core.detection.tree;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeFellerConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.DetectionConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IBlockDefinition;
import com.thizthizzydizzy.treefeller.core.connector.world.IWorldConnector;
public class TreeScanner{
    public static int step(IWorldConnector world, TreeTree tree, TreeConfiguration treeConfig, ScanMode mode, TreeNodeType type, int sectionId){
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
        if(tree.isEmpty()){
            // Root node hasn't been scanned yet
            return scanNode(world, tree, tree.root, blocks, type)?1:0;
        }

        //TODO A WHOLE LOTTA SCANNIN
        int currentDepth = tree.getScanDepth(type, sectionId);


        return 0;
    }
    private static boolean scanNode(IWorldConnector world, TreeTree tree, TreeNode node, IBlockDefinition[] blocks, TreeNodeType type){
        boolean isMatch = false;
        for(IBlockDefinition block : blocks){
            if(isMatch |= world.matches(node.pos, block)){
                node.setType(type);
                break;
            }
        }
        tree.addNode(node);
        return false;
    }
    public enum ScanMode{
        ROOTS,
        TRUNK,
        LEAVES,
        DECORATION
    }
}
