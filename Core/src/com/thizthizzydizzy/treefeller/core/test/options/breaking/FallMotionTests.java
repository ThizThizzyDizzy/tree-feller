package com.thizthizzydizzy.treefeller.core.test.options.breaking;
import com.thizthizzydizzy.treefeller.core.test.*;
import java.util.List;
public final class FallMotionTests{
    private FallMotionTests(){}
    private static final String FALL="global.breaking.trunk-behavior.fall-as-entities = true\nglobal.breaking.trunk-behavior.landing = BREAK\n";
    public static void register(ITestPlatform platform){
        BreakingTestSuite direction=new BreakingTestSuite("breaking.fall-direction");
        direction.add("fixed",FALL+"global.breaking.fall-direction = FIXED\nglobal.breaking.directional-fall-velocity = 0.5\n",(c,h,f) -> verify(c,h,direction,f,.5,0,false))
                .add("relative",FALL+"global.breaking.fall-direction = RELATIVE\nglobal.breaking.fall-direction-yaw = 0\nglobal.breaking.directional-fall-velocity = 0.5\n",(c,h,f) -> verify(c,h,direction,f,0,.5,false))
                .add("random",FALL+"global.breaking.fall-direction = RANDOM\nglobal.breaking.directional-fall-velocity = 0.5\n",(c,h,f) -> verify(c,h,direction,f,0,0,true)).register(platform);
        BreakingTestSuite yaw=new BreakingTestSuite("breaking.fall-direction-yaw");
        for(int angle : new int[]{0,90,180,270,45,450})yaw.add("fixed-"+angle,FALL+"global.breaking.fall-direction-yaw = "+angle+"\nglobal.breaking.directional-fall-velocity = 0.5\n",
                (c,h,f) -> verify(c,h,yaw,f,.5*Math.sin(Math.toRadians(angle)),-.5*Math.cos(Math.toRadians(angle)),false));
        yaw.add("relative-right",FALL+"global.breaking.fall-direction = RELATIVE\nglobal.breaking.fall-direction-yaw = 90\nglobal.breaking.directional-fall-velocity = 0.5\n",
                (c,h,f) -> verify(c,h,yaw,f,-.5,0,false)).register(platform);
        BreakingTestSuite cardinal=new BreakingTestSuite("breaking.fall-cardinal");
        for(boolean enabled : new boolean[]{false,true})cardinal.add("fixed-"+enabled,FALL+"global.breaking.fall-cardinal = "+enabled+"\nglobal.breaking.fall-direction-yaw = 30\nglobal.breaking.directional-fall-velocity = 0.5\n",
                (c,h,f) -> verify(c,h,cardinal,f,enabled?0:.25,enabled?-.5:-.5*Math.cos(Math.toRadians(30)),false));
        cardinal.add("random-locked",FALL+"global.breaking.fall-cardinal = true\nglobal.breaking.fall-direction = RANDOM\nglobal.breaking.directional-fall-velocity = 0.5\n",(c,h,f) -> {
            TestArena.onServer(c,() -> {for(ITestPlatform.FallingObservation p : cardinal.falling(f))cardinal.check(Math.abs(p.vx)<.0001||Math.abs(p.vz)<.0001,"Random motion must be cardinal");return null;});
            verify(c,h,cardinal,f,0,0,true);
        }).register(platform);
        for(String option : new String[]{"directional-fall-velocity","vertical-fall-velocity","explosive-fall-velocity","random-fall-velocity"}){
            BreakingTestSuite t=new BreakingTestSuite("breaking."+option);
            for(double value : new double[]{0,.6})t.add(value==0?"zero":"changed",FALL+"global.breaking."+option+" = "+value+"\n",(c,h,f) -> {
                TestArena.onServer(c,() -> {
                    List<ITestPlatform.FallingObservation> observed=t.falling(f);t.check(!observed.isEmpty(),"Need real falling blocks");
                    for(ITestPlatform.FallingObservation p : observed){
                        t.check(Double.isFinite(p.vx)&&Double.isFinite(p.vy)&&Double.isFinite(p.vz),"Finite velocity");
                        if(option.equals("directional-fall-velocity"))near(t,p.vx,value,"Directional magnitude");
                        else if(option.equals("vertical-fall-velocity")){if(value>0)t.check(p.vy>.2&&p.vy<=value,"Upward motion");else t.check(p.vy<=0,"No upward motion");}
                        else if(option.equals("explosive-fall-velocity"))near(t,p.vx,0,"Center has no radial motion");
                        else{t.check(Math.abs(p.vx)<=value+.0001&&Math.abs(p.vz)<=value+.0001,"Random bounds");}
                    }
                    if(option.equals("random-fall-velocity")&&value>0)t.check(observed.stream().anyMatch(p -> Math.hypot(p.vx,p.vz)>.01),"Random motion is nonzero");
                    return null;
                });
                t.await(c,f);TestArena.onServer(c,() -> {t.equal(8,t.drops(f,"spruce_log")+h.inventoryItemCount(OptionTestSuite.PLAYER,"spruce_log"),"All landing drops, including client pickups");return null;});
            });
            if(option.equals("explosive-fall-velocity")){
                String config="global.breaking.leaves-behavior.fall-as-entities = true\nglobal.breaking.leaves-behavior.landing = BREAK\nglobal.breaking.explosive-fall-velocity = .6\n";
                t.add("off-center",config,(c,h,f) -> {
                    TestArena.onServer(c,() -> {List<ITestPlatform.FallingObservation> observed=t.falling(f);t.check(!observed.isEmpty(),"Need radial foliage entities");
                        t.check(observed.stream().anyMatch(p -> Math.hypot(p.vx,p.vz)>.4),"Off-axis radial velocity");
                        for(ITestPlatform.FallingObservation p : observed)if(Math.hypot(p.vx,p.vz)>.01)t.check((p.x-f.x-.5)*p.vx+(p.z-f.z-.5)*p.vz>0,"Radial motion points away from origin");return null;});t.await(c,f);
                });
            }
            if(option.equals("directional-fall-velocity"))t.add("combined-components",FALL+"global.breaking.directional-fall-velocity = .4\nglobal.breaking.vertical-fall-velocity = .6\nglobal.breaking.explosive-fall-velocity = .7\nglobal.breaking.random-fall-velocity = 0\n",(c,h,f) -> {
                TestArena.onServer(c,() -> {for(ITestPlatform.FallingObservation p : t.falling(f)){near(t,p.vx,.4,"Directional component preserved");near(t,p.vz,0,"No center radial component");t.check(p.vy>.2,"Vertical component added");}return null;});t.await(c,f);
            });
            t.register(platform);
        }
    }
    private static void near(BreakingTestSuite t,double observed,double expected,String message){
        t.check(Math.abs(observed-expected)<.13,message+": expected near "+expected+", got "+observed);
    }
    private static void verify(com.thizthizzydizzy.mcautotester.mod.core.TestContext c,ITestPlatform h,BreakingTestSuite t,BreakingTestSuite.Fixture f,double x,double z,boolean random){
        TestArena.onServer(c,() -> {
            List<ITestPlatform.FallingObservation> observed=t.falling(f);t.check(!observed.isEmpty(),"Falling blocks must spawn");
            for(ITestPlatform.FallingObservation p : observed){
                if(random)t.check(Math.hypot(p.vx,p.vz)>.35&&Math.hypot(p.vx,p.vz)<=.501,"Random direction magnitude");
                else{near(t,p.vx,x,"X direction");near(t,p.vz,z,"Z direction");}
            }
            if(random){ITestPlatform.FallingObservation first=observed.get(0);for(ITestPlatform.FallingObservation p : observed)t.check(first.vx*p.vx+first.vz*p.vz>0,"One coherent direction per tree");}
            return null;
        });
        t.await(c,f);TestArena.onServer(c,() -> {t.equal(8,t.drops(f,"spruce_log")+h.inventoryItemCount(OptionTestSuite.PLAYER,"spruce_log"),"Exactly one drop per trunk, including client pickups");return null;});
    }
}
