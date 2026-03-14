package com.thizthizzydizzy.treefeller.core.criteria;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.config.structure.section.CriteriaConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IBlockDefinition;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockAxis;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
import com.thizthizzydizzy.treefeller.core.connector.world.IWorldConnector;
import com.thizthizzydizzy.treefeller.core.debug.DebuggerContext;
import com.thizthizzydizzy.treefeller.core.debug.TreeFellerDebugger;
import com.thizthizzydizzy.treefeller.core.detection.DetectionResult;
import com.thizthizzydizzy.treefeller.core.detection.TreeNode;
import com.thizthizzydizzy.treefeller.core.detection.TreeNodeType;
import com.thizthizzydizzy.treefeller.core.detection.TreeTree;
import com.thizthizzydizzy.treefeller.lib.it.unimi.dsi.fastutil.objects.ReferenceArrayList;
public class TreeFellerCriteria{
    public static boolean check(IWorldConnector world, TreeTree detected, CriteriaConfiguration config){
        DebuggerContext context = TreeFellerDebugger.begin("Criteria");
        if(config.required_trunk!=null){
            int trunks = detected.count(TreeNodeType.TRUNK);
            if(!context.checkTrue("Trunk Count", config.required_trunk.matches(trunks)))
                return false;
        }
        if(config.required_leaves!=null){
            int leaves = detected.count(TreeNodeType.LEAVES);
            if(!context.checkTrue("Leaf Count", config.required_leaves.matches(leaves)))
                return false;
        }
        ReferenceArrayList<TreeNode> trunks = detected.getNodes(TreeNodeType.TRUNK, -1, -1);
        ReferenceArrayList<TreeNode> leaves = detected.getNodes(TreeNodeType.LEAVES, -1, -1);
        ReferenceArrayList<TreeNode> decorations = detected.getNodes(TreeNodeType.DECORATION, -1, -1);
        ReferenceArrayList<TreeNode> allBlocks = new ReferenceArrayList<>(trunks.size()+leaves.size()+decorations.size());
        allBlocks.addAll(trunks);
        allBlocks.addAll(leaves);
        allBlocks.addAll(decorations);
        if(config.max_height!=null){
            int lowestTrunkY = trunks.stream().mapToInt((n) -> BlockPos.getY(n.pos)).min().getAsInt();
            int cutY = BlockPos.getY(detected.root.pos);
            int cutHeight = cutY-lowestTrunkY;
            if(!context.checkTrue("Cut Height", cutHeight<=config.max_height))
                return false;
        }
        if(config.require_cross_section!=null){
            if(trunks.stream().anyMatch((n) -> n.pos!=detected.root.pos&&BlockPos.getY(n.pos)==BlockPos.getY(detected.root.pos)))
                context.fail("Cross Section");
        }
        if(config.trunk_filter!=null){
            if(config.trunk_filter.required!=null){
                for(IBlockDefinition block : config.trunk_filter.required){
                    boolean found = false;
                    for(TreeNode node : trunks){
                        if(world.matches(node.pos, block)){
                            found = true;
                            break;
                        }
                    }
                    if(!found){
                        context.fail("Missing required trunk");
                        context.info(block);
                        return false;
                    }
                }
            }
            if(config.trunk_filter.blacklist!=null){
                for(IBlockDefinition block : config.trunk_filter.blacklist){
                    for(TreeNode node : trunks){
                        if(world.matches(node.pos, block)){
                            context.fail("Blacklisted trunk");
                            context.info(node.pos, block);
                            return false;
                        }
                    }
                }
            }
        }
        if(config.leaf_filter!=null){
            if(config.leaf_filter.required!=null){
                for(IBlockDefinition block : config.leaf_filter.required){
                    boolean found = false;
                    for(TreeNode node : leaves){
                        if(world.matches(node.pos, block)){
                            found = true;
                            break;
                        }
                    }
                    if(!found){
                        context.fail("Missing required leaf");
                        context.info(block);
                        return false;
                    }
                }
            }
            if(config.leaf_filter.blacklist!=null){
                for(IBlockDefinition block : config.leaf_filter.blacklist){
                    for(TreeNode node : leaves){
                        if(world.matches(node.pos, block)){
                            context.fail("Blacklisted leaf");
                            context.info(node.pos, block);
                            return false;
                        }
                    }
                }
            }
        }
        if(config.decoration_filter!=null){
            if(config.decoration_filter.required!=null){
                for(IBlockDefinition block : config.decoration_filter.required){
                    boolean found = false;
                    for(TreeNode node : decorations){
                        if(world.matches(node.pos, block)){
                            found = true;
                            break;
                        }
                    }
                    if(!found){
                        context.fail("Missing required decoration");
                        context.info(block);
                        return false;
                    }
                }
            }
            if(config.decoration_filter.blacklist!=null){
                for(IBlockDefinition block : config.decoration_filter.blacklist){
                    for(TreeNode node : decorations){
                        if(world.matches(node.pos, block)){
                            context.fail("Blacklisted decoration");
                            context.info(node.pos, block);
                            return false;
                        }
                    }
                }
            }
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
                maxHorizontalLine = Math.max(maxHorizontalLine, line.size());
                zAxis.removeAll(line);//don't need to recheck the same line many times. Maybe this will actually make it faster even with all the nonsense above
            }
            if(!context.checkTrue("Horizontal Line", maxHorizontalLine<=config.trunk_max_horizontal_line));
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
            int width = Math.max(maxX-minX, maxZ-minZ);
            int height = maxY-minY;
            float value = height/(float)width;
            if(!context.checkTrue("Height Ratio", config.height_ratio.matches(value)))
                return false;
        }
        if(config.trunk_vertical_ratio!=null){
            int horizontal = 0;
            int vertical = 0;
            for(TreeNode blok : trunks){
                BlockAxis axis = world.getBlockAxis(blok.pos);
                if(axis==BlockAxis.Y)vertical++;
                else if(axis!=null)horizontal++;
            }
            float value = vertical/(float)horizontal;
            if(!context.checkTrue("Trunk Vertical Ratio", config.trunk_vertical_ratio.matches(value)));
        }
        return true;
    }
    public static boolean check(IWorldConnector world, DetectionResult detected){
        if(!check(world, detected.detected, TreeFellerCore.config.global.criteria))return false;
        if(!check(world, detected.detected, detected.tree.criteria))return false;
        if(!check(world, detected.detected, detected.tool.criteria))return false;
        return true;
    }
}
