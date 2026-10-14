package com.thizthizzydizzy.treefeller.core.test.options.result;
import com.thizthizzydizzy.treefeller.core.test.*;
public final class SaplingGrassesTests{
    private SaplingGrassesTests(){}
    public static void register(ITestPlatform platform){
        SaplingTestSuite t=new SaplingTestSuite("result.sapling-replant.grasses");
        t.add("default-soil",SaplingTestSuite.SPAWN,1);
        t.add("default-refused",SaplingTestSuite.SPAWN,0).soil="stone";
        t.add("changed-allowed",SaplingTestSuite.SPAWN+"global.result.sapling_replant.grasses = [podzol]",1).soil="podzol";
        t.add("changed-refused",SaplingTestSuite.SPAWN+"global.result.sapling_replant.grasses = [podzol]",0);
        t.register(platform);
    }
}
