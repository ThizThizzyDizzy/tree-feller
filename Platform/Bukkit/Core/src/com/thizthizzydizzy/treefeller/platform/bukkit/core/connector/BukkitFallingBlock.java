package com.thizthizzydizzy.treefeller.platform.bukkit.core.connector;
import com.thizthizzydizzy.treefeller.core.breaking.FallVector;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
import com.thizthizzydizzy.treefeller.core.connector.world.FallingBlockHandle;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.LongConsumer;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
/** Native handles and callbacks only; all landing decisions are made in Core. */
public final class BukkitFallingBlock implements FallingBlockHandle{
    private static final Map<java.util.UUID,BukkitFallingBlock> tracked=new HashMap<>();
    final FallingBlock entity;
    private final BiConsumer<Long,Float> impact;
    private final LongConsumer failed;
    BukkitFallingBlock(FallingBlock entity,BiConsumer<Long,Float> impact,LongConsumer failed){
        this.entity=entity;this.impact=impact;this.failed=failed;
        tracked.put(entity.getUniqueId(),this);
    }
    public static BukkitFallingBlock get(Entity entity){return tracked.get(entity.getUniqueId());}
    public void impact(long pos){impact.accept(pos,entity.getFallDistance());}
    public void failed(){Location p=entity.getLocation();failed.accept(BlockPos.toPos(p.getBlockX(),p.getBlockY(),p.getBlockZ()));}
    @Override public boolean alive(){return entity.isValid()&&!entity.isDead();}
    @Override public FallVector position(){Location p=entity.getLocation();return new FallVector(p.getX(),p.getY(),p.getZ());}
    @Override public FallVector velocity(){org.bukkit.util.Vector v=entity.getVelocity();return new FallVector(v.getX(),v.getY(),v.getZ());}
    @Override public void remove(){tracked.remove(entity.getUniqueId());entity.remove();}
    @Override public void damageOverlappingEntities(float amount){
        for(Entity target : entity.getNearbyEntities(0,0,0))if(target instanceof LivingEntity)((LivingEntity)target).damage(amount,entity);
    }
}
