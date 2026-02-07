package com.thizthizzydizzy.treefeller.config;
import com.thizthizzydizzy.treefeller.config.hjson.SerializedComment;
public class GlobalConfiguration {
    @SerializedComment("This section defines when TreeFeller can start searching for a tree")
    public TriggerConfiguration trigger;
    
    @SerializedComment("This section defines how TreeFeller searches for all the blocks in a tree.")
    public DetectionConfiguration detection;
    
    @SerializedComment("This section defines how TreeFeller decides if a given detection is a tree or not.")
    public CriteriaConfiguration criteria;
    
    @SerializedComment("This section defines how a tree is cut")
    public CuttingConfiguration cutting;
    
    @SerializedComment("This section defines how blocks are broken")
    public BreakingConfiguration breaking;
    
    @SerializedComment("This section defines the results after felling a tree")
    public ResultConfiguration result;
}
