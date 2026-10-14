package com.thizthizzydizzy.treefeller.core.test.options.result;
import com.thizthizzydizzy.treefeller.core.test.*;
public final class TreeSaplingsTests{
    private TreeSaplingsTests(){}
    public static void register(ITestPlatform platform){
        SaplingTestSuite t=new SaplingTestSuite("trees.saplings");
        t.add("empty",SaplingTestSuite.SPAWN,0).saplings="[]";
        t.add("material", "global.result.sapling_replant.use_inventory_saplings = true",1).inventory(2,1);
        SaplingTestSuite.Case pair=t.add("block-item-pair","global.result.sapling_replant.use_inventory_saplings = true",1).inventory(2,1);
        pair.saplings="[{ block = oak_sapling, item = diamond }]";pair.item="diamond";pair.expectedBlock="oak_sapling";
        SaplingTestSuite.Case multiple=t.add("multiple-matching","global.result.sapling_replant.use_inventory_saplings = true",1).inventory(2,1);
        multiple.saplings="[spruce_sapling, oak_sapling]";multiple.item="oak_sapling";multiple.expectedBlock="oak_sapling";
        t.register(platform);
    }
}
