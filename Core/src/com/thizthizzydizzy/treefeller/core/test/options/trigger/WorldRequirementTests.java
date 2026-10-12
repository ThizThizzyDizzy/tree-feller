package com.thizthizzydizzy.treefeller.core.test.options.trigger;
import com.thizthizzydizzy.mcautotester.mod.core.TestContext;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
import com.thizthizzydizzy.treefeller.core.test.OptionTestSuite;
import com.thizthizzydizzy.treefeller.core.test.TestArena;
import com.thizthizzydizzy.treefeller.core.test.TestTrees;
/** Independent world-option suites; each owns its fixtures and restores environmental changes. */
public final class WorldRequirementTests{
    private final String option;
    private final int count;
    private final TestTrees.Tree tree = TestTrees.specimen(TestTrees.Kind.SPRUCE);
    private TestArena arena;
    private long previousTime;
    private boolean previousCycle;
    private String[] previousBiomes;
    private String dimension;
    private WorldRequirementTests(String option, int count){this.option = option; this.count = count;}
    public static void register(ITestPlatform platform){
        new WorldRequirementTests("day_time", 7).registerRange(platform, 6000, 12000, 18000, 3000, 24000);
        new WorldRequirementTests("moon_phase", 7).registerRange(platform, 2, 4, 6, 1, 8);
        new WorldRequirementTests("biomes", 4).registerBiomes(platform);
        new WorldRequirementTests("dimensions", 4).registerDimensions(platform);
    }
    private OptionTestSuite suite(){
        return new OptionTestSuite("trigger."+option.replace('_', '-'))
                .lifecycle(this::prepare, this::finish, this::cleanup);
    }
    private void registerRange(ITestPlatform platform, int min, int max, int wrapMin, int wrapMax, int period){
        OptionTestSuite suite = suite();
        String normal = "global.trigger."+option+" = {min="+min+", max="+max+"}";
        String wrapping = "global.trigger."+option+" = {min="+wrapMin+", max="+wrapMax+"}";
        range(suite, "minimum", normal, 0, min, true);
        range(suite, "maximum", normal, 1, max, true);
        range(suite, "below-minimum", normal, 2, min-1, false);
        range(suite, "above-maximum", normal, 3, max+1, false);
        range(suite, "wrapping-upper", wrapping, 4, period-1, true);
        range(suite, "wrapping-lower", wrapping, 5, 0, true);
        range(suite, "wrapping-outside", wrapping, 6, max, false);
        suite.register(platform);
    }
    private void range(OptionTestSuite suite, String name, String config, int index, int value, boolean allowed){
        suite.add(name, config, (ctx, platform) -> {
            TestArena.onServer(ctx, () -> {
                arena.world.setFullTime(option.equals("day_time")?value:value*24000L+18000);
                return null;
            });
            cut(ctx, platform, index, config.substring("global.trigger.".length())+" | actual="+value, allowed);
        });
    }
    private void registerBiomes(ITestPlatform platform){
        OptionTestSuite suite = suite();
        biome(suite, "whitelist-match", "whitelist", 0, "PLAINS", true);
        biome(suite, "whitelist-miss", "whitelist", 1, "DESERT", false);
        biome(suite, "blacklist-match", "blacklist", 2, "PLAINS", false);
        biome(suite, "blacklist-miss", "blacklist", 3, "DESERT", true);
        suite.register(platform);
    }
    private void biome(OptionTestSuite suite, String name, String list, int index, String biome, boolean allowed){
        suite.add(name, "global.trigger.biomes."+list+" = [PLAINS]", (ctx, platform) -> {
            TestArena.onServer(ctx, () -> {
                if(!arena.world.biome(index*8, 2).equals(biome))throw new AssertionError("Unexpected fixture biome");
                return null;
            });
            cut(ctx, platform, index, "biomes."+list+"=[PLAINS] | actual="+biome, allowed);
        });
    }
    private void registerDimensions(ITestPlatform platform){
        OptionTestSuite suite = suite();
        dimension(suite, "whitelist-match", "whitelist", 0, true, true);
        dimension(suite, "whitelist-miss", "whitelist", 1, false, false);
        dimension(suite, "blacklist-match", "blacklist", 2, true, false);
        dimension(suite, "blacklist-miss", "blacklist", 3, false, true);
        suite.register(platform);
    }
    private void dimension(OptionTestSuite suite, String name, String list, int index, boolean matching, boolean allowed){
        suite.add(name, () -> "global.trigger.dimensions."+list+" = ["+quoted(dimensionValue(matching))+"]",
                (ctx, platform) -> cut(ctx, platform, index,
                        "dimensions."+list+"=["+dimensionValue(matching)+"] | actual="
                                +TestArena.onServer(ctx, () -> arena.world.dimension()), allowed));
    }
    private String dimensionValue(boolean matching){
        return dimension+(matching?"":"-nonmatching");
    }
    private static String quoted(String value){return "\""+value.replace("\\", "\\\\").replace("\"", "\\\"")+"\"";}
    private void prepare(TestContext ctx, ITestPlatform platform){
        arena = null;
        previousBiomes = null;
        TestArena.onServer(ctx, () -> {
            ITestPlatform.ITestWorld world = platform.world(OptionTestSuite.PLAYER);
            previousTime = world.fullTime();
            previousCycle = world.daylightCycle();
            dimension = world.dimension();
            return null;
        });
        arena = TestArena.setup(ctx, platform, OptionTestSuite.PLAYER, -4, -6, count*8, 99);
        TestArena.onServer(ctx, () -> {
            if(option.equals("day_time")||option.equals("moon_phase"))arena.world.setDaylightCycle(false);
            if(option.equals("biomes")){
                previousBiomes = new String[arena.size*arena.size];
                for(int x = 0; x<arena.size; x++)for(int z = 0; z<arena.size; z++)
                    previousBiomes[x*arena.size+z] = arena.world.biome(arena.minX+x, arena.minZ+z);
                for(int x = 0; x<arena.size; x++)for(int z = 0; z<arena.size; z++){
                    int fixture = Math.min(count-1, (x+arena.minX+4)/8);
                    arena.world.setBiome(arena.minX+x, arena.minZ+z, fixture%2==0?"PLAINS":"DESERT");
                }
            }
            return null;
        });
        int[][] origins = new int[count][2];
        for(int i = 0; i<count; i++)origins[i] = new int[]{i*8, 2};
        arena.spawnAll(tree, origins);
    }
    private void cut(TestContext ctx, ITestPlatform platform, int index, String label, boolean allowed){
        int x = index*8;
        TestArena.onServer(ctx, () -> {
            platform.preparePlayer(OptionTestSuite.PLAYER, true);
            platform.setPlayerSneaking(OptionTestSuite.PLAYER, false);
            return null;
        });
        ctx.teleportPlayer(OptionTestSuite.PLAYER, x+0.5, arena.floorY+1, -1.5, 0.0, 0.0);
        TestArena.onServer(ctx, () -> {
            platform.showTestStatus(OptionTestSuite.PLAYER, label+" | expect "+(allowed?"fell":"refuse"));
            return null;
        });
        ctx.breakBlock(x, arena.floorY+1, 2);
        if(allowed)arena.assertLogsRemoved(tree, x, 2);
        else arena.assertOnlyLogRemoved(tree, x, 2, 0, 0, 0);
    }
    private void finish(TestContext ctx, ITestPlatform platform){
        TestArena.onServer(ctx, () -> {platform.prepareCamera(OptionTestSuite.PLAYER); return null;});
        ctx.teleportPlayer(OptionTestSuite.PLAYER, (count-1)*4+0.5, arena.floorY+24, -22.5, 0.0, 45.0);
        ctx.waitTicks(40);
    }
    private void cleanup(TestContext ctx, ITestPlatform platform){
        if(arena==null)return;
        TestArena.onServer(ctx, () -> {
            arena.world.setFullTime(previousTime);
            arena.world.setDaylightCycle(previousCycle);
            if(previousBiomes!=null){
                for(int x = 0; x<arena.size; x++)for(int z = 0; z<arena.size; z++)
                    arena.world.setBiome(arena.minX+x, arena.minZ+z, previousBiomes[x*arena.size+z]);
                previousBiomes = null;
            }
            return null;
        });
    }
}
