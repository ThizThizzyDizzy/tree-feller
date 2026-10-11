package com.thizthizzydizzy.treefeller.core.test;
import com.thizthizzydizzy.mcautotester.mod.api.MCAutoTesterAPI;
import com.thizthizzydizzy.mcautotester.mod.core.TestContext;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.config.ConfigReader;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeFellerConfiguration;
import java.util.LinkedHashMap;
import java.util.Map;
/** One config option, with independently selectable end-to-end scenarios. */
public final class OptionTestSuite{
    public static final String PLAYER = "testplayer";
    private final String name;
    private final Map<String, Scenario> scenarios = new LinkedHashMap<>();
    private Test prepare = (ctx, platform) -> {};
    private Test finish = (ctx, platform) -> {};
    private Test cleanup = (ctx, platform) -> {};
    public OptionTestSuite(String optionPath){
        name = "treefeller-option-"+optionPath.replace('.', '-');
    }
    public OptionTestSuite add(String name, String config, Test test){
        if(scenarios.containsKey(name))throw new IllegalArgumentException("Duplicate scenario: "+name);
        scenarios.put(name, new Scenario(config, test));
        return this;
    }
    /** Shared fixture setup, one final camera check, and unconditional temporary-block cleanup. */
    public OptionTestSuite lifecycle(Test prepare, Test finish, Test cleanup){
        this.prepare = prepare;
        this.finish = finish;
        this.cleanup = cleanup;
        return this;
    }
    public void register(ITestPlatform platform){
        MCAutoTesterAPI.register(name, ctx -> {
            ctx.waitForJoin(PLAYER);
            batch(ctx, platform, scenarios);
            ctx.success();
        }, true);
        for(Map.Entry<String, Scenario> entry : scenarios.entrySet()){
            MCAutoTesterAPI.register(name+"-"+entry.getKey(), ctx -> {
                ctx.waitForJoin(PLAYER);
                Map<String, Scenario> selected = new LinkedHashMap<>();
                selected.put(entry.getKey(), entry.getValue());
                batch(ctx, platform, selected);
                ctx.success();
            }, true);
        }
    }
    private void batch(TestContext ctx, ITestPlatform platform, Map<String, Scenario> selected){
        try{
            prepare.run(ctx, platform);
            for(Map.Entry<String, Scenario> entry : selected.entrySet())run(ctx, platform, entry.getKey(), entry.getValue());
            cleanup.run(ctx, platform);
            finish.run(ctx, platform);
        }finally{
            cleanup.run(ctx, platform);
        }
    }
    private void run(TestContext ctx, ITestPlatform platform, String scenarioName, Scenario scenario){
        ctx.log(name+" / "+scenarioName);
        TreeFellerConfiguration previous = TestArena.onServer(ctx, () -> {
            TreeFellerConfiguration fresh = ConfigReader.readFromString(scenario.config);
            TreeFellerConfiguration old = TreeFellerCore.config;
            TreeFellerCore.config = fresh;
            return old;
        });
        try{
            scenario.test.run(ctx, platform);
            ctx.log("PASS: "+name+" / "+scenarioName);
        }finally{
            TestArena.onServer(ctx, () -> {TreeFellerCore.config = previous; return null;});
        }
    }
    @FunctionalInterface
    public interface Test{
        void run(TestContext ctx, ITestPlatform platform);
    }
    private static final class Scenario{
        final String config;
        final Test test;
        Scenario(String config, Test test){this.config = config; this.test = test;}
    }
}
