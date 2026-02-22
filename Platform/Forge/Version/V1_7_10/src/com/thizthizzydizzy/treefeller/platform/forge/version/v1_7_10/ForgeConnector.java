package com.thizthizzydizzy.treefeller.platform.forge.version.v1_7_10;
import com.thizthizzydizzy.treefeller.core.TreeFellerConnector;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeFellerConfiguration;
import cpw.mods.fml.common.Loader;
import java.nio.file.Path;
import java.util.function.Function;

public class ForgeConnector implements TreeFellerConnector{
    @Override
    public void log(String text){
        System.out.println("["+TreeFellerForge.MOD_ID+"] "+text);
    }
    @Override
    public TreeFellerConfiguration loadConfig(Function<Path, TreeFellerConfiguration> defaultLoader){
        return defaultLoader.apply(Loader.instance().getConfigDir().toPath().resolve(TreeFellerForge.MOD_ID+".conf"));
    }
}
