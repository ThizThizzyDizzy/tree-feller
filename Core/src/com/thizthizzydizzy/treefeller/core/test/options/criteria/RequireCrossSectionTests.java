package com.thizthizzydizzy.treefeller.core.test.options.criteria;
import com.thizthizzydizzy.mcautotester.mod.core.TestContext;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
import com.thizthizzydizzy.treefeller.core.test.OptionTestSuite;
import com.thizthizzydizzy.treefeller.core.test.TestArena;
import com.thizthizzydizzy.treefeller.core.test.TestTrees;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
import java.util.Map;
public final class RequireCrossSectionTests{
    private final TestTrees.Tree wide = TestTrees.specimen(TestTrees.Kind.DARK_OAK);
    private final TestTrees.Tree narrow = TestTrees.specimen(TestTrees.Kind.SPRUCE);
    private TestArena arena;
    private RequireCrossSectionTests(){}
    public static void register(ITestPlatform platform){
        RequireCrossSectionTests tests = new RequireCrossSectionTests();
        new OptionTestSuite("criteria.require-cross-section")
                .lifecycle(tests::prepare, tests::finish, (ctx, hooks) -> {})
                .add("disabled", "global.criteria.require-cross-section = false", (ctx, hooks) -> {
                    tests.cut(ctx, hooks, 0, 0, 0, false, true);
                    tests.arena.assertLogsRemoved(tests.wide, 0, 2);
                })
                .add("single-log", "global.criteria.require-cross-section = true", (ctx, hooks) -> {
                    tests.cut(ctx, hooks, 10, 0, 0, true, true);
                    tests.arena.assertLogsRemoved(tests.narrow, 10, 2);
                })
                .add("full-section", "global.criteria.require-cross-section = true", (ctx, hooks) -> {
                    int[][] cuts = {{0,0}, {1,0}, {0,1}, {1,1}};
                    for(int i = 0; i<cuts.length; i++){
                        tests.cut(ctx, hooks, 20, cuts[i][0], cuts[i][1], true, i==3);
                        if(i==3)tests.arena.assertLogsRemoved(tests.wide, 20, 2);
                        else tests.assertRemaining(ctx, cuts, i);
                    }
                }).register(platform);
    }
    private void prepare(TestContext ctx, ITestPlatform platform){
        arena = TestArena.setup(ctx, platform, OptionTestSuite.PLAYER, -5, -6, 30, 99);
        arena.spawnAll(wide, new int[][]{{0,2}, {20,2}});
        arena.spawn(narrow, 10, 2);
    }
    private void cut(TestContext ctx, ITestPlatform platform, int x, int dx, int dz, boolean required, boolean allowed){
        ctx.teleportPlayer(OptionTestSuite.PLAYER, x+dx+0.5, arena.floorY+1, dz==0?-1.5:6.5, dz==0?0.0:180.0, 0.0);
        TestArena.onServer(ctx, () -> {
            platform.preparePlayer(OptionTestSuite.PLAYER, true);
            platform.showTestStatus(OptionTestSuite.PLAYER, "require-cross-section="+required+" | base log "+dx+","+dz+" | expect "+(allowed?"fell":"refuse"));
            return null;
        });
        ctx.breakBlock(x+dx, arena.floorY+1, 2+dz);
    }
    private void assertRemaining(TestContext ctx, int[][] cuts, int last){
        TestArena.onServer(ctx, () -> {
            for(Map.Entry<Long, TestTrees.Block> entry : wide.blocks.entrySet()){
                if(entry.getValue().axis==null)continue;
                long pos = entry.getKey();
                boolean removed = false;
                for(int i = 0; i<=last; i++)if(pos==BlockPos.toPos(cuts[i][0], 0, cuts[i][1]))removed = true;
                if(!arena.world.isMaterial(20+BlockPos.getX(pos), arena.floorY+1+BlockPos.getY(pos), 2+BlockPos.getZ(pos),
                        removed?"air":entry.getValue().material))
                    throw new AssertionError("Partial cross-section cut changed an unexpected log at "+pos);
            }
            return null;
        });
    }
    private void finish(TestContext ctx, ITestPlatform platform){
        TestArena.onServer(ctx, () -> {platform.prepareCamera(OptionTestSuite.PLAYER); return null;});
        ctx.teleportPlayer(OptionTestSuite.PLAYER, 10.5, arena.floorY+20, -5.5, 0.0, 65.0);
        ctx.waitTicks(40);
    }
}
