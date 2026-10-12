package com.thizthizzydizzy.treefeller.core.test.options.result;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
public final class DropChanceTests{
    private DropChanceTests(){}
    public static void register(ITestPlatform platform){
        for(String group : new String[]{"trunk", "leaves", "decorations"}){
            ResultTestSuite tests = new ResultTestSuite(group+"-drop-chance", group.equals("decorations")?"decoration":group, false);
            String material = group.equals("trunk")?"spruce_log":group.equals("leaves")?"spruce_leaves":"glowstone";
            for(int chance : new int[]{0, 1, 2})tests.drops("value-"+chance, "global.result."+group+"-drop-chance = "+chance,
                    "iron_axe[enchantments={silk_touch:1}]", material, tests.units()*chance, tests.units()*chance, false);
            tests.register(platform);
        }
    }
}
