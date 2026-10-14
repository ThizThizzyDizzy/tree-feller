package com.thizthizzydizzy.treefeller.core.test.options.item;
import com.thizthizzydizzy.treefeller.core.test.FixtureOptionTestSuite;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
import com.thizthizzydizzy.treefeller.core.test.OptionTestSuite;
import com.thizthizzydizzy.treefeller.core.test.TestArena;
public final class CustomModelDataTests{
    private CustomModelDataTests(){}
    public static void register(ITestPlatform platform){
        FixtureOptionTestSuite tests = new FixtureOptionTestSuite("item.custom-model-data");
        add(tests, "whitelist-match", "whitelist = [42]", 42, true);
        add(tests, "whitelist-mismatch", "whitelist = [42]", 43, false);
        add(tests, "whitelist-absent", "whitelist = [42]", null, false);
        add(tests, "blacklist-match", "blacklist = [42]", 42, false);
        add(tests, "blacklist-mismatch", "blacklist = [42]", 43, true);
        add(tests, "blacklist-absent", "blacklist = [42]", null, true);
        add(tests, "combined-zero", "whitelist = [0, 42], blacklist = [42]", 0, true);
        add(tests, "combined-absent", "whitelist = [0, 42], blacklist = [42]", null, false);
        add(tests, "combined-blacklist-wins", "whitelist = [0, 42], blacklist = [42]", 42, false);
        tests.register(platform);
    }
    private static void add(FixtureOptionTestSuite tests, String name, String filter, Integer data, boolean allowed){
        String config = "tools = [{ item = { material = IRON_AXE, custom_model_data = { "+filter+" } } }]\n";
        FixtureOptionTestSuite.Fixture fixture = tests.add(name+"-data-"+(data==null?"absent":data), config);
        fixture.allowed = allowed;
        fixture.before = (ctx, hooks) -> TestArena.onServer(ctx, () -> {
            hooks.equipItem(OptionTestSuite.PLAYER, data==null?"iron_axe":"iron_axe[custom_model_data={floats:["+data+".0f]}]");
            return null;
        });
    }
}
