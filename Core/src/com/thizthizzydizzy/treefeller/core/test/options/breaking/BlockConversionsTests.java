package com.thizthizzydizzy.treefeller.core.test.options.breaking;
import com.thizthizzydizzy.treefeller.core.test.*;
public final class BlockConversionsTests{
    private BlockConversionsTests(){}
    public static void register(ITestPlatform platform){
        BreakingTestSuite t=new BreakingTestSuite("breaking.block-conversions");
        // Cut an unconverted root: breakBlock holds attack until air, which would
        // otherwise mine the replacement block after the successful conversion.
        t.cutRoot=true;
        t.add("matching-overrides-fall","trees = [{ trunk = [spruce_log], leaves = [spruce_leaves], roots = [oak_log] }]\nglobal.breaking.block-conversions = { spruce_log = stone }\nglobal.breaking.trunk-behavior.fall-as-entities = true\n",(c,h,f) -> {
            c.waitTicks(2);
            TestArena.onServer(c,() -> {t.equal(8,t.blocks(f,"stone"),"Matching trunk converted");t.equal(0,t.falling(f).size(),"Conversion precedes falling");t.equal(0,t.drops(f,"spruce_log"),"Converted sources produce no drops");t.equal(0,t.drops(f,"stone"),"Converted blocks produce no drops");return null;});
        }).add("nonmatching","trees = [{ trunk = [spruce_log], leaves = [spruce_leaves], roots = [oak_log] }]\nglobal.breaking.block-conversions = { oak_log = stone }\n",(c,h,f) -> {
            t.removed(c,f);TestArena.onServer(c,() -> {t.equal(0,t.blocks(f,"stone"),"No unmatched conversion");t.equal(8,t.drops(f,"spruce_log"),"Unmatched logs break normally");return null;});
        }).add("tool-tree-overrides","trees = [{ trunk = [spruce_log], leaves = [spruce_leaves], roots = [oak_log] }]\nglobal.breaking.block-conversions = { spruce_log = stone }\ntrees = [{ trunk = [spruce_log], leaves = [spruce_leaves], roots = [oak_log], breaking.block-conversions = { spruce_log = gold_block } }]\ntools = [{ item = iron_axe, breaking.block-conversions = { spruce_log = diamond_block } }]\n",(c,h,f) -> {
            c.waitTicks(2);
            TestArena.onServer(c,() -> {t.equal(8,t.blocks(f,"diamond_block"),"Tool conversion wins tree/global conflict");t.equal(0,t.blocks(f,"stone"),"No global conversion");t.equal(0,t.blocks(f,"gold_block"),"No tree conversion");return null;});
        }).register(platform);
    }
}
