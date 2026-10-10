package com.thizthizzydizzy.treefeller.core.config.structure;
import com.thizthizzydizzy.treefeller.core.config.legacy.LegacyBukkitOption;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
import com.thizthizzydizzy.treefeller.core.config.structure.section.BreakingConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.CriteriaConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.CuttingConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.DetectionConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.ResultConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.TriggerConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IBlockDefinition;
import com.thizthizzydizzy.treefeller.core.config.structure.special.SaplingConfiguration;
public class TreeConfiguration{
    @ConfigComment("Blocks that can be used to trigger tree felling indirectly (tree felling will be triggered at the nearest connected trunk)")
    @LegacyBukkitOption(value = "roots",
            scopes = {LegacyBukkitOption.Scope.TREE})
    public IBlockDefinition[] roots;

    @ConfigComment("The blocks to search for in the tree trunk")
    public IBlockDefinition[] trunk;

    @ConfigComment("The blocks to search for in the tree leaves")
    public IBlockDefinition[] leaves;

    @ConfigComment("The maximum number of saplings that may be planted for this tree")
    @LegacyBukkitOption(value = "max-saplings",
            scopes = {LegacyBukkitOption.Scope.TREE})
    public int max_saplings = 1;

    @ConfigComment("Saplings for this tree: a material name, or a pair with block and item definitions")
    @LegacyBukkitOption(value = "sapling",
            scopes = {LegacyBukkitOption.Scope.TREE},
            conversion = LegacyBukkitOption.Conversion.SAPLING)
    public SaplingConfiguration[] saplings;

    @ConfigComment("The tree indicies that this tree can cascade into")
    @LegacyBukkitOption(value = "cascade-trees",
            scopes = {LegacyBukkitOption.Scope.TREE},
            conversion = LegacyBukkitOption.Conversion.TREE_SELECTOR)
    public int[] cascade_trees;

    public TriggerConfiguration trigger;
    public DetectionConfiguration detection;
    public CriteriaConfiguration criteria;
    public CuttingConfiguration cutting;
    public BreakingConfiguration breaking;
    public ResultConfiguration result;
}
