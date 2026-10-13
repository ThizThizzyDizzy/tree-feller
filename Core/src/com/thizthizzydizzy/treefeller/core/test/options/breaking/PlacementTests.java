package com.thizthizzydizzy.treefeller.core.test.options.breaking;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockAxis;
import com.thizthizzydizzy.treefeller.core.test.*;
public final class PlacementTests{
    private PlacementTests(){}
    public static void register(ITestPlatform platform){
        BreakingTestSuite rotate=new BreakingTestSuite("result.rotate-logs");
        rotate.cutRoot=true;
        for(boolean entity : new boolean[]{false,true})for(boolean enabled : new boolean[]{false,true}){
            rotate.add((entity?"entity":"instant")+"-"+enabled,"global.breaking.trunk-behavior = { natural = true, fall-as-entities = "+entity+", landing = PLACE }\nglobal.result.rotate-logs = "+enabled+"\n",(c,h,f) -> {
                if(entity)rotate.await(c,f);else c.waitTicks(2);
                TestArena.onServer(c,() -> {
                    rotate.equal(8,rotate.blocks(f,"spruce_log"),"All placed logs");
                    for(int dx=1;dx<8;dx++){
                        String state=rotate.arena.world.blockState(f.x+dx,rotate.arena.floorY+1,f.z);
                        rotate.check(state.contains("axis="+(enabled?"x":"y")),"Correct rotated axis: "+state);
                    }
                    return null;
                });
            });
        }
        for(BlockAxis axis : new BlockAxis[]{BlockAxis.X,BlockAxis.Z})rotate.add("horizontal-"+axis.name().toLowerCase(),"global.breaking.trunk-behavior.natural = true\nglobal.criteria.trunk-vertical-ratio = null\nglobal.result.rotate-logs = true\n",
                (c,h,f) -> TestArena.onServer(c,() -> {for(int y=0;y<8;y++)rotate.arena.world.setBlock(f.x,rotate.arena.floorY+1+y,f.z,"spruce_log",axis,-1);return null;}),
                (c,h,f) -> {c.waitTicks(2);TestArena.onServer(c,() -> {String state=rotate.arena.world.blockState(f.x+7,rotate.arena.floorY+1,f.z);rotate.check(state.contains("axis="+(axis==BlockAxis.X?"y":"z")),"Original horizontal axis rotates: "+state);return null;});});
        rotate.register(platform);
        BreakingTestSuite override=new BreakingTestSuite("result.overrideable-blocks");
        override.cutRoot=true;
        for(boolean allowed : new boolean[]{false,true})override.add(allowed?"allowed":"forbidden","global.breaking.trunk-behavior.natural = true\nglobal.result.overrideable-blocks = "+(allowed?"[glass]":"[]")+"\n",
                (c,h,f) -> TestArena.onServer(c,() -> {for(int y=1;y<=10;y++)override.arena.world.setBlock(f.x+3,override.arena.floorY+y,f.z,"glass",null,-1);return null;}),
                (c,h,f) -> {c.waitTicks(2);TestArena.onServer(c,() -> {
                    override.check(override.arena.world.isMaterial(f.x+7,override.arena.floorY+1,f.z,"spruce_log")==allowed,"Natural ray honors obstacle allow/refuse");
                    override.check(override.arena.world.isMaterial(f.x+3,override.arena.floorY+1,f.z,"glass")!=allowed,"Allowed obstruction replaced; forbidden obstruction remains");
                    override.equal(8,override.blocks(f,"spruce_log"),"No lost logs at obstruction");return null;
                });});
        override.add("non-pillar-state","global.breaking.decoration-behavior.natural = true\nglobal.result.rotate-logs = true\n",(c,h,f) -> {c.waitTicks(2);TestArena.onServer(c,() -> {override.equal(1,override.blocks(f,"glowstone"),"Non-pillar natural block preserved");return null;});}).register(platform);
    }
}
