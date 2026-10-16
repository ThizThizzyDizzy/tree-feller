package com.thizthizzydizzy.treefeller.core.test.options.breaking;
import com.thizthizzydizzy.treefeller.core.test.*;
import java.util.*;
/** Each v1 mode is expressed using independent v2 fields for each block group. */
public final class FellBehaviorTests{
    private FellBehaviorTests(){}
    public static void register(ITestPlatform platform){
        for(String group : new String[]{"trunk","leaves","decoration"}){
            BreakingTestSuite t=new BreakingTestSuite("breaking."+group+"-behavior");
            t.cutRoot=true;
            for(String mode : new String[]{"BREAK","INVENTORY","NATURAL","FALL","FALL_HURT","FALL_BREAK","FALL_HURT_BREAK","FALL_INVENTORY","FALL_HURT_INVENTORY","FALL_NATURAL","FALL_NATURAL_HURT","FALL_NATURAL_BREAK","FALL_NATURAL_HURT_BREAK","FALL_NATURAL_INVENTORY","FALL_NATURAL_HURT_INVENTORY"}){
                boolean entity=mode.startsWith("FALL"),natural=mode.contains("NATURAL"),inventory=mode.contains("INVENTORY");
                boolean place=natural&&!mode.contains("BREAK")&&!inventory||entity&&!mode.contains("BREAK")&&!inventory;
                String landing=inventory?"INVENTORY":place?"PLACE":"BREAK";
                String path="global.breaking."+group+"-behavior.";
                String config=path+"natural = "+natural+"\n"+path+"fall-as-entities = "+entity+"\n"+path+"landing = "+landing+"\n"
                        +path+"fall-damage-amount = "+(mode.contains("HURT")?2:0)+"\n"
                        +"global.breaking.directional-fall-velocity = 0.3\n"
                        +"global.result."+(group.equals("decoration")?"decorations":group)+"-drop-chance = 1\n"
                        +"global.result."+group+"-silk-touch = true\n";
                String material=group.equals("trunk")?"spruce_log":group.equals("leaves")?"spruce_leaves":"glowstone";
                int units=group.equals("trunk")?8:group.equals("leaves")?(int)t.tree.blocks.values().stream().filter(b -> b.axis==null).count():1;
                t.add(mode.toLowerCase(Locale.ROOT).replace('_','-'),config,(ctx,h,f) -> {
                    TestArena.onServer(ctx,() -> {h.equipItem(OptionTestSuite.PLAYER,"iron_axe[enchantments={silk_touch:1}]");
                        return null;});
                },(ctx,h,f) -> {
                    if(entity){
                        TestArena.onServer(ctx,() -> {
                            t.check(!t.falling(f).isEmpty(),"Expected live falling entities for "+mode);
                            t.equal(0,h.inventoryItemCount(OptionTestSuite.PLAYER,material),"No premature inventory delivery");
                            // Some lowest blocks may have landed already; at least the upper entity remains.
                            return null;
                        });
                        t.await(ctx,f);
                    }else if(natural)ctx.waitTicks(2);
                    TestArena.onServer(ctx,() -> {
                        int placed=t.blocks(f,material),dropped=t.drops(f,material),carried=h.inventoryItemCount(OptionTestSuite.PLAYER,material);
                        if(place&&entity&&group.equals("leaves")){
                            // Dense falling foliage can contest a destination in the same
                            // tick. Refused placement must yield the original drops once.
                            t.check(placed>0,"Foliage PLACE must place blocks");
                            t.equal(units,placed+dropped,"Placed foliage plus collision fallback drops");
                        }else{
                            t.equal(place?units:0,placed,"Placed "+material+" for "+mode);
                            t.equal(!place&&!inventory?units:0,dropped,"World drops for "+mode);
                        }
                        t.equal(inventory?units:0,carried,"Inventory items for "+mode);
                        t.equal(0,t.falling(f).size(),"No remaining entities");
                        return null;
                    });
                });
            }
            t.register(platform);
        }
    }
}
