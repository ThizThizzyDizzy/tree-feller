package com.thizthizzydizzy.treefeller.core.detection;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.message.TreeFellerMessages;
import com.thizthizzydizzy.treefeller.core.config.structure.ToolConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeFellerConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.CriteriaConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.DetectionConfiguration;
import com.thizthizzydizzy.treefeller.core.connector.item.IItemConnector;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import com.thizthizzydizzy.treefeller.core.connector.world.IWorldConnector;
import com.thizthizzydizzy.treefeller.core.debug.*;
import com.thizthizzydizzy.treefeller.core.trigger.TreeFellerTrigger;
import com.thizthizzydizzy.treefeller.lib.it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import com.thizthizzydizzy.treefeller.core.criteria.TreeFellerCriteria;
public class TreeFellerDetection{
    public static TreeTree detect(IPlayerConnector player, IWorldConnector world, TreeConfiguration tree, ToolConfiguration tool, long pos, IItemConnector item, boolean secondary){
        return detect(player,world,tree,tool,pos,item,secondary,true);
    }
    private static TreeTree detect(IPlayerConnector player, IWorldConnector world, TreeConfiguration tree, ToolConfiguration tool, long pos, IItemConnector item, boolean secondary, boolean allowSeparation){
        DebuggerContext context = TreeFellerDebugger.begin("Detect", player, world, tree, tool, pos, item);
        final TreeTree scan = new TreeTree(pos, secondary);
        DetectionConfiguration detection = TreeFellerConfiguration.getCombinedDetectionConfiguration(tree);

        context.values("Detection settings",detection);
        if(!secondary){
            TreeFellerMessages.Checks messages=TreeFellerMessages.checks(player,"tree","detection");
            messages.applied(detection);
            if(tree.roots!=null)messages.check("roots",true,(Object)tree.roots);
            if(tree.trunk!=null)messages.check("trunk",true,(Object)tree.trunk);
            if(tree.leaves!=null)messages.check("leaves",true,(Object)tree.leaves);
        }
        boolean separate=allowSeparation&&detection.separate_trunks;
        boolean partitioned=false;
        // only use for short-circuiting. Full criteria checking is the job of TreeFellerCriteria
        int trunkScanLimit = Integer.MAX_VALUE;
        int trunkScanMinimum = 0;
        for(CriteriaConfiguration criteria :
                new CriteriaConfiguration[]{TreeFellerCore.config.global.criteria, tree.criteria, tool.criteria}){
            if(criteria!=null&&criteria.required_trunk!=null&&criteria.required_trunk.min!=null)
                trunkScanMinimum=Math.max(trunkScanMinimum,criteria.required_trunk.min);
            if(criteria!=null&&criteria.required_trunk!=null&&criteria.required_trunk.max!=null
                    &&(criteria.required_trunk.min==null||criteria.required_trunk.min<=criteria.required_trunk.max))
                trunkScanLimit = Math.min(trunkScanLimit, criteria.required_trunk.max);
        }

        context.info("Root Distance: "+detection.root_distance);
        // Find trunk (roots scan)
        for(int i = 0; i<=detection.root_distance; i++){
            if(i>0&&TreeScanner.step(context, world, scan, tree, TreeScanner.ScanMode.ROOTS, TreeNodeType.ROOTS, 0, null, null)==0){
                context.summary("No connected roots or trunk found");return null;
            } // No trunk, and no roots. This is not a tree.
            if(TreeScanner.step(context, world, scan, tree, TreeScanner.ScanMode.ROOTS, TreeNodeType.TRUNK, 0, null, null)>0){
                if(scan.nodeMap.size()==1&&scan.root==scan.nodeMap.values().stream().findAny().orElse(null)){
                    context.info("Tree only has current root. Node type: "+scan.root.type.toString());
                }
                context.info("Rebasing around trunk node. ("+scan.nodeMap.size()+" total nodes)");
                scan.rebase(scan.findAnyNode(n -> n.type==TreeNodeType.TRUNK));
                break;
            }
        }

        if(scan.findAnyNode(n -> n.type==TreeNodeType.TRUNK)==null){context.summary("No trunk found within root distance "+detection.root_distance);return null;}
        if(scanTrunk(context, world, scan, tree, 0, separate?Integer.MAX_VALUE:trunkScanLimit)==-1){
            scanLimitMessage(secondary?null:player,scan,tree,tool);
            return null;
        }

        context.info("Leaf Range: "+detection.leaf_detect_range);
        context.info("Disconnected Trunk Distance: "+detection.disconnected_trunk_distance);
        // Leaves/trunk connectors scan
        for(int i = 0; i<detection.leaf_detect_range; i++){
            //leaf scan
            if(TreeScanner.step(context, world, scan, tree, TreeScanner.ScanMode.LEAVES, TreeNodeType.LEAVES, -1, null, null)==0){
                // no more leaves to find
                context.info("Leaf scan complete.");
                break;
            }
            if(i<detection.disconnected_trunk_distance){
                // scan for disconnected trunks
                ReferenceArrayList<TreeNode> nodes = new ReferenceArrayList<>();
                if(TreeScanner.step(context, world, scan, tree, TreeScanner.ScanMode.LEAVES, TreeNodeType.TRUNK, -1, nodes::add, null)>0){
                    context.info("Found "+nodes.size()+" disconnected trunk blocks");
                    // disconnected trunks were found! Add them to the tree and give them all section IDs.
                    for(TreeNode node : nodes){
                        if(scan.contains(node.pos))continue; // got nabbed by another section
                        node.sectionId = scan.nextSectionId();
                        scan.addNode(node);
                        context.info("Scanning new trunk section: "+node.sectionId);
                        if(scanTrunk(context, world, scan, tree, node.sectionId, separate?Integer.MAX_VALUE:trunkScanLimit)==-1){scanLimitMessage(secondary?null:player,scan,tree,tool);return null;}
                        i = -1; // restart leaf scan to detect this section's leaves
                    }
                }

            }
        }

        TreeTree owned=scan;
        if(separate){
            final int minimum=trunkScanMinimum,maximum=trunkScanLimit;
            List<TreeTree> forest=TrunkOwnership.split(context,scan,world,tree,detection,size -> size>=minimum&&size<=maximum);
            if(forest.size()>1){
                TreeTree selected=forest.stream().filter(t -> t.contains(scan.root.pos)).findFirst().orElseThrow(
                        () -> new IllegalStateException("No owner for the selected trunk"));
                scanForest(context,world,tree,forest);
                for(TreeTree part:forest)if(part!=selected&&!TreeFellerCriteria.checkRetained(world,new DetectionResult(part,tree,tool))){
                    context.summary("Log clumps cannot qualify independently; keep the connected tree");
                    return detect(player,world,tree,tool,pos,item,secondary,false);
                }
                owned=selected;
                partitioned=true;
            }
            if(owned.count(TreeNodeType.TRUNK)>trunkScanLimit){scanLimitMessage(secondary?null:player,owned,tree,tool);return null;}
        }

        final TreeTree detected=owned;
        if(!secondary){
            if(detection.secondary_tree_verification){
                context.info("Begin Secondary Verification Scan");
                // find additional possibly conflicting trees
                int extendedLeafScanRange = 0;
                for(TreeConfiguration tc : TreeFellerCore.config.trees){
                    DetectionConfiguration det = TreeFellerConfiguration.getCombinedDetectionConfiguration(tc);
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
                            DetectionResult additional = TreeFellerTrigger.detect(player, world, node.pos, true);
                            if(additional!=null)
                                additionalTrees.add(additional.detected);
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
                                        context.mark(DebugLevel.DETAIL,node.pos,OverlayMark.RETAINED);
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
                                        context.mark(DebugLevel.DETAIL,node.pos,OverlayMark.RETAINED);
                                        context.info("Trimmed "+trimmed+" nodes");
                                        continue POTENTIAL;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if(!partitioned){
                int additionalDecorations;
                do{
                    additionalDecorations=TreeScanner.step(context,world,detected,tree,TreeScanner.ScanMode.DECORATION,TreeNodeType.DECORATION,-1,null,null);
                }while(additionalDecorations>0);
            }
        }

        context.user("Found tree: "+detected.count(TreeNodeType.TRUNK)+" logs, "+detected.count(TreeNodeType.LEAVES)+" leaves, "+detected.count(TreeNodeType.DECORATION)+" decorations");
        context.info(detected);
        return detected;
    }
    /** All clumps use the ordinary scanner, one breadth-first layer at a time.
     * A block joins the first frontier that reaches it; no trimming or protection pass. */
    private static void scanForest(DebuggerContext context,IWorldConnector world,TreeConfiguration definition,List<TreeTree> forest){
        Map<Long,TreeTree> owners=new HashMap<>();
        for(TreeTree part:forest)for(long pos:part.nodeMap.keySet())owners.put(pos,part);
        DetectionConfiguration config=TreeFellerConfiguration.getCombinedDetectionConfiguration(definition);
        for(int depth=0;depth<config.leaf_detect_range;depth++){
            int found=0;
            for(TreeTree part:forest)found+=TreeScanner.step(context,world,part,definition,TreeScanner.ScanMode.LEAVES,TreeNodeType.LEAVES,-1,
                    node -> {part.addNode(node);owners.put(node.pos,part);},pos -> !owners.containsKey(pos));
            if(found==0)break;
        }
        int found;
        do{
            found=0;
            for(TreeTree part:forest)found+=TreeScanner.step(context,world,part,definition,TreeScanner.ScanMode.DECORATION,TreeNodeType.DECORATION,-1,
                    node -> {part.addNode(node);owners.put(node.pos,part);},pos -> !owners.containsKey(pos));
        }while(found>0);
    }
    private static void scanLimitMessage(IPlayerConnector player,TreeTree detected,TreeConfiguration tree,ToolConfiguration tool){
        int count=detected.count(TreeNodeType.TRUNK);
        CriteriaConfiguration[] criteria={TreeFellerCore.config.global.criteria,tree.criteria,tool.criteria};
        String[] scopes={"global","tree","tool"};
        for(int i=0;i<criteria.length;i++)if(criteria[i]!=null&&criteria[i].required_trunk!=null
                &&criteria[i].required_trunk.max!=null&&count>criteria[i].required_trunk.max){
            TreeFellerMessages.checks(player,scopes[i],"criteria").check("required_trunk",false,count,criteria[i].required_trunk);
            break;
        }
    }
    private static int scanTrunk(DebuggerContext context, IWorldConnector world, TreeTree detected, TreeConfiguration tree, int sectionId, int trunkScanLimit){
        int total = 0;
        int stepCount;
        while((stepCount = TreeScanner.step(context, world, detected, tree, TreeScanner.ScanMode.TRUNK, TreeNodeType.TRUNK, sectionId, null, null))>0){
            total += stepCount;
            if(detected.count(TreeNodeType.TRUNK)>trunkScanLimit){
                // short-circuit with the tree size limit to prevent endless scanning
                context.fail("Scan short-circuit on tree size limit! ("+detected.count(TreeNodeType.TRUNK)+">"+trunkScanLimit);
                return -1;
            }
        }
        return detected.count(TreeNodeType.TRUNK)>trunkScanLimit?-1:total;
    }
}
