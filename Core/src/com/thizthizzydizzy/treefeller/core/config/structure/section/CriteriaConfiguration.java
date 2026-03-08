package com.thizthizzydizzy.treefeller.core.config.structure.section;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
import com.thizthizzydizzy.treefeller.core.config.globaldefault.ConfigGlobalDefaultBoolean;
import com.thizthizzydizzy.treefeller.core.config.globaldefault.ConfigGlobalDefaultInteger;
import com.thizthizzydizzy.treefeller.core.config.structure.general.BlockFilter;
import com.thizthizzydizzy.treefeller.core.config.structure.general.Range;
import com.thizthizzydizzy.treefeller.core.config.globaldefault.ConfigGlobalDefaultValue;
public class CriteriaConfiguration{
    @ConfigComment("The minimum number of total trunk blocks required")
    @ConfigGlobalDefaultInteger(4)
    public Integer required_trunk;

    @ConfigComment("The minimum number of total leaves required")
    @ConfigGlobalDefaultInteger(10)
    public Integer required_leaves;

    @ConfigComment("The maximum number of total trunk blocks allowed")
    @ConfigGlobalDefaultInteger(256)
    public Integer max_trunk;

    @ConfigComment("The maximum distance from the base of the trunk that a tree may be cut")
    @ConfigGlobalDefaultInteger(5)
    public Integer max_height;

    @ConfigComment("Allow a tool to fully cut down a tree, even with insufficient durability")
    @ConfigGlobalDefaultBoolean(true)
    public Boolean allow_partial_tool;
    
    @ConfigComment("Prevent felling a tree if doing so would break the tool")
    @ConfigGlobalDefaultBoolean(false)
    public Boolean prevent_breakage;

    @ConfigComment("Require a full horizontal cross-section of the tree trunk to be broken before felling the tree")
    @ConfigGlobalDefaultBoolean(false)
    public Boolean require_cross_section;

    @ConfigComment("This filter applies to the tree trunk\nBlocks in this filter MUST also be present in the tree trunk in detection settings.")
    public BlockFilter trunk_filter;

    @ConfigComment("This filter applies to the tree leaves\nBlocks in this filter MUST also be present in the tree leaves in detection settings.")
    public BlockFilter leaf_filter;

    @ConfigComment("This filter applies to the tree decorations\nBlocks in this filter MUST also be present in the tree decorations in detection settings.")
    public BlockFilter decoration_filter;

    @ConfigComment("The maximum length of a straight horizontal line of trunk blocks that may be present in a tree")
    @ConfigGlobalDefaultInteger(6)
    public Integer trunk_max_horizontal_line;

    @ConfigComment("The maximum number of trunks that a tree may have.")
    public Integer max_trunks;

    @ConfigComment("This range constrains the ratio of height to width of a tree")
    public Range height_ratio;
    
    @ConfigComment("This range constrains the ratio of vertical logs to horizontal logs in a tree. (This includes all pillar-type blocks, i.e. axis=x/y/z)")
    @ConfigGlobalDefaultValue("0.5+")
    public Range vertical_ratio;
}
