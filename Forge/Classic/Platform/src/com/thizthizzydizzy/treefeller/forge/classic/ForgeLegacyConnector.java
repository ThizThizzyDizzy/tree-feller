package com.thizthizzydizzy.treefeller.forge.classic;
import com.thizthizzydizzy.treefeller.config.structure.TreeFellerConfiguration;
import com.thizthizzydizzy.treefeller.connector.TreeFellerConnector;
import java.nio.file.Path;
import java.util.function.Function;
import net.minecraftforge.fml.common.Loader;

public class ForgeLegacyConnector implements TreeFellerConnector{
    @Override
    public void log(String text){
        System.out.println("["+TreeFellerForgeClassic.MOD_ID+"] "+text);
    }
    @Override
    public TreeFellerConfiguration loadConfig(Function<Path, TreeFellerConfiguration> defaultLoader){
        return defaultLoader.apply(Loader.instance().getConfigDir().toPath().resolve(TreeFellerForgeClassic.MOD_ID+".conf"));
    }
}
