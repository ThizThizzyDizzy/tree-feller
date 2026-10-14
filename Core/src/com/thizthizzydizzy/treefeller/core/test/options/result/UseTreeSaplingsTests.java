package com.thizthizzydizzy.treefeller.core.test.options.result;
import com.thizthizzydizzy.treefeller.core.test.*;
public final class UseTreeSaplingsTests{
    private UseTreeSaplingsTests(){}
    public static void register(ITestPlatform platform){
        SaplingTestSuite t=new SaplingTestSuite("result.sapling-replant.use-tree-saplings");
        t.add("disabled",SaplingTestSuite.DROPS,0).treeDrops(0);
        t.add("enabled",SaplingTestSuite.DROPS+"global.result.sapling_replant.use_tree_saplings = true",1).treeDrops(1);
        t.add("no-source","global.result.sapling_replant.use_tree_saplings = true",0);
        SaplingTestSuite.Case priority=t.add("tree-before-inventory",SaplingTestSuite.DROPS+"global.result.sapling_replant.use_tree_saplings = true\nglobal.result.sapling_replant.use_inventory_saplings = true",1).treeDrops(1);
        priority.inventory=2;priority.item="diamond";priority.remaining=2;priority.inventoryLeft=2;
        priority.saplings="[spruce_sapling, { block = oak_sapling, item = diamond }]";
        t.add("inventory-delivery",SaplingTestSuite.DROPS+"global.result.sapling_replant.use_tree_saplings = true\nglobal.result.drop_to_inventory = true",1).treeDrops(1);
        SaplingTestSuite.Case delayed=t.add("landing-drops",SaplingTestSuite.DROPS+"global.result.sapling_replant.use_tree_saplings = true\nglobal.result.sapling_replant.sapling_timeout = 80\nglobal.breaking.leaves_behavior { fall_as_entities = true, landing = BREAK }\nglobal.breaking.fall_delay = 10\nglobal.breaking.vertical_fall_velocity = -1\nglobal.breaking.random_fall_velocity = 0\nglobal.breaking.explosive_fall_velocity = 0\nglobal.breaking.directional_fall_velocity = 0",1).treeDrops(1);
        delayed.wait=65;
        t.register(platform);
    }
}
