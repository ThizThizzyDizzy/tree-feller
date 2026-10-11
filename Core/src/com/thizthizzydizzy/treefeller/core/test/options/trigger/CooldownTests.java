package com.thizthizzydizzy.treefeller.core.test.options.trigger;
import com.thizthizzydizzy.mcautotester.mod.core.TestContext;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
import com.thizthizzydizzy.treefeller.core.test.OptionTestSuite;
import com.thizthizzydizzy.treefeller.core.test.TestArena;
import com.thizthizzydizzy.treefeller.core.test.TestTrees;
/** Consecutive cuts with cooldown disabled, then refusal and expiry with it enabled. */
public final class CooldownTests{
    private static final int COOLDOWN = 100;
    private final TestTrees.Tree tree = TestTrees.specimen(TestTrees.Kind.SPRUCE);
    private TestArena arena;
    private CooldownTests(){}
    public static void register(ITestPlatform platform){
        CooldownTests tests = new CooldownTests();
        new OptionTestSuite("trigger.cooldown")
                .lifecycle(tests::prepare, tests::finish, (ctx, hooks) -> {})
                .add("disabled", "global.trigger.cooldown = 0", (ctx, hooks) -> {
                    tests.cut(ctx, hooks, 0, 0, "first fell", true);
                    tests.cut(ctx, hooks, 8, 0, "consecutive fell without waiting", true);
                })
                .add("enabled", "global.trigger.cooldown = "+COOLDOWN, (ctx, hooks) -> {
                    tests.cut(ctx, hooks, 16, COOLDOWN, "first fell starts cooldown", true);
                    tests.cut(ctx, hooks, 24, COOLDOWN, "consecutive fell during cooldown", false);
                    ctx.waitTicks(COOLDOWN); // Allow the real per-player cooldown to expire.
                    tests.cut(ctx, hooks, 32, COOLDOWN, "fell after cooldown expires", true);
                })
                .register(platform);
    }
    private void prepare(TestContext ctx, ITestPlatform platform){
        arena = TestArena.setup(ctx, platform, OptionTestSuite.PLAYER, -4, -6, 40, 99);
        arena.spawnAll(tree, new int[][]{{0, 2}, {8, 2}, {16, 2}, {24, 2}, {32, 2}});
    }
    private void cut(TestContext ctx, ITestPlatform platform, int x, int cooldown, String action, boolean allowed){
        TestArena.onServer(ctx, () -> {
            platform.preparePlayer(OptionTestSuite.PLAYER, true);
            return null;
        });
        ctx.teleportPlayer(OptionTestSuite.PLAYER, x+0.5, arena.floorY+1, -1.5, 0.0, 0.0);
        TestArena.onServer(ctx, () -> {
            platform.showTestStatus(OptionTestSuite.PLAYER,
                    "cooldown="+cooldown+" ticks | "+action+" | expect "+(allowed?"fell":"refuse"));
            return null;
        });
        ctx.breakBlock(x, arena.floorY+1, 2);
        if(allowed)arena.assertLogsRemoved(tree, x, 2);
        else arena.assertOnlyLogRemoved(tree, x, 2, 0, 0, 0);
    }
    private void finish(TestContext ctx, ITestPlatform platform){
        TestArena.onServer(ctx, () -> {
            platform.prepareCamera(OptionTestSuite.PLAYER);
            return null;
        });
        ctx.teleportPlayer(OptionTestSuite.PLAYER, 16.5, arena.floorY+20, -5.5, 0.0, 60.0);
        ctx.waitTicks(40);
    }
}
