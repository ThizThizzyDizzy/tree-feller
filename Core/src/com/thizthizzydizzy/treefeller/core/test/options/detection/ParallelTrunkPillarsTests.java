package com.thizthizzydizzy.treefeller.core.test.options.detection;
import com.thizthizzydizzy.treefeller.core.test.*;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockAxis;
public final class ParallelTrunkPillarsTests{
    public static void register(ITestPlatform hooks){
        FixtureOptionTestSuite suite=new FixtureOptionTestSuite("detection.block-data-rules.ignore-parallel-trunk-pillars");
        for(boolean ignore : new boolean[]{false,true}){
            FixtureOptionTestSuite.Fixture f=suite.add(ignore?"enabled":"disabled",
                    "global.detection.block_data_rules.ignore_parallel_trunk_pillars = "+ignore);
            for(int y=0;y<4;y++)f.block(1,y,0,"spruce_log",BlockAxis.Y,-1,ignore?"spruce_log":"air");
            f.block(-1,3,0,"spruce_log",BlockAxis.X,-1,"air");
        }
        suite.register(hooks);
    }
}
