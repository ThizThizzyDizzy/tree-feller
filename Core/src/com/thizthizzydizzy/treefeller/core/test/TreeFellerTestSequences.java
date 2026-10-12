package com.thizthizzydizzy.treefeller.core.test;
public class TreeFellerTestSequences{
    public static void register(ITestPlatform platform){
        com.thizthizzydizzy.treefeller.core.test.options.criteria.MaxHeightTests.register(platform);
        com.thizthizzydizzy.treefeller.core.test.options.criteria.RequiredTrunkTests.register(platform);
        com.thizthizzydizzy.treefeller.core.test.options.criteria.RequiredLeavesTests.register(platform);
        com.thizthizzydizzy.treefeller.core.test.options.criteria.RequireCrossSectionTests.register(platform);
        com.thizthizzydizzy.treefeller.core.test.options.criteria.TrunkFilterTests.register(platform);
        com.thizthizzydizzy.treefeller.core.test.options.criteria.LeafFilterTests.register(platform);
        com.thizthizzydizzy.treefeller.core.test.options.criteria.DecorationFilterTests.register(platform);
        com.thizthizzydizzy.treefeller.core.test.options.criteria.TrunkMaxHorizontalLineTests.register(platform);
        com.thizthizzydizzy.treefeller.core.test.options.criteria.HeightRatioTests.register(platform);
        com.thizthizzydizzy.treefeller.core.test.options.criteria.TrunkVerticalRatioTests.register(platform);
        com.thizthizzydizzy.treefeller.core.test.options.trigger.CooldownTests.register(platform);
        com.thizthizzydizzy.treefeller.core.test.options.trigger.RequiredLevelTests.register(platform);
        com.thizthizzydizzy.treefeller.core.test.options.trigger.GameModeTests.register(platform);
        com.thizthizzydizzy.treefeller.core.test.options.trigger.SneakingTests.register(platform);
        com.thizthizzydizzy.treefeller.core.test.options.trigger.WorldRequirementTests.register(platform);
    }
}
