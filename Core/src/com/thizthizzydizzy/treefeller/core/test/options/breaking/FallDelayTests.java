package com.thizthizzydizzy.treefeller.core.test.options.breaking;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.config.ConfigReader;
import com.thizthizzydizzy.treefeller.core.test.*;
public final class FallDelayTests{
    private FallDelayTests(){}
    public static void register(ITestPlatform platform){
        BreakingTestSuite t=new BreakingTestSuite("breaking.fall-delay");
        for(int delay : new int[]{0,40})t.add(delay==0?"zero":"positive",config(delay,false),(c,h,f) -> {
            TestArena.onServer(c,() -> {
                t.equal(0,t.blocks(f,"spruce_log"),"Sources are removed before entity spawn");
                t.check(t.falling(f).isEmpty()==(delay>0),"Entity presence follows delay");
                if(delay>0){t.equal(0,t.drops(f,"spruce_log"),"Drops deferred until landing");TreeFellerCore.config=ConfigReader.readFromString("");}
                return null;
            });
            if(delay>0){c.waitTicks(20);TestArena.onServer(c,() -> {t.equal(0,t.falling(f).size(),"No early entities");return null;});waitSpawn(c,t,f);}
            t.await(c,f);TestArena.onServer(c,() -> {t.equal(8,t.drops(f,"spruce_log"),"Delayed drops use initiating settings");return null;});
        });
        t.add("cutting-animation",config(40,true)+"global.result.consumed-food-base = 1\nglobal.result.consumed-food-trunk = 1\n",
                (c,h,f) -> {
                    TestArena.onServer(c,() -> {t.equal(0,t.falling(f).size(),"First layer remains delayed");TreeFellerCore.config=ConfigReader.readFromString("");return null;});
                    c.waitTicks(95);t.await(c,f);
                    TestArena.onServer(c,() -> {t.equal(8,t.drops(f,"spruce_log"),"Animated delayed conservation");t.equal(0,t.blocks(f,"spruce_log"),"All layers removed");
                        t.check(Math.abs(h.playerState(OptionTestSuite.PLAYER).getFoodLevel()-1)<.01,"Costs occur once per layer and base once");return null;});
                }).register(platform);
    }
    private static String config(int delay,boolean animation){return "global.breaking.trunk-behavior = { fall-as-entities = true, landing = BREAK }\nglobal.breaking.fall-delay = "+delay+"\nglobal.cutting.cutting-animation = "+animation+"\nglobal.cutting.animation-delay = 4\n";}
    private static void waitSpawn(com.thizthizzydizzy.mcautotester.mod.core.TestContext c,BreakingTestSuite t,BreakingTestSuite.Fixture f){
        for(int i=0;i<40;i++){if(TestArena.onServer(c,() -> !t.falling(f).isEmpty()))return;c.waitTicks(1);}throw new AssertionError("Delayed entities never spawned");
    }
}
