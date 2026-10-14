package com.thizthizzydizzy.treefeller.core.test.options.result;
import com.thizthizzydizzy.mcautotester.mod.core.TestContext;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
import com.thizthizzydizzy.treefeller.core.test.*;
import java.util.*;
import java.util.concurrent.*;
/** Deterministic sapling sources, intact shared fixtures, and real client cuts. */
final class SaplingTestSuite{
    private static final TestTrees.Tree TREE=TestTrees.specimen(TestTrees.Kind.SPRUCE,8);
    final OptionTestSuite suite;
    final String option;
    final List<Case> cases=new ArrayList<>();
    TestArena arena;
    SaplingTestSuite(String option){
        this.option=option;
        suite=new OptionTestSuite(option).lifecycle(this::prepare,this::finish,(ctx,hooks) -> {});
    }
    Case add(String name,String config,int planted){
        Case c=new Case(cases.size()*12,name,config,planted);
        cases.add(c);
        suite.add(name,() -> (c.defaults?"":"trees = [{ trunk = [spruce_log], leaves = [spruce_leaves], saplings = "+c.saplings+", roots = [oak_log], max_saplings = "+c.cap+" }]\n")
                +"global.result.sapling_replant { enable = true, use_tree_saplings = false, use_inventory_saplings = false, spawn_saplings = false, sapling_timeout = 8 }\n"
                +"global.result.leaves_drop_chance = 0\nglobal.criteria.required_leaves = null\n"+config,(ctx,hooks) -> run(ctx,hooks,c));
        return c;
    }
    void register(ITestPlatform hooks){suite.register(hooks);}
    private Object setting(Case c){
        if(option.equals("trees.saplings"))return c.saplings;
        if(option.equals("trees.max-saplings"))return c.cap;
        Object settings=TreeFellerCore.config.global.result.sapling_replant;
        Object value;
        try{value=settings.getClass().getField(option.substring(option.lastIndexOf('.')+1).replace('-','_')).get(settings);}
        catch(ReflectiveOperationException ex){throw new IllegalStateException("Unknown option "+option,ex);}
        if(value instanceof Object[]){
            List<Object> definitions=new ArrayList<>();
            for(Object entry : (Object[])value)definitions.add(((com.thizthizzydizzy.treefeller.core.config.ISpecialConfigObject)entry).asSimplified());
            return definitions;
        }
        return value;
    }
    private void run(TestContext ctx,ITestPlatform hooks,Case c){
        ctx.teleportPlayer(OptionTestSuite.PLAYER,c.x+.5,arena.floorY+1,-1.5,0.0,0.0);
        TestArena.onServer(ctx,() -> {
            hooks.preparePlayer(OptionTestSuite.PLAYER,true);
            hooks.equipItem(OptionTestSuite.PLAYER,"iron_axe[enchantments={silk_touch:1}]");
            if(c.inventory>0)hooks.setInventoryItem(OptionTestSuite.PLAYER,9,c.item,c.inventory);
            return null;
        });
        Observation observation=new Observation(c,hooks);
        TestArena.onServer(ctx,() -> {
            observation.poll();
            hooks.showTestStatus(OptionTestSuite.PLAYER,option+" = "+setting(c)+" | "+c.name+" | "+c.config.replace("global.result.","").replace('\n',';'));
            return null;
        });
        try{
            // Cut above the base so breakBlock can see air after replanting.
            ctx.breakBlock(c.x-(c.rootCut?1:0),arena.floorY+(c.rootCut?1:2),2);
            observation.await();
            TestArena.onServer(ctx,() -> {
                int planted=0;
                Set<Long> plantedPositions=new HashSet<>();
                for(Map.Entry<Long,TestTrees.Block> entry : c.tree.blocks.entrySet()){
                    long pos=entry.getKey();
                    int x=c.x+BlockPos.getX(pos),y=arena.floorY+1+BlockPos.getY(pos),z=2+BlockPos.getZ(pos);
                    if(arena.world.isMaterial(x,y,z,c.expectedBlock)){planted++;plantedPositions.add(pos);continue;}
                    String expected=entry.getValue().axis!=null?c.trunks:"air";
                    if(c.stump&&entry.getValue().axis!=null&&BlockPos.getY(pos)==0)expected="spruce_log";
                    if(c.blocked&&BlockPos.getY(pos)==0&&BlockPos.getX(pos)==0&&BlockPos.getZ(pos)==0)expected="stone";
                    if(!arena.world.isMaterial(x,y,z,expected))throw new AssertionError("Expected "+expected+" at "+x+","+y+","+z+", got "+arena.world.blockState(x,y,z));
                }
                if(c.rootCut&&!arena.world.isMaterial(c.x-1,arena.floorY+1,2,"air"))throw new AssertionError("Trigger root remains");
                if(planted!=c.planted)throw new AssertionError("Expected "+c.planted+" saplings, got "+planted);
                if(c.plantedPositions!=null&&!plantedPositions.equals(c.plantedPositions))
                    throw new AssertionError("Expected saplings at "+c.plantedPositions+", got "+plantedPositions);
                if(c.inventoryLeft>=0&&hooks.inventoryItemCount(OptionTestSuite.PLAYER,c.item)!=c.inventoryLeft)
                    throw new AssertionError("Tree sources must be consumed before inventory saplings");
                if(c.remaining>=0){
                    int count=hooks.inventoryItemCount(OptionTestSuite.PLAYER,c.item)
                            +arena.world.droppedItemCount(c.item,c.x-4,arena.floorY,-2,c.x+5,arena.floorY+32,7);
                    if(count!=c.remaining)throw new AssertionError("Expected "+c.remaining+" unconsumed "+c.item+", got "+count);
                }
                return null;
            });
        }finally{observation.active=false;}
    }
    private void prepare(TestContext ctx,ITestPlatform hooks){
        arena=TestArena.setup(ctx,hooks,OptionTestSuite.PLAYER,-5,-6,Math.max(24,cases.size()*12+6),99);
        TestArena.onServer(ctx,() -> {
            arena.world.removeDroppedItems(arena.minX,arena.floorY,arena.minZ,arena.minX+arena.size,arena.world.maxHeight(),arena.minZ+arena.size);
            return null;
        });
        Map<TestTrees.Tree,List<int[]>> specimens=new LinkedHashMap<>();
        for(Case c : cases)specimens.computeIfAbsent(c.tree,tree -> new ArrayList<>()).add(new int[]{c.x,2});
        for(Map.Entry<TestTrees.Tree,List<int[]>> entry : specimens.entrySet())arena.spawnAll(entry.getKey(),entry.getValue().toArray(new int[0][]));
        TestArena.onServer(ctx,() -> {
            for(Case c : cases){
                if(c.rootCut)arena.world.setBlock(c.x-1,arena.floorY+1,2,"oak_log",null,-1);
                for(Map.Entry<Long,TestTrees.Block> e : c.tree.blocks.entrySet()){
                    long pos=e.getKey();
                    if(e.getValue().axis!=null&&BlockPos.getY(pos)==0)
                        arena.world.setBlock(c.x+BlockPos.getX(pos),arena.floorY,2+BlockPos.getZ(pos),c.soil,null,-1);
                }
            }
            return null;
        });
    }
    private void finish(TestContext ctx,ITestPlatform hooks){
        TestArena.onServer(ctx,() -> {hooks.prepareCamera(OptionTestSuite.PLAYER);return null;});
        int height=Math.max(22,cases.size()*8);
        ctx.teleportPlayer(OptionTestSuite.PLAYER,(cases.size()-1)*6+.5,arena.floorY+1+height,2.5-height/Math.tan(Math.toRadians(55)),0.0,55.0);
        ctx.waitTicks(40);
    }
    static final class Case{
        final int x,planted;final String name,config;
        TestTrees.Tree tree=TREE;
        String saplings="[spruce_sapling]",item="spruce_sapling",expectedBlock="spruce_sapling",soil="grass_block",trunks="air";
        int cap=1,inventory,remaining=-1,arrival=-1,wait=12,timeout=-1,inventoryLeft=-1;
        Set<Long> plantedPositions;
        boolean blocked,stump,defaults,rootCut;
        Case(int x,String name,String config,int planted){this.x=x;this.name=name;this.config=config;this.planted=planted;}
        Case inventory(int count,int remaining){this.inventory=count;this.remaining=remaining;return this;}
        Case treeDrops(int consumed){remaining=(int)tree.blocks.values().stream().filter(b -> b.axis==null).count()-consumed;return this;}
    }
    private final class Observation{
        final Case c;final ITestPlatform hooks;final CompletableFuture<Void> finished=new CompletableFuture<>();
        volatile boolean active=true;int ticks=-1;
        Observation(Case c,ITestPlatform hooks){this.c=c;this.hooks=hooks;}
        void poll(){
            if(!active)return;
            try{
                if(ticks<0){if(!arena.world.isMaterial(c.x-(c.rootCut?1:0),arena.floorY+(c.rootCut?1:2),2,c.rootCut?"oak_log":"spruce_log"))ticks=0;}
                else ticks++;
                if(ticks==0&&c.blocked)arena.world.setBlock(c.x,arena.floorY+1,2,"stone",null,-1);
                if(ticks==c.arrival&&c.arrival>=0)hooks.setInventoryItem(OptionTestSuite.PLAYER,9,c.item,2);
                if(c.timeout>=0&&ticks>=0&&ticks<c.timeout-1&&arena.world.isMaterial(c.x,arena.floorY+1,2,c.expectedBlock))
                    throw new AssertionError("Fallback spawned before timeout "+c.timeout);
                if(ticks>=c.wait){active=false;finished.complete(null);return;}
                TreeFellerCore.connector.schedule(1,this::poll);
            }catch(Throwable ex){active=false;finished.completeExceptionally(ex);}
        }
        void await(){
            try{finished.get(15,TimeUnit.SECONDS);}
            catch(Exception ex){throw new IllegalStateException("Sapling observation failed",ex);}
        }
    }
    static final String DROPS="global.result.leaves_drop_chance = 1\nglobal.result.drop_conversions = { spruce_leaves = spruce_sapling }\n";
    static final String SPAWN="global.result.sapling_replant.spawn_saplings = true\n";
}
