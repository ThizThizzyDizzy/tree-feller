package com.thizthizzydizzy.treefeller.config;
import com.thizthizzydizzy.treefeller.config.hjson.SerializedComment;
public class TreeConfiguration {
    @SerializedComment("The blocks to search for in the tree trunk")
    public String[] trunk;
    
    @SerializedComment("The blocks to search for in the tree leaves")
    public String[] leaves;

    public TriggerConfiguration trigger;
    public DetectionConfiguration detection;
    public CriteriaConfiguration criteria;
    public CuttingConfiguration cutting;
    public BreakingConfiguration breaking;
    public ResultConfiguration result;
}
