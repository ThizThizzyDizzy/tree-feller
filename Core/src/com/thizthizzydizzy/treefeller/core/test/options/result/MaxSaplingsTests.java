package com.thizthizzydizzy.treefeller.core.test.options.result;
import com.thizthizzydizzy.treefeller.core.test.*;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
import java.util.*;
public final class MaxSaplingsTests{
    private MaxSaplingsTests(){}
    public static void register(ITestPlatform platform){
        SaplingTestSuite t=new SaplingTestSuite("trees.max-saplings");
        TestTrees.Tree giant=TestTrees.specimen(TestTrees.Kind.GIANT_SPRUCE,4);
        for(int cap : new int[]{0,1,2,4}){
            SaplingTestSuite.Case c=t.add("cap-"+cap,"global.result.sapling_replant.use_inventory_saplings = true",cap).inventory(6,6-cap);
            c.cap=cap;c.tree=giant;
        }
        TestTrees.Tree cluster=TestTrees.spruceCluster(new int[]{0,0,5},new int[]{1,0,7},new int[]{2,0,4},
                new int[]{0,1,10},new int[]{1,1,6},new int[]{2,1,9},new int[]{1,2,8});
        for(boolean limitedInventory : new boolean[]{false,true}){
            SaplingTestSuite.Case c=t.add(limitedInventory?"seven-candidates-two-items":"seven-candidates-cap-two",
                    "global.result.sapling_replant.use_inventory_saplings = true",2)
                    .inventory(limitedInventory?2:7,limitedInventory?0:5);
            c.tree=cluster;c.cap=limitedInventory?7:2;
            // Only the 10-log and 9-log columns may be replanted; all five shorter bases must stay air.
            c.plantedPositions=new HashSet<>(Arrays.asList(BlockPos.toPos(0,0,1),BlockPos.toPos(2,0,1)));
        }
        t.register(platform);
    }
}
