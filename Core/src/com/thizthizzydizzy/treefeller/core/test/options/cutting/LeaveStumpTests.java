package com.thizthizzydizzy.treefeller.core.test.options.cutting;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
public final class LeaveStumpTests{
    private LeaveStumpTests(){}
    public static void register(ITestPlatform platform){
        CuttingTestSuite tests = new CuttingTestSuite("leave-stump");
        tests
                .add("false", "global.cutting.leave-stump = false", "iron_axe", 1, 0, 8, 1)
                .add("true", "global.cutting.leave-stump = true", "iron_axe", 1, 1, 7, 1)
                .register(platform);
    }
}
