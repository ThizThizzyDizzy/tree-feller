package com.thizthizzydizzy.treefeller.core.test.options.cutting;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
public final class StackedToolsTests{
    private StackedToolsTests(){}
    public static void register(ITestPlatform platform){
        CuttingTestSuite tests = new CuttingTestSuite("stacked-tools");
        tests
                .add("false", "global.cutting.stacked-tools = false", "iron_axe[damage=247]", 0, 0, 0, 0)
                .add("true", "global.cutting.stacked-tools = true", "iron_axe[damage=247]", 0, 0, 5, 1)
                .register(platform);
    }
}
