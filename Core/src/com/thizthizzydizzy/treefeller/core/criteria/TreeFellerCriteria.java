package com.thizthizzydizzy.treefeller.core.criteria;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.message.TreeFellerMessages;
import com.thizthizzydizzy.treefeller.core.config.structure.section.CriteriaConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IBlockDefinition;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockAxis;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
import com.thizthizzydizzy.treefeller.core.connector.world.IWorldConnector;
import com.thizthizzydizzy.treefeller.core.debug.*;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;

import com.thizthizzydizzy.treefeller.core.detection.DetectionResult;
import com.thizthizzydizzy.treefeller.core.detection.TreeNode;
import com.thizthizzydizzy.treefeller.core.detection.TreeNodeType;
import com.thizthizzydizzy.treefeller.core.detection.TreeTree;
import com.thizthizzydizzy.treefeller.lib.it.unimi.dsi.fastutil.objects.ReferenceArrayList;
public class TreeFellerCriteria{
    public static boolean check(IWorldConnector world, TreeTree detected, CriteriaConfiguration config){
        return check(world, detected, config, false, null, "explicit");
    }
    private static boolean check(IWorldConnector world, TreeTree detected, CriteriaConfiguration config, boolean cascading,IPlayerConnector player,String scope){
        if(config==null)return true;
        DebuggerContext context = TreeFellerDebugger.begin(DebugPhase.CRITERIA,player,world);
        context.values(scope+" criteria",config);
        TreeFellerMessages.Checks messages=TreeFellerMessages.checks(player,scope,"criteria");
        context.at(detected.root.pos);
        if(config.required_trunk!=null){
            int trunks = detected.count(TreeNodeType.TRUNK);
            if(!messages.check("required_trunk", context.check("Trunk Count", trunks, config.required_trunk, config.required_trunk.matches(trunks)), trunks, config.required_trunk))
                return false;
        }
        if(config.required_leaves!=null){
            int leaves = detected.count(TreeNodeType.LEAVES);
            if(!messages.check("required_leaves", context.check("Leaf Count", leaves, config.required_leaves, config.required_leaves.matches(leaves)), leaves, config.required_leaves))
                return false;
        }
        ReferenceArrayList<TreeNode> trunks = detected.getNodes(TreeNodeType.TRUNK, -1, -1);
        if(trunks.isEmpty()){
            context.fail("No trunk");
            return false;
        }
        ReferenceArrayList<TreeNode> leaves = detected.getNodes(TreeNodeType.LEAVES, -1, -1);
        ReferenceArrayList<TreeNode> decorations = detected.getNodes(TreeNodeType.DECORATION, -1, -1);
        ReferenceArrayList<TreeNode> allBlocks = new ReferenceArrayList<>(trunks.size()+leaves.size()+decorations.size());
        allBlocks.addAll(trunks);
        allBlocks.addAll(leaves);
        allBlocks.addAll(decorations);
        if(config.max_height!=null&&!cascading){
            int lowestTrunkY = trunks.stream().mapToInt((n) -> BlockPos.getY(n.pos)).min().getAsInt();
            int cutY = BlockPos.getY(detected.root.pos);
            int cutHeight = cutY-lowestTrunkY;
            context.info("Cut Y="+cutY+", lowest trunk Y="+lowestTrunkY);
            if(!messages.check("max_height", context.check("Cut Height", cutHeight, config.max_height, cutHeight<=config.max_height), cutHeight, config.max_height))
                return false;
        }
        if(Boolean.TRUE.equals(config.require_cross_section)){
            if(!messages.check("require_cross_section", !trunks.stream().anyMatch((n) -> n.pos!=detected.root.pos&&BlockPos.getY(n.pos)==BlockPos.getY(detected.root.pos)), detected.root.pos))
            {
                context.fail("Cut a complete horizontal cross-section first");
                return false;
            }
        }
        if(config.trunk_filter!=null){
            if(config.trunk_filter.required!=null){
                for(IBlockDefinition block : config.trunk_filter.required){
                    boolean found = false;
                    for(TreeNode node : trunks){
                        context.decision(DebugLevel.TRACE,node.pos,OverlayMark.CANDIDATE,"Check block filter against "+DebuggerContext.describe(block));
                        if(world.matches(node.pos, block)){
                            found = true;
                            break;
                        }
                    }
                    if(!found){
                        messages.check("trunk_filter",false,block,"required");
                        context.fail(scope+" requires trunk: "+DebuggerContext.describe(block));
                        context.info(block);
                        return false;
                    }
                }
            }
            if(config.trunk_filter.blacklist!=null){
                for(IBlockDefinition block : config.trunk_filter.blacklist){
                    for(TreeNode node : trunks){
                        context.decision(DebugLevel.TRACE,node.pos,OverlayMark.CANDIDATE,"Check block filter against "+DebuggerContext.describe(block));
                        if(world.matches(node.pos, block)){
                            messages.check("trunk_filter",false,block,"blacklist");
                            context.fail(scope+" forbids trunk "+DebuggerContext.describe(block)+" at "+DebuggerContext.position(node.pos));
                            context.mark(DebugLevel.USER,node.pos,OverlayMark.REJECTED);
                            context.info(node.pos, block);
                            return false;
                        }
                    }
                }
            }
            messages.check("trunk_filter",true,config.trunk_filter);
        }
        if(config.leaf_filter!=null){
            if(config.leaf_filter.required!=null){
                for(IBlockDefinition block : config.leaf_filter.required){
                    boolean found = false;
                    for(TreeNode node : leaves){
                        context.decision(DebugLevel.TRACE,node.pos,OverlayMark.CANDIDATE,"Check block filter against "+DebuggerContext.describe(block));
                        if(world.matches(node.pos, block)){
                            found = true;
                            break;
                        }
                    }
                    if(!found){
                        messages.check("leaf_filter",false,block,"required");
                        context.fail(scope+" requires leaf: "+DebuggerContext.describe(block));
                        context.info(block);
                        return false;
                    }
                }
            }
            if(config.leaf_filter.blacklist!=null){
                for(IBlockDefinition block : config.leaf_filter.blacklist){
                    for(TreeNode node : leaves){
                        context.decision(DebugLevel.TRACE,node.pos,OverlayMark.CANDIDATE,"Check block filter against "+DebuggerContext.describe(block));
                        if(world.matches(node.pos, block)){
                            messages.check("leaf_filter",false,block,"blacklist");
                            context.fail(scope+" forbids leaf "+DebuggerContext.describe(block)+" at "+DebuggerContext.position(node.pos));
                            context.mark(DebugLevel.USER,node.pos,OverlayMark.REJECTED);
                            context.info(node.pos, block);
                            return false;
                        }
                    }
                }
            }
            messages.check("leaf_filter",true,config.leaf_filter);
        }
        if(config.decoration_filter!=null){
            if(config.decoration_filter.required!=null){
                for(IBlockDefinition block : config.decoration_filter.required){
                    boolean found = false;
                    for(TreeNode node : decorations){
                        context.decision(DebugLevel.TRACE,node.pos,OverlayMark.CANDIDATE,"Check block filter against "+DebuggerContext.describe(block));
                        if(world.matches(node.pos, block)){
                            found = true;
                            break;
                        }
                    }
                    if(!found){
                        messages.check("decoration_filter",false,block,"required");
                        context.fail(scope+" requires decoration: "+DebuggerContext.describe(block));
                        context.info(block);
                        return false;
                    }
                }
            }
            if(config.decoration_filter.blacklist!=null){
                for(IBlockDefinition block : config.decoration_filter.blacklist){
                    for(TreeNode node : decorations){
                        context.decision(DebugLevel.TRACE,node.pos,OverlayMark.CANDIDATE,"Check block filter against "+DebuggerContext.describe(block));
                        if(world.matches(node.pos, block)){
                            messages.check("decoration_filter",false,block,"blacklist");
                            context.fail(scope+" forbids decoration "+DebuggerContext.describe(block)+" at "+DebuggerContext.position(node.pos));
                            context.mark(DebugLevel.USER,node.pos,OverlayMark.REJECTED);
                            context.info(node.pos, block);
                            return false;
                        }
                    }
                }
            }
            messages.check("decoration_filter",true,config.decoration_filter);
        }
        if(config.trunk_max_horizontal_line!=null){
            int maxHorizontalLine = 0;
            ReferenceArrayList<TreeNode> xAxis = new ReferenceArrayList<>(trunks);
            while(!xAxis.isEmpty()){
                TreeNode block = xAxis.get(0);
                boolean stillPartOfTheTree;
                do{
                    stillPartOfTheTree = false;
                    long previous = BlockPos.getRelative(block.pos, -1, 0, 0);
                    //dunno if Block is immutable, so I'll check it a more complicated way
                    for(TreeNode checking : xAxis){
                        if(checking.pos==previous){
                            block = checking;
                            stillPartOfTheTree = true;
                            break;
                        }
                    }
                }while(stillPartOfTheTree);
                //b should now be the first block in this row
                ReferenceArrayList<TreeNode> line = new ReferenceArrayList<>();
                do{
                    line.add(block);
                    stillPartOfTheTree = false;
                    long next = BlockPos.getRelative(block.pos, 1, 0, 0);
                    //dunno if Block is immutable, so I'll check it a more complicated way
                    for(TreeNode checking : xAxis){
                        if(checking.pos==next){
                            block = checking;
                            stillPartOfTheTree = true;
                            break;
                        }
                    }
                }while(stillPartOfTheTree);
                context.trace("Trunk horizontal line length="+line.size()+", starting at "+DebuggerContext.position(line.get(0).pos));
                for(TreeNode node : line)context.mark(DebugLevel.DETAIL,node.pos,OverlayMark.CANDIDATE);
                maxHorizontalLine = Math.max(maxHorizontalLine, line.size());
                xAxis.removeAll(line);//don't need to recheck the same line many times. Maybe this will actually make it faster even with all the nonsense above
            }
            //DUPLICATED CODE ALERT
            ReferenceArrayList<TreeNode> zAxis = new ReferenceArrayList<>(trunks);
            while(!zAxis.isEmpty()){
                TreeNode block = zAxis.get(0);
                boolean stillPartOfTheTree;
                do{
                    stillPartOfTheTree = false;
                    long previous = BlockPos.getRelative(block.pos, 0, 0, -1);
                    //dunno if Block is immutable, so I'll check it a more complicated way
                    for(TreeNode checking : zAxis){
                        if(checking.pos==previous){
                            block = checking;
                            stillPartOfTheTree = true;
                            break;
                        }
                    }
                }while(stillPartOfTheTree);
                //b should now be the first block in this row
                ReferenceArrayList<TreeNode> line = new ReferenceArrayList<>();
                do{
                    line.add(block);
                    stillPartOfTheTree = false;
                    long next = BlockPos.getRelative(block.pos, 0, 0, 1);
                    //dunno if Block is immutable, so I'll check it a more complicated way
                    for(TreeNode checking : zAxis){
                        if(checking.pos==next){
                            block = checking;
                            stillPartOfTheTree = true;
                            break;
                        }
                    }
                }while(stillPartOfTheTree);
                context.trace("Trunk horizontal line length="+line.size()+", starting at "+DebuggerContext.position(line.get(0).pos));
                for(TreeNode node : line)context.mark(DebugLevel.DETAIL,node.pos,OverlayMark.CANDIDATE);
                maxHorizontalLine = Math.max(maxHorizontalLine, line.size());
                zAxis.removeAll(line);//don't need to recheck the same line many times. Maybe this will actually make it faster even with all the nonsense above
            }
            if(!messages.check("trunk_max_horizontal_line", context.check("Horizontal Line", maxHorizontalLine, config.trunk_max_horizontal_line, maxHorizontalLine<=config.trunk_max_horizontal_line), maxHorizontalLine, config.trunk_max_horizontal_line))return false;
        }
        if(config.height_ratio!=null){
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;
            int maxZ = Integer.MIN_VALUE;
            for(TreeNode node : allBlocks){
                int x = BlockPos.getX(node.pos);
                int y = BlockPos.getY(node.pos);
                int z = BlockPos.getZ(node.pos);
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                minZ = Math.min(minZ, z);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
                maxZ = Math.max(maxZ, z);
            }
            int width = Math.max(maxX-minX, maxZ-minZ)+1;
            int height = maxY-minY+1;
            float value = height/(float)width;
            context.info("Tree height="+height+", width="+width);
            if(!messages.check("height_ratio", context.check("Height Ratio", value, config.height_ratio, config.height_ratio.matches(value)), value, config.height_ratio))
                return false;
        }
        if(config.trunk_vertical_ratio!=null){
            int horizontal = 0;
            int vertical = 0;
            for(TreeNode blok : trunks){
                BlockAxis axis = world.getBlockAxis(blok.pos);
                context.decision(DebugLevel.TRACE,blok.pos,OverlayMark.CANDIDATE,"Trunk axis="+axis);
                if(axis==BlockAxis.Y)vertical++;
                else if(axis!=null)horizontal++;
            }
            float value = vertical/(float)horizontal;
            context.info("Vertical logs="+vertical+", horizontal logs="+horizontal);
            if(!messages.check("trunk_vertical_ratio", context.check("Trunk Vertical Ratio", value, config.trunk_vertical_ratio, config.trunk_vertical_ratio.matches(value)), value, config.trunk_vertical_ratio))return false;
        }
        context.summary(scope+" criteria passed");
        context.snapshot(detected,DebugLevel.SUMMARY,OverlayMark.ACCEPTED);
        return true;
    }
    public static boolean check(IWorldConnector world, DetectionResult detected){
        return check(world, detected, false, null);
    }
    public static boolean check(IWorldConnector world, DetectionResult detected, boolean cascading){return check(world,detected,cascading,null);}
    public static boolean check(IWorldConnector world, DetectionResult detected, boolean cascading,IPlayerConnector player){
        if(!check(world, detected.detected, TreeFellerCore.config.global.criteria, cascading,player,"global"))return false;
        if(!check(world, detected.detected, detected.tree.criteria, cascading,player,"tree"))return false;
        if(!check(world, detected.detected, detected.tool.criteria, cascading,player,"tool"))return false;
        DebuggerContext context=TreeFellerDebugger.begin(DebugPhase.CRITERIA,player,world);
        context.user("Tree meets the felling requirements");
        context.snapshot(detected.detected,DebugLevel.USER,OverlayMark.ACCEPTED);
        return true;
    }
}
