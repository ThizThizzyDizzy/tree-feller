package com.thizthizzydizzy.treefeller.core.test.options.detection;
import com.thizthizzydizzy.treefeller.core.test.*;
public final class DiagonalLeavesTests{
    public static void register(ITestPlatform hooks){
        FixtureOptionTestSuite suite=new FixtureOptionTestSuite("detection.diagonal-leaves");
        for(boolean diagonal : new boolean[]{false,true}){
            FixtureOptionTestSuite.Fixture f=suite.add(diagonal?"enabled":"disabled",
                    "global.detection.diagonal_leaves = "+diagonal+"\nglobal.detection.leaf_detect_range = 1\nglobal.detection.block_data_rules.use_leaf_distance = false");
            f.block(1,0,1,"spruce_leaves",null,2,diagonal?"air":"spruce_leaves");
            f.block(1,0,0,"spruce_leaves",null,1,"air");
            f.block(1,4,1,"spruce_leaves",null,3,diagonal?"air":"spruce_leaves");
            f.block(-1,0,-1,"spruce_leaves",null,2,diagonal?"air":"spruce_leaves");
        }
        suite.register(hooks);
    }
}
