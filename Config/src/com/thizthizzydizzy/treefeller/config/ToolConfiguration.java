package com.thizthizzydizzy.treefeller.config;
import com.thizthizzydizzy.treefeller.config.hjson.SerializedComment;
public class ToolConfiguration{
    @SerializedComment("The item to match for this tool")
    public String item;
    
    public TriggerConfiguration trigger;
    public DetectionConfiguration detection;
    public CriteriaConfiguration criteria;
    public CuttingConfiguration cutting;
    public BreakingConfiguration breaking;
    public ResultConfiguration result;
}
