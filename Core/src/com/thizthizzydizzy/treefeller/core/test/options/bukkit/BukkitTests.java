package com.thizthizzydizzy.treefeller.core.test.options.bukkit;
import com.thizthizzydizzy.treefeller.core.test.*;
import com.thizthizzydizzy.treefeller.core.compatibility.*;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import java.util.*;
public final class BukkitTests{
    private static final String PLAYER=OptionTestSuite.PLAYER;
    private static final TestTrees.Tree TREE=TestTrees.specimen(TestTrees.Kind.SPRUCE,4);
    private static final String BASE="trees=[{trunk=[spruce_log],leaves=[spruce_leaves]}]\nglobal.detection.secondary_tree_verification=false\nglobal.criteria.required_leaves=null\nglobal.result.leaves_drop_chance=0\n";
    private TestArena arena;
    private final List<Runnable> cleanup=new ArrayList<>();
    private final List<int[]> locations=new ArrayList<>();
    private BukkitTests(){}
    public static void register(ITestPlatform platform){
        new BukkitTests().events(platform);
        new BukkitTests().protection(platform,"worldguard");
        new BukkitTests().protection(platform,"griefprevention");
        new BukkitTests().placeholders(platform);
        new BukkitTests().quests(platform);
        new BukkitTests().experienceToggle(platform);
        new BukkitTests().experience(platform,"auraskills",false);
        new BukkitTests().experience(platform,"auraskills",true);
    }
    private OptionTestSuite suite(String name){return new OptionTestSuite("bukkit."+name).lifecycle((ctx,hooks) -> {
        arena=TestArena.setup(ctx,hooks,PLAYER,0,0,Math.max(48,locations.size()*16+8),99);
        arena.spawnAll(TREE,locations.toArray(new int[0][]));
        if(name.equals("compatibility.worldguard")||name.equals("compatibility.griefprevention")){
            TestArena.onServer(ctx,() -> {cleanup.add(hooks.operator(PLAYER,false));return null;});
            // WorldGuard caches operator bypass for two seconds; expire it once in shared setup.
            if(name.equals("compatibility.worldguard"))ctx.waitTicks(45);
        }
    },(ctx,hooks) -> {
        TestArena.onServer(ctx,() -> {hooks.prepareCamera(PLAYER);return null;});
        ctx.teleportPlayer(PLAYER,arena.size/2.0,arena.floorY+30,-8.5,0.0,60.0);ctx.waitTicks(40);
    },(ctx,hooks) -> TestArena.onServer(ctx,() -> {for(Runnable action:cleanup)action.run();cleanup.clear();TreeFellerCompatibility.reload();return null;}));}
    private int[] location(){int[] p={8+locations.size()*16,8};locations.add(p);return p;}
    private void prepare(com.thizthizzydizzy.mcautotester.mod.core.TestContext ctx,ITestPlatform hooks,int[] p){
        ctx.teleportPlayer(PLAYER,p[0]+.5,100,p[1]-3.5,0.0,0.0);
        TestArena.onServer(ctx,() -> {hooks.preparePlayer(PLAYER,true);TreeFellerCompatibility.reload();return null;});
    }
    private void label(com.thizthizzydizzy.mcautotester.mod.core.TestContext ctx,ITestPlatform hooks,String label){TestArena.onServer(ctx,() -> {hooks.showTestStatus(PLAYER,label);return null;});}
    private void assertTree(int[] p,boolean refused,boolean cancelledTop){
        for(Map.Entry<Long,TestTrees.Block> e:TREE.blocks.entrySet()){
            int x=BlockPos.getX(e.getKey()),y=BlockPos.getY(e.getKey()),z=BlockPos.getZ(e.getKey());
            boolean root=x==0&&y==0&&z==0;
            String expected=(refused&&!root||cancelledTop&&x==0&&z==0&&y==3)?e.getValue().material:"air";
            if(!arena.world.isMaterial(p[0]+x,100+y,p[1]+z,expected))throw new AssertionError("Expected "+expected+" at "+(p[0]+x)+","+(100+y)+","+(p[1]+z)+"; got "+arena.world.blockState(p[0]+x,100+y,p[1]+z));
        }
    }
    private void events(ITestPlatform platform){
        OptionTestSuite suite=suite("send-block-break-events");
        for(int mode=0;mode<3;mode++){
            final boolean enabled=mode!=0,cancel=mode!=1;int[] p=location();
            suite.add(mode==0?"false":mode==1?"true-allowed":"true-cancelled",BASE+"bukkit.send-block-break-events="+enabled,(ctx,hooks) -> {
                prepare(ctx,hooks,p);
                ITestPlatform.BreakCapture capture=TestArena.onServer(ctx,() -> {
                    ITestPlatform.BreakCapture result=hooks.captureBreaks(PLAYER,pos -> cancel&&BlockPos.getX(pos)==p[0]&&BlockPos.getZ(pos)==p[1]&&BlockPos.getY(pos)==103);
                    cleanup.add(result::close);return result;
                });
                label(ctx,hooks,"bukkit.send-block-break-events="+enabled+" | "+(cancel?"listener refuses top log":"listener allows all blocks"));
                ctx.breakBlock(p[0],100,p[1]);
                TestArena.onServer(ctx,() -> {
                    assertTree(p,false,enabled&&cancel);
                    List<Long> positions=capture.positions();
                    if(new HashSet<>(positions).size()!=positions.size())throw new AssertionError("Duplicate/recursive block break events: "+positions.size());
                    if(positions.size()!=(enabled?TREE.blocks.size():1))throw new AssertionError("Expected "+(enabled?TREE.blocks.size():1)+" events; got "+positions.size());
                    return null;
                });
            });
        }
        int[] p=location();
        suite.add("true-cancelled-natural-animation",BASE+"bukkit.send-block-break-events=true\nglobal.cutting.cutting-animation=true\nglobal.cutting.animation-delay=20\nglobal.breaking.trunk-behavior={natural=true,fall-as-entities=true,landing=PLACE}",(ctx,hooks) -> {
            prepare(ctx,hooks,p);
            TestArena.onServer(ctx,() -> {
                ITestPlatform.BreakCapture capture=hooks.captureBreaks(PLAYER,pos -> BlockPos.getX(pos)==p[0]&&BlockPos.getZ(pos)==p[1]&&BlockPos.getY(pos)==103);
                cleanup.add(capture::close);return null;
            });
            label(ctx,hooks,"bukkit.send-block-break-events=true | cancel last log while earlier animated natural falls wait to place");
            ctx.breakBlock(p[0],100,p[1]);ctx.waitTicks(105);
            TestArena.onServer(ctx,() -> {
                if(!arena.world.isMaterial(p[0],103,p[1],"spruce_log"))throw new AssertionError("Cancelled top log was removed");
                int logs=0;
                for(int x=p[0]-6;x<=p[0]+6;x++)for(int z=p[1]-6;z<=p[1]+6;z++)for(int y=100;y<=108;y++)if(arena.world.isMaterial(x,y,z,"spruce_log"))logs++;
                java.util.List<ITestPlatform.FallingObservation> falling=arena.world.fallingBlocks().stream()
                    .filter(entity -> Math.abs(entity.x-p[0])<=6&&Math.abs(entity.z-p[1])<=6&&entity.y>=98&&entity.y<=120)
                    .collect(java.util.stream.Collectors.toList());
                if(logs!=4||!falling.isEmpty())throw new AssertionError("Cancelled final natural entity must release earlier landings; blocks="+logs+", entities="+falling.stream().map(entity -> entity.x+","+entity.y+","+entity.z).collect(java.util.stream.Collectors.toList()));
                return null;
            });
        });
        suite.register(platform);
    }
    private void protection(ITestPlatform platform,String id){
        OptionTestSuite suite=suite("compatibility."+id);
        for(int mode=0;mode<4;mode++){
            final boolean enabled=mode<2,owner=mode%2==0;int[] p=location();
            suite.add((enabled?"true":"false")+(owner?"-owner":"-outsider"),BASE+"bukkit.compatibility."+id+"="+enabled,(ctx,hooks) -> {
                prepare(ctx,hooks,p);
                TestArena.onServer(ctx,() -> {
                    Map<String,Boolean> permissions=new HashMap<>();
                    permissions.put("worldguard.region.bypass."+arena.world.dimension(),false);
                    permissions.put("worldguard.region.bypass.world",false);
                    permissions.put("griefprevention.ignoreclaims",false);
                    permissions.put("griefprevention.adminclaims",false);
                    cleanup.add(hooks.permissions(PLAYER,permissions));
                    cleanup.add(TreeFellerCompatibility.hook(id).testRegion(arena.world.state(),BlockPos.toPos(p[0]+1,100,p[1]-2),BlockPos.toPos(p[0]+4,110,p[1]+2),owner?hooks.playerId(PLAYER):UUID.randomUUID()));
                    return null;
                });
                label(ctx,hooks,"bukkit.compatibility."+id+"="+enabled+" | "+(owner?"claim owner allowed":"outsider refused when enabled"));
                ctx.breakBlock(p[0],100,p[1]);
                TestArena.onServer(ctx,() -> {assertTree(p,enabled&&!owner,false);return null;});
            });
        }
        suite.register(platform);
    }
    private void quests(ITestPlatform platform){
        OptionTestSuite suite=suite("compatibility.quests");
        for(boolean enabled:new boolean[]{false,true}){
            int[] p=location();
            suite.add(Boolean.toString(enabled),BASE+"bukkit.compatibility.quests="+enabled,(ctx,hooks) -> {
                prepare(ctx,hooks,p);
                PluginCompatibility.TestCounter progress=TestArena.onServer(ctx,() -> {
                    Map<String,Object> options=new HashMap<>();options.put("amount",100);options.put("block","SPRUCE_LOG");
                    PluginCompatibility.TestCounter counter=TreeFellerCompatibility.hook("quests").testQuest(hooks.playerState(PLAYER),options);
                    cleanup.add(counter::close);return counter;
                });
                label(ctx,hooks,"bukkit.compatibility.quests="+enabled+" | fell tree and check blockbreak quest progress");
                ctx.breakBlock(p[0],100,p[1]);
                TestArena.onServer(ctx,() -> {
                    assertTree(p,false,false);
                    // The compatibility reports every log while it is still a live block.
                    int expected=enabled?4:0;
                    if(progress.count()!=expected)throw new AssertionError("Expected quest progress "+expected+", got "+progress.count());
                    return null;
                });
            });
        }
        suite.register(platform);
    }
    private void experienceToggle(ITestPlatform platform){
        OptionTestSuite suite=suite("compatibility.auraskills");
        for(boolean enabled:new boolean[]{false,true}){
            xpCase(suite,Boolean.toString(enabled),BASE+"bukkit.compatibility.auraskills="+enabled+"\nbukkit.compatibility.auraskills-trunk-xp={\"auraskills/mining\"=2}\nbukkit.compatibility.auraskills-apply-modifiers=false",false,enabled?8:0,
                "bukkit.compatibility.auraskills="+enabled+" | configured mining XP "+(enabled?"awarded":"suppressed"));
        }
        suite.register(platform);
    }
    private void experience(ITestPlatform platform,String id,boolean leaves){
        OptionTestSuite suite=suite("compatibility."+id+(leaves?"-leaves-xp":"-trunk-xp"));
        long blocks=TREE.blocks.values().stream().filter(block -> block.material.equals(leaves?"spruce_leaves":"spruce_log")).count();
        String option=id+(leaves?"-leaves-xp":"-trunk-xp");
        String raw="\nbukkit.compatibility.auraskills-apply-modifiers=false";
        for(int value:new int[]{0,2})xpCase(suite,Integer.toString(value),BASE+"bukkit.compatibility."+option+"={\"auraskills/mining\"="+value+"}"+raw,leaves,blocks*value,
            "bukkit.compatibility."+option+"="+value+" XP per block | verify mining XP");
        if(!leaves){
            String global=BASE+"bukkit.compatibility.auraskills-trunk-xp={\"auraskills/mining\"=2}"+raw;
            xpCase(suite,"animated",global+"\nglobal.cutting.cutting-animation=true\nglobal.cutting.animation-delay=1",false,blocks*2,
                "bukkit.compatibility.auraskills-trunk-xp=2 | animated cut batches XP after ten ticks");
            String tree="trees=[{trunk=[spruce_log],leaves=[spruce_leaves],bukkit.compatibility.auraskills-trunk-xp={\"auraskills/mining\"=0}}]";
            xpCase(suite,"tree-zero",global+"\n"+tree,false,0,"trees[].bukkit.compatibility.auraskills-trunk-xp=0 | override global XP=2");
            xpCase(suite,"tool-conflict",global+"\n"+tree+"\ntools=[{item=iron_axe,bukkit.compatibility.auraskills-trunk-xp={\"auraskills/mining\"=3}}]",false,blocks*3,
                "tools[].bukkit.compatibility.auraskills-trunk-xp=3 | overrides tree XP=0 and global XP=2");
        }
        suite.register(platform);
    }
    private void xpCase(OptionTestSuite suite,String name,String config,boolean leaves,double expected,String description){
        int[] p=location();
        suite.add(name,config,(ctx,hooks) -> {
            prepare(ctx,hooks,p);
            final CompatibilityContext[] context={null};
            double before=TestArena.onServer(ctx,() -> {
                context[0]=new CompatibilityContext(arena.world.state(),hooks.playerState(PLAYER),hooks.tool(PLAYER),
                    new com.thizthizzydizzy.treefeller.core.detection.DetectionResult(null,TreeFellerCore.config.trees[0],TreeFellerCore.config.tools[0]),Collections.emptyList());
                return TreeFellerCompatibility.hook("auraskills").testExperience(context[0],"auraskills/mining");
            });
            label(ctx,hooks,description);
            ctx.breakBlock(p[0],100,p[1]);
            ctx.waitTicks(TestArena.onServer(ctx,() -> Boolean.TRUE.equals(com.thizthizzydizzy.treefeller.core.config.structure.TreeFellerConfiguration.getCombinedCuttingConfiguration(TreeFellerCore.config.trees[0],TreeFellerCore.config.tools[0]).cutting_animation)?12:2));
            TestArena.onServer(ctx,() -> {
                assertTree(p,false,false);
                double after=TreeFellerCompatibility.hook("auraskills").testExperience(context[0],"auraskills/mining");
                if(Math.abs(after-before-expected)>.001)throw new AssertionError("Expected XP delta "+expected+", got "+(after-before));
                return null;
            });
        });
    }
    private void placeholders(ITestPlatform platform){
        OptionTestSuite suite=suite("compatibility.placeholderapi");
        for(boolean enabled:new boolean[]{false,true}){
            int[] p=location();
            suite.add(Boolean.toString(enabled),BASE+"bukkit.compatibility.placeholderapi="+enabled+"\nglobal.trigger.cooldown=200",(ctx,hooks) -> {
                prepare(ctx,hooks,p);
                label(ctx,hooks,"bukkit.compatibility.placeholderapi="+enabled+" | fell tree and resolve toggle/cooldown placeholders");
                ctx.breakBlock(p[0],100,p[1]);
                TestArena.onServer(ctx,() -> {
                    assertTree(p,false,false);
                    PluginCompatibility hook=TreeFellerCompatibility.hook("placeholderapi");
                    String toggle=hook.testPlaceholder(hooks.playerState(PLAYER),"toggled");
                    if(!toggle.equals(enabled?"ON":"%treefeller_toggled%"))throw new AssertionError("Unexpected toggle placeholder: "+toggle);
                    String cooldown=hook.testPlaceholder(hooks.playerState(PLAYER),"cooldown_global_ms");
                    if(enabled){if(Long.parseLong(cooldown)<=0)throw new AssertionError("Cooldown was not exposed: "+cooldown);}
                    else if(!cooldown.equals("%treefeller_cooldown_global_ms%"))throw new AssertionError("Disabled expansion still resolves placeholders");
                    return null;
                });
            });
        }
        suite.register(platform);
    }
}
