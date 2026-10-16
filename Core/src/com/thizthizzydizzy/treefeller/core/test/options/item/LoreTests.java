package com.thizthizzydizzy.treefeller.core.test.options.item;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
public final class LoreTests{
    private LoreTests(){}
    public static void register(ITestPlatform platform){
        ItemTestSuite tests = new ItemTestSuite("lore");
        String required = "material = IRON_AXE, lore = { whitelist = [First, Second] }";
        tests.add("required-all-lines", required, "iron_axe[lore=['First','Second','Extra']]", true)
                .add("required-missing-line", required, "iron_axe[lore=['First']]", false)
                .add("required-absent", required, "iron_axe", false);
        String banned = "material = IRON_AXE, lore = { blacklist = [First, Second] }";
        tests.add("banned-one-line", banned, "iron_axe[lore=['Second']]", false)
                .add("banned-unrelated-line", banned, "iron_axe[lore=['Extra']]", true)
                .add("banned-absent", banned, "iron_axe", true);
        tests.register(platform);
    }
}
