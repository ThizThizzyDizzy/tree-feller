package com.thizthizzydizzy.treefeller.core.test.options.detection;
import com.thizthizzydizzy.treefeller.core.test.*;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
import java.util.Map;
public final class SecondaryTreeVerificationTests{
    public static void register(ITestPlatform hooks){
        FixtureOptionTestSuite suite=new FixtureOptionTestSuite("detection.secondary-tree-verification");
        for(boolean verify : new boolean[]{true,false}){
            FixtureOptionTestSuite.Fixture f=suite.add(verify?"enabled":"disabled",
                    "global.detection.secondary_tree_verification = "+verify+"\nglobal.detection.diagonal_leaves = true\nglobal.detection.block_data_rules.use_leaf_distance = false");
            for(Map.Entry<Long,TestTrees.Block> e : TestTrees.specimen(TestTrees.Kind.SPRUCE,4).blocks.entrySet()){
                TestTrees.Block b=e.getValue(); long pos=e.getKey();
                f.block(5+BlockPos.getX(pos),BlockPos.getY(pos),BlockPos.getZ(pos),b.material,b.axis,b.leafDistance,
                        b.axis!=null||verify?b.material:"air");
            }
            // Touching crowns, with leaves nearer the neighboring tree than the cut tree.
            for(int x=2;x<5;x++)f.block(x,3,0,"spruce_leaves",null,Math.min(x,5-x),verify&&x>=3?"spruce_leaves":"air");
            f.expect(1,3,0,"air");
        }
        suite.register(hooks);
    }
}
