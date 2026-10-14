package com.thizthizzydizzy.treefeller.core.test.options.result;
import com.thizthizzydizzy.treefeller.core.test.*;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform.*;
import com.thizthizzydizzy.mcautotester.mod.core.TestContext;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
/** One shared set of intact trees for an effect option's real client cuts. */
final class EffectTestSuite{
    final OptionTestSuite suite;
    final TestTrees.Tree tree=TestTrees.specimen(TestTrees.Kind.SPRUCE);
    final String option;
    String foundationMaterial;
    TestArena arena;
    EffectCapture capture;
    int fixtures;
    EffectTestSuite(String option){
        this.option=option;
        suite=new OptionTestSuite("result.effects"+(option.isEmpty()?"":"."+option)).lifecycle(this::prepare,this::finish,this::cleanup);
    }
    EffectTestSuite add(String name,String effect,String extra,String tool,Check check){
        return add(name,effect,extra,tool,true,check);
    }
    EffectTestSuite add(String name,String effect,String extra,String tool,boolean felled,Check check){
        int index=fixtures++;
        String config="trees = [{ trunk = [spruce_log], leaves = [spruce_leaves] }]\n"
                +"global.trigger.creative-mode = true\n"
                +"global.detection.block-data-rules.use-leaf-distance = false\n"
                +"global.detection.decorations = [{ blocks = [glowstone], direction = SIDE }]\n"
                +"global.result.effects = ["+effect+"]\n"+extra;
        suite.add(name,config,(ctx,hooks) -> {
            int x=index*12;
            ctx.teleportPlayer(OptionTestSuite.PLAYER,x+.5,arena.floorY+1,-1.5,0.0,0.0);
            TestArena.onServer(ctx,() -> {
                if(capture!=null)capture.close();
                hooks.preparePlayer(OptionTestSuite.PLAYER,true);
                hooks.equipItem(OptionTestSuite.PLAYER,tool);
                if(effect.contains("EXPLOSION"))hooks.setPlayerGameMode(OptionTestSuite.PLAYER,com.thizthizzydizzy.treefeller.core.connector.player.PlayerGameMode.CREATIVE);
                capture=hooks.captureEffects(OptionTestSuite.PLAYER);
                hooks.showTestStatus(OptionTestSuite.PLAYER,"result.effects"+(option.isEmpty()?"":"."+option)+" | "+name+" | "+effect);
                return null;
            });
            ctx.breakBlock(x,arena.floorY+1,2);
            if(extra.contains("cutting-animation"))ctx.waitTicks(25);
            // Ensure server output has passed through the network observer; ephemeral clouds have expired.
            ctx.waitTicks(2);
            if(felled)arena.assertLogsRemoved(tree,x,2);
            else arena.assertOnlyLogRemoved(tree,x,2,0,0,0);
            TestArena.onServer(ctx,() -> {check.run(capture,x);return null;});
        });
        return this;
    }
    EffectTestSuite add(String name,String effect,Check check){return add(name,effect,"","iron_axe",check);}
    void register(ITestPlatform platform){suite.register(platform);}
    private void prepare(TestContext ctx,ITestPlatform hooks){
        arena=TestArena.setup(ctx,hooks,OptionTestSuite.PLAYER,-5,-6,Math.max(20,fixtures*12+5),99);
        int[][] origins=new int[fixtures][2];
        for(int i=0;i<fixtures;i++)origins[i]=new int[]{i*12,2};
        arena.spawnAll(tree,origins);
        TestArena.onServer(ctx,() -> {
            if(foundationMaterial!=null)for(int x=arena.minX;x<arena.minX+arena.size;x++)for(int z=arena.minZ;z<arena.minZ+arena.size;z++)
                arena.world.setBlock(x,arena.floorY-1,z,foundationMaterial,null,-1);
            for(int i=0;i<fixtures;i++)arena.world.setBlock(i*12+1,arena.floorY+1,2,"glowstone",null,-1);
            return null;
        });
    }
    private void cleanup(TestContext ctx,ITestPlatform hooks){
        TestArena.onServer(ctx,() -> {if(capture!=null){capture.close();capture=null;}return null;});
    }
    private void finish(TestContext ctx,ITestPlatform hooks){
        TestArena.onServer(ctx,() -> {hooks.prepareCamera(OptionTestSuite.PLAYER);return null;});
        ctx.teleportPlayer(OptionTestSuite.PLAYER,(fixtures-1)*6+.5,arena.floorY+20,-12.5,0.0,55.0);
        ctx.waitTicks(40);
    }
    @FunctionalInterface interface Check{void run(EffectCapture capture,int x);}
}
