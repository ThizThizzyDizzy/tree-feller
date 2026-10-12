package com.thizthizzydizzy.treefeller.core.test.options.cutting;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
public final class DecorationsDamageMultTests{
    private DecorationsDamageMultTests(){}
    public static void register(ITestPlatform platform){
        CuttingTestSuite tests = new CuttingTestSuite("decorations-damage-mult");
        tests
                .add("zero", "global.cutting.decorations-damage-mult = 0", "iron_axe", 0, 0, 8, 1)
                .add("three", "global.cutting.decorations-damage-mult = 3", "iron_axe", 0, 0, 11, 1)
                .register(platform);
    }
}
