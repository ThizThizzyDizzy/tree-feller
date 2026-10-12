package com.thizthizzydizzy.treefeller.core.test.options.cutting;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
public final class LeavesDamageMultTests{
    private LeavesDamageMultTests(){}
    public static void register(ITestPlatform platform){
        CuttingTestSuite tests = new CuttingTestSuite("leaves-damage-mult");
        tests
                .add("zero", "global.cutting.leaves-damage-mult = 0", "iron_axe", 0, 0, 8, 1)
                .add("one", "global.cutting.leaves-damage-mult = 1", "iron_axe", 0, 0, 8+tests.leaves(), 1)
                .register(platform);
    }
}
