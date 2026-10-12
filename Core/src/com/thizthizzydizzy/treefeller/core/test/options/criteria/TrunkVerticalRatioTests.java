package com.thizthizzydizzy.treefeller.core.test.options.criteria;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
import com.thizthizzydizzy.treefeller.core.test.TestTrees;
public final class TrunkVerticalRatioTests{
    private TrunkVerticalRatioTests(){}
    public static void register(ITestPlatform platform){
        // Acacia has seven Y-axis and two X-axis logs; cherry has ten and two.
        RangeCriteriaTests.register(platform, "trunk-vertical-ratio", TestTrees.specimen(TestTrees.Kind.ACACIA), 3.5,
                TestTrees.specimen(TestTrees.Kind.CHERRY), 5);
    }
}
