package com.thizthizzydizzy.treefeller.core.config.structure;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
import com.thizthizzydizzy.treefeller.core.config.structure.section.BreakingConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.CriteriaConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.CuttingConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.DetectionConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.ResultConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.TriggerConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IBlockDefinition;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IItemDefinition;
public class TreeConfiguration {
    @ConfigComment("Blocks that can be used to trigger tree felling indirectly (tree felling will be triggered at the nearest connected trunk)")
    public IBlockDefinition[] roots;
    
    @ConfigComment("The blocks to search for in the tree trunk")
    public IBlockDefinition[] trunk;
    
    @ConfigComment("The blocks to search for in the tree leaves")
    public IBlockDefinition[] leaves;
    
    @ConfigComment("The maximum number of saplings that may be planted for this tree")
    public int max_saplings;
    
    @ConfigComment("The sapling block to place for this tree")
    public IBlockDefinition sapling_block;
    
    @ConfigComment("The sapling item to consume for this tree")
    public IItemDefinition sapling_item;
    
    @ConfigComment("The tree indicies that this tree can cascade into")
    public int[] cascade_trees;

    public TriggerConfiguration trigger;
    public DetectionConfiguration detection;
    public CriteriaConfiguration criteria;
    public CuttingConfiguration cutting;
    public BreakingConfiguration breaking;
    public ResultConfiguration result;
}
