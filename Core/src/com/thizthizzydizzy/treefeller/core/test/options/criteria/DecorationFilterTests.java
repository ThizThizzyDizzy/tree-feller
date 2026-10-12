package com.thizthizzydizzy.treefeller.core.test.options.criteria;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
import com.thizthizzydizzy.treefeller.core.test.TestTrees;
public final class DecorationFilterTests{
    private DecorationFilterTests(){}
    public static void register(ITestPlatform platform){
        TestTrees.Tree tree = TestTrees.specimen(TestTrees.Kind.OAK);
        // Both materials are detectable decorations, so failures cannot come from detection.
        String detection = "global.detection.decorations = [{ blocks = [glowstone, sea_lantern], direction = SIDE }]\n";
        new CriteriaTestSuite("decoration-filter.required")
                .add("present", detection+"global.criteria.decoration-filter.required = [glowstone]", tree, true, "glowstone")
                .add("missing", detection+"global.criteria.decoration-filter.required = [glowstone]", tree, false, "sea_lantern")
                .register(platform);
        new CriteriaTestSuite("decoration-filter.blacklist")
                .add("absent", detection+"global.criteria.decoration-filter.blacklist = [glowstone]", tree, true, "sea_lantern")
                .add("present", detection+"global.criteria.decoration-filter.blacklist = [glowstone]", tree, false, "glowstone")
                .register(platform);
    }
}
