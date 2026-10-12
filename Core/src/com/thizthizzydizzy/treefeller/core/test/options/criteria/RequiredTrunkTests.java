package com.thizthizzydizzy.treefeller.core.test.options.criteria;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
import com.thizthizzydizzy.treefeller.core.test.TestTrees;
public final class RequiredTrunkTests{
    private RequiredTrunkTests(){}
    public static void register(ITestPlatform platform){
        RangeCriteriaTests.register(platform, "required-trunk", TestTrees.specimen(TestTrees.Kind.SPRUCE, 5), 5,
                TestTrees.specimen(TestTrees.Kind.SPRUCE, 8), 8);
    }
}
