package com.thizthizzydizzy.treefeller.core.test.options.cutting;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
public final class AnimationDelayTests{
    private AnimationDelayTests(){}
    public static void register(ITestPlatform platform){
        CuttingTestSuite tests = new CuttingTestSuite("animation-delay");
        tests
                .add("zero", "global.cutting.cutting-animation = true\nglobal.cutting.animation-delay = 0", "iron_axe", 0, 0, 8, 1)
                .add("twenty", "global.cutting.cutting-animation = true\nglobal.cutting.animation-delay = 20", "iron_axe", 0, 0, 8, 1, null, 20)
                .add("forty", "global.cutting.cutting-animation = true\nglobal.cutting.animation-delay = 40", "iron_axe", 0, 0, 8, 1, null, 40)
                .register(platform);
    }
}
