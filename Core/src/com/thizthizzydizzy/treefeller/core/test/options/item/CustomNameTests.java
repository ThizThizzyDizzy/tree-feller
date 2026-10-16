package com.thizthizzydizzy.treefeller.core.test.options.item;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
public final class CustomNameTests{
    private CustomNameTests(){}
    public static void register(ITestPlatform platform){
        ItemTestSuite tests = new ItemTestSuite("custom-name");
        for(String list : new String[]{"whitelist", "blacklist"}){
            boolean required = list.equals("whitelist");
            String definition = "material = IRON_AXE, custom-name = { "+list+" = [Feller] }";
            tests.add(list+"-match", definition, "iron_axe[custom_name='Feller']", required)
                    .add(list+"-mismatch", definition, "iron_axe[custom_name='Other']", !required)
                    .add(list+"-absent", definition, "iron_axe", !required);
        }
        tests.register(platform);
    }
}
