package com.thizthizzydizzy.treefeller.core.test.options.cutting;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
public final class LeafBreakRangeTests{
    private LeafBreakRangeTests(){}
    public static void register(ITestPlatform platform){
        CuttingTestSuite tests = new CuttingTestSuite("leaf-break-range");
        tests
                .add("one", "global.cutting.leaf-break-range = 1", "iron_axe", 0, 0, 8, 1, 1, 0)
                .add("two", "global.cutting.leaf-break-range = 2", "iron_axe", 0, 0, 8, 1, 2, 0)
                .register(platform);
    }
}
