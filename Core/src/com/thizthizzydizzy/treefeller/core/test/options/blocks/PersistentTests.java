package com.thizthizzydizzy.treefeller.core.test.options.blocks;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
import com.thizthizzydizzy.treefeller.core.test.*;
import java.util.*;
public final class PersistentTests{
    private final TestTrees.Tree tree=TestTrees.specimen(TestTrees.Kind.SPRUCE,4);
    private final List<Fixture> fixtures=new ArrayList<>();
    private final OptionTestSuite suite=new OptionTestSuite("blocks.persistent")
            .lifecycle(this::prepare,this::finish,(ctx,hooks) -> {});
    private TestArena arena;
    public static void register(ITestPlatform hooks){
        PersistentTests tests=new PersistentTests();
        tests.add("default-natural",null,false,false,true);
        tests.add("default-player",null,true,false,false);
        tests.add("false-mixed","false",false,true,true);
        tests.add("true-mixed","true",false,true,true);
        tests.add("omitted-mixed","omitted",false,true,true);
        tests.add("null-mixed","null",false,true,true);
        tests.suite.register(hooks);
    }
    private void add(String name,String filter,boolean persistent,boolean mixed,boolean allowed){
        Fixture f=new Fixture(fixtures.size()*10,persistent,mixed);
        fixtures.add(f);
        String config="debug.chat = true\n"
                +"global.criteria.required_leaves = { min = 1 }\n"
                +"global.detection.secondary_tree_verification = false\n"
                +"global.result.leaves_drop_chance = 0\n";
        if(filter!=null)config+="trees = [{ trunk = [spruce_log], leaves = [{ material = SPRUCE_LEAVES"
                +(filter.equals("omitted")?"":", persistent = "+filter)+" }] }]\n";
        suite.add(name,config,(ctx,hooks) -> {
            ctx.teleportPlayer(OptionTestSuite.PLAYER,f.x+.5,arena.floorY+1,-1.5,0.0,0.0);
            TestArena.onServer(ctx,() -> {
                hooks.preparePlayer(OptionTestSuite.PLAYER,true);
                return null;
            });
            ctx.waitTicks(2); // Let the client receive survival mode and the equipped axe.
            TestArena.onServer(ctx,() -> {
                hooks.showTestStatus(OptionTestSuite.PLAYER,"persistent="+(filter==null?"false (default trees)":filter)
                        +" | "+name+" | "+(allowed?"fell; verify each leaf state":"refuse; retain crown and remaining trunk"));
                return null;
            });
            ctx.breakBlock(f.x,arena.floorY+1,2);
            ctx.waitTicks(2); // Observe the server outcome after the mining packets.
            if(allowed)arena.assertLogsRemoved(tree,f.x,2);
            else arena.assertOnlyLogRemoved(tree,f.x,2,0,0,0);
            TestArena.onServer(ctx,() -> {
                for(Map.Entry<Long,TestTrees.Block> entry:tree.blocks.entrySet()){
                    if(!entry.getValue().material.endsWith("_leaves"))continue;
                    long pos=entry.getKey();
                    boolean state=f.persistent(pos);
                    boolean matches=filter==null||filter.equals("false")?!state:filter.equals("true")?state:true;
                    String expected=allowed&&matches?"air":entry.getValue().material;
                    int x=f.x+BlockPos.getX(pos),y=arena.floorY+1+BlockPos.getY(pos),z=2+BlockPos.getZ(pos);
                    if(!arena.world.isMaterial(x,y,z,expected))
                        throw new AssertionError(name+": expected "+expected+" at "+x+","+y+","+z+", got "+arena.world.blockState(x,y,z));
                }
                return null;
            });
        });
    }
    private void prepare(com.thizthizzydizzy.mcautotester.mod.core.TestContext ctx,ITestPlatform hooks){
        arena=TestArena.setup(ctx,hooks,OptionTestSuite.PLAYER,-5,-6,64,99);
        arena.spawnAll(tree,fixtures.stream().map(f -> new int[]{f.x,2}).toArray(int[][]::new));
        TestArena.onServer(ctx,() -> {
            for(Fixture f:fixtures)for(Map.Entry<Long,TestTrees.Block> entry:tree.blocks.entrySet()){
                if(!entry.getValue().material.endsWith("_leaves"))continue;
                long pos=entry.getKey();
                arena.world.setPersistent(f.x+BlockPos.getX(pos),arena.floorY+1+BlockPos.getY(pos),2+BlockPos.getZ(pos),f.persistent(pos));
            }
            return null;
        });
    }
    private void finish(com.thizthizzydizzy.mcautotester.mod.core.TestContext ctx,ITestPlatform hooks){
        TestArena.onServer(ctx,() -> {hooks.prepareCamera(OptionTestSuite.PLAYER);return null;});
        ctx.teleportPlayer(OptionTestSuite.PLAYER,25.5,arena.floorY+35,arena.minZ+1.5,0.0,85.0);
        ctx.waitTicks(40);
    }
    private static final class Fixture{
        final int x;final boolean state,mixed;
        Fixture(int x,boolean state,boolean mixed){this.x=x;this.state=state;this.mixed=mixed;}
        boolean persistent(long pos){return mixed?BlockPos.getX(pos)>=0:state;}
    }
}
