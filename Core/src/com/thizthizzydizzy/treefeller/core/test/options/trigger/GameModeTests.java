package com.thizthizzydizzy.treefeller.core.test.options.trigger;
import com.thizthizzydizzy.mcautotester.mod.core.TestContext;
import com.thizthizzydizzy.treefeller.core.connector.player.PlayerGameMode;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
import com.thizthizzydizzy.treefeller.core.test.OptionTestSuite;
import com.thizthizzydizzy.treefeller.core.test.TestArena;
import com.thizthizzydizzy.treefeller.core.test.TestTrees;
/** Each game-mode option is tested enabled and disabled in that mode only. */
public final class GameModeTests{
    private final String option;
    private final PlayerGameMode mode;
    private final TestTrees.Tree tree = TestTrees.specimen(TestTrees.Kind.SPRUCE);
    private TestArena arena;
    private GameModeTests(String option, PlayerGameMode mode){this.option = option; this.mode = mode;}
    public static void register(ITestPlatform platform){
        new GameModeTests("creative_mode", PlayerGameMode.CREATIVE).registerOption(platform);
        new GameModeTests("survival_mode", PlayerGameMode.SURVIVAL).registerOption(platform);
        new GameModeTests("adventure_mode", PlayerGameMode.ADVENTURE).registerOption(platform);
    }
    private void registerOption(ITestPlatform platform){
        OptionTestSuite suite = new OptionTestSuite("trigger."+option.replace('_', '-'))
                .lifecycle(this::prepare, this::finish, (ctx, hooks) -> {
                    TestArena.onServer(ctx, () -> {hooks.setPlayerSneaking(OptionTestSuite.PLAYER, false); return null;});
                });
        for(boolean enabled : new boolean[]{true, false}){
            suite.add(enabled?"enabled":"disabled", "global.trigger."+option+" = "+enabled,
                    (ctx, hooks) -> cut(ctx, hooks, enabled?0:8, enabled));
        }
        suite.register(platform);
    }
    private void prepare(TestContext ctx, ITestPlatform platform){
        arena = TestArena.setup(ctx, platform, OptionTestSuite.PLAYER, -4, -6, 16, 99);
        arena.spawnAll(tree, new int[][]{{0, 2}, {8, 2}});
    }
    private void cut(TestContext ctx, ITestPlatform platform, int x, boolean enabled){
        TestArena.onServer(ctx, () -> {
            platform.preparePlayer(OptionTestSuite.PLAYER, true);
            platform.setPlayerGameMode(OptionTestSuite.PLAYER, mode);
            if(mode==PlayerGameMode.ADVENTURE){
                platform.equipItem(OptionTestSuite.PLAYER,
                        "minecraft:iron_axe[minecraft:can_break={blocks:[\"minecraft:spruce_log\",\"minecraft:spruce_leaves\"]}]");
            }
            return null;
        });
        ctx.teleportPlayer(OptionTestSuite.PLAYER, x+0.5, arena.floorY+1, -1.5, 0.0, 0.0);
        TestArena.onServer(ctx, () -> {
            platform.setPlayerSneaking(OptionTestSuite.PLAYER, false);
            platform.showTestStatus(OptionTestSuite.PLAYER, option+"="+enabled+" | "+mode
                    +" | expect "+(enabled?"fell":"refuse"));
            return null;
        });
        ctx.breakBlock(x, arena.floorY+1, 2);
        if(enabled)arena.assertLogsRemoved(tree, x, 2);
        else arena.assertOnlyLogRemoved(tree, x, 2, 0, 0, 0);
    }
    private void finish(TestContext ctx, ITestPlatform platform){
        TestArena.onServer(ctx, () -> {platform.prepareCamera(OptionTestSuite.PLAYER); return null;});
        ctx.teleportPlayer(OptionTestSuite.PLAYER, 4.5, arena.floorY+16, -10.5, 0.0, 50.0);
        ctx.waitTicks(40);
    }
}
