package com.thizthizzydizzy.treefeller.core.test.options.messages;
import com.thizthizzydizzy.mcautotester.mod.core.TestContext;
import com.thizthizzydizzy.treefeller.core.test.*;
import java.util.List;
/** One focused suite per output type: refusal, success, and independent disablement. */
public final class MessageTests{
    private final boolean actionbar;
    private final TestTrees.Tree tree=TestTrees.specimen(TestTrees.Kind.SPRUCE);
    private TestArena arena;
    private MessageTests(boolean actionbar){this.actionbar=actionbar;}
    public static void register(ITestPlatform platform){
        new MessageTests(false).registerType(platform);
        new MessageTests(true).registerType(platform);
    }
    private void registerType(ITestPlatform platform){
        String channel=actionbar?"actionbar":"chat";
        OptionTestSuite suite=new OptionTestSuite("messages."+channel).lifecycle(this::prepare,this::finish,(ctx,hooks) -> {});
        for(int i=0;i<3;i++){
            final int test=i;
            String config="global.trigger.food = "+(i==0?"0":"20")+"\n"
                    +"messages.trigger.food.global."+channel+" = \"Refused: food {0}, required {1}\"\n"
                    +"messages.trigger.food.success."+channel+" = \"Allowed: food {0}, required {1}\"\n"
                    +"messages.success."+channel+" = \"Felling tree\"\n";
            if(i==2)config+="messages.trigger.food.global."+channel+" = null\nmessages.trigger.food.success."+channel+" = null\nmessages.success."+channel+" = null\n";
            suite.add(i==0?"refused":i==1?"allowed":"disabled",config,(ctx,hooks) -> cut(ctx,hooks,test));
        }
        suite.register(platform);
    }
    private void prepare(TestContext ctx,ITestPlatform platform){
        arena=TestArena.setup(ctx,platform,OptionTestSuite.PLAYER,-4,-6,24,99);
        arena.spawnAll(tree,new int[][]{{0,2},{8,2},{16,2}});
    }
    private void cut(TestContext ctx,ITestPlatform platform,int test){
        int x=test*8;
        TestArena.onServer(ctx,() -> {platform.preparePlayer(OptionTestSuite.PLAYER,true);platform.setPlayerLevels(OptionTestSuite.PLAYER,20,20,20);return null;});
        ctx.teleportPlayer(OptionTestSuite.PLAYER,x+0.5,arena.floorY+1,-1.5,0.0,0.0);
        ITestPlatform.EffectCapture capture=TestArena.onServer(ctx,() -> platform.captureEffects(OptionTestSuite.PLAYER));
        try{
            TestArena.onServer(ctx,() -> {
                platform.showTestStatus(OptionTestSuite.PLAYER,(actionbar?"actionbar":"chat")+"="+(test==2?"null":"custom")+" | food="+(test==0?0:20)+" | expect "+(test==0?"refusal":"felling"));
                return null;
            });
            ctx.breakBlock(x,arena.floorY+1,2);
            if(test==0)arena.assertOnlyLogRemoved(tree,x,2,0,0,0);
            else arena.assertLogsRemoved(tree,x,2);
            ctx.waitTicks(2); // Flush native output before examining captured packets.
            List<ITestPlatform.MessageObservation> messages=TestArena.onServer(ctx,capture::messages);
            String expected=test==0?"Refused: food 20.0, required min=0.0, max=0.0":"Allowed: food 20.0, required min=20.0, max=20.0";
            long matching=messages.stream().filter(m -> m.text.equals(expected)&&m.actionbar==actionbar).count();
            if(matching!=(test==2?0:1))throw new AssertionError("Expected one formatted "+(actionbar?"actionbar":"chat")+" message; got "+matching+": "+describe(messages));
            if(messages.stream().anyMatch(m -> m.actionbar!=actionbar&&(m.text.startsWith("Refused:")||m.text.startsWith("Allowed:")||m.text.equals("Felling tree"))))
                throw new AssertionError("Message leaked into disabled channel");
            long success=messages.stream().filter(m -> m.text.equals("Felling tree")&&m.actionbar==actionbar).count();
            if(success!=(test==1?1:0))throw new AssertionError("Unexpected success message count: "+success);
        }finally{TestArena.onServer(ctx,() -> {capture.close();return null;});}
    }
    private static String describe(List<ITestPlatform.MessageObservation> messages){
        StringBuilder result=new StringBuilder();
        for(ITestPlatform.MessageObservation m:messages)result.append(m.actionbar?"actionbar: ":"chat: ").append(m.text).append("; ");
        return result.toString();
    }
    private void finish(TestContext ctx,ITestPlatform platform){
        TestArena.onServer(ctx,() -> {platform.prepareCamera(OptionTestSuite.PLAYER);return null;});
        ctx.teleportPlayer(OptionTestSuite.PLAYER,8.5,arena.floorY+16,-10.5,0.0,50.0);
        ctx.waitTicks(40);
    }
}
