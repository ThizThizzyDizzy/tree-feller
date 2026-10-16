package com.thizthizzydizzy.treefeller.core.test.options.item;
import com.thizthizzydizzy.treefeller.core.test.*;
import com.thizthizzydizzy.treefeller.core.config.ConfigReader;
import com.thizthizzydizzy.treefeller.core.config.ConfigWriter;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IItemDefinition;
public final class NbtTests{
    private NbtTests(){}
    public static void register(ITestPlatform platform){
        FixtureOptionTestSuite tests=new FixtureOptionTestSuite("item.nbt");
        String required="{\"minecraft:custom_data\":{treefeller:{flag:1b,values:[I;1,2],entries:[{name:oak}]}}}";
        String converted="{\"minecraft:custom_data\":{converted:1b,nested:{kept:2s},bytes:[B;1b,2b]}}";
        for(boolean allowed:new boolean[]{true,false}){
            ITestPlatform.EffectCapture[] capture={null};
            String config="tools=[{item={material=IRON_AXE,nbt="+quote(required)+"}}]\n"
                    +"global.result.drop-conversions={"+quote("spruce_log{\"minecraft:custom_data\":{missing:1b}}")+"=emerald,"
                    +quote("spruce_log{}")+"={material=DIAMOND,nbt="+quote(converted)+"}}\n"
                    +"global.result.effects=[{type=MARKER,permanent=true,location=TOOL,filter=[{material=IRON_AXE,nbt="+quote(required)+"}]},{type=MARKER,permanent=true,location=TOOL,filter=[{material=IRON_AXE,nbt="+quote(required.replace("flag:1b","flag:2b"))+"}]}]\n";
            FixtureOptionTestSuite.Fixture fixture=tests.add(allowed?"partial-match-and-convert":"numeric-type-mismatch",config);
            fixture.allowed=allowed;
            fixture.description="nbt="+required+" | actual flag="+(allowed?"1b":"1 (int)");
            fixture.before=(ctx,hooks) -> TestArena.onServer(ctx,() -> {
                com.thizthizzydizzy.treefeller.core.config.structure.TreeFellerConfiguration saved=ConfigReader.readFromString(ConfigWriter.writeToString(TreeFellerCore.config));
                TreeFellerCore.config.tools=saved.tools;
                TreeFellerCore.config.global.result.drop_conversions=saved.global.result.drop_conversions;
                TreeFellerCore.config.global.result.effects=saved.global.result.effects;
                capture[0]=hooks.captureEffects(OptionTestSuite.PLAYER);tests.cleanupWith(() -> capture[0].close());
                hooks.equipItem(OptionTestSuite.PLAYER,"iron_axe[custom_data={treefeller:{flag:"+(allowed?"1b":"1")+",values:[I;1,2],entries:[{name:oak,extra:3},{name:birch}],extra:42}}]");return null;
            });
            fixture.after=(ctx,hooks) -> TestArena.onServer(ctx,() -> {
                if(capture[0].markers().size()!=(allowed?1:0))throw new AssertionError("NBT effect filter should select only the matching tool");
                if(!allowed)return null;
                ITestPlatform.ITestWorld world=hooks.world(OptionTestSuite.PLAYER);
                IItemDefinition expected=ConfigReader.readFromString("tools=[{item={material=DIAMOND,nbt="+quote(converted)+"}}]").tools[0].item;
                int count=world.droppedItemCount(expected,-4,99,-6,80,115,80);
                if(count!=4)throw new AssertionError("Expected four converted diamonds with typed NBT; got "+count);
                if(world.droppedItemCount("emerald",-4,99,-6,80,115,80)!=0)throw new AssertionError("Nonmatching NBT source converted drops");
                return null;
            });
        }
        tests.register(platform);
    }
    private static String quote(String value){return "\""+value.replace("\\","\\\\").replace("\"","\\\"")+"\"";}
}
