package com.thizthizzydizzy.treefeller.core;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeFellerConfiguration;
import java.nio.file.Path;
import java.util.function.Function;
public interface TreeFellerConnector{
    public void log(String text);
    public TreeFellerConfiguration loadConfig(Function<Path, TreeFellerConfiguration> defaultLoader);
}
