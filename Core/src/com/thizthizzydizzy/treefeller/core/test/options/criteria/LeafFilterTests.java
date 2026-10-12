package com.thizthizzydizzy.treefeller.core.test.options.criteria;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
import com.thizthizzydizzy.treefeller.core.test.TestTrees;
public final class LeafFilterTests{
    private LeafFilterTests(){}
    public static void register(ITestPlatform platform){
        TestTrees.Tree oak = TestTrees.specimen(TestTrees.Kind.OAK);
        TestTrees.Tree birch = TestTrees.specimen(TestTrees.Kind.BIRCH);
        new CriteriaTestSuite("leaf-filter.required")
                .add("present", "global.criteria.leaf-filter.required = [oak_leaves]", oak, true)
                .add("missing", "global.criteria.leaf-filter.required = [oak_leaves]", birch, false)
                .register(platform);
        new CriteriaTestSuite("leaf-filter.blacklist")
                .add("absent", "global.criteria.leaf-filter.blacklist = [oak_leaves]", birch, true)
                .add("present", "global.criteria.leaf-filter.blacklist = [oak_leaves]", oak, false)
                .register(platform);
    }
}
