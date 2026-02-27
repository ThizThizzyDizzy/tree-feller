package com.thizthizzydizzy.treefeller.platform.neoforge.version.v1_20_2;
import com.thizthizzydizzy.treefeller.core.connector.TreeFellerConnector;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeFellerConfiguration;
import java.nio.file.Path;
import java.util.function.Function;
import net.neoforged.fml.loading.FMLPaths;
public class NeoforgeConnector implements TreeFellerConnector{
    @Override
    public void log(String text){
        System.out.println("["+TreeFellerNeoforge.MOD_ID+"] "+text);
    }
    @Override
    public TreeFellerConfiguration loadConfig(Function<Path, TreeFellerConfiguration> defaultLoader){
        return defaultLoader.apply(FMLPaths.CONFIGDIR.get().resolve(TreeFellerNeoforge.MOD_ID+".conf"));
    }
}
