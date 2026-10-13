package com.thizthizzydizzy.treefeller.core.breaking;
import com.thizthizzydizzy.treefeller.core.config.structure.section.BreakingConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.breaking.FallDirection;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
import java.util.Random;
/** Platform-independent motion. Direction is selected once per fell. */
public final class FallPhysics{
    private FallPhysics(){}
    public static FallVector direction(BreakingConfiguration c, long origin, FallVector player, Random random){
        double angle;
        if(c.fall_direction==FallDirection.RANDOM||c.fall_direction==null)angle=random.nextDouble()*Math.PI*2;
        else{
            angle=Math.toRadians(value(c.fall_direction_yaw));
            if(c.fall_direction==FallDirection.RELATIVE){
                double dx=BlockPos.getX(origin)+.5-player.x, dz=BlockPos.getZ(origin)+.5-player.z;
                angle+=Math.atan2(dx,-dz);
            }
        }
        FallVector v=new FallVector(Math.sin(angle),0,-Math.cos(angle));
        return Boolean.TRUE.equals(c.fall_cardinal)?cardinal(v):v;
    }
    private static FallVector cardinal(FallVector v){
        if(Math.abs(v.x)>Math.abs(v.z))return new FallVector(Math.signum(v.x),0,0);
        return new FallVector(0,0,Math.signum(v.z));
    }
    public static FallVector velocity(BreakingConfiguration c, long pos, long origin, FallVector direction, Random random){
        FallVector v=direction.scale(value(c.directional_fall_velocity))
                .add(new FallVector((random.nextDouble()*2-1)*value(c.random_fall_velocity),value(c.vertical_fall_velocity),
                        (random.nextDouble()*2-1)*value(c.random_fall_velocity)));
        FallVector radial=new FallVector(BlockPos.getX(pos)-BlockPos.getX(origin),0,BlockPos.getZ(pos)-BlockPos.getZ(origin));
        double length=radial.horizontalLength();
        if(length>0){
            radial=Boolean.TRUE.equals(c.fall_cardinal)?cardinal(radial):radial.scale(1/length);
            v=v.add(radial.scale(value(c.explosive_fall_velocity)));
        }
        return v;
    }
    /** V1 natural falls travel their height horizontally; gravity and drag determine flight time. */
    public static FallVector naturalVelocity(long pos, int lowest, FallVector direction, double upward){
        double height=Math.max(0,BlockPos.getY(pos)-lowest);
        if(height==0)return new FallVector(0,upward,0);
        double remaining=height, downward=-upward;
        int ticks=0;
        while(remaining>0&&ticks<1200){downward=(downward+.04)*.98;remaining-=downward;ticks++;}
        double horizontal=height*.02/(1-Math.pow(.98,Math.max(1,ticks)));
        FallVector v=direction.scale(horizontal).add(new FallVector(0,upward,0));
        return v.finite()?v:new FallVector(0,0,0);
    }
    public static float value(Float v){return v==null?0:v;}
}
