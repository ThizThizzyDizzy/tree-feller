package com.thizthizzydizzy.treefeller.core.test.options.detection;
import com.thizthizzydizzy.treefeller.core.test.*;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockAxis;
public final class RootDistanceTests{
    public static void register(ITestPlatform hooks){
        FixtureOptionTestSuite suite=new FixtureOptionTestSuite("detection.root-distance");
        for(int limit : new int[]{1,2})for(int distance : new int[]{limit,limit+1}){
            boolean pass=distance<=limit;
            FixtureOptionTestSuite.Fixture f=suite.add("limit-"+limit+"-distance-"+distance,"global.detection.root_distance = "+limit);
            f.cutZ=-distance; f.allowed=pass;
            for(int z=-distance;z<0;z++)f.block(0,0,z,"oak_log",BlockAxis.Y,-1,z==-distance?"air":"oak_log");
        }
        suite.register(hooks);
    }
}
