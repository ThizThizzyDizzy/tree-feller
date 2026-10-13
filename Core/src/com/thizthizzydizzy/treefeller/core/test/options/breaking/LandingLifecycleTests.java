package com.thizthizzydizzy.treefeller.core.test.options.breaking;
import com.thizthizzydizzy.treefeller.core.connector.player.PlayerGameMode;
import com.thizthizzydizzy.treefeller.core.test.*;
/** Integration cases that prove deferred delivery cannot duplicate costs or drops. */
public final class LandingLifecycleTests{
    private LandingLifecycleTests(){}
    public static void register(ITestPlatform platform){
        BreakingTestSuite t=new BreakingTestSuite("breaking.landing");
        String fall="global.breaking.trunk-behavior.fall-as-entities = true\n";
        t.add("inventory-overflow",fall+"global.breaking.trunk-behavior.landing = INVENTORY\n",(c,h,f) -> TestArena.onServer(c,() -> {
            for(int slot=1;slot<36;slot++)h.setInventoryItem(OptionTestSuite.PLAYER,slot,"stone",64);return null;
        }),(c,h,f) -> {t.await(c,f);TestArena.onServer(c,() -> {t.equal(0,h.inventoryItemCount(OptionTestSuite.PLAYER,"spruce_log"),"Full inventory has no room");t.equal(8,t.drops(f,"spruce_log"),"Overflow delivered once at landing");return null;});});
        t.add("shared-inventory-routing",fall+"global.breaking.trunk-behavior.landing = BREAK\nglobal.result.drop-to-inventory = true\n",(c,h,f) -> {
            t.await(c,f);TestArena.onServer(c,() -> {t.equal(8,h.inventoryItemCount(OptionTestSuite.PLAYER,"spruce_log"),"Result inventory option applies at landing");t.equal(0,t.drops(f,"spruce_log"),"No duplicate world drops");return null;});
        });
        t.add("deferred-drop-conversions",fall+"global.breaking.trunk-behavior.landing = BREAK\nglobal.result.drop-conversions = { spruce_log = diamond }\n",(c,h,f) -> {
            t.await(c,f);TestArena.onServer(c,() -> {t.equal(8,t.drops(f,"diamond"),"Original captured drops converted once");t.equal(0,t.drops(f,"spruce_log"),"No unconverted drops");return null;});
        });
        t.add("creative",fall+"global.trigger.creative-mode = true\nglobal.breaking.trunk-behavior.landing = PLACE\n",(c,h,f) -> TestArena.onServer(c,() -> {h.setPlayerGameMode(OptionTestSuite.PLAYER,PlayerGameMode.CREATIVE);return null;}),
                (c,h,f) -> {t.await(c,f);TestArena.onServer(c,() -> {t.equal(0,t.blocks(f,"spruce_log"),"Creative entity falls disappear, as in v1");t.equal(0,t.drops(f,"spruce_log"),"No creative drops");return null;});});
        t.add("occupied-destination",fall+"global.breaking.trunk-behavior.landing = PLACE\n",(c,h,f) -> {
            TestArena.onServer(c,() -> {t.check(!t.falling(f).isEmpty(),"Entities in flight");
                for(int y=1;y<=10;y++)t.arena.world.setBlock(f.x,t.arena.floorY+y,f.z,"glass",null,-1);return null;});
            t.await(c,f);TestArena.onServer(c,() -> {
                t.equal(10,t.blocks(f,"glass"),"Occupied destination preserved");
                t.equal(8,t.drops(f,"spruce_log")+t.blocks(f,"spruce_log"),"Every blocked placement resolved once");return null;
            });
        });
        t.add("entity-removal-fallback",fall+"global.breaking.trunk-behavior.landing = BREAK\n",(c,h,f) -> {
            TestArena.onServer(c,() -> {t.check(!t.falling(f).isEmpty(),"Live entities to expire");t.arena.world.removeFallingBlocks();return null;});
            c.waitTicks(2);TestArena.onServer(c,() -> {t.equal(8,t.drops(f,"spruce_log"),"Removed entities resolve once");t.equal(0,t.falling(f).size(),"No tracked native entity survives");return null;});
        });
        t.add("passable-support","global.breaking.decoration-behavior = { fall-as-entities = true, landing = PLACE }\nglobal.result.overrideable-blocks = [short_grass]\n",(c,h,f) -> TestArena.onServer(c,() -> {
            t.arena.world.setBlock(f.x+2,t.arena.floorY+1,f.z,"short_grass",null,-1);return null;
        }),(c,h,f) -> {t.await(c,f);TestArena.onServer(c,() -> {t.equal(1,t.blocks(f,"glowstone"),"Placement conserves decoration");
            t.check(t.arena.world.isMaterial(f.x+2,t.arena.floorY+1,f.z,"glowstone"),"Allowed passable block replaced above solid ground");return null;});});
        t.register(platform);
        BreakingTestSuite stationary=new BreakingTestSuite("breaking.landing-in-place");
        stationary.cutRoot=true;
        for(String landing : new String[]{"PLACE","BREAK"})stationary.add(landing.toLowerCase(),
                "trees = [{ trunk = [spruce_log], leaves = [spruce_leaves], roots = [oak_log] }]\n"
                +"global.breaking.trunk-behavior = { natural = false, fall-as-entities = false, landing = "+landing+" }\n",
                (c,h,f) -> {c.waitTicks(2);TestArena.onServer(c,() -> {
                    stationary.equal(landing.equals("PLACE")?8:0,stationary.blocks(f,"spruce_log"),"Explicit in-place landing blocks");
                    stationary.equal(landing.equals("BREAK")?8:0,stationary.drops(f,"spruce_log"),"Explicit in-place landing drops");
                    stationary.equal(0,stationary.falling(f).size(),"In-place outcomes do not spawn entities");return null;
                });});
        stationary.register(platform);
    }
}
