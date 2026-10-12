package com.thizthizzydizzy.treefeller.core.test.options.criteria;
import com.thizthizzydizzy.mcautotester.mod.core.TestContext;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
import com.thizthizzydizzy.treefeller.core.test.OptionTestSuite;
import com.thizthizzydizzy.treefeller.core.test.TestArena;
import com.thizthizzydizzy.treefeller.core.test.TestTrees;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
/** Shared placement and real client cuts; every case uses its own intact specimen. */
final class CriteriaTestSuite{
    private final OptionTestSuite suite;
    private final List<Fixture> fixtures = new ArrayList<>();
    private TestArena arena;
    CriteriaTestSuite(String option){
        suite = new OptionTestSuite("criteria."+option).lifecycle(this::prepare, this::finish, (ctx, platform) -> {});
    }
    CriteriaTestSuite add(String name, String config, TestTrees.Tree tree, boolean allowed){
        return add(name, config, tree, allowed, null);
    }
    CriteriaTestSuite add(String name, String config, TestTrees.Tree tree, boolean allowed, String decoration){
        int x = fixtures.size()*10;
        fixtures.add(new Fixture(tree, x, decoration));
        suite.add(name, config, (ctx, platform) -> {
            ctx.teleportPlayer(OptionTestSuite.PLAYER, x+0.5, arena.floorY+1, -1.5, 0.0, 0.0);
            TestArena.onServer(ctx, () -> {
                platform.preparePlayer(OptionTestSuite.PLAYER, true);
                platform.showTestStatus(OptionTestSuite.PLAYER,
                        config.substring(config.lastIndexOf("global.criteria."))+" | "+name+" | expect "+(allowed?"fell":"refuse"));
                return null;
            });
            ctx.breakBlock(x, arena.floorY+1, 2);
            if(allowed)arena.assertLogsRemoved(tree, x, 2);
            else{
                arena.assertOnlyLogRemoved(tree, x, 2, 0, 0, 0);
                if(decoration!=null)TestArena.onServer(ctx, () -> {
                    if(!arena.world.isMaterial(x+1, arena.floorY+2, 2, decoration))
                        throw new AssertionError("Refused cut removed the decoration");
                    return null;
                });
            }
        });
        return this;
    }
    void register(ITestPlatform platform){suite.register(platform);}
    private void prepare(TestContext ctx, ITestPlatform platform){
        arena = TestArena.setup(ctx, platform, OptionTestSuite.PLAYER, -5, -6, Math.max(22, fixtures.size()*10+2), 99);
        Map<TestTrees.Tree, List<int[]>> groups = new LinkedHashMap<>();
        for(Fixture fixture : fixtures)groups.computeIfAbsent(fixture.tree, tree -> new ArrayList<>()).add(new int[]{fixture.x, 2});
        for(Map.Entry<TestTrees.Tree, List<int[]>> group : groups.entrySet())
            arena.spawnAll(group.getKey(), group.getValue().toArray(new int[0][]));
        TestArena.onServer(ctx, () -> {
            for(Fixture fixture : fixtures)if(fixture.decoration!=null)
                arena.world.setBlock(fixture.x+1, arena.floorY+2, 2, fixture.decoration, null, -1);
            return null;
        });
    }
    private void finish(TestContext ctx, ITestPlatform platform){
        TestArena.onServer(ctx, () -> {platform.prepareCamera(OptionTestSuite.PLAYER); return null;});
        ctx.teleportPlayer(OptionTestSuite.PLAYER, (fixtures.size()-1)*5+0.5, arena.floorY+20, -5.5, 0.0, 65.0);
        ctx.waitTicks(40);
    }
    private static final class Fixture{
        final TestTrees.Tree tree;
        final int x;
        final String decoration;
        Fixture(TestTrees.Tree tree, int x, String decoration){this.tree = tree; this.x = x; this.decoration = decoration;}
    }
}
