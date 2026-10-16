package com.thizthizzydizzy.treefeller.core.test.options.result;
import com.thizthizzydizzy.treefeller.core.test.*;
import com.thizthizzydizzy.treefeller.core.config.legacy.LegacyBukkitConfigImporter;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.ConfigFactory;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.ConfigRenderOptions;
import java.util.Map;
public final class SpawnSaplingsTests{
    private SpawnSaplingsTests(){}
    public static void register(ITestPlatform platform){
        SaplingTestSuite t=new SaplingTestSuite("result.sapling-replant.spawn-saplings");
        t.add("disabled","",0);
        SaplingTestSuite.Case spawn=t.add("fallback",SaplingTestSuite.SPAWN,1);
        spawn.timeout=8;
        t.add("existing-source",SaplingTestSuite.SPAWN+"global.result.sapling_replant.use_inventory_saplings = true",1).inventory(2,1);
        LegacyBukkitConfigImporter.Result imported=LegacyBukkitConfigImporter.importConfiguration(Map.of("spawn-saplings",2));
        if(!imported.warnings.isEmpty())throw new IllegalStateException(imported.report());
        String legacySettings=ConfigFactory.parseString(imported.configuration).getConfig("global.result.sapling_replant").root().render(ConfigRenderOptions.concise());
        SaplingTestSuite.Case always=t.add("always-sources","global.result.sapling_replant = "+legacySettings+"\n"
                +"global.result.sapling_replant { use_tree_saplings = true, use_inventory_saplings = true }\n"+SaplingTestSuite.DROPS,1);
        always.inventory(2,2).treeDrops(0);
        always.remaining+=2;
        always.immediate=true;
        t.add("always-invalid-soil","global.result.sapling_replant.always_spawn_saplings = true",0).soil="stone";
        t.register(platform);
    }
}
