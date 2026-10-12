package com.thizthizzydizzy.treefeller.core.test;
import com.thizthizzydizzy.mcautotester.mod.api.MCAutoTesterAPI;
public class TreeFellerTestSequences{
    public static void register(ITestPlatform platform){
        com.thizthizzydizzy.treefeller.core.test.options.criteria.MaxHeightTests.register(platform);
        com.thizthizzydizzy.treefeller.core.test.options.trigger.CooldownTests.register(platform);
        com.thizthizzydizzy.treefeller.core.test.options.trigger.RequiredLevelTests.register(platform);
        com.thizthizzydizzy.treefeller.core.test.options.trigger.GameModeTests.register(platform);
        com.thizthizzydizzy.treefeller.core.test.options.trigger.SneakingTests.register(platform);
        com.thizthizzydizzy.treefeller.core.test.options.trigger.WorldRequirementTests.register(platform);
        MCAutoTesterAPI.register("treefeller-acacia", ctx -> {
            ctx.waitForJoin("testplayer");
            TestArena arena = TestArena.setup(ctx, platform, "testplayer");
            TestTrees.Tree tree = TestTrees.specimen(TestTrees.Kind.ACACIA);
            arena.spawn(tree, 0, 2);
            ctx.teleportPlayer("testplayer", 0, arena.floorY+1, -7.5, 0.0, -20.0);
            ctx.waitTicks(40); // Record the complete starting shape.
            ctx.teleportPlayer("testplayer", 0.5, arena.floorY+1, 0.5, 0.0, 0.0);
            TestArena.onServer(ctx, () -> {platform.preparePlayer("testplayer", true); return null;});
            ctx.waitTicks(40); // Render the fixed tree and equipped axe.
            ctx.log("Breaking the second trunk block with the iron axe");
            ctx.breakBlock(0, arena.floorY+2, 2);
            ctx.log("Moving the camera to the middle of the back edge");
            ctx.teleportPlayer("testplayer", 0, arena.floorY+1, -7.5, 0.0, -20.0);
            ctx.waitTicks(60);
            arena.assertLogsRemoved(tree, 0, 2);
            ctx.success();
        }, true);
    }
}
