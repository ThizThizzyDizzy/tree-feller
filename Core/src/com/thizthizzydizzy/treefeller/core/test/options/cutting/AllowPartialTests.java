package com.thizthizzydizzy.treefeller.core.test.options.cutting;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
public final class AllowPartialTests{
    private AllowPartialTests(){}
    public static void register(ITestPlatform platform){
        CuttingTestSuite tests = new CuttingTestSuite("allow-partial");
        tests
                .add("false-low", "global.cutting.allow-partial = false\nglobal.cutting.allow-partial-tool = false", "iron_axe[damage=247]", 0, -1, 248, 1)
                .add("false-enough", "global.cutting.allow-partial = false\nglobal.cutting.allow-partial-tool = false", "iron_axe", 0, 0, 8, 1)
                .add("true-low", "global.cutting.allow-partial = true\nglobal.cutting.allow-partial-tool = false", "iron_axe[damage=247]", 0, 5, 0, 0)
                .add("true-enough", "global.cutting.allow-partial = true\nglobal.cutting.allow-partial-tool = false", "iron_axe", 0, 0, 8, 1)
                .register(platform);
    }
}
