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
import com.thizthizzydizzy.treefeller.core.trigger.TreeFellerTrigger;
import com.thizthizzydizzy.treefeller.lib.it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
public class TreeFellerDetection{
    public static TreeTree detect(IPlayerConnector player, IWorldConnector world, TreeConfiguration tree, ToolConfiguration tool, long pos, IItemConnector item, boolean secondary){
        DebuggerContext context = TreeFellerDebugger.begin("Detect", player, world, tree, tool, pos, item);
        TreeTree detected = new TreeTree(pos, secondary);
        DetectionConfiguration detection = TreeFellerConfiguration.overlay(TreeFellerCore.config.global.detection, tree.detection);

        // only use for short-circuiting. Full criteria checking is the job of TreeFellerCriteria
        CriteriaConfiguration criteria = TreeFellerConfiguration.overlay(TreeFellerCore.config.global.criteria, tree.criteria);

        context.info("Root Distance: "+detection.root_distance);
        // Find trunk (roots scan)
        for(int i = 0; i<=detection.root_distance; i++){
            if(i>0&&TreeScanner.step(context, world, detected, tree, TreeScanner.ScanMode.ROOTS, TreeNodeType.ROOTS, 0, null, null)==0)
                return null; // No trunk, and no roots. This is not a tree.
            if(TreeScanner.step(context, world, detected, tree, TreeScanner.ScanMode.ROOTS, TreeNodeType.TRUNK, 0, null, null)>0){
                if(detected.nodeMap.size()==1&&detected.root==detected.nodeMap.values().stream().findAny().orElse(null)){
                    context.info("Tree only has current root. Node type: "+detected.root.type.toString());
                }
                context.info("Rebasing around trunk node. ("+detected.nodeMap.size()+" total nodes)");
                detected.rebase(detected.findAnyNode(n -> n.type==TreeNodeType.TRUNK));
                break;
            }
        }

        if(scanTrunk(context, world, detected, tree, 0, criteria)==-1)
            return null;

        context.info("Leaf Range: "+detection.leaf_detect_range);
        context.info("Disconnected Trunk Distance: "+detection.disconnected_trunk_distance);
        // Leaves/trunk connectors scan
        for(int i = 0; i<detection.leaf_detect_range; i++){
            //leaf scan
            if(TreeScanner.step(context, world, detected, tree, TreeScanner.ScanMode.LEAVES, TreeNodeType.LEAVES, -1, null, null)==0){
                // no more leaves to find
                context.info("Leaf scan complete.");
                break;
            }
            if(i<detection.disconnected_trunk_distance){
                // scan for disconnected trunks
                ReferenceArrayList<TreeNode> nodes = new ReferenceArrayList<>();
                if(TreeScanner.step(context, world, detected, tree, TreeScanner.ScanMode.LEAVES, TreeNodeType.TRUNK, -1, nodes::add, null)>0){
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

        if(!secondary){
            if(detection.secondary_tree_verification){
                context.info("Begin Secondary Verification Scan");
                // find additional possibly conflicting trees
                int extendedLeafScanRange = 0;
                for(TreeConfiguration tc : TreeFellerCore.config.trees){
                    DetectionConfiguration det = TreeFellerConfiguration.overlay(TreeFellerCore.config.global.detection, tc.detection);
                    extendedLeafScanRange = Math.max(extendedLeafScanRange, det.leaf_detect_range);
                }

                ReferenceArrayList<TreeTree> additionalTrees = new ReferenceArrayList<>();

                context.info("Extended leaf scan range: "+extendedLeafScanRange);
                for(int i = 0; i<extendedLeafScanRange; i++){
                    if(i>0&&TreeScanner.step(context, world, detected, tree, TreeScanner.ScanMode.EXTENDED_LEAVES, TreeNodeType.EXTENDED_LEAVES, -1, null, null)==0){
                        context.info("End extended leaf scan");
                        // extended leaf scan is complete
                        break;
                    }

                    int prevTreeCount = additionalTrees.size();
                    ReferenceArrayList<TreeNode> nodes = new ReferenceArrayList<>();
                    if(TreeScanner.step(context, world, detected, tree, TreeScanner.ScanMode.EXTENDED_LEAVES, TreeNodeType.TRUNK, -1, nodes::add, null)>0){

                        // Found possible adjacent trees! Scan 'em all.
                        for(TreeNode node : nodes){
                            if(detected.contains(node.pos)||additionalTrees.stream().anyMatch(t -> t.contains(node.pos)))
                                continue;
                            context.info("Begin secondary tree scan", node.pos);
                            TreeTree additionalTree = TreeFellerTrigger.detect(player, world, node.pos, true);
                            if(additionalTree!=null)
                                additionalTrees.add(additionalTree);
                        }
                    }

                    if(additionalTrees.size()>prevTreeCount){
                        // new trees have been found, trim off the detection surfaces to limit scan depth
                        List<TreeNode> potential = detected.getNodes(TreeNodeType.EXTENDED_LEAVES, -1, -1).stream().filter(t -> t.extendedDistance<=detection.leaf_detect_range).toList();
                        context.info("Potential extended leaves: "+potential.size());
                        POTENTIAL:
                        for(TreeNode node : potential){
                            if(!detected.contains(node.pos))continue;
                            for(TreeTree additional : additionalTrees){
                                if(additional.contains(node.pos)){
                                    TreeNode additionalNode = additional.nodeMap.get(node.pos);
                                    if(additionalNode.type==TreeNodeType.LEAVES&&additionalNode.distance<node.extendedDistance){
                                        // this node does not belong to this tree. trim it and all children.
                                        context.info("Trimming externally owned extended leaves", node.pos);
                                        int trimmed = detected.trim(node);
                                        context.info("Trimmed "+trimmed+" nodes");
                                        continue POTENTIAL;
                                    }
                                }
                            }
                            // We've found extra leaves that have not been claimed by another tree!
                            detected.reclassify(node, n->{
                                n.type = TreeNodeType.LEAVES;
                                n.distance = node.extendedDistance;
                            });
                        }

                        potential = detected.getNodes(TreeNodeType.LEAVES, -1, -1);
                        context.info("Potential leaves: "+potential.size());
                        POTENTIAL:
                        for(TreeNode node : potential){
                            if(!detected.contains(node.pos))continue;
                            for(TreeTree additional : additionalTrees){
                                if(additional.contains(node.pos)){
                                    TreeNode additionalNode = additional.nodeMap.get(node.pos);
                                    if(additionalNode.type==TreeNodeType.LEAVES&&additionalNode.distance<node.distance){
                                        // this node does not belong to this tree. trim it and all children.
                                        context.info("Trimming externally owned leaves", node.pos);
                                        int trimmed = detected.trim(node);
                                        context.info("Trimmed "+trimmed+" nodes");
                                        continue POTENTIAL;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            int additionalDecorations;
            do{
                additionalDecorations = 0;

                additionalDecorations += TreeScanner.step(context, world, detected, tree, TreeScanner.ScanMode.DECORATION, TreeNodeType.DECORATION, -1, null, null);
            }while(additionalDecorations>0);
        }

        context.info("End of detection");
        context.info(detected);
        return detected;
    }
    private static int scanTrunk(DebuggerContext context, IWorldConnector world, TreeTree detected, TreeConfiguration tree, int sectionId, CriteriaConfiguration criteria){
        int total = 0;
        int stepCount;
        while((stepCount = TreeScanner.step(context, world, detected, tree, TreeScanner.ScanMode.TRUNK, TreeNodeType.TRUNK, sectionId, null, null))>0){
            total += stepCount;
            if(total>criteria.max_trunk){
                // short-circuit with the tree size limit to prevent endless scanning
                context.fail("Scan short-circuit on tree size limit! ("+total+">"+criteria.max_trunk);
                return -1;
            }
        }
        return total;
    }
}
