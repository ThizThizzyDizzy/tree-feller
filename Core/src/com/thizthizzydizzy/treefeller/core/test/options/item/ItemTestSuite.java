package com.thizthizzydizzy.treefeller.core.test.options.item;
import com.thizthizzydizzy.treefeller.core.test.FixtureOptionTestSuite;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
import com.thizthizzydizzy.treefeller.core.test.OptionTestSuite;
import com.thizthizzydizzy.treefeller.core.test.TestArena;
/** Requirements are configured on the item definition and exercised by client mining. */
final class ItemTestSuite{
    private final FixtureOptionTestSuite tests;
    ItemTestSuite(String option){tests = new FixtureOptionTestSuite("item."+option);}
    ItemTestSuite add(String name, String definition, String item, boolean allowed){
        FixtureOptionTestSuite.Fixture fixture = tests.add(name, "tools = [{ item = { "+definition+" } }]\n");
        fixture.allowed = allowed;
        fixture.before = (ctx, hooks) -> TestArena.onServer(ctx, () -> {
            hooks.equipItem(OptionTestSuite.PLAYER, item);
            return null;
        });
        return this;
    }
    void register(ITestPlatform platform){tests.register(platform);}
}
