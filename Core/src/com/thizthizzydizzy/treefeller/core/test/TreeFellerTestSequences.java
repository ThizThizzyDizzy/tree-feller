package com.thizthizzydizzy.treefeller.core.test;
public class TreeFellerTestSequences{
    public static void register(ITestPlatform platform){
        com.thizthizzydizzy.treefeller.core.test.options.result.EnchantmentTests.register(platform);
        com.thizthizzydizzy.treefeller.core.test.options.result.DropChanceTests.register(platform);
        com.thizthizzydizzy.treefeller.core.test.options.result.DropConversionsTests.register(platform);
        com.thizthizzydizzy.treefeller.core.test.options.result.DropToInventoryTests.register(platform);
        com.thizthizzydizzy.treefeller.core.test.options.result.ConsumedResourceTests.register(platform);
        com.thizthizzydizzy.treefeller.core.test.options.cutting.LeaveStumpTests.register(platform);
        com.thizthizzydizzy.treefeller.core.test.options.cutting.LeafBreakRangeTests.register(platform);
        com.thizthizzydizzy.treefeller.core.test.options.cutting.PartialTrunkLimitTests.register(platform);
        com.thizthizzydizzy.treefeller.core.test.options.cutting.TrunkDamageMultTests.register(platform);
        com.thizthizzydizzy.treefeller.core.test.options.cutting.LeavesDamageMultTests.register(platform);
        com.thizthizzydizzy.treefeller.core.test.options.cutting.DecorationsDamageMultTests.register(platform);
        com.thizthizzydizzy.treefeller.core.test.options.cutting.RespectUnbreakingTests.register(platform);
        com.thizthizzydizzy.treefeller.core.test.options.cutting.RespectUnbreakableTests.register(platform);
        com.thizthizzydizzy.treefeller.core.test.options.cutting.StackedToolsTests.register(platform);
        com.thizthizzydizzy.treefeller.core.test.options.cutting.PreventBreakageTests.register(platform);
        com.thizthizzydizzy.treefeller.core.test.options.cutting.AllowPartialTests.register(platform);
        com.thizthizzydizzy.treefeller.core.test.options.cutting.AllowPartialToolTests.register(platform);
        com.thizthizzydizzy.treefeller.core.test.options.cutting.CuttingAnimationTests.register(platform);
        com.thizthizzydizzy.treefeller.core.test.options.cutting.AnimationDelayTests.register(platform);

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
