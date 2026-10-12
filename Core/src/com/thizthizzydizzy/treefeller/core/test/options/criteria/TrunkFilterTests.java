package com.thizthizzydizzy.treefeller.core.test.options.criteria;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
import com.thizthizzydizzy.treefeller.core.test.TestTrees;
public final class TrunkFilterTests{
    private TrunkFilterTests(){}
    public static void register(ITestPlatform platform){
        TestTrees.Tree oak = TestTrees.specimen(TestTrees.Kind.OAK);
        TestTrees.Tree birch = TestTrees.specimen(TestTrees.Kind.BIRCH);
        new CriteriaTestSuite("trunk-filter.required")
                .add("present", "global.criteria.trunk-filter.required = [oak_log]", oak, true)
                .add("missing", "global.criteria.trunk-filter.required = [oak_log]", birch, false)
                .register(platform);
        new CriteriaTestSuite("trunk-filter.blacklist")
                .add("absent", "global.criteria.trunk-filter.blacklist = [oak_log]", birch, true)
                .add("present", "global.criteria.trunk-filter.blacklist = [oak_log]", oak, false)
                .register(platform);
    }
}
