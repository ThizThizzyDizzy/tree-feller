package com.thizthizzydizzy.treefeller.core.test.options.criteria;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
import com.thizthizzydizzy.treefeller.core.test.TestTrees;
/** Each bound is independently configured; equality passes and the outside value refuses. */
final class RangeCriteriaTests{
    private RangeCriteriaTests(){}
    static void register(ITestPlatform platform, String option, TestTrees.Tree lower, Number low,
            TestTrees.Tree upper, Number high){
        new CriteriaTestSuite(option+".min")
                .add("at-minimum", config(option, "min", high), upper, true)
                .add("below-minimum", config(option, "min", high), lower, false)
                .register(platform);
        new CriteriaTestSuite(option+".max")
                .add("at-maximum", config(option, "max", low), lower, true)
                .add("above-maximum", config(option, "max", low), upper, false)
                .register(platform);
    }
    private static String config(String option, String bound, Number value){
        // Clear the other bound inherited from defaults, so each sequence isolates one setting.
        return "global.criteria."+option+" = { min = null, max = null, "+bound+" = "+value+" }";
    }
}
