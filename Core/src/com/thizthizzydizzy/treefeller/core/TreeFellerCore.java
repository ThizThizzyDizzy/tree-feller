package com.thizthizzydizzy.treefeller.core;
import com.thizthizzydizzy.treefeller.core.config.ConfigReader;
import com.thizthizzydizzy.treefeller.core.config.ConfigWriter;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeFellerConfiguration;
public class TreeFellerCore{
    private static TreeFellerConnector connector;
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
}
