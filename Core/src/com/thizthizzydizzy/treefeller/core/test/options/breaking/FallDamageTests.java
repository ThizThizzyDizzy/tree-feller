package com.thizthizzydizzy.treefeller.core.test.options.breaking;
import com.thizthizzydizzy.treefeller.core.test.*;
public final class FallDamageTests{
    private FallDamageTests(){}
    public static void register(ITestPlatform platform){
        for(String group : new String[]{"trunk","leaves","decoration"}){
            for(String option : new String[]{"fall-damage-amount","fall-damage-maximum"}){
                BreakingTestSuite t=new BreakingTestSuite("breaking."+group+"-behavior."+option);
                add(t,group,"disabled-hit",option.equals("fall-damage-amount")?0:1,option.equals("fall-damage-maximum")?0:40,false,0);
                if(group.equals("decoration")){
                    add(t,group,"changed-hit",.5f,option.equals("fall-damage-maximum")?4:40,false,3);
                    add(t,group,"changed-miss",.5f,option.equals("fall-damage-maximum")?4:40,true,0);
                    if(option.equals("fall-damage-maximum")){
                        add(t,group,"capped-hit",2,4,false,4);
                        add(t,group,"capped-miss",2,4,true,0);
                    }
                }else{
                    add(t,group,"enabled-hit",.25f,option.equals("fall-damage-maximum")?1:40,false,-1);
                    add(t,group,"enabled-miss",.25f,option.equals("fall-damage-maximum")?1:40,true,0);
                }
                t.register(platform);
            }
        }
    }
    private static void add(BreakingTestSuite t,String group,String name,float amount,float cap,boolean miss,float expected){
        final ITestPlatform.TestTarget[] target=new ITestPlatform.TestTarget[1];
        String config="global.breaking."+group+"-behavior = { fall-as-entities = true, landing = BREAK, fall-damage-amount = "+amount+", fall-damage-maximum = "+cap+" }\n";
        t.add(name,config,(c,h,f) -> {
            TestArena.onServer(c,() -> {
                double x=f.x+.5+(group.equals("trunk")?0:group.equals("decoration")?2:1)+(miss?6:0);
                target[0]=t.arena.world.spawnTarget(x,t.arena.floorY+1,f.z+.5);t.targets.add(target[0]);return null;
            });
            t.await(c,f);
            TestArena.onServer(c,() -> {
                float damage=20-target[0].health();
                if(expected<0)t.check(damage>0&&damage<20,"Actual target takes bounded damage: "+damage);
                else t.check(Math.abs(damage-expected)<.01,"Expected damage "+expected+", got "+damage);
                target[0].remove();t.targets.remove(target[0]);return null;
            });
        });
    }
}
