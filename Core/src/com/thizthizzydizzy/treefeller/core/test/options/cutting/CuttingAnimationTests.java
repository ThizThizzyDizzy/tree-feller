package com.thizthizzydizzy.treefeller.core.test.options.cutting;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
public final class CuttingAnimationTests{
    private CuttingAnimationTests(){}
    public static void register(ITestPlatform platform){
        CuttingTestSuite tests = new CuttingTestSuite("cutting-animation");
        tests
                .add("false", "global.cutting.cutting-animation = false", "iron_axe", 0, 0, 8, 1)
                .add("true", "global.cutting.cutting-animation = true\nglobal.cutting.animation-delay = 20", "iron_axe", 0, 0, 8, 1, null, 20)
                .register(platform);
    }
}
