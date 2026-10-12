package com.thizthizzydizzy.treefeller.core.test.options.cutting;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
public final class AllowPartialToolTests{
    private AllowPartialToolTests(){}
    public static void register(ITestPlatform platform){
        CuttingTestSuite tests = new CuttingTestSuite("allow-partial-tool");
        tests
                .add("false-low", "global.cutting.allow-partial-tool = false", "iron_axe[damage=247]", 0, -1, 248, 1)
                .add("false-enough", "global.cutting.allow-partial-tool = false", "iron_axe", 0, 0, 8, 1)
                .add("true-low", "global.cutting.allow-partial-tool = true", "iron_axe[damage=247]", 0, 0, 0, 0)
                .add("true-enough", "global.cutting.allow-partial-tool = true", "iron_axe", 0, 0, 8, 1)
                .register(platform);
    }
}
