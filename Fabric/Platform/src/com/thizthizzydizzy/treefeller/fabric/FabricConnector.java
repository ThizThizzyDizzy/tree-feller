package com.thizthizzydizzy.treefeller.fabric;
import com.thizthizzydizzy.treefeller.config.structure.TreeFellerConfiguration;
import com.thizthizzydizzy.treefeller.connector.TreeFellerConnector;
import java.nio.file.Path;
import java.util.function.Function;
import net.fabricmc.loader.api.FabricLoader;
public class FabricConnector implements TreeFellerConnector{
    @Override
    public void log(String text){
        System.out.println("["+TreeFellerFabric.MOD_ID+"] "+text);
    }
    @Override
    public TreeFellerConfiguration loadConfig(Function<Path, TreeFellerConfiguration> defaultLoader){
        return defaultLoader.apply(FabricLoader.getInstance().getConfigDir().resolve(TreeFellerFabric.MOD_ID+".conf"));
    }
}
