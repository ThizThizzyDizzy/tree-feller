package com.thizthizzydizzy.treefeller.core.test.options.result;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
public final class DropConversionsTests{
    private DropConversionsTests(){}
    public static void register(ITestPlatform platform){
        ResultTestSuite tests = new ResultTestSuite("drop-conversions", "trunk", false);
        tests.drops("unconfigured", "", "iron_axe", "spruce_log", 8, 8, false);
        tests.add("matching-and-unmatched", "global.result.drop-conversions = { spruce_log = diamond }", "iron_axe[enchantments={silk_touch:1}]",
                (ctx, hooks) -> {}, (hooks, x) -> {
                    for(String material : new String[]{"diamond", "spruce_log", "spruce_leaves", "glowstone"}){
                        int expected = material.equals("diamond")?8:material.equals("spruce_log")?0:material.equals("glowstone")?1:tests.tree.blocks.size()-8;
                        int actual = tests.arena.world.droppedItemCount(material, x-3, tests.arena.floorY, -1, x+3, tests.arena.floorY+12, 5);
                        if(actual!=expected)throw new AssertionError(material+": expected "+expected+", got "+actual);
                    }
                }).register(platform);
    }
}
