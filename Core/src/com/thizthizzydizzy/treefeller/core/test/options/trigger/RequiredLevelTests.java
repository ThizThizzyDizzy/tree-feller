package com.thizthizzydizzy.treefeller.core.test.options.trigger;
import com.thizthizzydizzy.mcautotester.mod.core.TestContext;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
import com.thizthizzydizzy.treefeller.core.test.OptionTestSuite;
import com.thizthizzydizzy.treefeller.core.test.TestArena;
import com.thizthizzydizzy.treefeller.core.test.TestTrees;
/** One sequence per player requirement, covering both inclusive bounds and refusals. */
public final class RequiredLevelTests{
    private final String option;
    private final TestTrees.Tree tree = TestTrees.specimen(TestTrees.Kind.SPRUCE);
    private TestArena arena;
    private RequiredLevelTests(String option){this.option = option;}
    public static void register(ITestPlatform platform){
        for(String option : new String[]{"food", "health", "saturation"}){
            RequiredLevelTests tests = new RequiredLevelTests(option);
            new OptionTestSuite("trigger."+option)
                    .lifecycle(tests::prepare, tests::finish, (ctx, hooks) -> {})
                    .add("range", "global.trigger."+option+" = { min = 8, max = 12 }", (ctx, hooks) -> {
                        tests.cut(ctx, hooks, 0, 7, "below minimum", false);
                        tests.cut(ctx, hooks, 8, 8, "at minimum", true);
                        tests.cut(ctx, hooks, 16, 12, "at maximum", true);
                        tests.cut(ctx, hooks, 24, 13, "above maximum", false);
                    })
                    .register(platform);
        }
    }
    private void prepare(TestContext ctx, ITestPlatform platform){
        arena = TestArena.setup(ctx, platform, OptionTestSuite.PLAYER, -4, -6, 32, 99);
        arena.spawnAll(tree, new int[][]{{0, 2}, {8, 2}, {16, 2}, {24, 2}});
    }
    private void cut(TestContext ctx, ITestPlatform platform, int x, int level, String condition, boolean allowed){
        ctx.teleportPlayer(OptionTestSuite.PLAYER, x+0.5, arena.floorY+1, -1.5, 0.0, 0.0);
        TestArena.onServer(ctx, () -> {
            platform.preparePlayer(OptionTestSuite.PLAYER, true);
            // Food below the regeneration threshold keeps health stable while mining.
            // Full health and reset exhaustion keep food/saturation stable for their cuts.
            platform.setPlayerLevels(OptionTestSuite.PLAYER,
                    option.equals("food")?level:option.equals("health")?10:20,
                    option.equals("health")?level:20,
                    option.equals("saturation")?level:0);
            platform.showTestStatus(OptionTestSuite.PLAYER,
                    option+"=8..12 | player "+option+"="+level+" ("+condition+") | expect "+(allowed?"fell":"refuse"));
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
        ctx.teleportPlayer(OptionTestSuite.PLAYER, 12.5, arena.floorY+24, -16.5, 0.0, 50.0);
        ctx.waitTicks(40);
    }
}
