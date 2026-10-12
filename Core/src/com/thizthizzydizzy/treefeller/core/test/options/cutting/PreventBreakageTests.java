package com.thizthizzydizzy.treefeller.core.test.options.cutting;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
public final class PreventBreakageTests{
    private PreventBreakageTests(){}
    public static void register(ITestPlatform platform){
        CuttingTestSuite tests = new CuttingTestSuite("prevent-breakage");
        tests
                .add("false-exact", "global.cutting.prevent-breakage = false", "iron_axe[damage=242]", 0, 0, 0, 0)
                .add("true-exact", "global.cutting.prevent-breakage = true", "iron_axe[damage=242]", 0, -1, 243, 1)
                .add("true-spare", "global.cutting.prevent-breakage = true", "iron_axe[damage=241]", 0, 0, 249, 1)
                .register(platform);
    }
}
