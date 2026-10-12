package com.thizthizzydizzy.treefeller.core.test.options.cutting;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
public final class RespectUnbreakingTests{
    private RespectUnbreakingTests(){}
    public static void register(ITestPlatform platform){
        CuttingTestSuite tests = new CuttingTestSuite("respect-unbreaking");
        tests
                .add("true", "global.cutting.respect-unbreaking = true", "iron_axe[enchantments={unbreaking:3}]", 0, 0, 2, 1)
                .add("false", "global.cutting.respect-unbreaking = false", "iron_axe[enchantments={unbreaking:3}]", 0, 0, 8, 1)
                .register(platform);
    }
}
