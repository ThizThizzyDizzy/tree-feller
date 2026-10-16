package com.thizthizzydizzy.treefeller.core.test.options.item;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
public final class AttributesTests{
    private AttributesTests(){}
    public static void register(ITestPlatform platform){
        ItemTestSuite tests = new ItemTestSuite("attributes");
        String hidden = "iron_axe[tooltip_display={hidden_components:['minecraft:enchantments']}]";
        for(String list : new String[]{"whitelist", "blacklist"}){
            boolean required = list.equals("whitelist");
            String definition = "material = IRON_AXE, attributes = { "+list+" = [HIDE_ENCHANTS] }";
            tests.add(list+"-present", definition, hidden, required)
                    .add(list+"-absent", definition, "iron_axe", !required);
        }
        tests.register(platform);
    }
}
