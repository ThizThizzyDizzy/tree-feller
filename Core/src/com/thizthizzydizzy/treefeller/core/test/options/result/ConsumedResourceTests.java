package com.thizthizzydizzy.treefeller.core.test.options.result;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import com.thizthizzydizzy.treefeller.core.test.*;
public final class ConsumedResourceTests{
    private ConsumedResourceTests(){}
    public static void register(ITestPlatform platform){
        for(String resource : new String[]{"food", "health"})for(String group : new String[]{"base", "trunk", "leaves", "decoration"}){
            String option = "consumed-"+resource+"-"+group;
            ResultTestSuite tests = new ResultTestSuite(option, group, false);
            for(int cost : new int[]{0, 2, -2})add(tests, resource, option, cost, 0);
            if(resource.equals("food"))add(tests, resource, option, 2, 3);
            tests.register(platform);
        }
    }
    private static void add(ResultTestSuite tests, String resource, String option, int cost, int saturation){
        float value = (float)cost/tests.units();
        tests.add("cost-"+cost+(saturation>0?"-from-saturation":""), "global.result."+option+" = "+value, "iron_axe",
                (ctx, hooks) -> TestArena.onServer(ctx, () -> {hooks.setPlayerLevels(OptionTestSuite.PLAYER, 10, 10, saturation); return null;}),
                (hooks, x) -> {
                    IPlayerConnector player = hooks.playerState(OptionTestSuite.PLAYER);
                    float expectedFood = resource.equals("food")?10-Math.max(0, cost-saturation)+(cost<0?-cost:0):10;
                    float expectedHealth = resource.equals("health")?10-cost:10;
                    float expectedSaturation = resource.equals("food")?Math.max(0, saturation-Math.max(0, cost)):saturation;
                    near("food", player.getFoodLevel(), expectedFood);
                    near("health", player.getHealth(), expectedHealth);
                    near("saturation", player.getSaturationLevel(), expectedSaturation);
                });
    }
    private static void near(String resource, float actual, float expected){
        if(Math.abs(actual-expected)>0.05f)throw new AssertionError(resource+": expected "+expected+", got "+actual);
    }
}
