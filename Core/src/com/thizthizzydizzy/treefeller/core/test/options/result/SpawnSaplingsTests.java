package com.thizthizzydizzy.treefeller.core.test.options.result;
import com.thizthizzydizzy.treefeller.core.test.*;
public final class SpawnSaplingsTests{
    private SpawnSaplingsTests(){}
    public static void register(ITestPlatform platform){
        SaplingTestSuite t=new SaplingTestSuite("result.sapling-replant.spawn-saplings");
        t.add("disabled","",0);
        SaplingTestSuite.Case spawn=t.add("fallback",SaplingTestSuite.SPAWN,1);
        spawn.timeout=8;
        t.add("existing-source",SaplingTestSuite.SPAWN+"global.result.sapling_replant.use_inventory_saplings = true",1).inventory(2,1);
        t.register(platform);
    }
}
