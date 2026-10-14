package com.thizthizzydizzy.treefeller.core.test.options.detection;
import com.thizthizzydizzy.treefeller.core.test.*;
import com.thizthizzydizzy.treefeller.core.config.structure.general.SimpleDirection;
public final class DecorationTests{
    public static void register(ITestPlatform hooks){
        TestTrees.Tree tree=TestTrees.specimen(TestTrees.Kind.SPRUCE);
        FixtureOptionTestSuite blocks=new FixtureOptionTestSuite("detection.decorations.blocks");
        for(String material : new String[]{"glowstone","sea_lantern"}){
            FixtureOptionTestSuite.Fixture f=blocks.add(material,"global.detection.decorations = [{ blocks = ["+material+"], direction = SIDE }]");
            f.block(1,1,0,"glowstone",null,-1,material.equals("glowstone")?"air":"glowstone");
            f.block(-1,1,0,"sea_lantern",null,-1,material.equals("sea_lantern")?"air":"sea_lantern");
        }
        blocks.register(hooks);
        FixtureOptionTestSuite direction=new FixtureOptionTestSuite("detection.decorations.direction");
        for(SimpleDirection value : SimpleDirection.values()){
            FixtureOptionTestSuite.Fixture f=direction.add(value.name().toLowerCase(java.util.Locale.ROOT),
                    "global.detection.decorations = [{ blocks = [glowstone], direction = "+value+" }]");
            f.tree=tree;
            f.block(0,9,0,"glowstone",null,-1,value==SimpleDirection.UP||value==SimpleDirection.ALL?"air":"glowstone");
            f.block(2,1,0,"glowstone",null,-1,value==SimpleDirection.DOWN||value==SimpleDirection.SIDE_AND_DOWN||value==SimpleDirection.ALL?"air":"glowstone");
            f.block(3,2,0,"glowstone",null,-1,value==SimpleDirection.SIDE||value==SimpleDirection.SIDE_AND_DOWN||value==SimpleDirection.ALL?"air":"glowstone");
            f.block(5,0,0,"glowstone",null,-1,"glowstone");
        }
        direction.register(hooks);
        FixtureOptionTestSuite column=new FixtureOptionTestSuite("detection.decorations.column");
        for(boolean value : new boolean[]{false,true}){
            FixtureOptionTestSuite.Fixture f=column.add(value?"enabled":"disabled",
                    "global.detection.decorations = [{ blocks = [glowstone], direction = SIDE, column = "+value+" }]");
            f.tree=tree;
            f.block(3,2,0,"glowstone",null,-1,"air");
            f.block(3,1,0,"glowstone",null,-1,value?"air":"glowstone");
            f.block(3,0,0,"glowstone",null,-1,value?"air":"glowstone");
            f.block(2,7,0,"glowstone",null,-1,"air");
            f.block(2,8,0,"glowstone",null,-1,value?"air":"glowstone");
            f.block(2,9,0,"glowstone",null,-1,value?"air":"glowstone");
            f.block(4,2,0,"glowstone",null,-1,"glowstone");
        }
        column.register(hooks);
    }
}
