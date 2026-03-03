package com.thizthizzydizzy.treefeller.core.config.structure;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
import com.thizthizzydizzy.treefeller.core.config.structure.general.DualList;
import com.thizthizzydizzy.treefeller.core.config.structure.section.BreakingConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.CriteriaConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.CuttingConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.ResultConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.TriggerConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IItemDefinition;
public class ToolConfiguration{
    @ConfigComment("The item to match for this tool")
    public IItemDefinition item;
    
    @ConfigComment("The indicies of trees that this tool may cut down")
    public DualList<Integer> trees;
    
    public TriggerConfiguration trigger;
    public CriteriaConfiguration criteria;
    public CuttingConfiguration cutting;
    public BreakingConfiguration breaking;
    public ResultConfiguration result;
}
