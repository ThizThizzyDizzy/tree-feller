package com.thizthizzydizzy.treefeller.core.result;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.result.SaplingReplantConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.special.*;
import com.thizthizzydizzy.treefeller.core.connector.item.IItemConnector;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import com.thizthizzydizzy.treefeller.core.connector.world.*;
import com.thizthizzydizzy.treefeller.core.detection.*;
import com.thizthizzydizzy.treefeller.core.debug.*;
import java.util.*;
/** One bounded replant lifecycle per accepted cut, shared by animated and landing drops. */
public final class SaplingReplant{
    private final DebuggerContext context;
    private final IWorldConnector world;
    private final IPlayerConnector player;
    private final SaplingReplantConfiguration config;
    private final SaplingConfiguration[] saplings;
    private final List<Long> sites = new ArrayList<>();
    private final Set<Long> settling=new HashSet<>();
    private final List<Reserved> reserved = new ArrayList<>();
    private boolean started, expired;
    private int age;
    public SaplingReplant(IWorldConnector world, IPlayerConnector player, TreeConfiguration tree,
            List<TreeNode> nodes, SaplingReplantConfiguration config){
        context=TreeFellerDebugger.begin(DebugPhase.REPLANT,player,world);
        context.values("Replant settings",config);
        this.world=world;this.player=player;this.config=config;this.saplings=tree.saplings;
        if(config==null||!Boolean.TRUE.equals(config.enable)||saplings==null||saplings.length==0||tree.max_saplings<=0){context.summary("Replanting disabled or no saplings/sites allowed");return;}
        Set<Long> trunks=new HashSet<>();
        for(TreeNode node : nodes)if(node.type==TreeNodeType.TRUNK)trunks.add(node.pos);
        for(long pos : trunks){if(soil(pos)){sites.add(pos);context.decision(DebugLevel.DETAIL,pos,OverlayMark.CANDIDATE,"Replant soil accepted");}else context.decision(DebugLevel.TRACE,pos,OverlayMark.REJECTED,"No permitted soil for sapling");}
        // V1 keeps the tallest continuous trunk columns when the cap is exceeded.
        sites.sort(Comparator.<Long>comparingInt(pos -> height(pos,trunks)).reversed().thenComparingLong(Long::longValue));
        while(sites.size()>tree.max_saplings){long pos=sites.remove(sites.size()-1);context.decision(DebugLevel.DETAIL,pos,OverlayMark.REJECTED,"Excluded by max saplings="+tree.max_saplings);}
        context.user("Replanting: "+sites.size()+" eligible sites");
        for(long pos : sites)context.mark(DebugLevel.USER,pos,OverlayMark.REPLANT);
    }
    private int height(long pos, Set<Long> trunks){
        int height=0;
        while(trunks.contains(pos)){height++;pos=BlockPos.getRelative(pos,0,1,0);}
        return height;
    }
    private boolean soil(long pos){
        long below=BlockPos.getRelative(pos,0,-1,0);
        if(BlockPos.getY(below)<world.minHeight()||config.grasses==null)return false;
        for(IBlockDefinition grass : config.grasses)if(world.matches(below,grass))return true;
        return false;
    }
    /** Original sites with outstanding block placement must settle before consuming saplings. */
    public void hold(long pos){if(sites.contains(pos))settling.add(pos);}
    public void release(long pos){settling.remove(pos);}
    public void start(){
        if(started||sites.isEmpty())return;
        started=true;
        // The initiating vanilla break must finish before a sapling replaces its target.
        TreeFellerCore.connector.schedule(1,this::tick);
    }
    private SaplingConfiguration matching(IItemConnector item){
        for(SaplingConfiguration sapling : saplings)if(item.matches(sapling.item))return sapling;
        return null;
    }
    /** Reserve only enough tree drops to fill outstanding sites; surplus keeps its usual destination. */
    public void offer(long pos, List<IItemConnector> drops, boolean inventory){
        if(expired||sites.isEmpty()||alwaysSpawn()||!Boolean.TRUE.equals(config.use_tree_saplings))return;
        for(IItemConnector item : drops){
            SaplingConfiguration sapling=matching(item);
            if(sapling==null)continue;
            int count=Math.max(0,Math.min(item.getCount(),sites.size()-reserved.size()));
            for(int i=0;i<count;i++){
                IItemConnector unit=item.copyForDrops(true,true);
                unit.setCount(1);
                reserved.add(new Reserved(pos,unit,sapling,inventory));
                context.decision(DebugLevel.DETAIL,pos,OverlayMark.DROP,"Reserved tree sapling "+unit.getItemId());
            }
            item.setCount(item.getCount()-count);
        }
    }
    public void deliver(long pos,List<IItemConnector> drops,boolean inventory){
        offer(pos,drops,inventory);
        TreeFellerResult.deliverDrops(world,player,pos,drops,inventory);
    }
    private boolean alwaysSpawn(){return Boolean.TRUE.equals(config.always_spawn_saplings);}
    private void tick(){
        age++;
        int timeout=config.sapling_timeout==null?50:Math.max(0,config.sapling_timeout);
        for(Iterator<Long> it=sites.iterator();it.hasNext();){
            long pos=it.next();
            if(settling.contains(pos)){context.trace("Waiting for landing at "+DebuggerContext.position(pos));continue;}
            boolean alreadyPlanted=false;
            for(SaplingConfiguration sapling : saplings)if(world.matches(pos,sapling.block)){alreadyPlanted=true;break;}
            if(alreadyPlanted){it.remove();continue;}
            if(!world.isAir(pos)||!soil(pos)){context.decision(DebugLevel.TRACE,pos,OverlayMark.REJECTED,"Planting blocked or soil changed, age="+age);continue;}
            if(alwaysSpawn()){
                world.setBlock(pos,saplings[0].block);
                context.decision(DebugLevel.USER,pos,OverlayMark.REPLANT,"Spawned new sapling (always)");
                it.remove();
                continue;
            }
            if(!reserved.isEmpty()){
                Reserved next=reserved.remove(0);
                world.setBlock(pos,next.sapling.block);
                context.decision(DebugLevel.USER,pos,OverlayMark.REPLANT,"Planted reserved tree sapling");
                it.remove();
                continue;
            }
            boolean planted=false;
            if(Boolean.TRUE.equals(config.use_inventory_saplings)){
                for(int slot=0;slot<player.inventorySize();slot++){
                    IItemConnector item=player.inventoryItem(slot);
                    if(item==null||item.getCount()<=0)continue;
                    SaplingConfiguration sapling=matching(item);
                    if(sapling==null)continue;
                    world.setBlock(pos,sapling.block);
                    context.decision(DebugLevel.USER,pos,OverlayMark.REPLANT,"Planted inventory sapling from slot "+slot);
                    item.setCount(item.getCount()-1);
                    player.setInventoryItem(slot,item);
                    planted=true;
                    break;
                }
            }
            if(!planted&&age>=timeout&&Boolean.TRUE.equals(config.spawn_saplings)){
                world.setBlock(pos,saplings[0].block);
                context.decision(DebugLevel.USER,pos,OverlayMark.REPLANT,"Spawned fallback sapling at timeout "+timeout);
                planted=true;
            }
            if(planted)it.remove();
        }
        if(sites.isEmpty()||age>=timeout){
            expired=true;
            context.user("Replanting finished: "+sites.size()+" unfilled sites; "+reserved.size()+" reserved items returned");
            for(Reserved next : reserved)TreeFellerResult.deliverDrops(world,player,next.pos,Collections.singletonList(next.item),next.inventory);
            reserved.clear();
            sites.clear();
            settling.clear();
        }else TreeFellerCore.connector.schedule(1,this::tick);
    }
    private static final class Reserved{
        final long pos;final IItemConnector item;final SaplingConfiguration sapling;final boolean inventory;
        Reserved(long pos,IItemConnector item,SaplingConfiguration sapling,boolean inventory){
            this.pos=pos;this.item=item;this.sapling=sapling;this.inventory=inventory;
        }
    }
}
