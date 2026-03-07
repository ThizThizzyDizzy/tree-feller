package com.thizthizzydizzy.treefeller.core.config.structure.section;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
import com.thizthizzydizzy.treefeller.core.config.ConfigGlobalDefaultBoolean;
import com.thizthizzydizzy.treefeller.core.config.ConfigGlobalDefaultInteger;
import com.thizthizzydizzy.treefeller.core.config.ConfigGlobalDefaultNewInstance;
import com.thizthizzydizzy.treefeller.core.config.structure.section.detection.DecorationConfiguration;
public class DetectionConfiguration{
    @ConfigComment("The maximum distance to check for a tree trunk when breaking roots")
    @ConfigGlobalDefaultInteger(6)
    public Integer root_distance = 6;

    @ConfigComment("The maximum distance between disconnected trunk sections, connected by leaves")
    @ConfigGlobalDefaultInteger(0)
    public Integer disconnected_trunk_distance;

    @ConfigComment("The maximum distance from the trunk that leaves will be searched for")
    @ConfigGlobalDefaultInteger(6)
    public Integer leaf_detect_range;

    @ConfigComment("Allow leaves to be detected diagonally")
    @ConfigGlobalDefaultBoolean(false)
    public Boolean diagonal_leaves;

    @ConfigComment("Special detection rules for block data / metadata")
    @ConfigGlobalDefaultNewInstance
    public BlockDataRules block_data_rules;

    @ConfigComment("Decorations that should be considered part of a tree")
    public DecorationConfiguration[] decorations;

    public static class BlockDataRules{
        @ConfigComment("When scanning tree trunks, avoid connecting parallel adjacent tree trunks. This may cause issues with 2x2 trees or trees with branches.")
        @ConfigGlobalDefaultBoolean(false)
        public Boolean ignore_parallel_trunk_pillars;

        @ConfigComment("Use leaf block data (distance) to speed up leaf detection")
        @ConfigGlobalDefaultBoolean(true)
        public Boolean use_leaf_distance;

    }
}
