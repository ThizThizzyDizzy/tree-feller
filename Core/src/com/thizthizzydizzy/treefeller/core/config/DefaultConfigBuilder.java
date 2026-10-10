package com.thizthizzydizzy.treefeller.core.config;
import com.thizthizzydizzy.treefeller.core.config.structure.ToolConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeFellerConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.general.SimpleDirection;
import com.thizthizzydizzy.treefeller.core.config.structure.section.DetectionConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.detection.DecorationConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IBlockDefinition;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IItemDefinition;
import java.util.ArrayList;
import java.util.Map;
public class DefaultConfigBuilder{
    /** Definitions are keyed by lowercase material names supplied by the platform. */
    public static void build(TreeFellerConfiguration config, Map<String, IBlockDefinition> blocks,
            Map<String, IItemDefinition> items){
        ArrayList<ToolConfiguration> tools = new ArrayList<>();
        ArrayList<TreeConfiguration> trees = new ArrayList<>();
        for(Map.Entry<String, IItemDefinition> entry : items.entrySet()){
            if(entry.getKey().endsWith("_axe")){
                ToolConfiguration tool = new ToolConfiguration();
                tool.item = entry.getValue();
                tools.add(tool);
            }
        }
        for(Map.Entry<String, IBlockDefinition> entry : blocks.entrySet()){
            String name = entry.getKey();
            if(!name.endsWith("_log")||name.startsWith("stripped"))continue;
            String wood = name.substring(0, name.length()-4);
            String leafName = wood+"_leaves";
            ArrayList<IBlockDefinition> leaves = new ArrayList<>();
            IBlockDefinition standardLeaves = blocks.get(leafName);
            if(standardLeaves!=null){
                leaves.add(standardLeaves);
            }else{
                // Some woods have only colored leaves, e.g. red_poplar_leaves.
                // Prefer an exact match so oak does not also use pale_oak_leaves.
                for(Map.Entry<String, IBlockDefinition> leaf : blocks.entrySet()){
                    if(leaf.getKey().endsWith("_"+leafName))leaves.add(leaf.getValue());
                }
            }
            // Do not infer a tree when the platform has no corresponding foliage.
            if(leaves.isEmpty())continue;
            TreeConfiguration tree = new TreeConfiguration();
            tree.trunk = new IBlockDefinition[]{entry.getValue()};
            IBlockDefinition roots = blocks.get(wood+"_roots");
            if(roots!=null){
                tree.roots = new IBlockDefinition[]{roots};
                leaves.add(roots);
                tree.detection = new DetectionConfiguration();
                tree.detection.max_leaf_distance_from_top = 4096;
                tree.detection.leaf_detect_range = config.global.detection.root_distance;
            }
            tree.leaves = leaves.toArray(IBlockDefinition[]::new);
            trees.add(tree);
        }
        config.tools = tools.toArray(ToolConfiguration[]::new);
        config.trees = trees.toArray(TreeConfiguration[]::new);
        config.global.detection.decorations = new DecorationConfiguration[]{
            new DecorationConfiguration(SimpleDirection.UP, false, blocks.get("snow")),
            new DecorationConfiguration(SimpleDirection.SIDE_AND_DOWN, true, blocks.get("vine")),
            new DecorationConfiguration(SimpleDirection.SIDE, false, blocks.get("cocoa"))
        };
    }
}
