package com.thizthizzydizzy.treefeller.core.test.options.item;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
public final class EnchantmentTests{
    private EnchantmentTests(){}
    public static void register(ITestPlatform platform){
        ItemTestSuite tests = new ItemTestSuite("enchantments");
        for(String list : new String[]{"whitelist", "blacklist"}){
            boolean required = list.equals("whitelist");
            String definition = "material = IRON_AXE, enchantments = { "+list
                    +" = [{ enchantment = UNBREAKING, level = { min = 2, max = 3 } }] }";
            tests.add(list+"-below-min", definition, "iron_axe[enchantments={unbreaking:1}]", !required)
                    .add(list+"-at-min", definition, "iron_axe[enchantments={unbreaking:2}]", required)
                    .add(list+"-at-max", definition, "iron_axe[enchantments={unbreaking:3}]", required)
                    .add(list+"-above-max", definition, "iron_axe[enchantments={unbreaking:4}]", !required)
                    .add(list+"-absent", definition, "iron_axe", !required);
        }
        tests.register(platform);
    }
}
