package com.thizthizzydizzy.treefeller.connector;
import com.thizthizzydizzy.treefeller.config.structure.TreeFellerConfiguration;
import java.nio.file.Path;
import java.util.function.Function;
public interface TreeFellerConnector{
    public void log(String text);
    public TreeFellerConfiguration loadConfig(Function<Path, TreeFellerConfiguration> defaultLoader);
}
