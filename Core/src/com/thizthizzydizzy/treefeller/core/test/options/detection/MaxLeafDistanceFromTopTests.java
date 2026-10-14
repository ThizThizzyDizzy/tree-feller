package com.thizthizzydizzy.treefeller.core.test.options.detection;
import com.thizthizzydizzy.treefeller.core.test.*;
public final class MaxLeafDistanceFromTopTests{
    public static void register(ITestPlatform hooks){
        TestTrees.Tree tree=TestTrees.specimen(TestTrees.Kind.SPRUCE);
        FixtureOptionTestSuite suite=new FixtureOptionTestSuite("detection.max-leaf-distance-from-top");
        for(int limit : new int[]{0,2}){
            FixtureOptionTestSuite.Fixture f=suite.add("limit-"+limit+"-boundary",
                    "global.detection.max_leaf_distance_from_top = "+limit+"\nglobal.detection.leaf_detect_range = 1");
            f.tree=tree;
            f.expect(1,7-limit,0,"air").expect(1,6-limit,0,"spruce_leaves");
        }
        suite.register(hooks);
    }
}
