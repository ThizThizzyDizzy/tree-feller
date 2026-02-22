package com.thizthizzydizzy.treefeller.platform.sponge.version.v17;
import com.thizthizzydizzy.treefeller.core.TreeFellerConnector;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeFellerConfiguration;
import java.nio.file.Path;
import java.util.function.Function;
public class SpongeConnector implements TreeFellerConnector{
    private final TreeFellerSponge treefeller;
    public SpongeConnector(TreeFellerSponge treefeller){
        this.treefeller = treefeller;
    }
    @Override
    public void log(String text){
        System.out.println("["+TreeFellerSponge.PLUGIN_ID+"] "+text);
    }
    @Override
    public TreeFellerConfiguration loadConfig(Function<Path, TreeFellerConfiguration> defaultLoader){
        return defaultLoader.apply(treefeller.configPath);
    }
}
