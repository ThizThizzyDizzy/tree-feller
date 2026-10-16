package com.thizthizzydizzy.treefeller.core.test.options.item;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
public final class DurabilityTests{
    private DurabilityTests(){}
    public static void register(ITestPlatform platform){
        ItemTestSuite tests = new ItemTestSuite("durability");
        // Iron axes have 250 durability. The requirement checks remaining durability.
        for(String mode : new String[]{"value", "percent"}){
            String range = mode.equals("value")?"{ min = 100, max = 150 }":"{ min = 0.4, max = 0.6 }";
            String definition = "material = IRON_AXE, durability = { "+mode+" = "+range+" }";
            tests.add(mode+"-below-min", definition, "iron_axe[damage=151]", false)
                    .add(mode+"-at-min", definition, "iron_axe[damage=150]", true)
                    .add(mode+"-at-max", definition, "iron_axe[damage=100]", true)
                    .add(mode+"-above-max", definition, "iron_axe[damage=99]", false);
        }
        tests.register(platform);
    }
}
