package com.thizthizzydizzy.treefeller.core.config.structure.section;
import com.thizthizzydizzy.treefeller.core.config.legacy.LegacyBukkitOption;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
import com.thizthizzydizzy.treefeller.core.config.globaldefault.ConfigGlobalDefaultBoolean;
import com.thizthizzydizzy.treefeller.core.config.globaldefault.ConfigGlobalDefaultInteger;
import com.thizthizzydizzy.treefeller.core.config.globaldefault.ConfigGlobalDefaultValue;
import com.thizthizzydizzy.treefeller.core.config.structure.general.BlockFilter;
import com.thizthizzydizzy.treefeller.core.config.structure.general.IntegerRange;
import com.thizthizzydizzy.treefeller.core.config.structure.general.Range;
public class CriteriaConfiguration{
    @ConfigComment("The number of total trunk blocks required")
    @ConfigGlobalDefaultValue("4-256")
    @LegacyBukkitOption(value = "required-logs",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE},
            suffix = ".min")
    @LegacyBukkitOption(value = "max-logs",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE},
            suffix = ".max")
    public IntegerRange required_trunk;
    
    @ConfigComment("The number of total leaves required")
    @ConfigGlobalDefaultValue("10+")
    @LegacyBukkitOption(value = "required-leaves",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE},
            suffix = ".min")
    public IntegerRange required_leaves;

    @ConfigComment("The maximum distance from the base of the trunk that a tree may be cut")
    @ConfigGlobalDefaultInteger(5)
    @LegacyBukkitOption(value = "max-height",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE})
    public Integer max_height;

    @ConfigComment("Require a full horizontal cross-section of the tree trunk to be broken before felling the tree")
    @ConfigGlobalDefaultBoolean(false)
    @LegacyBukkitOption(value = "require-cross-section",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE})
    public Boolean require_cross_section;

    @ConfigComment("This filter applies to the tree trunk\nBlocks in this filter MUST also be present in the tree trunk in detection settings.")
    @LegacyBukkitOption(value = "banned-logs",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE},
            suffix = ".blacklist")
    public BlockFilter trunk_filter;

    @ConfigComment("This filter applies to the tree leaves\nBlocks in this filter MUST also be present in the tree leaves in detection settings.")
    @LegacyBukkitOption(value = "banned-leaves",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE},
            suffix = ".blacklist")
    public BlockFilter leaf_filter;

    @ConfigComment("This filter applies to the tree decorations\nBlocks in this filter MUST also be present in the tree decorations in detection settings.")
    public BlockFilter decoration_filter;

    @ConfigComment("The maximum length of a straight horizontal line of trunk blocks that may be present in a tree")
    @ConfigGlobalDefaultInteger(6)
    @LegacyBukkitOption(value = "max-horizontal-trunk-pillar-length",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE})
    public Integer trunk_max_horizontal_line;

    @ConfigComment("This range constrains the ratio of height to width of a tree")
    @LegacyBukkitOption(value = "min-height-ratio",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE},
            suffix = ".min")
    @LegacyBukkitOption(value = "max-height-ratio",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE},
            suffix = ".max")
    public Range height_ratio;
    
    @ConfigComment("This range constrains the ratio of vertical logs to horizontal logs in a tree. (This includes all pillar-type blocks, i.e. axis=x/y/z)")
    @ConfigGlobalDefaultValue("0.5+")
    @LegacyBukkitOption(value = "min-vertical-log-ratio",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE},
            suffix = ".min")
    @LegacyBukkitOption(value = "max-vertical-log-ratio",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE},
            suffix = ".max")
    public Range trunk_vertical_ratio;
}
