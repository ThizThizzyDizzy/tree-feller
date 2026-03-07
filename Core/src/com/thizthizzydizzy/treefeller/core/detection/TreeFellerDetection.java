package com.thizthizzydizzy.treefeller.core.detection;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.config.structure.ToolConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeFellerConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.CriteriaConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.DetectionConfiguration;
import com.thizthizzydizzy.treefeller.core.connector.item.IItemConnector;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import com.thizthizzydizzy.treefeller.core.connector.world.IWorldConnector;
import com.thizthizzydizzy.treefeller.core.debug.DebuggerContext;
import com.thizthizzydizzy.treefeller.core.debug.TreeFellerDebugger;
import com.thizthizzydizzy.treefeller.core.detection.tree.TreeNode;
import com.thizthizzydizzy.treefeller.core.detection.tree.TreeNodeType;
import com.thizthizzydizzy.treefeller.core.detection.tree.TreeScanner;
import com.thizthizzydizzy.treefeller.core.detection.tree.TreeTree;
import com.thizthizzydizzy.treefeller.lib.it.unimi.dsi.fastutil.objects.ReferenceArrayList;
public class TreeFellerDetection{
    public static boolean detect(IPlayerConnector player, IWorldConnector world, TreeConfiguration tree, ToolConfiguration tool, long pos, IItemConnector item){
        DebuggerContext context = TreeFellerDebugger.begin("Detect", player, world, tree, tool, pos, item);
        TreeTree detected = new TreeTree(pos);
        DetectionConfiguration detection = TreeFellerConfiguration.overlay(TreeFellerCore.config.global.detection, tree.detection);
        
        // only use for short-circuiting. Full criteria checking is the job of TreeFellerCriteria
        CriteriaConfiguration criteria = TreeFellerConfiguration.overlay(TreeFellerCore.config.global.criteria, tree.criteria);

        context.info("Root Distance: "+detection.root_distance);
        // Find trunk (roots scan)
        for(int i = 0; i<=detection.root_distance; i++){
            if(i>0&&TreeScanner.step(context, world, detected, tree, TreeScanner.ScanMode.ROOTS, TreeNodeType.ROOTS, 0, null)==0)
                return false; // No trunk, and no roots. This is not a tree.
            if(TreeScanner.step(context, world, detected, tree, TreeScanner.ScanMode.ROOTS, TreeNodeType.TRUNK, 0, null)>0){
                if(detected.nodeMap.size()==1&&detected.root==detected.nodeMap.values().stream().findAny().orElse(null)){
                    context.info("Tree only has current root. Node type: "+detected.root.type.toString());
                }
                context.info("Rebasing around trunk node. ("+detected.nodeMap.size()+" total nodes)");
                detected.rebase(detected.findAnyNode(n -> n.type==TreeNodeType.TRUNK));
                break;
            }
        }
        
        int totalTrunks = 1; // (include root block)
        int additional = scanTrunk(context, world, detected, tree, 0, criteria);
        if(additional==-1)return false;
        totalTrunks+=additional;
        
        context.info("Leaf Range: "+detection.leaf_detect_range);
        context.info("Disconnected Trunk Distance: "+detection.disconnected_trunk_distance);
        // Leaves/trunk connectors scan
        for(int i = 0; i<detection.leaf_detect_range; i++){
            //leaf scan
            int leafCount = TreeScanner.step(context, world, detected, tree, TreeScanner.ScanMode.LEAVES, TreeNodeType.LEAVES, -1, null);
            if(leafCount==0){
                // no more leaves to find
                context.info("Leaf scan complete.");
                break;
            }
            if(i<detection.disconnected_trunk_distance){
                // scan for disconnected trunks
                ReferenceArrayList<TreeNode> nodes = new ReferenceArrayList<>();
                if(TreeScanner.step(context, world, detected, tree, TreeScanner.ScanMode.LEAVES, TreeNodeType.TRUNK, -1, nodes::add)>0){
                    context.info("Found "+nodes.size()+" disconnected trunk blocks");
                    // disconnected trunks were found! Add them to the tree and give them all section IDs.
                    for(TreeNode node : nodes){
                        if(detected.contains(node.pos))continue; // got nabbed by another section
                        node.sectionId = detected.nextSectionId();
                        detected.addNode(node);
                        context.info("Sanning new trunk section: "+node.sectionId);
                        scanTrunk(context, world, detected, tree, node.sectionId, criteria);
                        i = -1; // restart leaf scan to detect this section's leaves
                    }
                }
                
            }
        }
        
        //TODO block data filtering
        //TODO decorations
        
        context.info("End of detection");
        context.info(detected);
        return true;
    }
    private static int scanTrunk(DebuggerContext context, IWorldConnector world, TreeTree detected, TreeConfiguration tree, int sectionId, CriteriaConfiguration criteria){
        int total = 0;
        int stepCount;
        while((stepCount = TreeScanner.step(context, world, detected, tree, TreeScanner.ScanMode.TRUNK, TreeNodeType.TRUNK, sectionId, null))>0){
            total+=stepCount;
            if(total>criteria.max_trunk){
                // short-circuit with the tree size limit to prevent endless scanning
                context.fail("Scan short-circuit on tree size limit! ("+total+">"+criteria.max_trunk);
                return -1;
            } 
        }
        return total;
    }
}
