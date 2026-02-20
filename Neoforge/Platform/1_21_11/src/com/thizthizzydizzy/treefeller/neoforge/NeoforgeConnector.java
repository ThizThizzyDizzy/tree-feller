package com.thizthizzydizzy.treefeller.neoforge;
import com.thizthizzydizzy.treefeller.config.structure.TreeFellerConfiguration;
import com.thizthizzydizzy.treefeller.connector.TreeFellerConnector;
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
