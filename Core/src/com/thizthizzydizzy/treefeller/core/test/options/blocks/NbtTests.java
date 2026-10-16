package com.thizthizzydizzy.treefeller.core.test.options.blocks;
import com.thizthizzydizzy.treefeller.core.test.*;
import com.thizthizzydizzy.treefeller.core.config.ConfigReader;
import com.thizthizzydizzy.treefeller.core.config.ConfigWriter;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IBlockDefinition;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
public final class NbtTests{
    private TestArena arena;
    private ITestPlatform.EffectCapture capture;
    private final TestTrees.Tree tree=TestTrees.specimen(TestTrees.Kind.SPRUCE);
    private static final String INITIAL="{PublicBukkitValues:{\"treefeller:fixture\":{flag:1b,kept:7s,values:[L;3L,4L]}}}";
    private static String tag(int flag){return "{PublicBukkitValues:{\"treefeller:fixture\":{flag:"+flag+"b}}}";}
    private static String quote(String value){return "\""+value.replace("\\","\\\\").replace("\"","\\\"")+"\"";}
    private static IBlockDefinition definition(String material,String nbt){
        return ConfigReader.readFromString("trees=[{trunk=[{material="+material.toUpperCase(java.util.Locale.ROOT)+",nbt="+quote(nbt)+"}]}]").trees[0].trunk[0];
    }
    public static void register(ITestPlatform platform){new NbtTests().registerTests(platform);}
    private void registerTests(ITestPlatform platform){
        OptionTestSuite suite=new OptionTestSuite("blocks.nbt").lifecycle((ctx,hooks) -> {
            arena=TestArena.setup(ctx,hooks,OptionTestSuite.PLAYER,-8,-8,48,99);
            arena.spawnAll(tree,new int[][]{{4,6},{24,6}});
            TestArena.onServer(ctx,() -> {
                for(int x:new int[]{4,24}){
                    arena.world.setBlock(x-1,100,6,"oak_log",null,-1);
                    arena.world.state().setBlock(BlockPos.toPos(x+1,101,6),definition("chest",x==4?INITIAL:INITIAL.replace("flag:1b","flag:9b")));
                    arena.world.state().setBlock(BlockPos.toPos(x-1,101,6),definition("chest",INITIAL.replace("flag:1b","flag:0b")));
                    if(!arena.world.state().matches(BlockPos.toPos(x+1,101,6),definition("chest",x==4?tag(1):tag(9))))throw new AssertionError("Incorrect initial NBT fixture");
                }
                return null;
            });
        },(ctx,hooks) -> {
            TestArena.onServer(ctx,() -> {hooks.prepareCamera(OptionTestSuite.PLAYER);return null;});
            ctx.teleportPlayer(OptionTestSuite.PLAYER,16.5,125,-7.5,0.0,60.0);ctx.waitTicks(40);
        },(ctx,hooks) -> TestArena.onServer(ctx,() -> {if(capture!=null){capture.close();capture=null;}return null;}));
        for(boolean allowed:new boolean[]{true,false}){
            int x=allowed?4:24;
            String filter=tag(1);
            String config="trees=[{trunk=[spruce_log],leaves=[spruce_leaves],roots=[oak_log]}]\n"
                    +"global.detection.decorations=[{blocks=[chest],direction=SIDE}]\n"
                    +"global.criteria.decoration-filter.required=[{material=CHEST,nbt="+quote(filter)+"}]\n"
                    +"global.breaking.block-conversions={spruce_log={material=CHEST,nbt="+quote(tag(2))+"},"
                    +quote("chest"+tag(1))+"={nbt="+quote(tag(2))+"}}\n"
                    +"global.result.effects=[{type=MARKER,permanent=true,location=DECORATION,filter=[{material=CHEST,nbt="+quote(tag(1))+"}]},{type=MARKER,permanent=true,location=DECORATION,filter=[{material=CHEST,nbt="+quote(tag(9))+"}]}]\n";
            suite.add(allowed?"filter-match-and-merge-conversions":"filter-mismatch-preserves-tree",config,(ctx,hooks) -> {
                ctx.teleportPlayer(OptionTestSuite.PLAYER,x-3.5,100,6.5,-90.0,0.0);
                TestArena.onServer(ctx,() -> {
                    TreeFellerCore.config=ConfigReader.readFromString(ConfigWriter.writeToString(TreeFellerCore.config));
                    hooks.preparePlayer(OptionTestSuite.PLAYER,true);
                    if(capture!=null)capture.close();capture=hooks.captureEffects(OptionTestSuite.PLAYER);return null;
                });
                ctx.waitTicks(2); // Let the client receive survival mode and the equipped axe.
                TestArena.onServer(ctx,() -> {
                    hooks.showTestStatus(OptionTestSuite.PLAYER,"blocks.nbt | required="+filter+" | "+(allowed?"match: merge conversions":"mismatch: refuse"));return null;
                });
                ctx.breakBlock(x-1,100,6);
                ctx.waitTicks(2);
                TestArena.onServer(ctx,() -> {
                    for(int y=100;y<108;y++){
                        boolean correct=allowed?arena.world.state().matches(BlockPos.toPos(x,y,6),definition("chest",tag(2))):arena.world.isMaterial(x,y,6,"spruce_log");
                        if(!correct)throw new AssertionError("Unexpected trunk/NBT at "+x+","+y+",6: "+arena.world.blockState(x,y,6));
                    }
                    if(capture.markers().size()!=(allowed?1:0))throw new AssertionError("NBT effect filter should select only the matching decoration; got "+capture.markers().size());
                    String expected=INITIAL.replace("flag:1b",allowed?"flag:2b":"flag:9b");
                    if(!arena.world.state().matches(BlockPos.toPos(x+1,101,6),definition("chest",expected)))throw new AssertionError("Decoration NBT merge lost original fields, or refusal changed it");
                    if(allowed){
                        if(!arena.world.isMaterial(x-1,101,6,"air"))throw new AssertionError("Nonmatching NBT source must break normally");
                    }else{
                        if(!arena.world.state().matches(BlockPos.toPos(x-1,101,6),definition("chest",INITIAL.replace("flag:1b","flag:0b"))))throw new AssertionError("Refusal changed the other decoration");
                        for(java.util.Map.Entry<Long,TestTrees.Block> entry:tree.blocks.entrySet()){
                            long pos=entry.getKey();
                            if(!arena.world.isMaterial(x+BlockPos.getX(pos),100+BlockPos.getY(pos),6+BlockPos.getZ(pos),entry.getValue().material))throw new AssertionError("Refused cut changed the tree");
                        }
                    }
                    if(!allowed&&!arena.world.isMaterial(x-1,100,6,"air"))throw new AssertionError("Vanilla cut should remove only the targeted root");
                    return null;
                });
            });
        }
        suite.register(platform);
    }
}
