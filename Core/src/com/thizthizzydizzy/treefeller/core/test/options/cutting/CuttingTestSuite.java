package com.thizthizzydizzy.treefeller.core.test.options.cutting;
import com.thizthizzydizzy.mcautotester.mod.core.TestContext;
import com.thizthizzydizzy.treefeller.core.connector.item.IItemConnector;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
import com.thizthizzydizzy.treefeller.core.test.*;
import java.util.Map;
/** Independent specimens, shared setup, real client actions and server-side outcome checks. */
final class CuttingTestSuite{
    private final OptionTestSuite suite;
    private final TestTrees.Tree tree = TestTrees.specimen(TestTrees.Kind.SPRUCE);
    private TestArena arena;
    private int fixtures;
    private final String option;
    CuttingTestSuite(String option){
        this.option = option;
        suite = new OptionTestSuite("cutting."+option).lifecycle(this::prepare, this::finish, (ctx, hooks) -> {});
    }
    // remainingLogs: -1 means refused, otherwise exact number of surviving logs.
    CuttingTestSuite add(String name, String config, String item, int cutHeight, int remainingLogs, int damage, int count){
        return add(name, config, item, cutHeight, remainingLogs, damage, count, null, 0);
    }
    CuttingTestSuite add(String name, String config, String item, int cutHeight, int remainingLogs, int damage, int count, Integer leafRange, int delay){
        int x = fixtures++*8;
        suite.add(name, "global.detection.decorations = [{ blocks = [glowstone], direction = SIDE }]\n"+config, (ctx, hooks) -> {
            TestArena.onServer(ctx, () -> {
                hooks.preparePlayer(OptionTestSuite.PLAYER, true);
                hooks.equipItem(OptionTestSuite.PLAYER, item);
                if(option.equals("stacked-tools"))hooks.setToolCount(OptionTestSuite.PLAYER, 2);
                return null;
            });
            ctx.teleportPlayer(OptionTestSuite.PLAYER, x+0.5, arena.floorY+1, -1.5, 0.0, 0.0);
            TestArena.onServer(ctx, () -> {
                hooks.showTestStatus(OptionTestSuite.PLAYER, "cutting."+option+" | "+config.replace("global.cutting.", "").replace('\n', ';')
                        +" | "+name+" | cut ("+x+","+(arena.floorY+1+cutHeight)+",2), height="+cutHeight);
                return null;
            });
            ctx.breakBlock(x, arena.floorY+1+cutHeight, 2);
            if(delay>0){
                // Verify the next layer stays intact before the delay and breaks after it.
                assertMaterial(ctx, x, 7, "spruce_log", true);
                assertMaterial(ctx, x+1, 0, "glowstone", false);
                ctx.waitTicks(delay/2);
                assertMaterial(ctx, x, 1, "spruce_log", true);
                ctx.waitTicks(delay/2+2);
                assertMaterial(ctx, x, 1, "spruce_log", false);
                assertMaterial(ctx, x, 7, "spruce_log", true);
                ctx.waitTicks(delay*6);
            }
            if(remainingLogs==-1)arena.assertOnlyLogRemoved(tree, x, 2, 0, cutHeight, 0);
            else TestArena.onServer(ctx, () -> {
                int remaining = 0;
                for(int y = 0; y<8; y++)if(arena.world.isMaterial(x, arena.floorY+1+y, 2, "spruce_log"))remaining++;
                if(remaining!=remainingLogs)throw new AssertionError("Expected "+remainingLogs+" remaining logs, got "+remaining);

                if(option.equals("leave-stump"))for(int y = 0; y<8; y++){
                    boolean expected = remainingLogs>0&&y<cutHeight;
                    if(arena.world.isMaterial(x, arena.floorY+1+y, 2, "spruce_log")!=expected)
                        throw new AssertionError("Incorrect stump at height "+y);
                }
                if(remainingLogs>0&&!option.equals("leave-stump"))for(int y = 0; y<8; y++){
                    if(arena.world.isMaterial(x, arena.floorY+1+y, 2, "spruce_log")!=(y>=8-remainingLogs))
                        throw new AssertionError("Incorrect partial cut at height "+y);
                }
                return null;
            });
            if(leafRange!=null||remainingLogs<=0||option.equals("leave-stump"))TestArena.onServer(ctx, () -> {
                for(Map.Entry<Long, TestTrees.Block> entry : tree.blocks.entrySet()){
                    TestTrees.Block block = entry.getValue();
                    if(block.axis!=null)continue;
                    long pos = entry.getKey();
                    boolean remains = remainingLogs==-1||(leafRange!=null&&block.leafDistance>leafRange);
                    if(arena.world.isMaterial(x+BlockPos.getX(pos), arena.floorY+1+BlockPos.getY(pos), 2+BlockPos.getZ(pos), block.material)!=remains)
                        throw new AssertionError("Incorrect leaf selection at "+BlockPos.getX(pos)+","+BlockPos.getY(pos)+","+BlockPos.getZ(pos));
                }
                return null;
            });
            if(option.equals("leave-stump"))assertMaterial(ctx, x+1, 0, "glowstone", remainingLogs>0);
            if(remainingLogs==-1)assertMaterial(ctx, x+1, 0, "glowstone", true);
            if(remainingLogs>=0&&!option.equals("leave-stump"))assertMaterial(ctx, x+1, 0, "glowstone", false);
            if(remainingLogs>0&&!option.equals("leave-stump")){
                assertMaterial(ctx, x+1, 2, "spruce_leaves", false);
                assertMaterial(ctx, x+1, 7, "spruce_leaves", true);
            }
            if(damage>=0)TestArena.onServer(ctx, () -> {
                IItemConnector tool = hooks.tool(OptionTestSuite.PLAYER);
                if(tool.getCount()!=count)throw new AssertionError("Expected tool count "+count+", got "+tool.getCount());
                if(count>0&&tool.getMaxDurability()-tool.getCurrentDurability()!=damage)
                    throw new AssertionError("Expected tool damage "+damage+", got "+(tool.getMaxDurability()-tool.getCurrentDurability()));
                return null;
            });
        });
        return this;
    }
    int leaves(){return (int)tree.blocks.values().stream().filter(b -> b.axis==null).count();}
    void register(ITestPlatform platform){suite.register(platform);}
    private void assertMaterial(TestContext ctx, int x, int y, String material, boolean present){
        TestArena.onServer(ctx, () -> {
            if(arena.world.isMaterial(x, arena.floorY+1+y, 2, material)!=present)
                throw new AssertionError("Expected "+material+" present="+present+" at "+x+","+y);
            return null;
        });
    }
    private void prepare(TestContext ctx, ITestPlatform hooks){
        arena = TestArena.setup(ctx, hooks, OptionTestSuite.PLAYER, -4, -6, Math.max(20, fixtures*8+4), 99);
        int[][] origins = new int[fixtures][2];
        for(int i = 0; i<fixtures; i++)origins[i] = new int[]{i*8, 2};
        arena.spawnAll(tree, origins);
        TestArena.onServer(ctx, () -> {
            for(int i = 0; i<fixtures; i++)arena.world.setBlock(i*8+1, arena.floorY+1, 2, "glowstone", null, -1);
            return null;
        });
    }
    private void finish(TestContext ctx, ITestPlatform hooks){
        TestArena.onServer(ctx, () -> {hooks.prepareCamera(OptionTestSuite.PLAYER); return null;});
        ctx.teleportPlayer(OptionTestSuite.PLAYER, (fixtures-1)*4+0.5, arena.floorY+20, -5.5, 0.0, 65.0);
        ctx.waitTicks(40);
    }
}
