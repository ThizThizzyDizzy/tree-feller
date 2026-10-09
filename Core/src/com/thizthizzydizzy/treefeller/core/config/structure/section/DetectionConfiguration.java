package com.thizthizzydizzy.treefeller.core.config.structure.section;
import com.thizthizzydizzy.treefeller.core.config.legacy.LegacyBukkitOption;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
import com.thizthizzydizzy.treefeller.core.config.globaldefault.ConfigGlobalDefaultBoolean;
import com.thizthizzydizzy.treefeller.core.config.globaldefault.ConfigGlobalDefaultInteger;
import com.thizthizzydizzy.treefeller.core.config.globaldefault.ConfigGlobalDefaultNewInstance;
import com.thizthizzydizzy.treefeller.core.config.structure.section.detection.DecorationConfiguration;
public class DetectionConfiguration{
    @ConfigComment("The maximum distance to check for a tree trunk when breaking roots")
    @ConfigGlobalDefaultInteger(16)
    @LegacyBukkitOption(value = "root-distance",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TREE})
    public Integer root_distance;

    @ConfigComment("The maximum distance between disconnected trunk sections, connected by leaves")
    @ConfigGlobalDefaultInteger(0)
    public Integer disconnected_trunk_distance;
    
    @ConfigComment("The maximum vertical distance between the topmost trunk block and where leaves may detected from connected trunks. This does not affect leaves detected from other leaves.")
    public Integer max_leaf_distance_from_top;

    @ConfigComment("The maximum distance from the trunk that leaves will be searched for")
    @ConfigGlobalDefaultInteger(6)
    @LegacyBukkitOption(value = "leaf-detect-range",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE})
    public Integer leaf_detect_range;

    @ConfigComment("Allow leaves to be detected diagonally")
    @ConfigGlobalDefaultBoolean(false)
    @LegacyBukkitOption(value = "diagonal-leaves",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TREE})
    public Boolean diagonal_leaves;

    @ConfigComment("Special detection rules for block data / metadata")
    @ConfigGlobalDefaultNewInstance
    @LegacyBukkitOption(value = "ignore-leaf-data",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TREE},
            conversion = LegacyBukkitOption.Conversion.INVERSE,
            suffix = ".use_leaf_distance")
    public BlockDataRules block_data_rules;
    
    @ConfigComment("Perform secondary tree scans to verify leaf/root ownership and improve edge case handling. (In some cases, this may significantly increase the performance cost for a comparitively small benefit)")
    @ConfigGlobalDefaultBoolean(true)
    public Boolean secondary_tree_verification;

    @ConfigComment("Decorations that should be considered part of a tree")
    @LegacyBukkitOption(value = "decorations",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE},
            conversion = LegacyBukkitOption.Conversion.DECORATIONS)
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
