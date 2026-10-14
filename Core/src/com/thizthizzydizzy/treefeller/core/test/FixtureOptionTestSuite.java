package com.thizthizzydizzy.treefeller.core.test;
import com.thizthizzydizzy.mcautotester.mod.core.TestContext;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockAxis;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
import java.util.*;
/** Shared intact specimens, client mining, block assertions, and one final camera view. */
public final class FixtureOptionTestSuite{
    private final OptionTestSuite suite;
    private final String option;
    private final List<Fixture> fixtures = new ArrayList<>();
    private final List<Runnable> cleanup = new ArrayList<>();
    private TestArena arena;
    private static final TestTrees.Tree SMALL_TREE = TestTrees.specimen(TestTrees.Kind.SPRUCE,4);
    public FixtureOptionTestSuite(String option){
        this.option = option;
        suite = new OptionTestSuite(option).lifecycle(this::prepare, this::finish, this::cleanup);
    }
    public Fixture add(String name, String config){
        Fixture f = new Fixture(4+(fixtures.size()%4)*16, 6+(fixtures.size()/4)*16);
        fixtures.add(f);
        String base = "trees = [{ trunk = [spruce_log], leaves = [spruce_leaves], roots = [oak_log] }]\n"
                +"global.criteria.required_leaves = null\n"
                +"global.detection.secondary_tree_verification = false\n"
                +"global.result.leaves_drop_chance = 0\n";
        suite.add(name, base+config, (ctx, hooks) -> {
            try{
                ctx.teleportPlayer(OptionTestSuite.PLAYER, f.x+f.cutX+.5, arena.floorY+1, f.z+f.cutZ-3.5, 0.0, 0.0);
                TestArena.onServer(ctx, () -> {hooks.preparePlayer(OptionTestSuite.PLAYER, true); return null;});
                f.before.run(ctx, hooks);
                TestArena.onServer(ctx, () -> {hooks.showTestStatus(OptionTestSuite.PLAYER, option+" | "+name+" | "+config.replace('\n', ' ')); return null;});
                ctx.breakBlock(f.x+f.cutX, arena.floorY+1+f.cutY, f.z+f.cutZ);
                if(f.allowed)arena.assertLogsRemoved(f.tree, f.x, f.z);
                else TestArena.onServer(ctx, () -> {
                    for(Map.Entry<Long, TestTrees.Block> e : f.tree.blocks.entrySet()){
                        long pos=e.getKey();
                        boolean cut=BlockPos.getX(pos)==f.cutX&&BlockPos.getY(pos)==f.cutY&&BlockPos.getZ(pos)==f.cutZ;
                        assertBlock(f, BlockPos.getX(pos), BlockPos.getY(pos), BlockPos.getZ(pos), cut?"air":e.getValue().material);
                    }
                    return null;
                });
                TestArena.onServer(ctx, () -> {
                    for(Probe probe : f.probes)assertBlock(f, probe.x, probe.y, probe.z, probe.expected);
                    return null;
                });
            }finally{
                cleanup(ctx, hooks);
            }
        });
        return f;
    }
    public void register(ITestPlatform hooks){suite.register(hooks);}
    public void cleanupWith(Runnable action){cleanup.add(action);}
    private void assertBlock(Fixture f, int x, int y, int z, String expected){
        if(!arena.world.isMaterial(f.x+x, arena.floorY+1+y, f.z+z, expected))
            throw new AssertionError("Expected "+expected+" at fixture "+f.x+","+f.z+" relative "+x+","+y+","+z+"; got "+arena.world.blockState(f.x+x,arena.floorY+1+y,f.z+z));
    }
    private void prepare(TestContext ctx, ITestPlatform hooks){
        int size=Math.max(32, Math.max(Math.min(4, fixtures.size())*16+8, ((fixtures.size()+3)/4)*16+12));
        arena=TestArena.setup(ctx, hooks, OptionTestSuite.PLAYER, -4, -6, size, 99);
        Map<TestTrees.Tree,List<int[]>> groups=new LinkedHashMap<>();
        for(Fixture f : fixtures)groups.computeIfAbsent(f.tree, t -> new ArrayList<>()).add(new int[]{f.x,f.z});
        for(Map.Entry<TestTrees.Tree,List<int[]>> e : groups.entrySet())arena.spawnAll(e.getKey(), e.getValue().toArray(new int[0][]));
        TestArena.onServer(ctx, () -> {
            for(Fixture f : fixtures)for(Probe b : f.blocks)arena.world.setBlock(f.x+b.x, arena.floorY+1+b.y, f.z+b.z, b.material, b.axis, b.distance);
            return null;
        });
    }
    private void cleanup(TestContext ctx, ITestPlatform hooks){
        TestArena.onServer(ctx, () -> {for(Runnable action : cleanup)action.run(); cleanup.clear(); return null;});
    }
    private void finish(TestContext ctx, ITestPlatform hooks){
        TestArena.onServer(ctx, () -> {hooks.prepareCamera(OptionTestSuite.PLAYER); return null;});
        ctx.teleportPlayer(OptionTestSuite.PLAYER, arena.minX+arena.size/2.0, arena.floorY+36, arena.minZ-8.5, 0.0, 60.0);
        ctx.waitTicks(40);
    }
    public static final class Fixture{
        final int x,z;
        public TestTrees.Tree tree=SMALL_TREE;
        public int cutX,cutY,cutZ;
        public boolean allowed=true;
        public OptionTestSuite.Test before=(ctx,hooks) -> {};
        final List<Probe> blocks=new ArrayList<>(), probes=new ArrayList<>();
        Fixture(int x,int z){this.x=x;this.z=z;}
        public Fixture block(int x,int y,int z,String material,BlockAxis axis,int distance,String expected){
            Probe b=new Probe(x,y,z,material,axis,distance,expected); blocks.add(b); probes.add(b); return this;
        }
        public Fixture expect(int x,int y,int z,String expected){probes.add(new Probe(x,y,z,null,null,-1,expected)); return this;}
    }
    private static final class Probe{
        final int x,y,z,distance; final String material,expected; final BlockAxis axis;
        Probe(int x,int y,int z,String material,BlockAxis axis,int distance,String expected){
            this.x=x;this.y=y;this.z=z;this.material=material;this.axis=axis;this.distance=distance;this.expected=expected;
        }
    }
}
