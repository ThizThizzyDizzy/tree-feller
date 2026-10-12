package com.thizthizzydizzy.treefeller.core.test.options.result;
import com.thizthizzydizzy.mcautotester.mod.core.TestContext;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
import com.thizthizzydizzy.treefeller.core.test.*;
import java.util.Map;
/** Result assertions after real client cuts, with intact specimens prepared together. */
final class ResultTestSuite{
    final TestTrees.Tree tree = TestTrees.specimen(TestTrees.Kind.SPRUCE);
    final OptionTestSuite suite;
    final String option, group;
    final String replacement;
    TestArena arena;
    int fixtures;
    ResultTestSuite(String option, String group, boolean melon){
        this(option, group, melon?"melon":null);
    }
    ResultTestSuite(String option, String group, String replacement){
        this.option = option;
        this.group = group;
        this.replacement = replacement;
        suite = new OptionTestSuite("result."+option).lifecycle(this::prepare, this::finish, (ctx, hooks) -> {});
    }
    int units(){
        if(group.equals("trunk"))return 8;
        if(group.equals("leaves"))return (int)tree.blocks.values().stream().filter(b -> b.axis==null).count();
        return group.equals("decoration")&&"gravel".equals(replacement)?8:1;
    }
    ResultTestSuite add(String name, String config, String item, OptionTestSuite.Test before, Check check){
        int x = fixtures++*8;
        String trunk = replacement!=null&&group.equals("trunk")?replacement:"spruce_log";
        String leaves = replacement!=null&&group.equals("leaves")?replacement:"spruce_leaves";
        String decoration = replacement!=null&&group.equals("decoration")?replacement:"glowstone";
        suite.add(name, "trees = [{ trunk = ["+trunk+"], leaves = ["+leaves+"] }]\n"
                +(replacement!=null&&group.equals("trunk")?"global.criteria.trunk-vertical-ratio = null\n":"")
                +"global.detection.block-data-rules.use-leaf-distance = false\n"
                +"global.detection.decorations = [{ blocks = ["+decoration+"], direction = SIDE }]\n"+config, (ctx, hooks) -> {
            ctx.teleportPlayer(OptionTestSuite.PLAYER, x+0.5, arena.floorY+1, -1.5, 0.0, 0.0);
            TestArena.onServer(ctx, () -> {
                hooks.preparePlayer(OptionTestSuite.PLAYER, true);
                hooks.equipItem(OptionTestSuite.PLAYER, item);
                hooks.setPlayerLevels(OptionTestSuite.PLAYER, 10, 10, 0);
                return null;
            });
            before.run(ctx, hooks);
            TestArena.onServer(ctx, () -> {
                hooks.showTestStatus(OptionTestSuite.PLAYER, "result."+option+" | "+name+" | "+config.replace("global.result.", "").replace('\n', ';'));
                return null;
            });
            ctx.breakBlock(x, arena.floorY+1, 2);
            TestArena.onServer(ctx, () -> {
                for(Map.Entry<Long, TestTrees.Block> entry : tree.blocks.entrySet()){
                    long pos = entry.getKey();
                    if(!arena.world.isMaterial(x+BlockPos.getX(pos), arena.floorY+1+BlockPos.getY(pos), 2+BlockPos.getZ(pos), "air"))
                        throw new AssertionError("Tree block remains at "+pos);
                }
                if(!arena.world.isMaterial(x+1, arena.floorY+1, 2, "air"))throw new AssertionError("Decoration remains");
                if(group.equals("decoration")&&"gravel".equals(replacement))
                    for(int y = 0; y<2; y++)for(int[] side : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}})
                        if(!arena.world.isMaterial(x+side[0], arena.floorY+1+y, 2+side[1], "air"))throw new AssertionError("Decoration remains");
                check.run(hooks, x);
                return null;
            });
        });
        return this;
    }
    ResultTestSuite drops(String name, String config, String item, String material, int min, int max, boolean inventory){
        return add(name, config, item, (ctx, hooks) -> {}, (hooks, x) -> {
            int carried = hooks.inventoryItemCount(OptionTestSuite.PLAYER, material);
            int dropped = arena.world.droppedItemCount(material, x-3, arena.floorY, -1, x+3, arena.floorY+12, 5);
            int count = inventory?carried:dropped;
            if(count<min||count>max)throw new AssertionError("Expected "+material+" count "+min+".."+max+", got "+count);
            if((inventory?dropped:carried)!=0)throw new AssertionError("Items delivered to incorrect destination");
        });
    }
    void register(ITestPlatform platform){suite.register(platform);}
    private void prepare(TestContext ctx, ITestPlatform hooks){
        arena = TestArena.setup(ctx, hooks, OptionTestSuite.PLAYER, -4, -6, Math.max(20, fixtures*8+4), 99);
        int[][] origins = new int[fixtures][2];
        for(int i = 0; i<fixtures; i++)origins[i] = new int[]{i*8, 2};
        arena.spawnAll(tree, origins);
        TestArena.onServer(ctx, () -> {
            arena.world.removeDroppedItems(arena.minX, arena.floorY, arena.minZ, arena.minX+arena.size, arena.world.maxHeight(), arena.minZ+arena.size);
            for(int i = 0; i<fixtures; i++){
                if(replacement!=null)for(Map.Entry<Long, TestTrees.Block> entry : tree.blocks.entrySet()){
                    if((entry.getValue().axis!=null)!=group.equals("trunk")||group.equals("decoration"))continue;
                    long pos = entry.getKey();
                    arena.world.setBlock(i*8+BlockPos.getX(pos), arena.floorY+1+BlockPos.getY(pos), 2+BlockPos.getZ(pos), replacement, null, -1);
                }
                if(group.equals("decoration")&&"gravel".equals(replacement)){
                    for(int y = 0; y<2; y++)for(int[] side : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}})
                        arena.world.setBlock(i*8+side[0], arena.floorY+1+y, 2+side[1], "gravel", null, -1);
                }else arena.world.setBlock(i*8+1, arena.floorY+1, 2, replacement!=null&&group.equals("decoration")?replacement:"glowstone", null, -1);
            }
            return null;
        });
    }
    private void finish(TestContext ctx, ITestPlatform hooks){
        TestArena.onServer(ctx, () -> {hooks.prepareCamera(OptionTestSuite.PLAYER); return null;});
        ctx.teleportPlayer(OptionTestSuite.PLAYER, (fixtures-1)*4+0.5, arena.floorY+20, -12.5, 0.0, 55.0);
        ctx.waitTicks(40);
    }
    @FunctionalInterface interface Check{void run(ITestPlatform hooks, int x);}
}
