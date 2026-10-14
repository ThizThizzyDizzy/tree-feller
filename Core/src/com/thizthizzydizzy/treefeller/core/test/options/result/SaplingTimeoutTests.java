package com.thizthizzydizzy.treefeller.core.test.options.result;
import com.thizthizzydizzy.treefeller.core.test.*;
public final class SaplingTimeoutTests{
    private SaplingTimeoutTests(){}
    public static void register(ITestPlatform platform){
        SaplingTestSuite t=new SaplingTestSuite("result.sapling-replant.sapling-timeout");
        for(int timeout : new int[]{8,16})for(boolean early : new boolean[]{true,false}){
            SaplingTestSuite.Case c=t.add("timeout-"+timeout+(early?"-before":"-after"),"global.result.sapling_replant.use_inventory_saplings = true\nglobal.result.sapling_replant.sapling_timeout = "+timeout,early?1:0);
            c.arrival=early?timeout-4:timeout+4;c.wait=timeout+7;c.remaining=early?1:2;
        }
        SaplingTestSuite.Case zero=t.add("zero",SaplingTestSuite.SPAWN+"global.result.sapling_replant.sapling_timeout = 0",1);
        zero.wait=3;
        t.register(platform);
    }
}
