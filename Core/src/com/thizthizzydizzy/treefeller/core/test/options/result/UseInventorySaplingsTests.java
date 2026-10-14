package com.thizthizzydizzy.treefeller.core.test.options.result;
import com.thizthizzydizzy.treefeller.core.test.*;
public final class UseInventorySaplingsTests{
    private UseInventorySaplingsTests(){}
    public static void register(ITestPlatform platform){
        SaplingTestSuite t=new SaplingTestSuite("result.sapling-replant.use-inventory-saplings");
        t.add("disabled","",0).inventory(2,2);
        t.add("enabled","global.result.sapling_replant.use_inventory_saplings = true",1).inventory(2,1);
        t.add("blocked","global.result.sapling_replant.use_inventory_saplings = true",0).inventory(2,2).blocked=true;
        SaplingTestSuite.Case wrong=t.add("unmatched","global.result.sapling_replant.use_inventory_saplings = true",0).inventory(2,2);
        wrong.item="oak_sapling";
        t.register(platform);
    }
}
