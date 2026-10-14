package com.thizthizzydizzy.treefeller.core.test.options.detection;
import com.thizthizzydizzy.treefeller.core.test.*;
public final class LeafDetectRangeTests{
    public static void register(ITestPlatform hooks){
        FixtureOptionTestSuite suite=new FixtureOptionTestSuite("detection.leaf-detect-range");
        for(int limit : new int[]{1,2}){
            FixtureOptionTestSuite.Fixture f=suite.add("range-"+limit+"-boundary","global.detection.leaf_detect_range = "+limit);
            for(int x=2;x<=3;x++)f.block(x,3,0,"spruce_leaves",null,x,x<=limit?"air":"spruce_leaves");
            f.expect(1,3,0,"air");
        }
        suite.register(hooks);
    }
}
