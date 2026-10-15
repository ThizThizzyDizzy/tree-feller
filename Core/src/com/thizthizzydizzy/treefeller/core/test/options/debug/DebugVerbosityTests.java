package com.thizthizzydizzy.treefeller.core.test.options.debug;
import com.thizthizzydizzy.mcautotester.mod.core.TestContext;
import com.thizthizzydizzy.treefeller.core.debug.*;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import com.thizthizzydizzy.treefeller.core.test.*;
import java.util.*;
import java.util.concurrent.*;
/** Real cuts, native output emission, independent channel extremes and command-driven replay. */
public final class DebugVerbosityTests{
    private final TestTrees.Tree tree=TestTrees.specimen(TestTrees.Kind.SPRUCE,6);
    private TestArena arena;
    private boolean readyForCamera,cameraUsed;
    private int fixtures,lastIndex;
    private static final String BASE="trees=[{trunk=[spruce_log],leaves=[spruce_leaves],saplings=[spruce_sapling],max_saplings=1}]\n"
            +"global.result.cascade.enable=true\nglobal.result.leaves_drop_chance=0\n"
            +"global.result.sapling_replant {enable=true,use_tree_saplings=false,spawn_saplings=true,sapling_timeout=3,grasses=[grass_block,dirt]}\n"
            +"global.result.effects=[{type=PARTICLE,location=TOOL,particle=FLAME,count=4,chance=1}]\n";
    public static void register(ITestPlatform hooks){
        new DebugVerbosityTests().registerSuite(hooks,"debug.text-level",true,false);
        new DebugVerbosityTests().registerSuite(hooks,"debug.overlay-level",false,false);
        new DebugVerbosityTests().registerSuite(hooks,"debug.step-through",true,true);
    }
    private void registerSuite(ITestPlatform hooks,String option,boolean text,boolean step){
        fixtures=2;
        OptionTestSuite suite=new OptionTestSuite(option).lifecycle(this::prepare,this::finish,this::cleanup);
        for(int i=0;i<fixtures;i++){
            final int index=i;
            DebugLevel level=i==0?DebugLevel.USER:DebugLevel.TRACE;
            String name=step&&i==0?"replay":level.name().toLowerCase(Locale.ROOT);
            suite.add(name,BASE+"debug {chat="+text+",block_changes="+(!text||step)+",text_level="+level+",overlay_level="+level
                    +",step_through="+step+",overlay_duration="+(!text&&level==DebugLevel.USER?20:0)+"}\n",
                    (ctx,platform) -> run(ctx,platform,index,level,text,step));
        }
        suite.register(hooks);
    }
    private void prepare(TestContext ctx,ITestPlatform hooks){
        cameraUsed=false;
        arena=TestArena.setup(ctx,hooks,OptionTestSuite.PLAYER,-4,-6,fixtures*20+8,99);
        int[][] positions=new int[fixtures*2][2];
        for(int i=0;i<fixtures;i++){positions[i*2]=new int[]{i*20,2};positions[i*2+1]=new int[]{i*20+5,2};}
        arena.spawnAll(tree,positions);
    }
    private void run(TestContext ctx,ITestPlatform hooks,int index,DebugLevel level,boolean text,boolean step){
        readyForCamera=false;
        lastIndex=index;
        int x=index*20;
        List<TreeFellerDebugger.Output> outputs=new ArrayList<>();
        IPlayerConnector player=TestArena.onServer(ctx,() -> {
            TreeFellerDebugger.clear(hooks.playerState(OptionTestSuite.PLAYER));
            hooks.preparePlayer(OptionTestSuite.PLAYER,true);
            return hooks.playerState(OptionTestSuite.PLAYER);
        });
        ctx.teleportPlayer(OptionTestSuite.PLAYER,x+.5,arena.floorY+1,-1.5,0.0,0.0);
        TreeFellerDebugger.Capture capture=TestArena.onServer(ctx,() -> TreeFellerDebugger.observe(output -> {if(output.player==player)outputs.add(output);}));
        Completion completion=new Completion(x);
        try{
            TestArena.onServer(ctx,() -> {
                completion.poll();
                hooks.showTestStatus(OptionTestSuite.PLAYER,"debug | "+(step?"step-through":text?"text":"world overlay")+" = "+level+" | fell and replant two connected trees");
                return null;
            });
            ctx.breakBlock(x,arena.floorY+2,2);
            try{completion.finished.get(20,TimeUnit.SECONDS);}
            catch(Exception ex){
                String state=TestArena.onServer(ctx,() -> "source base="+arena.world.blockState(x,100,2)+", neighbor base="+arena.world.blockState(x+5,100,2)+", soils="+arena.world.blockState(x,99,2)+" / "+arena.world.blockState(x+5,99,2)
                        +", phases="+outputs.stream().map(o -> o.phase).distinct().collect(java.util.stream.Collectors.toList())
                        +", last="+outputs.stream().skip(Math.max(0,outputs.size()-12)).map(o -> o.phase+":"+o.mark+"@"+o.pos).collect(java.util.stream.Collectors.toList()));
                throw new IllegalStateException("Felling/replant completion failed: "+state,ex);
            }
            arena.assertLogsRemoved(tree,x,2);arena.assertLogsRemoved(tree,x+5,2);
            if(!text&&level==DebugLevel.USER)ctx.waitTicks(21);
            if(step){
                int before=TestArena.onServer(ctx,() -> {
                    if(!outputs.isEmpty())throw new AssertionError("Step-through emitted output before next");
                    int pending=TreeFellerDebugger.pending(player);
                    if(pending<2)throw new AssertionError("Trace was not recorded");
                    return pending;
                });
                TestArena.onServer(ctx,() -> {hooks.showTestStatus(OptionTestSuite.PLAYER,"debug.step-through = true | advance exactly one recorded step");return null;});
                ctx.sendPlayerCommand("treefeller debug next");
                TestArena.onServer(ctx,() -> {
                    if(TreeFellerDebugger.pending(player)!=before-1||outputs.size()!=1)throw new AssertionError("Next must emit exactly one step");
                    return null;
                });
                if(level==DebugLevel.TRACE)traceWalkthrough(ctx,hooks,player,outputs,x);
                if(level==DebugLevel.TRACE){
                    for(int phase=0;phase<3&&TestArena.onServer(ctx,() -> TreeFellerDebugger.pending(player))>0;phase++){
                        TreeFellerDebugger.ReplayStep boundary=TestArena.onServer(ctx,() -> TreeFellerDebugger.peek(player));
                        TestArena.onServer(ctx,() -> {hooks.showTestStatus(OptionTestSuite.PLAYER,"debug.step-through = TRACE | next phase: "+boundary.phase);return null;});
                        int start=TestArena.onServer(ctx,outputs::size);
                        ctx.sendPlayerCommand("treefeller debug next phase");
                        TestArena.onServer(ctx,() -> {
                            if(outputs.size()==start)throw new AssertionError("Phase did not advance");
                            for(TreeFellerDebugger.Output output:outputs.subList(start,outputs.size()))
                                if(output.phase!=boundary.phase)throw new AssertionError("Phase crossed its boundary");
                            TreeFellerDebugger.ReplayStep next=TreeFellerDebugger.peek(player);
                            if(next!=null&&next.phase==boundary.phase)throw new AssertionError("Phase stopped early");
                            return null;
                        });
                        ctx.waitTicks(20);
                    }
                }
                {
                    TestArena.onServer(ctx,() -> {hooks.showTestStatus(OptionTestSuite.PLAYER,"debug.step-through = "+level+" | next all: explicitly finish the remaining trace");return null;});
                    ctx.sendPlayerCommand("treefeller debug next all");
                    TestArena.onServer(ctx,() -> {if(TreeFellerDebugger.pending(player)!=0)throw new AssertionError("All left pending events");return null;});
                }
            }
            TestArena.onServer(ctx,() -> {
                EnumSet<DebugPhase> phases=EnumSet.noneOf(DebugPhase.class);
                for(TreeFellerDebugger.Output output:outputs){
                    if(output.mark==null&&output.text==null)continue; // restoration
                    if(!level.includes(output.level))throw new AssertionError("Output exceeded verbosity: "+output.level);
                    if(text&&output.text!=null||(!text||step)&&output.mark!=null)phases.add(output.phase);
                    if(!step&&text&&output.mark!=null)throw new AssertionError("Text-only case emitted an overlay");
                    if(!text&&output.text!=null)throw new AssertionError("Overlay-only case emitted chat debug");
                }
                EnumSet<DebugPhase> expected=EnumSet.allOf(DebugPhase.class);expected.remove(DebugPhase.EVENTS);
                if(!phases.containsAll(expected)){expected.removeAll(phases);throw new AssertionError("Missing phase output: "+expected);}
                if(level==DebugLevel.TRACE){
                    if(text){
                        requireText(outputs,"material did not match");requireText(outputs,"Trunk Count: 6");
                        requireText(outputs,"velocity=");requireText(outputs,"Fractional drop roll=");requireText(outputs,"Effect PARTICLE");
                    }else{
                        for(OverlayMark mark:new OverlayMark[]{OverlayMark.CANDIDATE,OverlayMark.REJECTED,OverlayMark.ACCEPTED,OverlayMark.SELECTED,OverlayMark.REMOVED,OverlayMark.DROP,OverlayMark.REPLANT,OverlayMark.EFFECT,OverlayMark.CASCADE})
                            if(outputs.stream().noneMatch(o -> o.mark==mark))throw new AssertionError("Missing overlay mark "+mark);
                        if(TreeFellerDebugger.overlaySize(player)==0)throw new AssertionError("Overlay was not retained");
                    }
                }else if(!text){
                    if(TreeFellerDebugger.overlaySize(player)!=0)throw new AssertionError("Timed overlay did not expire");
                    if(outputs.stream().noneMatch(o -> o.pos!=null&&o.mark==null&&o.text==null))throw new AssertionError("No real blocks were restored");
                }else if(text){
                    requireText(outputs,"Found tree: 6 logs");requireText(outputs,"tool durability cost");requireText(outputs,"Replanting finished");
                }
                return null;
            });
            if(step){
                TestArena.onServer(ctx,() -> {hooks.showTestStatus(OptionTestSuite.PLAYER,"debug.step-through = true | restore displayed blocks and clear the trace");return null;});
                ctx.sendPlayerCommand("treefeller debug clear");
                TestArena.onServer(ctx,() -> {
                    if(TreeFellerDebugger.pending(player)!=0||TreeFellerDebugger.overlaySize(player)!=0)throw new AssertionError("Clear did not restore the overlay and discard steps");
                    return null;
                });
            }
            readyForCamera=true;
        }finally{
            completion.active=false;
            TestArena.onServer(ctx,() -> {capture.close();return null;});
        }
    }
    /** The requested TRACE demonstration observes successive states in one final camera view. */
    private void traceWalkthrough(TestContext ctx,ITestPlatform hooks,IPlayerConnector player,List<TreeFellerDebugger.Output> outputs,int x){
        TestArena.onServer(ctx,() -> {hooks.prepareCamera(OptionTestSuite.PLAYER);return null;});
        ctx.teleportPlayer(OptionTestSuite.PLAYER,x+.5,109,-.5,0.0,90.0);
        cameraUsed=true;
        TestArena.onServer(ctx,() -> {hooks.showTestStatus(OptionTestSuite.PLAYER,"debug.step-through = true, level=TRACE | advance to the first air candidate (clear glass)");return null;});
        TreeFellerDebugger.Output candidate=advanceUntil(ctx,player,outputs,o -> o.phase==DebugPhase.DETECTION&&o.mark==OverlayMark.CANDIDATE
                &&o.pos!=null&&!fixtureContains(o.pos,x)&&BlockPos.getY(o.pos)>=100);
        long pos=candidate.pos;
        TestArena.onServer(ctx,() -> {
            int px=BlockPos.getX(pos),py=BlockPos.getY(pos),pz=BlockPos.getZ(pos);
            if(!arena.world.isMaterial(px,py,pz,"air"))throw new AssertionError("Expected the candidate to be real air");
            return null;
        });
        ctx.waitTicks(20); // Explicitly requested observation of a paused TRACE step.
        TestArena.onServer(ctx,() -> {hooks.showTestStatus(OptionTestSuite.PLAYER,"debug.step-through = true, level=TRACE | reject that same air position (red glass)");return null;});
        advanceUntil(ctx,player,outputs,o -> o.phase==DebugPhase.DETECTION&&o.mark==OverlayMark.REJECTED&&o.pos!=null&&o.pos==pos);
        TestArena.onServer(ctx,() -> {requireText(outputs,"actual=AIR");return null;});
        ctx.waitTicks(20);
        TestArena.onServer(ctx,() -> {hooks.showTestStatus(OptionTestSuite.PLAYER,"debug.step-through = true, level=TRACE | finish the first trunk-neighbor scan layer");return null;});
        DebugLayer current=candidate.layer;
        if(current==null)throw new AssertionError("Candidate lacks a recorded layer");
        int start=TestArena.onServer(ctx,outputs::size);
        ctx.sendPlayerCommand("treefeller debug next layer");
        TestArena.onServer(ctx,() -> {
            if(outputs.size()==start)throw new AssertionError("Layer did not advance");
            for(TreeFellerDebugger.Output output:outputs.subList(start,outputs.size()))
                if(output.layer!=current)throw new AssertionError("Layer crossed its recorded boundary");
            TreeFellerDebugger.ReplayStep next=TreeFellerDebugger.peek(player);
            if(next!=null&&next.layer==current)throw new AssertionError("Layer stopped early");
            return null;
        });
        TestArena.onServer(ctx,() -> {
            for(TreeFellerDebugger.Output output:outputs)if(output.phase==DebugPhase.DETECTION&&output.mark==OverlayMark.REJECTED){
                long rejected=output.pos;
                int dx=Math.abs(BlockPos.getX(rejected)-x);
                int dy=Math.abs(BlockPos.getY(rejected)-101);
                int dz=Math.abs(BlockPos.getZ(rejected)-2);
                TestTrees.Block block=fixtureBlock(rejected,x);
                if(block!=null&&block.axis!=null||dx>1||dy>1||dz>1)throw new AssertionError("Invalid rejection in the first trunk scan neighborhood");
            }
            requireText(outputs,"actual=SPRUCE_LEAVES");
            return null;
        });
        ctx.waitTicks(20);
        TestArena.onServer(ctx,() -> {hooks.showTestStatus(OptionTestSuite.PLAYER,"debug.step-through = TRACE | next layer: advance another scan pass, including its preamble");return null;});
        int layerStart=TestArena.onServer(ctx,outputs::size);
        ctx.sendPlayerCommand("treefeller debug next layer");
        TestArena.onServer(ctx,() -> {
            DebugLayer emitted=null;
            for(TreeFellerDebugger.Output output:outputs.subList(layerStart,outputs.size())){
                if(output.phase!=DebugPhase.DETECTION)throw new AssertionError("Layer crossed a phase");
                if(output.layer!=null){
                    if(output.layer==current||emitted!=null&&emitted!=output.layer)throw new AssertionError("Layer crossed scan passes");
                    emitted=output.layer;
                }
            }
            if(emitted==null)throw new AssertionError("Next layer did not reach a scan pass");
            TreeFellerDebugger.ReplayStep next=TreeFellerDebugger.peek(player);
            if(next!=null&&next.layer==emitted)throw new AssertionError("Next layer stopped early");
            return null;
        });
        ctx.waitTicks(20);
    }
    private boolean fixtureContains(long pos,int x){return fixtureBlock(pos,x)!=null;}
    private TestTrees.Block fixtureBlock(long pos,int x){
        int px=BlockPos.getX(pos),py=BlockPos.getY(pos),pz=BlockPos.getZ(pos);
        for(int origin:new int[]{x,x+5}){
            TestTrees.Block block=tree.blocks.get(BlockPos.toPos(px-origin,py-100,pz-2));
            if(block!=null)return block;
        }
        return null;
    }
    private TreeFellerDebugger.Output advanceUntil(TestContext ctx,IPlayerConnector player,List<TreeFellerDebugger.Output> outputs,java.util.function.Predicate<TreeFellerDebugger.Output> condition){
        for(int i=0;i<1000;i++){
            int before=TestArena.onServer(ctx,() -> TreeFellerDebugger.pending(player));
            if(before==0)throw new AssertionError("Trace ended before the checkpoint");
            ctx.sendPlayerCommand("treefeller debug next");
            TreeFellerDebugger.Output output=TestArena.onServer(ctx,() -> {
                if(TreeFellerDebugger.pending(player)!=before-1)throw new AssertionError("Next did not advance exactly one TRACE event");
                return outputs.get(outputs.size()-1);
            });
            if(condition.test(output))return output;
        }
        throw new AssertionError("TRACE checkpoint was not reached");
    }
    private static void requireText(List<TreeFellerDebugger.Output> outputs,String text){
        if(outputs.stream().noneMatch(o -> o.text!=null&&o.text.contains(text)))throw new AssertionError("Missing debug text: "+text);
    }
    private final class Completion{
        final int x;volatile boolean active=true;final CompletableFuture<Void> finished=new CompletableFuture<>();
        Completion(int x){this.x=x;}
        void poll(){
            if(!active)return;
            try{
                if(arena.world.isMaterial(x,100,2,"spruce_sapling")&&arena.world.isMaterial(x+5,100,2,"spruce_sapling")){
                    finished.complete(null);active=false;
                }else com.thizthizzydizzy.treefeller.core.TreeFellerCore.connector.schedule(1,this::poll);
            }catch(Throwable ex){finished.completeExceptionally(ex);active=false;}
        }
    }
    private void cleanup(TestContext ctx,ITestPlatform hooks){
        if(!readyForCamera)TestArena.onServer(ctx,() -> {TreeFellerDebugger.clear(hooks.playerState(OptionTestSuite.PLAYER));return null;});
    }
    private void finish(TestContext ctx,ITestPlatform hooks){
        try{
            if(!cameraUsed){
                TestArena.onServer(ctx,() -> {hooks.prepareCamera(OptionTestSuite.PLAYER);return null;});
                ctx.teleportPlayer(OptionTestSuite.PLAYER,lastIndex*20+2.5,119,-2.5,0.0,90.0);
            }
            ctx.waitTicks(40);
        }finally{readyForCamera=false;cleanup(ctx,hooks);}
    }
}
