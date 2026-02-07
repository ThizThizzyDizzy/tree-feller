package com.thizthizzydizzy.treefeller.core;
import com.thizthizzydizzy.treefeller.connector.TreeFellerConnector;
public class TreeFellerCore{
    private static TreeFellerConnector connector;
    public static void initialize(TreeFellerConnector connector){
        TreeFellerCore.connector = connector;
        connector.log("Initializing Tree Feller...");
    }
}
