package com.thizthizzydizzy.treefeller.core.test.options.trigger;
import com.thizthizzydizzy.treefeller.core.test.*;
import java.util.LinkedHashMap;
import java.util.Map;
public final class PermissionsTests{
    public static void register(ITestPlatform hooks){
        FixtureOptionTestSuite suite=new FixtureOptionTestSuite("trigger.permissions");
        for(String list : new String[]{"whitelist","blacklist"})for(boolean granted : new boolean[]{false,true}){
            boolean allowed=list.equals("whitelist")?granted:!granted;
            FixtureOptionTestSuite.Fixture f=suite.add(list+(granted?"-granted":"-missing"),
                    "global.trigger.permissions."+list+" = [treefeller.test.first, treefeller.test.second]");
            f.allowed=allowed;
            f.before=(ctx,platform) -> TestArena.onServer(ctx, () -> {
                Map<String,Boolean> values=new LinkedHashMap<>();
                values.put("treefeller.test.first",list.equals("whitelist"));
                values.put("treefeller.test.second",granted);
                suite.cleanupWith(platform.permissions(OptionTestSuite.PLAYER,values));
                return null;
            });
        }
        suite.register(hooks);
    }
}
