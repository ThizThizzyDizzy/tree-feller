package com.thizthizzydizzy.treefeller.forge;
import com.thizthizzydizzy.treefeller.config.structure.TreeFellerConfiguration;
import com.thizthizzydizzy.treefeller.connector.TreeFellerConnector;
import java.nio.file.Path;
import java.util.function.Function;
import net.minecraftforge.fml.loading.FMLPaths;

public class ForgeConnector implements TreeFellerConnector{
    @Override
    public void log(String text){
        System.out.println("["+TreeFellerForge.MOD_ID+"] "+text);
    }
    @Override
    public TreeFellerConfiguration loadConfig(Function<Path, TreeFellerConfiguration> defaultLoader){
        return defaultLoader.apply(FMLPaths.CONFIGDIR.get().resolve(TreeFellerForge.MOD_ID+".conf"));
    }
}
