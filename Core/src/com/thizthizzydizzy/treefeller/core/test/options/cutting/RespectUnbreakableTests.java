package com.thizthizzydizzy.treefeller.core.test.options.cutting;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
public final class RespectUnbreakableTests{
    private RespectUnbreakableTests(){}
    public static void register(ITestPlatform platform){
        CuttingTestSuite tests = new CuttingTestSuite("respect-unbreakable");
        tests
                .add("true", "global.cutting.respect-unbreakable = true", "iron_axe[unbreakable={}]", 0, 0, 0, 1)
                .add("false", "global.cutting.respect-unbreakable = false", "iron_axe[unbreakable={}]", 0, 0, 8, 1)
                .register(platform);
    }
}
