package com.thizthizzydizzy.treefeller.core.test;
import com.thizthizzydizzy.mcautotester.mod.api.MCAutoTesterAPI;
import com.thizthizzydizzy.mcautotester.mod.core.TestContext;
public class TreeFellerTestSequences{
    public static void register(){
        MCAutoTesterAPI.register("treefeller-acacia", ctx -> {
            ctx.waitForJoin("testplayer");
            prepareAcacia(ctx);
            ctx.log("Breaking the second trunk block with the iron axe");
            ctx.breakBlock(0, 101, 2);
            ctx.log("Moving the camera to the middle of the back edge");
            ctx.teleportPlayer("testplayer", 0, 100, -7.5, 0.0, -20.0);
            ctx.waitTicks(60); // Show the whole tree area for about three seconds.
            ctx.executeConsole("forceload remove -8 -8 7 7");
            ctx.success();
        }, true);
    }
    private static void prepareAcacia(TestContext ctx){
        ctx.log("Clearing a 16x16x16 space with a grass floor around the player");
        ctx.executeConsole("gamemode creative testplayer");
        // Set up a fixed arena so the client can mine an absolute block position.
        ctx.executeConsole("forceload add -8 -8 7 7");
        ctx.waitTicks(40); // Load the arena chunks before filling them.
        ctx.executeConsole("fill -8 99 -8 7 114 7 minecraft:air");
        ctx.executeConsole("fill -8 99 -8 7 99 7 minecraft:grass_block");
        ctx.teleportPlayer("testplayer", 0.5, 100, 0.5, 0.0, 0.0);
        ctx.executeConsole("time set noon");
        ctx.executeConsole("weather clear");
        ctx.waitTicks(5);
        ctx.log("Growing an acacia tree two blocks in front of the player");
        // Feature placement can fail randomly; retry only while the trunk is absent.
        for(int i = 0; i<10; i++){
            atPlayer(ctx, "execute unless block ~ ~ ~2 minecraft:acacia_log run place feature minecraft:acacia ~ ~ ~2");
        }
        ctx.executeConsole("clear testplayer");
        ctx.executeConsole("item replace entity testplayer weapon.mainhand with minecraft:iron_axe");
        ctx.executeConsole("gamemode survival testplayer");
        ctx.waitTicks(40); // Allow the client to render the tree and equipped axe.
    }
    private static void atPlayer(TestContext ctx, String command){
        ctx.executeConsole("execute at testplayer run "+command);
    }
}
