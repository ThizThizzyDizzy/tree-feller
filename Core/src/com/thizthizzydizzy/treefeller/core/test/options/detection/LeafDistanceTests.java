package com.thizthizzydizzy.treefeller.core.test.options.detection;
import com.thizthizzydizzy.treefeller.core.test.*;
public final class LeafDistanceTests{
    public static void register(ITestPlatform hooks){
        FixtureOptionTestSuite suite=new FixtureOptionTestSuite("detection.block-data-rules.use-leaf-distance");
        for(boolean use : new boolean[]{true,false}){
            FixtureOptionTestSuite.Fixture f=suite.add(use?"enabled":"disabled",
                    "global.detection.block_data_rules.use_leaf_distance = "+use);
            // Separate arms: increasing distance is accepted; equal/decreasing distance is rejected.
            f.block(1,0,0,"spruce_leaves",null,1,"air").block(2,0,0,"spruce_leaves",null,2,"air");
            f.block(-1,0,0,"spruce_leaves",null,2,"air").block(-2,0,0,"spruce_leaves",null,2,use?"spruce_leaves":"air");
            f.block(0,0,1,"spruce_leaves",null,2,"air").block(0,0,2,"spruce_leaves",null,1,use?"spruce_leaves":"air");
        }
        suite.register(hooks);
    }
}
