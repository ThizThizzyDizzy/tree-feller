package com.thizthizzydizzy.treefeller.core.test.options.result;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
/** Fortune III guarantees flint. Eight gravel blocks make an accidental all-flint
 * unenchanted result a one-in-one-hundred-million event; foliage provides more trials. */
public final class EnchantmentTests{
    private EnchantmentTests(){}
    public static void register(ITestPlatform platform){
        for(String group : new String[]{"trunk", "leaves", "decoration"})for(String enchantment : new String[]{"fortune", "silk-touch"}){
            String option = group+"-"+enchantment;
            boolean silk = enchantment.equals("silk-touch");
            ResultTestSuite tests = new ResultTestSuite(option, group, silk?"melon":"gravel");
            int n = tests.units();
            String selective = "global.result.trunk-drop-chance = "+(group.equals("trunk")?1:0)+"\n"
                    +"global.result.leaves-drop-chance = "+(group.equals("leaves")?1:0)+"\n"
                    +"global.result.decorations-drop-chance = "+(group.equals("decoration")?1:0)+"\n";
            String item = "iron_axe[enchantments={"+(silk?"silk_touch:1":"fortune:3")+"}]";
            if(silk)tests.drops("true", selective+"global.result."+option+" = true", item, silk?"melon":"melon_slice", n, n, false)
                    .drops("false", selective+"global.result."+option+" = false", item, "melon_slice", n*3, n*7, false)
                    .register(platform);
            else{
                for(boolean enabled : new boolean[]{true, false})tests.add(Boolean.toString(enabled), selective+"global.result."+option+" = "+enabled,
                        item, (ctx, hooks) -> {}, (hooks, x) -> {
                            int flint = tests.arena.world.droppedItemCount("flint", x-3, tests.arena.floorY, -1, x+3, tests.arena.floorY+12, 5);
                            int gravel = tests.arena.world.droppedItemCount("gravel", x-3, tests.arena.floorY, -1, x+3, tests.arena.floorY+12, 5);
                            if(flint+gravel!=n||enabled&&flint!=n||!enabled&&gravel==0)
                                throw new AssertionError("Expected "+n+" gravel drops with Fortune "+enabled+", got "+flint+" flint and "+gravel+" gravel");
                        });
                tests.register(platform);
            }
        }
    }
}
