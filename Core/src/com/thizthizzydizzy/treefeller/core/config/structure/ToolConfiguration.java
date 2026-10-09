package com.thizthizzydizzy.treefeller.core.config.structure;
import com.thizthizzydizzy.treefeller.core.config.legacy.LegacyBukkitOption;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
import com.thizthizzydizzy.treefeller.core.config.structure.general.DualList;
import com.thizthizzydizzy.treefeller.core.config.structure.section.BreakingConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.CriteriaConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.CuttingConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.ResultConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.TriggerConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IItemDefinition;
public class ToolConfiguration{
    @ConfigComment("The item to match for this tool")
    @LegacyBukkitOption(value = "min-durability",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE},
            conversion = LegacyBukkitOption.Conversion.INTEGER,
            suffix = ".durability.value.min")
    @LegacyBukkitOption(value = "max-durability",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE},
            conversion = LegacyBukkitOption.Conversion.INTEGER,
            suffix = ".durability.value.max")
    @LegacyBukkitOption(value = "min-durability-percent",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE},
            conversion = LegacyBukkitOption.Conversion.FLOAT32,
            suffix = ".durability.percent.min")
    @LegacyBukkitOption(value = "max-durability-percent",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE},
            conversion = LegacyBukkitOption.Conversion.FLOAT32,
            suffix = ".durability.percent.max")
    @LegacyBukkitOption(value = "required-name",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE},
            conversion = LegacyBukkitOption.Conversion.LIST,
            suffix = ".custom_name.whitelist")
    @LegacyBukkitOption(value = "custom-model-data",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE},
            conversion = LegacyBukkitOption.Conversion.LIST,
            suffix = ".custom_model_data.whitelist")
    public IItemDefinition item;

    @ConfigComment("The indicies of trees that this tool may cut down")
    @LegacyBukkitOption(value = "allowed-trees",
            scopes = {LegacyBukkitOption.Scope.TOOL},
            conversion = LegacyBukkitOption.Conversion.TREE_SELECTOR,
            suffix = ".whitelist")
    public DualList<Integer> trees;

    public TriggerConfiguration trigger;
    public CriteriaConfiguration criteria;
    public CuttingConfiguration cutting;
    public BreakingConfiguration breaking;
    public ResultConfiguration result;
}
