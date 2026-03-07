package com.thizthizzydizzy.treefeller.test;
import com.thizthizzydizzy.treefeller.core.config.ISpecialConfigObject;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeFellerConfiguration;
import com.thizthizzydizzy.treefeller.core.connector.TreeFellerConnector;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.Config;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.ConfigValue;
import com.thizthizzydizzy.treefeller.test.definition.TestBlockDefinition;
import com.thizthizzydizzy.treefeller.test.definition.TestItemDefinition;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
public class TestConnector implements TreeFellerConnector{
    @Override
    public void log(String text){
        System.out.println(text);
    }
    @Override
    public TreeFellerConfiguration loadConfig(Function<Path, TreeFellerConfiguration> defaultLoader){
        return null;
    }
    @Override
    public Class<? extends ISpecialConfigObject> mapBlockDefinitionClass(Config rawConfig, String key, ConfigValue value){
        return TestBlockDefinition.class;
    }
    @Override
    public Class<? extends ISpecialConfigObject> mapItemDefinitionClass(Config rawConfig, String key, ConfigValue value){
        return TestItemDefinition.class;
    }
    @Override
    public void buildDefaultConfig(TreeFellerConfiguration config){
    }
    @Override
    public Collection<IPlayerConnector> getAdminPlayers(){
        return Collections.emptyList();
    }

}
