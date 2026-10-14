package com.thizthizzydizzy.treefeller.core.test.options.trigger;
import com.thizthizzydizzy.treefeller.core.test.*;
import com.thizthizzydizzy.treefeller.core.player.PlayerSettings;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import java.lang.reflect.Field;
import java.util.Map;
public final class DefaultEnabledTests{
    public static void register(ITestPlatform hooks){
        FixtureOptionTestSuite suite=new FixtureOptionTestSuite("trigger.default-enabled");
        for(boolean value : new boolean[]{true,false})for(boolean override : new boolean[]{false,true}){
            boolean expected=override?!value:value;
            FixtureOptionTestSuite.Fixture f=suite.add("default-"+value+(override?"-manual-toggle":"-fresh-player"),
                    "global.trigger.default_enabled = "+value);
            f.allowed=expected;
            f.before=(ctx,platform) -> TestArena.onServer(ctx, () -> {
                IPlayerConnector player=platform.playerState(OptionTestSuite.PLAYER);
                // Isolate the actual cached player entry to exercise first-use initialization,
                // without exposing test-only reset operations in the production settings API.
                Map<IPlayerConnector,PlayerSettings> settings=settings();
                PlayerSettings old=settings.remove(player);
                suite.cleanupWith(() -> {settings.remove(player); if(old!=null)settings.put(player,old);});
                PlayerSettings fresh=PlayerSettings.get(player);
                if(fresh.isToggledOn()!=value)throw new AssertionError("Fresh player ignored default_enabled="+value);
                if(override)fresh.setToggledOn(!value);
                return null;
            });
        }
        suite.register(hooks);
    }
    @SuppressWarnings("unchecked")
    private static Map<IPlayerConnector,PlayerSettings> settings(){
        try{
            Field field=PlayerSettings.class.getDeclaredField("playerSettings");
            field.setAccessible(true);
            return (Map<IPlayerConnector,PlayerSettings>)field.get(null);
        }catch(ReflectiveOperationException ex){throw new IllegalStateException(ex);}
    }
}
