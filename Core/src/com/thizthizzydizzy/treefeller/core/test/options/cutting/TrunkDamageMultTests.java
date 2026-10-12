package com.thizthizzydizzy.treefeller.core.test.options.cutting;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
public final class TrunkDamageMultTests{
    private TrunkDamageMultTests(){}
    public static void register(ITestPlatform platform){
        CuttingTestSuite tests = new CuttingTestSuite("trunk-damage-mult");
        tests
                .add("zero", "global.cutting.trunk-damage-mult = 0", "iron_axe", 0, 0, 0, 1)
                .add("two", "global.cutting.trunk-damage-mult = 2", "iron_axe", 0, 0, 16, 1)
                .register(platform);
    }
}
