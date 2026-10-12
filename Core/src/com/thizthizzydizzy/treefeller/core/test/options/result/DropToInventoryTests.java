package com.thizthizzydizzy.treefeller.core.test.options.result;
import com.thizthizzydizzy.treefeller.core.test.*;
public final class DropToInventoryTests{
    private DropToInventoryTests(){}
    public static void register(ITestPlatform platform){
        ResultTestSuite tests = new ResultTestSuite("drop-to-inventory", "trunk", false);
        for(boolean enabled : new boolean[]{false, true})tests.drops(Boolean.toString(enabled), "global.result.drop-to-inventory = "+enabled,
                "iron_axe", "spruce_log", 8, 8, enabled);
        tests.add("full-inventory-overflow", "global.result.drop-to-inventory = true", "iron_axe", (ctx, hooks) -> {
            TestArena.onServer(ctx, () -> {
                for(int slot = 1; slot<36; slot++)hooks.setInventoryItem(OptionTestSuite.PLAYER, slot, "dirt", 64);
                return null;
            });
        }, (hooks, x) -> {
            int dropped = tests.arena.world.droppedItemCount("spruce_log", x-3, tests.arena.floorY, -1, x+3, tests.arena.floorY+12, 5);
            if(dropped!=8||hooks.inventoryItemCount(OptionTestSuite.PLAYER, "spruce_log")!=0)
                throw new AssertionError("Expected all eight logs to overflow onto the ground");
        });
        tests.register(platform);
    }
}
