package com.thizthizzydizzy.treefeller.core.test.options.criteria;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
import com.thizthizzydizzy.treefeller.core.test.TestTrees;
public final class TrunkMaxHorizontalLineTests{
    private TrunkMaxHorizontalLineTests(){}
    public static void register(ITestPlatform platform){
        TestTrees.Tree narrow = TestTrees.specimen(TestTrees.Kind.SPRUCE);
        TestTrees.Tree wide = TestTrees.specimen(TestTrees.Kind.DARK_OAK);
        new CriteriaTestSuite("trunk-max-horizontal-line")
                .add("max-1-at-limit", "global.criteria.trunk-max-horizontal-line = 1", narrow, true)
                .add("max-1-too-wide", "global.criteria.trunk-max-horizontal-line = 1", wide, false)
                .add("max-2-at-limit", "global.criteria.trunk-max-horizontal-line = 2", wide, true)
                .add("max-2-too-wide", "global.criteria.trunk-max-horizontal-line = 2", TestTrees.specimen(TestTrees.Kind.ACACIA), false)
                .register(platform);
    }
}
