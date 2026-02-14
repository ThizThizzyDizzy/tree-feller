package com.thizthizzydizzy.treefeller.config.structure;
import com.thizthizzydizzy.treefeller.config.structure.section.BreakingConfiguration;
import com.thizthizzydizzy.treefeller.config.structure.section.CriteriaConfiguration;
import com.thizthizzydizzy.treefeller.config.structure.section.CuttingConfiguration;
import com.thizthizzydizzy.treefeller.config.structure.section.DetectionConfiguration;
import com.thizthizzydizzy.treefeller.config.structure.section.ResultConfiguration;
import com.thizthizzydizzy.treefeller.config.structure.section.TriggerConfiguration;
import com.thizthizzydizzy.treefeller.config.ConfigComment;
public class TreeConfiguration {
    @ConfigComment("The blocks to search for in the tree trunk")
    public String[] trunk;
    
    @ConfigComment("The blocks to search for in the tree leaves")
    public String[] leaves;

    public TriggerConfiguration trigger;
    public DetectionConfiguration detection;
    public CriteriaConfiguration criteria;
    public CuttingConfiguration cutting;
    public BreakingConfiguration breaking;
    public ResultConfiguration result;
}
