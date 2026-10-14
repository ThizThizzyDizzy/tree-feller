package com.thizthizzydizzy.treefeller.core.test.options.result;
import com.thizthizzydizzy.treefeller.core.test.*;
public final class SaplingEnableTests{
    private SaplingEnableTests(){}
    public static void register(ITestPlatform platform){
        SaplingTestSuite t=new SaplingTestSuite("result.sapling-replant.enable");
        t.add("disabled",SaplingTestSuite.SPAWN+"global.result.sapling_replant.enable = false",0);
        t.add("enabled",SaplingTestSuite.SPAWN,1);
        t.add("animated",SaplingTestSuite.SPAWN+"global.cutting.cutting_animation = true\nglobal.cutting.animation_delay = 2",1).wait=22;
        t.add("stump",SaplingTestSuite.SPAWN+"global.cutting.leave_stump = true",0).stump=true;
        t.add("blocked",SaplingTestSuite.DROPS+"global.result.sapling_replant.use_tree_saplings = true",0).treeDrops(0).blocked=true;
        t.add("default-species", SaplingTestSuite.SPAWN,1).defaults=true;
        SaplingTestSuite.Case converted=t.add("converted-base","global.result.sapling_replant.use_inventory_saplings = true\nglobal.result.sapling_replant.sapling_timeout = 0\nglobal.breaking.block_conversions = { spruce_log = stone }",0).inventory(2,2);
        converted.rootCut=true;converted.trunks="stone";
        SaplingTestSuite.Case placed=t.add("animated-placement","global.result.sapling_replant.use_inventory_saplings = true\nglobal.result.sapling_replant.sapling_timeout = 40\nglobal.cutting.cutting_animation = true\nglobal.cutting.animation_delay = 2\nglobal.breaking.trunk_behavior { landing = PLACE }",0).inventory(2,2);
        placed.rootCut=true;placed.trunks="spruce_log";placed.wait=22;
        t.register(platform);
    }
}
