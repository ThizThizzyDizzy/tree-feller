package com.thizthizzydizzy.treefeller.core.config.structure.section;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
import com.thizthizzydizzy.treefeller.core.config.structure.section.detection.DecorationConfiguration;
public class DetectionConfiguration{
    @ConfigComment("The maximum distance to check for a tree trunk when breaking roots")
    public int root_distance = 6;

    @ConfigComment("The maximum distance between disconnected trunk sections, connected by leaves")
    public int disconnected_trunk_distance = 0;

    @ConfigComment("The maximum distance from the trunk that leaves will be searched for")
    public int leaf_detect_range = 6;

    @ConfigComment("Allow leaves to be detected diagonally")
    public boolean diagonal_leaves = false;

    @ConfigComment("Special detection rules for block data / metadata")
    public BlockDataRules block_data_rules = new BlockDataRules();

    @ConfigComment("Decorations that should be considered part of the tree")
    public DecorationConfiguration[] decorations;

    public static class BlockDataRules{
        @ConfigComment("When scanning tree trunks, avoid connecting parallel adjacent tree trunks. This may cause issues with 2x2 trees or trees with branches.")
        public boolean ignore_parallel_trunk_pillars = false;

        @ConfigComment("Use leaf block data (distance) to speed up leaf detection")
        public boolean use_leaf_distance = true;

    }
}
