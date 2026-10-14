package com.thizthizzydizzy.treefeller.core.test.options.result;
import com.thizthizzydizzy.mcautotester.mod.core.TestContext;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.test.*;
import java.util.*;
import java.util.concurrent.*;
/** Intact connected forests; one independently selectable suite per cascading option. */
public final class CascadeTests{
    private static final String BASE = "global.result.cascade.enable = true\nglobal.result.sapling_replant.enable = false\n"
            +"trees = [{trunk=[spruce_log], leaves=[spruce_leaves]}, {trunk=[birch_log], leaves=[birch_leaves]}]\n";
    private CascadeTests(){}
    public static void register(ITestPlatform hooks){
        Suite enable = new Suite("result.cascade.enable");
        enable.add("disabled", "global.result.cascade.enable=false", false, false, false, 0);
        enable.add("connected-chain", "global.criteria.max_height=0", true, false, false, 0);
        enable.add("disconnected", "", false, false, true, 0);
        enable.add("criteria-refused", "trees=[{trunk=[spruce_log],leaves=[spruce_leaves],criteria.required_trunk.min=8}]", false, false, false, 0).shortTargets=true;
        enable.add("tool-enabled", "global.result.cascade.enable=false\ntools=[{item=iron_axe,result.cascade.enable=true}]", true, false, false, 0);
        enable.register(hooks);
        Suite trees = new Suite("trees.cascade-trees");
        trees.add("default-same-species", "", true, false, false, 0);
        trees.add("default-other-species", "", false, true, false, 0);
        trees.add("empty", "trees=[{trunk=[spruce_log],leaves=[spruce_leaves],cascade_trees=[]}]", false, false, false, 0);
        trees.add("selected-other-species", "trees=[{trunk=[spruce_log],leaves=[spruce_leaves],cascade_trees=[1]}, {trunk=[birch_log],leaves=[birch_leaves]}]", true, true, false, 0);
        trees.add("selected-refuses-same-species", "trees=[{trunk=[spruce_log],leaves=[spruce_leaves],cascade_trees=[1]}, {trunk=[birch_log],leaves=[birch_leaves]}]", false, false, false, 0);
        trees.register(hooks);
        Suite parallel = new Suite("result.cascade.parallel-cascade-limit");
        for(int limit : new int[]{1,2}){
            parallel.add("immediate-"+limit,"global.result.cascade.parallel_cascade_limit="+limit,true,false,false,limit).branch=true;
            Case animated=parallel.add("animated-"+limit,"global.result.cascade.parallel_cascade_limit="+limit+"\nglobal.cutting.cutting_animation=true\nglobal.cutting.animation_delay=2",true,false,false,limit);
            animated.branch=true;animated.animated=true;
        }
        parallel.register(hooks);
        Suite checks = new Suite("result.cascade.cascade-check-limit");
        for(int limit : new int[]{1,64}){
            Case c=checks.add("checks-"+limit,"global.result.cascade.cascade_check_limit="+limit+"\nglobal.result.cascade.parallel_cascade_limit=2",true,false,false,limit==1?1:2);
            c.branch=true;
        }
        checks.register(hooks);
    }
    private static final class Case{
        final int x;final String name,config;final boolean fell,mixed,disconnected;final int cap;
        boolean branch,animated,shortTargets;
        Case(int x,String name,String config,boolean fell,boolean mixed,boolean disconnected,int cap){
            this.x=x;this.name=name;this.config=config;this.fell=fell;this.mixed=mixed;this.disconnected=disconnected;this.cap=cap;
        }
        int[] positions(){return new int[]{x,x+(disconnected?8:5),x+(disconnected?16:10)};}
        int source(){return branch?1:0;}
        TestTrees.Tree tree(int index){return TestTrees.specimen(index>0&&mixed?TestTrees.Kind.BIRCH:TestTrees.Kind.SPRUCE,index>0&&shortTargets?6:8);}
    }
    private static final class Suite{
        final String option;final OptionTestSuite suite;final List<Case> cases=new ArrayList<>();TestArena arena;
        Suite(String option){this.option=option;suite=new OptionTestSuite(option).lifecycle(this::prepare,this::finish,(ctx,hooks)->{});}
        Case add(String name,String config,boolean fell,boolean mixed,boolean disconnected,int cap){
            Case c=new Case(cases.size()*24,name,config,fell,mixed,disconnected,cap);cases.add(c);
            suite.add(name,BASE+config,(ctx,hooks)->run(ctx,hooks,c));return c;
        }
        void register(ITestPlatform hooks){suite.register(hooks);}
        Object setting(){
            if(option.equals("trees.cascade-trees"))return Arrays.toString(TreeFellerCore.config.trees[0].cascade_trees);
            try{return TreeFellerCore.config.global.result.cascade.getClass()
                    .getField(option.substring(option.lastIndexOf('.')+1).replace('-','_'))
                    .get(TreeFellerCore.config.global.result.cascade);}
            catch(ReflectiveOperationException ex){throw new IllegalStateException(ex);}
        }
        void prepare(TestContext ctx,ITestPlatform hooks){
            arena=TestArena.setup(ctx,hooks,OptionTestSuite.PLAYER,-4,-5,Math.max(24,cases.size()*24),99);
            Map<TestTrees.Tree,List<int[]>> fixtures=new LinkedHashMap<>();
            // Spawn every full crown without clearing any foliage for access.
            for(Case c:cases)for(int i=0;i<3;i++)fixtures.computeIfAbsent(c.tree(i),t->new ArrayList<>()).add(new int[]{c.positions()[i],2});
            for(Map.Entry<TestTrees.Tree,List<int[]>> e:fixtures.entrySet())arena.spawnAll(e.getKey(),e.getValue().toArray(new int[0][]));
        }
        void run(TestContext ctx,ITestPlatform hooks,Case c){
            int source=c.positions()[c.source()];
            ctx.teleportPlayer(OptionTestSuite.PLAYER,source+.5,arena.floorY+1,-1.5,0.0,0.0);
            Observation observation=new Observation(c);
            TestArena.onServer(ctx,()->{
                hooks.preparePlayer(OptionTestSuite.PLAYER,true);
                observation.poll();
                hooks.showTestStatus(OptionTestSuite.PLAYER,option+" = "+setting()+" | "+c.name+" | "+c.config.replace('\n',';')+" | connected neighbors "+(c.fell?"fell":"remain"));
                return null;
            });
            try{
                ctx.breakBlock(source,arena.floorY+1,2);
                observation.await();
                for(int i=0;i<3;i++){
                    if(i==c.source()||c.fell)arena.assertLogsRemoved(c.tree(i),c.positions()[i],2);
                    else{
                        final int index=i;
                        TestArena.onServer(ctx,()->{
                            for(int y=0;y<(c.shortTargets?6:8);y++)if(!arena.world.isMaterial(c.positions()[index],arena.floorY+1+y,2,c.mixed?"birch_log":"spruce_log"))
                                throw new AssertionError("Refused neighbor lost log at height "+y);
                            return null;
                        });
                    }
                }
                TestArena.onServer(ctx,()->{
                    int expected=8+(c.fell?16:0);
                    int damage=hooks.tool(OptionTestSuite.PLAYER).getMaxDurability()-hooks.tool(OptionTestSuite.PLAYER).getCurrentDurability();
                    if(damage!=expected)throw new AssertionError("Expected durability cost "+expected+", got "+damage);
                    return null;
                });
            }finally{observation.active=false;}
        }
        void finish(TestContext ctx,ITestPlatform hooks){
            TestArena.onServer(ctx,()->{hooks.prepareCamera(OptionTestSuite.PLAYER);return null;});
            int height=Math.max(24,cases.size()*15);
            ctx.teleportPlayer(OptionTestSuite.PLAYER,(cases.size()-1)*12+5.5,arena.floorY+height,2.5-height/Math.tan(Math.toRadians(55)),0.0,55.0);
            ctx.waitTicks(40);
        }
        final class Observation{
            final Case c;final CompletableFuture<Void> finished=new CompletableFuture<>();volatile boolean active=true;
            int ticks=-1,previousRemoved,peak;
            Observation(Case c){this.c=c;}
            void poll(){
                if(!active)return;
                try{
                    int removed=0,animating=0;
                    for(int x:c.positions()){
                        int missing=0;
                        for(int y=0;y<8;y++)if(!arena.world.isMaterial(x,arena.floorY+1+y,2,"spruce_log")
                                &&!arena.world.isMaterial(x,arena.floorY+1+y,2,"birch_log"))missing++;
                        if(missing>0)removed++;
                        if(missing>0&&missing<8)animating++;
                    }
                    if(removed>0&&ticks<0)ticks=0;
                    if(ticks>=0){
                        if(c.cap>0){
                            int simultaneous=c.animated?animating:removed-previousRemoved;
                            // The initiating manual cut is outside the cascade tick budget.
                            if(!c.animated&&previousRemoved==0)simultaneous=0;
                            if(simultaneous>c.cap)throw new AssertionError("Cascade limit "+c.cap+" exceeded: "+simultaneous);
                            peak=Math.max(peak,simultaneous);
                        }
                        int wait=c.animated?70:c.cap==1?60:20;
                        if(++ticks>=wait){
                            if(c.cap>0&&peak!=c.cap)throw new AssertionError("Expected observed concurrency "+c.cap+", got "+peak);
                            active=false;finished.complete(null);return;
                        }
                    }
                    previousRemoved=removed;
                    TreeFellerCore.connector.schedule(1,this::poll);
                }catch(Throwable ex){active=false;finished.completeExceptionally(ex);}
            }
            void await(){try{finished.get(15,TimeUnit.SECONDS);}catch(Exception ex){throw new IllegalStateException("Cascade observation failed: "+(ex.getCause()==null?ex:ex.getCause()),ex);}}
        }
    }
}
