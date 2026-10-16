package com.thizthizzydizzy.treefeller.core.test.options.item;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
public final class MaterialTests{
    private MaterialTests(){}
    public static void register(ITestPlatform platform){
        new ItemTestSuite("material")
                .add("match", "material = IRON_AXE", "iron_axe", true)
                .add("mismatch", "material = DIAMOND_AXE", "iron_axe", false)
                .add("wildcard-iron", "material = null", "iron_axe", true)
                .add("wildcard-diamond", "material = null", "diamond_axe", true)
                .register(platform);
    }
}
