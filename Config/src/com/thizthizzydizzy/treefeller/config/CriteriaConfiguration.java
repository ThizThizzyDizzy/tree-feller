package com.thizthizzydizzy.treefeller.config;
import com.thizthizzydizzy.treefeller.config.hjson.SerializedComment;
public class CriteriaConfiguration{
    @SerializedComment("The minimum number of total trunk blocks required")
    public int required_trunk = 4;

    @SerializedComment("The minimum number of total leaves required")
    public int required_leaves = 10;

    @SerializedComment("The maximum number of total trunk blocks allowed")
    public int max_trunk = 250;

    @SerializedComment("The maximum distance from the base of the trunk that a tree may be cut")
    public int max_height = 5;

    @SerializedComment("Allow a tool to fully cut down a tree, even with insufficient durability")
    public boolean allow_partial_tool = true;
    
    @SerializedComment("Prevent felling a tree if doing so would break the tool")
    public boolean prevent_breakage = false;

    @SerializedComment("Require a full horizontal cross-section of the tree trunk to be broken before felling the tree")
    public boolean require_cross_section = false;

    @SerializedComment("This filter applies to the tree trunk\nBlocks in this filter MUST also be present in the tree trunk in detection settings.")
    public BlockFilter trunk_filter;

    @SerializedComment("This filter applies to the tree leaves\nBlocks in this filter MUST also be present in the tree leaves in detection settings.")
    public BlockFilter leaf_filter;

    @SerializedComment("This filter applies to the tree decorations\nBlocks in this filter MUST also be present in the tree decorations in detection settings.")
    public BlockFilter decoration_filter;

    @SerializedComment("The maximum length of a straight horizontal line of trunk blocks that may be present in a tree")
    public int trunk_max_horizontal_line = 6;

    @SerializedComment("The maximum number of trunks that a tree may have.")
    public int max_trunks = -1;

    @SerializedComment("This range constrains the ratio of height to width of a tree")
    public Range height_ratio = new Range(-1, -1);
    
    @SerializedComment("This range constrains the ratio of vertical logs to horizontal logs in a tree. (This includes all pillar-type blocks, i.e. axis=x/y/z)")
    public Range vertical_ratio = new Range(0.5f, -1);
}
