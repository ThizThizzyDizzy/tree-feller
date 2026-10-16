package com.thizthizzydizzy.treefeller.core.test.options.detection;
import com.thizthizzydizzy.mcautotester.mod.core.TestContext;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockAxis;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
import com.thizthizzydizzy.treefeller.core.test.*;
import java.util.*;
/** Real client cuts, including subsequent cuts of every retained component. */
public final class SeparateTrunksTests{
    private final OptionTestSuite suite=new OptionTestSuite("detection.separate-trunks");
    private final List<Fixture> fixtures=new ArrayList<>();
    private TestArena arena;
    private Boolean previousMobSpawning;
    public static void register(ITestPlatform hooks){new SeparateTrunksTests().registerTests(hooks);}
    private void registerTests(ITestPlatform hooks){
        Fixture forest=add("true-forest-no-orphans",true,false);
        for(int row=0;row<3;row++){
            int offset=new int[]{0,3,2}[row];
            for(int col=0;col<3;col++)forest.tree(TestTrees.Kind.DARK_OAK,offset+col*6,0,row*6);
            forest.bridge(offset+1,offset+12,5,row*6);
        }
        for(int z=0;z<=6;z++)forest.log(3,5,z);
        for(int z=6;z<=12;z++)forest.log(8,5,z);
        forest.order=new int[]{4,0,8,3,5,1,7,2,6};
        forest.extra="global.criteria.max_height = 0\n";
        Fixture bare=add("crownless-clump-stays-attached",true,false);
        bare.tree(TestTrees.Kind.DARK_OAK,0,0,0).cube(20,0,0,2,2,2).bridge(1,20,5,0);
        for(int y=2;y<5;y++)bare.log(20,y,0);
        bare.together=true;
        Fixture reversed=add("tree-override-true-reverse-order",true,true);
        reversed.tree(TestTrees.Kind.DARK_OAK,0,0,0).tree(TestTrees.Kind.DARK_OAK,6,0,0).bridge(1,6,5,0);
        reversed.order=new int[]{1,0};
        reversed.extra="global.detection.secondary_tree_verification = false\nglobal.detection.block_data_rules.use_leaf_distance = false\nglobal.detection.disconnected_trunk_distance = 2\n";
        Fixture off=add("false-connected",false,false);
        off.tree(TestTrees.Kind.DARK_OAK,0,0,0).tree(TestTrees.Kind.DARK_OAK,6,0,0).bridge(1,6,5,0);
        off.together=true;
        Fixture singleOff=add("false-oversized-refused",false,false);
        singleOff.tree(TestTrees.Kind.DARK_OAK,0,0,0).cube(0,0,0,5,12,5);
        singleOff.refused=true;
        Fixture slope=add("uneven-ground",true,false);
        slope.tree(TestTrees.Kind.DARK_OAK,0,0,0).tree(TestTrees.Kind.DARK_OAK,6,3,0).bridge(1,6,5,0);
        Fixture cross=add("full-cross-section",true,false);
        cross.tree(TestTrees.Kind.DARK_OAK,0,0,0).tree(TestTrees.Kind.DARK_OAK,6,0,0).bridge(1,6,5,0);
        cross.extra="global.criteria.require_cross_section = true\n";
        cross.crossSection=true;
        cross.cuts.set(0,new int[]{1,0,1});cross.cuts.set(1,new int[]{7,0,1});
        Fixture roots=add("custom-roots-below-trunk",true,false);
        roots.tree(TestTrees.Kind.DARK_OAK,0,5,0);
        roots.cube(-4,0,0,2,2,2);
        roots.bridge(-3,0,1,0);
        for(int y=1;y<=5;y++)roots.log(0,y,0);
        roots.cuts.set(0,new int[]{0,1,0});
        roots.together=true;
        Fixture fork=add("low-fork",true,false);
        fork.tree(TestTrees.Kind.DARK_OAK,0,0,0).tree(TestTrees.Kind.DARK_OAK,6,0,0).bridge(1,6,0,0);
        fork.together=true;
        Fixture crown=add("bulky-high-branch",true,false);
        crown.tree(TestTrees.Kind.DARK_OAK,0,0,0).tree(TestTrees.Kind.DARK_OAK,6,5,0).bridge(1,6,5,0);
        crown.together=true;
        Fixture bent=add("bent-thick-trunk",true,false);
        bent.tree(TestTrees.Kind.DARK_OAK,0,0,0).tree(TestTrees.Kind.DARK_OAK,4,9,0);
        for(int i=0;i<=4;i++)bent.log(i,5+i,0);
        bent.together=true;
        Fixture decoration=add("retained-required-decoration",true,false);
        decoration.tree(TestTrees.Kind.DARK_OAK,0,0,0).tree(TestTrees.Kind.DARK_OAK,6,0,0).bridge(1,6,5,0);
        decoration.blocks.put(BlockPos.toPos(-1,1,0),new Block("glowstone",null,-1));
        decoration.blocks.put(BlockPos.toPos(3,5,-1),new Block("glowstone",null,-1));
        decoration.decorations.put(BlockPos.toPos(3,5,-1),0);
        decoration.blocks.put(BlockPos.toPos(8,1,0),new Block("glowstone",null,-1));
        decoration.decorations.put(BlockPos.toPos(-1,1,0),0);
        decoration.decorations.put(BlockPos.toPos(8,1,0),1);
        decoration.extra="global.detection.block_data_rules.use_leaf_distance = false\n"
                +"global.detection.secondary_tree_verification = false\n"
                +"global.detection.decorations = [{blocks=[glowstone],direction=SIDE}]\n"
                +"global.criteria.decoration_filter.required = [glowstone]\n";
        Fixture oak=add("large-oak-thin-branches",true,false);
        oak.tree(TestTrees.Kind.OAK,0,0,0).tree(TestTrees.Kind.OAK,5,4,0).tree(TestTrees.Kind.OAK,-4,3,0);
        oak.bridge(-4,5,4,0);oak.together=true;
        Fixture mangrove=add("mangrove-roots",true,false);
        mangrove.tree(TestTrees.Kind.MANGROVE,0,0,0);mangrove.cuts.set(0,new int[]{0,2,0});
        Fixture jungle=add("large-jungle",true,false);
        jungle.tree(TestTrees.Kind.GIANT_JUNGLE,0,0,0);
        Fixture acacia=add("bent-acacia",true,false);
        acacia.tree(TestTrees.Kind.ACACIA,0,0,0);
        Fixture dependent=add("disconnected-section-cannot-support-both-owners",true,false);
        dependent.tree(TestTrees.Kind.DARK_OAK,0,0,0).tree(TestTrees.Kind.DARK_OAK,6,0,0)
                .tree(TestTrees.Kind.DARK_OAK,0,0,6).bridge(1,6,5,0);
        dependent.blocks.put(BlockPos.toPos(0,1,6),new Block("oak_log",BlockAxis.Y,-1));
        dependent.extra="global.detection.block_data_rules.use_leaf_distance = false\n"
                +"global.detection.diagonal_leaves = true\nglobal.detection.leaf_detect_range = 12\n"
                +"global.detection.disconnected_trunk_distance = 12\n"
                +"global.criteria.required_leaves = null\n"
                +"global.criteria.trunk_filter.required = [oak_log]\n";
        dependent.together=true;
        Fixture cherry=add("forked-cherry",true,false);
        cherry.tree(TestTrees.Kind.CHERRY,0,0,0);
        Fixture oversize=add("oversized-single-refused",true,false);
        oversize.tree(TestTrees.Kind.DARK_OAK,0,0,0).cube(0,0,0,5,12,5);
        oversize.refused=true;
        Fixture high=add("above-fork-cut",true,false);
        high.tree(TestTrees.Kind.DARK_OAK,0,0,0).tree(TestTrees.Kind.DARK_OAK,6,0,0).bridge(1,6,5,0);
        high.cuts.set(0,new int[]{0,4,0});
        Fixture rootedPair=add("rooted-branch-connected-pair",true,false);
        for(int x:new int[]{0,6}){
            rootedPair.tree(TestTrees.Kind.DARK_OAK,x,4,0);
            for(int y=0;y<4;y++)rootedPair.log(x,y,0);
            rootedPair.log(x-1,0,0).log(x-2,0,0);
        }
        rootedPair.bridge(1,6,8,0);
        rootedPair.cuts.set(0,new int[]{0,0,0});rootedPair.cuts.set(1,new int[]{6,0,0});
        Fixture overrideOff=add("tree-override-false-connected",false,true);
        overrideOff.tree(TestTrees.Kind.DARK_OAK,0,0,0).tree(TestTrees.Kind.DARK_OAK,6,0,0).bridge(1,6,5,0);
        overrideOff.together=true;
        Fixture shared=add("shared-canopy-keeps-clumps-together",true,false);
        shared.tree(TestTrees.Kind.DARK_OAK,0,0,0).tree(TestTrees.Kind.DARK_OAK,6,0,0).bridge(1,6,5,0);
        shared.blocks.entrySet().removeIf(e -> e.getValue().axis==null);
        for(int x=3;x<=4;x++)for(int y=6;y<=8;y++)for(int z=-1;z<=1;z++)
            shared.blocks.put(BlockPos.toPos(x,y,z),new Block("dark_oak_leaves",null,1));
        shared.extra="global.detection.block_data_rules.use_leaf_distance = false\n";
        shared.together=true;
        Fixture small=add("undersized-clump-stays-attached",true,false);
        small.tree(TestTrees.Kind.DARK_OAK,0,0,0).cube(6,0,0,2,2,2).bridge(1,6,5,0);
        for(int y=2;y<5;y++)small.log(6,y,0);
        small.extra="global.criteria.required_trunk = \"20-256\"\n";
        small.together=true;
        Fixture large=add("oversized-neighbor-refused",true,false);
        large.tree(TestTrees.Kind.DARK_OAK,0,0,0).tree(TestTrees.Kind.DARK_OAK,6,0,0).cube(6,0,0,5,12,5).bridge(1,6,5,0);
        large.refused=true;
        Fixture required=add("required-material-keeps-clumps-together",true,false);
        required.tree(TestTrees.Kind.DARK_OAK,0,0,0).tree(TestTrees.Kind.DARK_OAK,6,0,0).bridge(1,6,5,0);
        required.blocks.put(BlockPos.toPos(0,1,0),new Block("oak_log",BlockAxis.Y,-1));
        required.extra="global.criteria.trunk_filter.required = [oak_log]\n";
        required.together=true;
        suite.lifecycle(this::prepare,this::finish,this::cleanup).register(hooks);
    }
    private Fixture add(String name,boolean separate,boolean local){
        int index=fixtures.size();
        Fixture f=new Fixture(name,separate,local,index%7*32+8,index/7*56+32);fixtures.add(f);
        suite.add(name,() -> config(f),(ctx,h) -> run(ctx,h,f));return f;
    }
    private String config(Fixture f){
        return "global.detection.separate_trunks = "+(f.local?!f.separate:f.separate)+"\n"
                +"trees=[{trunk=[dark_oak_log,oak_log,mangrove_log,jungle_log,acacia_log,cherry_log],"
                +"leaves=[dark_oak_leaves,oak_leaves,mangrove_leaves,mangrove_roots,jungle_leaves,acacia_leaves,cherry_leaves]"
                +(f.local?",detection.separate_trunks="+f.separate:"")+"}]\n"
                +"global.criteria.trunk_max_horizontal_line = null\n"
                +"debug {chat=true,text_level=USER}\n"
                +"global.result.leaves_drop_chance = 0\nglobal.result.sapling_replant.enable = false\n"+f.extra;
    }
    private void prepareAxe(ITestPlatform h){
        h.preparePlayer(OptionTestSuite.PLAYER,true);
        // The real client can mine with the entire fixture in view.
        h.equipItem(OptionTestSuite.PLAYER,"iron_axe[attribute_modifiers=[{id:'treefeller:test_reach',type:'minecraft:block_interaction_range',amount:60,operation:'add_value',slot:'mainhand'}]]");
    }
    private void prepare(TestContext ctx,ITestPlatform h){
        TestArena.onServer(ctx,() -> {
            ITestPlatform.ITestWorld world=h.world(OptionTestSuite.PLAYER);
            previousMobSpawning=world.mobSpawning();
            world.setMobSpawning(false);
            return null;
        });
        arena=TestArena.setup(ctx,h,OptionTestSuite.PLAYER,0,0,224,99);
        TestArena.onServer(ctx,() -> {
            arena.world.removeMobs(arena.minX,arena.minZ,arena.minX+arena.size-1,arena.minZ+arena.size-1);
            // Separate bays keep other test fixtures out of the action view.
            for(Fixture f:fixtures){
                int left=f.x-8,front=f.z-32;
                for(int y=100;y<124;y++){
                    for(int x=left;x<left+32;x++)for(int z:new int[]{front,front+55})
                        arena.world.setBlock(x,y,z,"white_concrete",null,-1);
                    for(int z=front;z<front+56;z++)for(int x:new int[]{left,left+31})
                        arena.world.setBlock(x,y,z,"white_concrete",null,-1);
                }
            }
            for(Fixture f:fixtures)for(Map.Entry<Long,Block> e:f.blocks.entrySet()){
                long p=e.getKey();Block b=e.getValue();
                arena.world.setBlock(f.x+BlockPos.getX(p),arena.floorY+1+BlockPos.getY(p),f.z+BlockPos.getZ(p),b.material,b.axis,b.distance);
                if(b.material.endsWith("_leaves"))arena.world.setPersistent(f.x+BlockPos.getX(p),arena.floorY+1+BlockPos.getY(p),f.z+BlockPos.getZ(p),false);
            }
            return null;
        });
    }
    private void run(TestContext ctx,ITestPlatform h,Fixture f){
        int height=f.blocks.keySet().stream().mapToInt(BlockPos::getY).max().orElse(8)+1;
        int width=f.blocks.keySet().stream().mapToInt(BlockPos::getX).max().orElse(8)
                -f.blocks.keySet().stream().mapToInt(BlockPos::getX).min().orElse(0)+1;
        double viewingDistance=Math.min(30,Math.max(16,Math.max(height*1.7,width)));
        Set<Integer> felled=new HashSet<>();
        Set<Long> crossCuts=new HashSet<>();
        int[] order=f.order;
        if(order==null){order=new int[f.cuts.size()];for(int i=0;i<order.length;i++)order[i]=i;}
        for(int index:order){
            int[] cut=f.cuts.get(index);
            ctx.teleportPlayer(OptionTestSuite.PLAYER,f.x+cut[0]+.5,arena.floorY+1,f.z-viewingDistance,0.0,0.0);
            // Position acknowledgement precedes destination chunk data on the client.
            // Wait before breakBlock can mistake an unloaded target for air.
            if(felled.isEmpty())ctx.waitTicks(10);
            if(f.crossSection){
                TestArena.onServer(ctx,() -> {prepareAxe(h);return null;});
                int[] base=f.origins.get(index);
                for(int[] offset:new int[][]{{0,0},{1,0},{0,1}}){
                    int x=base[0]+offset[0],z=base[2]+offset[1];
                    ctx.teleportPlayer(OptionTestSuite.PLAYER,f.x+x+.5,arena.floorY+1,f.z-viewingDistance,0.0,0.0);
                    TestArena.onServer(ctx,() -> {h.showTestStatus(OptionTestSuite.PLAYER,"separate_trunks=true | full-cross-section | cut ("+x+",0,"+z+")");return null;});
                    ctx.breakBlock(f.x+x,arena.floorY+1,f.z+z);
                    crossCuts.add(BlockPos.toPos(x,0,z));
                    TestArena.onServer(ctx,() -> {
                        for(int i=0;i<f.logs.size();i++)for(long p:f.logs.get(i))
                            assertMaterial(f,p,felled.contains(i)||crossCuts.contains(p)?"air":f.blocks.get(p).material);
                        return null;
                    });
                }
                ctx.teleportPlayer(OptionTestSuite.PLAYER,f.x+cut[0]+.5,arena.floorY+1,f.z-viewingDistance,0.0,0.0);
            }
            TestArena.onServer(ctx,() -> {prepareAxe(h);h.showTestStatus(OptionTestSuite.PLAYER,
                    "separate_trunks="+f.separate+" | "+f.name+" | tree "+index+" cut ("+cut[0]+","+cut[1]+","+cut[2]+")");return null;});
            {
                ctx.breakBlock(f.x+cut[0],arena.floorY+1+cut[1],f.z+cut[2]);
                felled.add(index);
                TestArena.onServer(ctx,() -> {
                    for(int i=0;i<f.logs.size();i++)for(long p:f.logs.get(i)){
                        boolean removed=f.together||felled.contains(i);
                        if(f.refused)removed=p==BlockPos.toPos(cut[0],cut[1],cut[2]);
                        assertMaterial(f,p,removed?"air":f.blocks.get(p).material);
                    }
                    if(!f.refused&&(f.together||felled.size()==f.logs.size())){
                        for(Map.Entry<Long,Block> e:f.blocks.entrySet())if(e.getValue().axis!=null)assertMaterial(f,e.getKey(),"air");
                    }
                    if(f.refused)for(Map.Entry<Long,Block> e:f.blocks.entrySet())
                        assertMaterial(f,e.getKey(),e.getKey()==BlockPos.toPos(cut[0],cut[1],cut[2])?"air":e.getValue().material);
                    for(Map.Entry<Long,Integer> e:f.decorations.entrySet())
                        assertMaterial(f,e.getKey(),felled.contains(e.getValue())?"air":f.blocks.get(e.getKey()).material);
                    if(!f.refused&&!f.together&&felled.size()==f.logs.size()&&f.logs.size()>1)
                        for(Map.Entry<Long,Block> e:f.blocks.entrySet())if(e.getValue().material.endsWith("_leaves"))assertMaterial(f,e.getKey(),"air");
                    // Every untouched crown keeps foliage close to its own trunk.
                    if(!f.together&&!f.refused)for(int i=0;i<f.cuts.size();i++)if(!felled.contains(i)){
                        int[] base=f.origins.get(i);
                        long leaf=BlockPos.toPos(base[0]-1,base[1]+4,base[2]);
                        Block b=f.blocks.get(leaf);
                        if(b!=null&&b.axis==null)assertMaterial(f,leaf,b.material);
                    }
                    return null;
                });
            }
            if(f.together||f.refused)break;
        }
    }
    private void assertMaterial(Fixture f,long p,String expected){
        int x=f.x+BlockPos.getX(p),y=arena.floorY+1+BlockPos.getY(p),z=f.z+BlockPos.getZ(p);
        if(!arena.world.isMaterial(x,y,z,expected))throw new AssertionError(f.name+": expected "+expected+" at "+x+","+y+","+z+", got "+arena.world.blockState(x,y,z));
    }
    private void cleanup(TestContext ctx,ITestPlatform h){
        TestArena.onServer(ctx,() -> {
            if(previousMobSpawning!=null){
                h.world(OptionTestSuite.PLAYER).setMobSpawning(previousMobSpawning);
                previousMobSpawning=null;
            }
            return null;
        });
    }
    private void finish(TestContext ctx,ITestPlatform h){
        TestArena.onServer(ctx,() -> {h.prepareCamera(OptionTestSuite.PLAYER);return null;});
        ctx.teleportPlayer(OptionTestSuite.PLAYER,112.0,arena.floorY+160,-16.0,0.0,55.0);ctx.waitTicks(40);
    }
    private static final class Block{
        final String material;final BlockAxis axis;final int distance;
        Block(String m,BlockAxis a,int d){material=m;axis=a;distance=d;}
    }
    private static final class Fixture{
        final String name;final int x,z;final boolean separate,local;
        final Map<Long,Block> blocks=new LinkedHashMap<>();
        final Map<Long,Integer> decorations=new LinkedHashMap<>();
        final List<Set<Long>> logs=new ArrayList<>();final List<int[]> cuts=new ArrayList<>(),origins=new ArrayList<>();
        int[] order;boolean together,refused,crossSection;String extra="";
        Fixture(String n,boolean s,boolean l,int x,int z){name=n;separate=s;local=l;this.x=x;this.z=z;}
        Fixture tree(TestTrees.Kind kind,int x,int y,int z){
            Set<Long> trunk=new HashSet<>();
            for(Map.Entry<Long,TestTrees.Block> e:TestTrees.specimen(kind).blocks.entrySet()){
                long p=BlockPos.getRelative(e.getKey(),x,y,z);TestTrees.Block b=e.getValue();
                if(b.axis!=null||!blocks.containsKey(p))blocks.put(p,new Block(b.material,b.axis,b.leafDistance));
                if(b.axis!=null)trunk.add(p);
            }
            logs.add(trunk);cuts.add(new int[]{x,y,z});origins.add(new int[]{x,y,z});return this;
        }
        Fixture log(int x,int y,int z){blocks.put(BlockPos.toPos(x,y,z),new Block("dark_oak_log",BlockAxis.Y,-1));return this;}
        Fixture bridge(int x1,int x2,int y,int z){for(int x=x1;x<=x2;x++)log(x,y,z);return this;}
        Fixture cube(int x,int y,int z,int w,int h,int d){for(int dx=0;dx<w;dx++)for(int dy=0;dy<h;dy++)for(int dz=0;dz<d;dz++)log(x+dx,y+dy,z+dz);return this;}
    }
}
