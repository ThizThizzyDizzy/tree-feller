package com.thizthizzydizzy.treefeller.core.test.options.cutting;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
public final class PartialTrunkLimitTests{
    private PartialTrunkLimitTests(){}
    public static void register(ITestPlatform platform){
        CuttingTestSuite tests = new CuttingTestSuite("partial-trunk-limit");
        tests
                .add("unlimited", "global.cutting.partial-trunk-limit = null", "iron_axe", 0, 0, 8, 1)
                .add("limited", "global.cutting.partial-trunk-limit = 3", "iron_axe", 0, 5, 3, 1)
                .add("within-limit", "global.cutting.partial-trunk-limit = 8", "iron_axe", 0, 0, 8, 1)
                .register(platform);
    }
}
