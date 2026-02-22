package com.thizthizzydizzy.treefeller.core.config.structure;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
import com.thizthizzydizzy.treefeller.core.config.structure.section.BreakingConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.CriteriaConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.CuttingConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.DetectionConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.ResultConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.TriggerConfiguration;
public class GlobalConfiguration {
    @ConfigComment("This section defines when TreeFeller can start searching for a tree")
    public TriggerConfiguration trigger = new TriggerConfiguration();
    
    @ConfigComment("This section defines how TreeFeller searches for all the blocks in a tree.")
    public DetectionConfiguration detection = new DetectionConfiguration();
    
    @ConfigComment("This section defines how TreeFeller decides if a given detection is a tree or not.")
    public CriteriaConfiguration criteria = new CriteriaConfiguration();
    
    @ConfigComment("This section defines how a tree is cut")
    public CuttingConfiguration cutting = new CuttingConfiguration();
    
    @ConfigComment("This section defines how blocks are broken")
    public BreakingConfiguration breaking = new BreakingConfiguration();
    
    @ConfigComment("This section defines the results after felling a tree")
    public ResultConfiguration result = new ResultConfiguration();
}
