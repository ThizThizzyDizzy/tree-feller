package com.thizthizzydizzy.treefeller.core;
import com.thizthizzydizzy.treefeller.core.config.ConfigReader;
import com.thizthizzydizzy.treefeller.core.config.ConfigWriter;
import com.thizthizzydizzy.treefeller.core.config.ISpecialConfigObject;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeFellerConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IBlockDefinition;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IItemDefinition;
import com.thizthizzydizzy.treefeller.core.connector.TreeFellerConnector;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.Config;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.ConfigValue;
public class TreeFellerCore{
    public static TreeFellerConnector connector;
    private static TreeFellerConfiguration config;
    public static void initialize(TreeFellerConnector connector){
        TreeFellerCore.connector = connector;
        connector.log("Initializing Tree Feller...");
        connector.log("Loading config...");
        config = connector.loadConfig(ConfigReader::read);
        if(config.debug.startup_logs){
            connector.log(ConfigWriter.writeToString(config));
        }
        connector.log("Tree Feller initialization complete!");
    }
    public static Class<? extends ISpecialConfigObject> mapSpecialConfigObject(Config rawConfig, String key, ConfigValue value, Class<? extends ISpecialConfigObject> targetType){
        if(targetType==IBlockDefinition.class){
            return connector.mapBlockDefinitionClass(rawConfig, key, value);
        }
        if(targetType==IItemDefinition.class){
            return connector.mapItemDefinitionClass(rawConfig, key, value);
        }
        throw new AssertionError("Unknown special config object type: "+targetType.getName());
    }
}
