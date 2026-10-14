package com.thizthizzydizzy.treefeller.core.breaking;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeFellerConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.BreakingConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.ResultConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.breaking.FellBehavior;
import com.thizthizzydizzy.treefeller.core.config.structure.section.breaking.Landing;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IBlockDefinition;
import com.thizthizzydizzy.treefeller.core.connector.item.IItemConnector;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import com.thizthizzydizzy.treefeller.core.connector.player.PlayerGameMode;
import com.thizthizzydizzy.treefeller.core.connector.world.*;
import com.thizthizzydizzy.treefeller.core.debug.DebuggerContext;
import com.thizthizzydizzy.treefeller.core.debug.TreeFellerDebugger;
import com.thizthizzydizzy.treefeller.core.detection.*;
import com.thizthizzydizzy.treefeller.core.result.TreeFellerResult;
import com.thizthizzydizzy.treefeller.core.result.SaplingReplant;
import java.util.*;
/** Core owns removal, motion plans, placement, and exactly-once deferred outcomes. */
public class TreeFellerBreaking{
    public static BreakingResult breakTree(IWorldConnector world, IPlayerConnector player, DetectionResult detected, List<TreeNode> nodes, IItemConnector tool){
        PreparedBreak prepared=prepare(world,player,detected,nodes,tool);
        return prepared==null?null:prepared.apply(nodes);
    }
    public static PreparedBreak prepare(IWorldConnector world, IPlayerConnector player, DetectionResult detected, List<TreeNode> nodes, IItemConnector tool){
        DebuggerContext context=TreeFellerDebugger.begin("Breaking",player,world);
        BreakingConfiguration config=TreeFellerConfiguration.getCombinedBreakingConfiguration(detected.tree,detected.tool);
        for(Float v : new Float[]{config.fall_direction_yaw,config.directional_fall_velocity,config.vertical_fall_velocity,config.explosive_fall_velocity,config.random_fall_velocity})
            if(v!=null&&!Float.isFinite(v)){context.fail("Non-finite fall configuration");return null;}
        for(FellBehavior b : new FellBehavior[]{config.trunk_behavior,config.leaves_behavior,config.decoration_behavior})
            if(b!=null&&((b.fall_damage_amount!=null&&!Float.isFinite(b.fall_damage_amount))
                    ||(b.fall_damage_maximum!=null&&!Float.isFinite(b.fall_damage_maximum)))){
                context.fail("Non-finite fall damage configuration");return null;
            }
        return new PreparedBreak(world,player,detected,nodes,tool,config,
                TreeFellerConfiguration.getCombinedResultConfiguration(detected.tree,detected.tool),context);
    }
    public static final class PreparedBreak{
        private final IWorldConnector world;
        private final IPlayerConnector player;
        private final BreakingConfiguration config;
        private final ResultConfiguration resultConfig;
        private final DebuggerContext context;
        private final Map<Long, BlockPlan> plans=new LinkedHashMap<>();
        private final List<BlockPlan> naturalPending=new ArrayList<>();
        private final SaplingReplant replant;
        private final long origin;
        private final int lowest;
        private final FallVector direction;
        private final boolean creative;
        private int remaining;
        private int naturalEntities;
        private final Map<BlockPlan,Long> naturalLandings=new LinkedHashMap<>();
        private PreparedBreak(IWorldConnector world,IPlayerConnector player,DetectionResult detected,List<TreeNode> nodes,IItemConnector tool,
                BreakingConfiguration config,ResultConfiguration resultConfig,DebuggerContext context){
            this.world=world;this.player=player;this.config=config;this.resultConfig=resultConfig;this.context=context;
            origin=detected.detected.root.pos;
            lowest=nodes.stream().filter(n -> n.type==TreeNodeType.TRUNK).mapToInt(n -> BlockPos.getY(n.pos)).min().orElse(BlockPos.getY(origin));
            remaining=nodes.size();
            creative=player.getGameMode()==PlayerGameMode.CREATIVE;
            Random random=new Random();
            direction=FallPhysics.direction(config,origin,player.position(),random);
            IItemConnector dropTool=tool.copyForDrops(true,true);
            // Capture every source before delayed layers, foliage physics, or conversions alter it.
            for(TreeNode node : nodes){
                FellBehavior behavior=node.type==TreeNodeType.TRUNK?config.trunk_behavior:node.type==TreeNodeType.LEAVES?config.leaves_behavior:config.decoration_behavior;
                if(behavior==null)behavior=new FellBehavior();
                IBlockDefinition conversion=null;
                if(config.block_conversions!=null)for(Map.Entry<IBlockDefinition,IBlockDefinition> entry : config.block_conversions.entrySet())
                    if(world.matches(node.pos,entry.getKey())){conversion=entry.getValue();break;}
                boolean natural=Boolean.TRUE.equals(behavior.natural),entity=Boolean.TRUE.equals(behavior.fall_as_entities);
                Landing landing=behavior.landing==null?(natural||entity?Landing.PLACE:Landing.BREAK):behavior.landing;
                FallVector velocity=natural?FallPhysics.naturalVelocity(node.pos,lowest,direction,FallPhysics.value(config.vertical_fall_velocity))
                        :FallPhysics.velocity(config,node.pos,origin,direction,random);
                if(conversion==null&&natural&&entity&&landing==Landing.PLACE)naturalEntities++;
                plans.put(node.pos,new BlockPlan(node,world.snapshot(node.pos),conversion,behavior,natural,entity,landing,velocity,
                        conversion==null?TreeFellerResult.collectDrops(world,player,node,dropTool,resultConfig):Collections.emptyList()));
            }
            List<TreeNode> replantNodes=new ArrayList<>();
            for(BlockPlan plan : plans.values())if(plan.conversion==null)replantNodes.add(plan.node);
            replant=new SaplingReplant(world,player,detected.tree,replantNodes,resultConfig.sapling_replant);
            for(BlockPlan plan : plans.values())if(plan.natural||plan.landing==Landing.PLACE)replant.hold(plan.node.pos);
        }
        public BreakingResult apply(List<TreeNode> nodes){
            Map<Long,List<IItemConnector>> immediate=new LinkedHashMap<>();
            List<BlockPlan> selected=new ArrayList<>();
            for(TreeNode node : nodes){
                BlockPlan plan=plans.remove(node.pos);
                if(plan==null)throw new IllegalStateException("Block processed twice");
                selected.add(plan);
                world.removeBlock(node.pos);
                if(plan.conversion!=null){
                    // Vanilla completes the initiating break after MONITOR listeners.
                    TreeFellerCore.connector.schedule(1,() -> {
                        world.setBlock(node.pos,plan.conversion);
                        context.info("Converted "+node.type,node.pos,plan.conversion);
                    });
                }else context.info("Removed "+node.type,node.pos);
                remaining--;
            }
            for(BlockPlan plan : selected){
                if(plan.conversion!=null)continue;
                if(plan.entity){
                    int delay=config.fall_delay==null?0:Math.max(0,config.fall_delay);
                    spawn(plan,delay>0);
                    if(delay>0)TreeFellerCore.connector.schedule(delay,() -> {
                        if(!plan.done){plan.held=false;plan.handle.release(plan.velocity);}
                    });
                }else if(plan.natural||plan.landing==Landing.PLACE){naturalPending.add(plan);}
                else if(plan.landing==Landing.INVENTORY)replant.deliver(plan.node.pos,plan.drops,true);
                else{
                    replant.offer(plan.node.pos,plan.drops,Boolean.TRUE.equals(resultConfig.drop_to_inventory));
                    immediate.put(plan.node.pos,plan.drops);
                }
            }
            // Lay out only after all selected sources are removed, including animated layers.
            if(remaining==0){
                List<BlockPlan> settled=new ArrayList<>(naturalPending);
                naturalPending.clear();
                // The original vanilla break finishes after this callback; do not let it
                // delete a block that has already been placed back at the cut position.
                TreeFellerCore.connector.schedule(1,() -> {
                    for(BlockPlan plan : settled)finish(plan,plan.natural?naturalTarget(plan):plan.node.pos,false,0);
                });
            }
            replant.start();
            context.pass("Processed "+nodes.size()+" blocks");
            return new BreakingResult(new ArrayList<>(nodes),immediate);
        }
        private boolean replaceable(long pos){
            if(BlockPos.getY(pos)<world.minHeight()||BlockPos.getY(pos)>=world.maxHeight())return false;
            if(world.isAir(pos))return true;
            if(resultConfig.overrideable_blocks!=null)for(IBlockDefinition block : resultConfig.overrideable_blocks)
                if(world.matches(pos,block))return true;
            return false;
        }
        private long naturalTarget(BlockPlan plan){
            long target=plan.node.pos;
            double height=Math.max(0,BlockPos.getY(target)-lowest);
            // V1's horizontal ray stops at a non-overridable obstacle, then settles downward.
            for(double distance=.1;distance<=height+.00001;distance+=.1){
                long candidate=BlockPos.toPos((int)Math.floor(BlockPos.getX(plan.node.pos)+.5+direction.x*distance),BlockPos.getY(plan.node.pos),
                        (int)Math.floor(BlockPos.getZ(plan.node.pos)+.5+direction.z*distance));
                if(!replaceable(candidate))break;
                target=candidate;
            }
            while(BlockPos.getY(target)>world.minHeight()){
                long below=BlockPos.toPos(BlockPos.getX(target),BlockPos.getY(target)-1,BlockPos.getZ(target));
                if(!replaceable(below))break;
                target=below;
            }
            return target;
        }
        private BlockSnapshot rotated(BlockPlan plan,long target){
            BlockAxis axis=plan.data.axis();
            if(axis==null||!Boolean.TRUE.equals(resultConfig.rotate_logs))return plan.data;
            double dx=Math.abs(BlockPos.getX(target)-BlockPos.getX(origin)),dy=Math.abs(BlockPos.getY(target)-BlockPos.getY(origin)),dz=Math.abs(BlockPos.getZ(target)-BlockPos.getZ(origin));
            if(dx==0&&dy==0&&dz==0)return plan.data;
            // Preserve v1's pillar-axis transformation without platform axis types in Core.
            if(dx>=dy&&dx>dz){if(axis==BlockAxis.Y)axis=BlockAxis.X;else if(axis==BlockAxis.X)axis=BlockAxis.Y;}
            else if(dz>=dy&&dz>=dx){if(axis==BlockAxis.Y)axis=BlockAxis.Z;else if(axis==BlockAxis.Z)axis=BlockAxis.X;}
            return plan.data.withAxis(axis);
        }
        private void spawn(BlockPlan plan,boolean held){
            plan.peak=BlockPos.getY(plan.node.pos)+.5;
            plan.handle=world.spawnFallingBlock(plan.node.pos,plan.data,held?new FallVector(0,0,0):plan.velocity,
                    (target,fallDistance) -> impact(plan,target,fallDistance),target -> finish(plan,target,true,0));
            if(held){
                plan.held=true;
                plan.handle.hold(new FallVector(BlockPos.getX(plan.node.pos)+.5,BlockPos.getY(plan.node.pos),BlockPos.getZ(plan.node.pos)+.5));
            }
            watch(plan,0);
        }
        private void impact(BlockPlan plan,long target,float fallDistance){
            long below=BlockPos.toPos(BlockPos.getX(target),BlockPos.getY(target)-1,BlockPos.getZ(target));
            if(BlockPos.getY(target)>world.minHeight()&&world.isPassable(below)){
                double downward=Math.min(-.1,plan.handle.velocity().y);
                plan.handle.remove();
                plan.handle=world.spawnFallingBlock(below,plan.data,new FallVector(0,downward,0),
                        (p,d) -> impact(plan,p,d),p -> finish(plan,p,true,0));
            }else finish(plan,target,false,Math.max(fallDistance,(float)(plan.peak-BlockPos.getY(target))));
        }
        private void watch(BlockPlan plan,int ticks){
            TreeFellerCore.connector.schedule(1,() -> {
                if(plan.done)return;
                FallVector p=plan.handle.position();
                plan.peak=Math.max(plan.peak,p.y);
                if(!plan.handle.alive()||(!plan.held&&ticks>=1200)||p.y<world.minHeight()){
                    long target=BlockPos.toPos((int)Math.floor(p.x),Math.max(world.minHeight(),(int)Math.floor(p.y)),(int)Math.floor(p.z));
                    finish(plan,target,true,0);
                }else{
                    if(plan.held)plan.handle.keepAlive();
                    watch(plan,plan.held?0:ticks+1);
                }
            });
        }
        private void watchLanding(BlockPlan plan){
            TreeFellerCore.connector.schedule(1,() -> {
                if(!naturalLandings.containsKey(plan))return;
                plan.handle.keepAlive();
                watchLanding(plan);
            });
        }
        private void finish(BlockPlan plan,long target,boolean failed,float distance){
            if(plan.done)return;
            plan.done=true;
            if(!(plan.natural&&plan.entity&&plan.landing==Landing.PLACE))replant.release(plan.node.pos);
            if(plan.handle!=null){
                float amount=Math.max(0,FallPhysics.value(plan.behavior.fall_damage_amount));
                float maximum=plan.behavior.fall_damage_maximum==null?40:Math.max(0,plan.behavior.fall_damage_maximum);
                float damage=Math.min(maximum,(float)Math.max(0,Math.ceil(distance-1))*amount);
                if(!failed&&damage>0)plan.handle.damageOverlappingEntities(damage);
            }
            if(plan.natural&&plan.entity&&plan.landing==Landing.PLACE){
                naturalEntities--;
                if(!failed&&!creative){
                    // Vanilla discards the impacting entity even when placement is cancelled.
                    // Replace it in the same tick with a gravity-free visual until the batch places.
                    plan.handle.remove();
                    plan.handle=world.spawnFallingBlock(target,plan.data,new FallVector(0,0,0),(p,d) -> {},p -> {});
                    plan.handle.hold(new FallVector(BlockPos.getX(target)+.5,BlockPos.getY(target),BlockPos.getZ(target)+.5));
                    naturalLandings.put(plan,target);
                    watchLanding(plan);
                }else{
                    if(plan.handle!=null)plan.handle.remove();
                    replant.release(plan.node.pos);
                    if(!creative)replant.deliver(target,plan.drops,Boolean.TRUE.equals(resultConfig.drop_to_inventory));
                }
                if(naturalEntities==0){
                    // Landing the bottom of the tree early would obstruct the higher
                    // trajectories before they can clear the original trunk footprint.
                    Set<Long> placed=new HashSet<>();
                    for(Map.Entry<BlockPlan,Long> entry : naturalLandings.entrySet()){
                        long destination=entry.getValue();
                        while(placed.contains(destination)&&BlockPos.getY(destination)+1<world.maxHeight())
                            destination=BlockPos.toPos(BlockPos.getX(destination),BlockPos.getY(destination)+1,BlockPos.getZ(destination));
                        if(replaceable(destination)){world.setBlock(destination,rotated(entry.getKey(),destination));placed.add(destination);}
                        else replant.deliver(destination,entry.getKey().drops,Boolean.TRUE.equals(resultConfig.drop_to_inventory));
                        replant.release(entry.getKey().node.pos);
                        entry.getKey().handle.remove();
                    }
                    naturalLandings.clear();
                }
                return;
            }
            if(plan.entity&&creative){
                if(plan.handle!=null)plan.handle.remove();
                return; // V1's entity falls disappear in creative mode.
            }
            if(!failed&&plan.landing==Landing.PLACE&&replaceable(target)){
                world.setBlock(target,rotated(plan,target));
                context.info("Landed "+plan.node.type,target);
            }else{
                boolean inventory=plan.landing==Landing.INVENTORY||Boolean.TRUE.equals(resultConfig.drop_to_inventory);
                replant.deliver(target,plan.drops,inventory);
                context.info("Delivered landing drops for "+plan.node.type,target);
            }
            if(plan.handle!=null)plan.handle.remove();
        }
    }
    private static final class BlockPlan{
        final TreeNode node;
        final BlockSnapshot data;
        final IBlockDefinition conversion;
        final FellBehavior behavior;
        final boolean natural,entity;
        final Landing landing;
        final FallVector velocity;
        final List<IItemConnector> drops;
        FallingBlockHandle handle;
        double peak;
        boolean done,held;
        BlockPlan(TreeNode node,BlockSnapshot data,IBlockDefinition conversion,FellBehavior behavior,boolean natural,boolean entity,Landing landing,FallVector velocity,List<IItemConnector> drops){
            this.node=node;this.data=data;this.conversion=conversion;this.behavior=behavior;this.natural=natural;this.entity=entity;this.landing=landing;this.velocity=velocity;this.drops=drops;
        }
    }
}
