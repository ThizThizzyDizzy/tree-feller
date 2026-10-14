package com.thizthizzydizzy.treefeller.core.test.options.detection;
import com.thizthizzydizzy.treefeller.core.test.*;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockAxis;
public final class DisconnectedTrunkDistanceTests{
    public static void register(ITestPlatform hooks){
        FixtureOptionTestSuite suite=new FixtureOptionTestSuite("detection.disconnected-trunk-distance");
        for(int limit : new int[]{1,2})for(int distance : new int[]{limit,limit+1}){
            FixtureOptionTestSuite.Fixture f=suite.add("limit-"+limit+"-gap-"+distance,
                    "global.detection.disconnected_trunk_distance = "+limit+"\nglobal.detection.block_data_rules.use_leaf_distance = false");
            for(int x=2;x<=distance;x++)f.block(x,3,0,"spruce_leaves",null,x,"air");
            f.block(distance+1,3,0,"spruce_log",BlockAxis.X,-1,distance<=limit?"air":"spruce_log");
        }
        suite.register(hooks);
    }
}
