package com.thizthizzydizzy.treefeller.core.test.options.breaking;
import com.thizthizzydizzy.mcautotester.mod.core.TestContext;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
import com.thizthizzydizzy.treefeller.core.test.*;
import java.util.*;
/** Real client actions and observable world outcomes, shared intact fixture setup. */
final class BreakingTestSuite{
    final TestTrees.Tree tree=TestTrees.specimen(TestTrees.Kind.SPRUCE);
    final OptionTestSuite suite;
    final String option;
    boolean cutRoot;
    final List<Fixture> fixtures=new ArrayList<>();
    final List<ITestPlatform.TestTarget> targets=new ArrayList<>();
    TestArena arena;
    BreakingTestSuite(String option){
        this.option=option;
        suite=new OptionTestSuite(option).lifecycle(this::prepare,this::finish,this::cleanup);
    }
    BreakingTestSuite add(String name,String config,Check check){return add(name,config,(ctx,h,f) -> {},check);}
    BreakingTestSuite add(String name,String config,Check before,Check check){
        int index=fixtures.size();
        Fixture f=new Fixture(4+(index%4)*24,8+(index/4)*24);
        fixtures.add(f);
        String base="trees = [{ trunk = [spruce_log], leaves = [spruce_leaves]"+(cutRoot?", roots = [oak_log]":"")+" }]\n"
                +"global.detection.decorations = [{ blocks = [glowstone], direction = SIDE }]\n"
                +"global.breaking.fall-direction = FIXED\nglobal.breaking.fall-direction-yaw = 90\n"
                +"global.breaking.directional-fall-velocity = 0\nglobal.breaking.vertical-fall-velocity = 0\n"
                +"global.result.trunk-drop-chance = 1\nglobal.result.leaves-drop-chance = 0\nglobal.result.decorations-drop-chance = 1\n";
        suite.add(name,base+config,(ctx,hooks) -> {
            ctx.teleportPlayer(OptionTestSuite.PLAYER,cutRoot?f.x-3.5:f.x+.5,arena.floorY+1,cutRoot?f.z+.5:f.z-3.5,cutRoot?-90.0:0.0,0.0);
            TestArena.onServer(ctx,() -> {
                hooks.preparePlayer(OptionTestSuite.PLAYER,true);
                hooks.setPlayerLevels(OptionTestSuite.PLAYER,10,20,0);
                arena.world.removeDroppedItems(f.x-12,arena.floorY,f.z-12,f.x+18,arena.floorY+24,f.z+18);
                return null;
            });
            before.run(ctx,hooks,f);
            TestArena.onServer(ctx,() -> {hooks.showTestStatus(OptionTestSuite.PLAYER,option+" | "+name+" | "+values(config));return null;});
            ctx.breakBlock(f.x-(cutRoot?1:0),arena.floorY+1,f.z);
            check.run(ctx,hooks,f);
        });
        return this;
    }
    private String values(String config){
        StringBuilder values=new StringBuilder();
        String prefix="global."+option;
        for(String line : config.split("\\n"))if(line.startsWith(prefix)){
            if(values.length()>0)values.append(", ");
            values.append(line.substring(prefix.length()).replaceFirst("^\\.",""));
        }
        return values.length()==0?config.split("\\n")[0].replace("global.",""):values.toString();
    }
    void register(ITestPlatform hooks){suite.register(hooks);}
    private void prepare(TestContext ctx,ITestPlatform hooks){
        int size=Math.max(32,Math.max(Math.min(4,fixtures.size())*24+12,((fixtures.size()+3)/4)*24+16));
        arena=TestArena.setup(ctx,hooks,OptionTestSuite.PLAYER,-16,-16,size+12,99);
        TestArena.onServer(ctx,() -> {arena.world.removeFallingBlocks();return null;});
        int[][] origins=fixtures.stream().map(f -> new int[]{f.x,f.z}).toArray(int[][]::new);
        arena.spawnAll(tree,origins);
        TestArena.onServer(ctx,() -> {
            for(Fixture f : fixtures){
                arena.world.setBlock(f.x+2,arena.floorY+7,f.z,"glowstone",null,-1);
                if(cutRoot)arena.world.setBlock(f.x-1,arena.floorY+1,f.z,"oak_log",null,-1);
            }
            return null;
        });
    }
    void await(TestContext ctx,Fixture f){
        for(int ticks=0;ticks<180;ticks+=2){
            boolean done=TestArena.onServer(ctx,() -> falling(f).isEmpty());
            if(done)return;
            ctx.waitTicks(2);
        }
        throw new AssertionError("Falling entities did not finish");
    }
    List<ITestPlatform.FallingObservation> falling(Fixture f){
        List<ITestPlatform.FallingObservation> result=new ArrayList<>();
        for(ITestPlatform.FallingObservation p : arena.world.fallingBlocks())
            if(p.x>=f.x-12&&p.x<=f.x+18&&p.z>=f.z-12&&p.z<=f.z+18)result.add(p);
        return result;
    }
    int blocks(Fixture f,String material){
        int count=0;
        for(int x=f.x-12;x<=f.x+18;x++)for(int y=arena.floorY+1;y<=arena.floorY+18;y++)for(int z=f.z-12;z<=f.z+18;z++)
            if(arena.world.isMaterial(x,y,z,material))count++;
        return count;
    }
    int drops(Fixture f,String material){return arena.world.droppedItemCount(material,f.x-12,arena.floorY,f.z-12,f.x+18,arena.floorY+24,f.z+18);}
    void removed(TestContext ctx,Fixture f){arena.assertLogsRemoved(tree,f.x,f.z);}
    void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    void equal(int expected,int actual,String message){check(expected==actual,message+": expected "+expected+", got "+actual);}
    private void cleanup(TestContext ctx,ITestPlatform hooks){
        if(arena==null)return;
        TestArena.onServer(ctx,() -> {for(ITestPlatform.TestTarget target : targets)target.remove();targets.clear();arena.world.removeFallingBlocks();return null;});
    }
    private void finish(TestContext ctx,ITestPlatform hooks){
        TestArena.onServer(ctx,() -> {hooks.prepareCamera(OptionTestSuite.PLAYER);return null;});
        ctx.teleportPlayer(OptionTestSuite.PLAYER,arena.minX+arena.size/2.0,arena.floorY+60,arena.minZ-14.5,0.0,55.0);
        ctx.waitTicks(40);
    }
    static final class Fixture{
        final int x,z;
        Fixture(int x,int z){this.x=x;this.z=z;}
    }
    @FunctionalInterface interface Check{void run(TestContext ctx,ITestPlatform hooks,Fixture fixture);}
}
