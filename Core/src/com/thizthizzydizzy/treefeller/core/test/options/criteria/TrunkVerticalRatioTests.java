package com.thizthizzydizzy.treefeller.core.test.options.criteria;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
import com.thizthizzydizzy.treefeller.core.test.TestTrees;
public final class TrunkVerticalRatioTests{
    private TrunkVerticalRatioTests(){}
    public static void register(ITestPlatform platform){
        // Acacia has seven Y-axis and two X-axis logs; cherry has ten and two.
        TestTrees.Tree forked=TestTrees.specimen(TestTrees.Kind.CHERRY);
        if(!platform.supportsMaterial("cherry_log"))forked=forked.withWood(TestTrees.Kind.OAK);
        RangeCriteriaTests.register(platform, "trunk-vertical-ratio", TestTrees.specimen(TestTrees.Kind.ACACIA), 3.5,
                forked, 5);
    }
}
