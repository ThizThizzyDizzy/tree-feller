package com.thizthizzydizzy.treefeller.core.test.options.criteria;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
import com.thizthizzydizzy.treefeller.core.test.TestTrees;
public final class HeightRatioTests{
    private HeightRatioTests(){}
    public static void register(ITestPlatform platform){
        // Intact crowns: oak is 6 high / 5 wide, spruce is 9 high / 5 wide.
        RangeCriteriaTests.register(platform, "height-ratio", TestTrees.specimen(TestTrees.Kind.OAK), 1.2,
                TestTrees.specimen(TestTrees.Kind.SPRUCE), 1.8);
    }
}
