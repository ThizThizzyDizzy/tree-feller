package com.thizthizzydizzy.treefeller.core.connector;
import com.thizthizzydizzy.treefeller.core.config.ISpecialConfigObject;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeFellerConfiguration;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.Config;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.ConfigValue;
import java.nio.file.Path;
import java.util.function.Function;
public interface TreeFellerConnector{
    public void log(String text);
    public TreeFellerConfiguration loadConfig(Function<Path, TreeFellerConfiguration> defaultLoader);
    public Class<? extends ISpecialConfigObject> mapBlockDefinitionClass(Config rawConfig, String key, ConfigValue value);
    public Class<? extends ISpecialConfigObject> mapItemDefinitionClass(Config rawConfig, String key, ConfigValue value);
}
