package com.thizthizzydizzy.treefeller.core.test.options.criteria;
import com.thizthizzydizzy.mcautotester.mod.core.TestContext;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
import com.thizthizzydizzy.treefeller.core.test.OptionTestSuite;
import com.thizthizzydizzy.treefeller.core.test.TestArena;
import com.thizthizzydizzy.treefeller.core.test.TestTrees;
import java.util.ArrayList;
import java.util.List;
/** Four independent cuts cover base, middle, and top using limits zero and four. */
public final class MaxHeightTests{
    private final TestTrees.Tree tree = TestTrees.specimen(TestTrees.Kind.SPRUCE, 8);
    private final List<Long> temporarySupports = new ArrayList<>();
    private TestArena arena;
    private MaxHeightTests(){}
    public static void register(ITestPlatform platform){
        MaxHeightTests tests = new MaxHeightTests();
        OptionTestSuite suite = new OptionTestSuite("criteria.max-height")
                .lifecycle(tests::prepare, tests::finish, tests::cleanup);
        tests.add(suite, 0, "base", 0, 0);
        tests.add(suite, 0, "middle", 4, 8);
        tests.add(suite, 4, "middle", 4, 16);
        tests.add(suite, 4, "top", 7, 24);
        suite.register(platform);
    }
    private void prepare(TestContext ctx, ITestPlatform platform){
        arena = TestArena.setup(ctx, platform, OptionTestSuite.PLAYER, -4, -6, 32, 99);
        int[][] origins = {{0, 2}, {8, 2}, {16, 2}, {24, 2}};
        arena.spawnAll(tree, origins);
        ctx.log("Spruce trees placed: max=0 base/middle, then max=4 middle/top");
    }
    private void add(OptionTestSuite suite, int maximum, String cut, int cutHeight, int x){
        suite.add("max-"+maximum+"-"+cut, "global.criteria.max-height = "+maximum, (ctx, platform) -> {
            cleanup(ctx, platform);
            boolean allowed = cutHeight<=maximum;
            int cutY = arena.floorY+1+cutHeight;
            int feetY = Math.max(arena.floorY+1, cutY-1);
            TestArena.onServer(ctx, () -> {
                // Four blocks from the trunk is outside the spruce's two-block canopy.
                // The arena floor already supports base cuts. Only fill air for high cuts.
                if(arena.world.isMaterial(x, feetY-1, -2, "air")){
                    arena.world.setBlock(x, feetY-1, -2, "grass_block", null, -1);
                    temporarySupports.add(BlockPos.toPos(x, feetY-1, -2));
                }
                platform.preparePlayer(OptionTestSuite.PLAYER, true);
                return null;
            });
            ctx.teleportPlayer(OptionTestSuite.PLAYER, x+0.5, feetY, -1.5, 0.0, 0.0);
            TestArena.onServer(ctx, () -> {
                platform.showTestStatus(OptionTestSuite.PLAYER,
                        "max-height="+maximum+" | cutting "+cut+" (height "+cutHeight+") | expect "+(allowed?"fell":"refuse"));
                return null;
            });
            ctx.log("Cutting "+cut+" at height "+cutHeight+", max-height="+maximum+"; expect "+(allowed?"felling":"only the manually broken log"));
            ctx.breakBlock(x, cutY, 2); // The client mines obstructing leaves normally.
            if(allowed)arena.assertLogsRemoved(tree, x, 2);
            else arena.assertOnlyLogRemoved(tree, x, 2, 0, cutHeight, 0);
        });
    }
    private void cleanup(TestContext ctx, ITestPlatform platform){
        if(arena==null||temporarySupports.isEmpty())return;
        TestArena.onServer(ctx, () -> {
            for(long pos : temporarySupports){
                arena.world.setBlock(BlockPos.getX(pos), BlockPos.getY(pos), BlockPos.getZ(pos), "air", null, -1);
                if(!arena.world.isMaterial(BlockPos.getX(pos), BlockPos.getY(pos), BlockPos.getZ(pos), "air"))
                    throw new AssertionError("Temporary standing block was not removed");
            }
            temporarySupports.clear();
            return null;
        });
    }
    private void finish(TestContext ctx, ITestPlatform platform){
        TestArena.onServer(ctx, () -> {
            platform.prepareCamera(OptionTestSuite.PLAYER);
            return null;
        });
        ctx.log("One final camera check; all temporary standing blocks removed");
        ctx.teleportPlayer(OptionTestSuite.PLAYER, 12.5, arena.floorY+15, -5.5, 0.0, 55.0);
        ctx.waitTicks(40);
    }
}
