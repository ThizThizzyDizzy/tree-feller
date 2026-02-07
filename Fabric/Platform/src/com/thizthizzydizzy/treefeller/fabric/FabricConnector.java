package com.thizthizzydizzy.treefeller.fabric;
import com.thizthizzydizzy.treefeller.connector.TreeFellerConnector;
public class FabricConnector implements TreeFellerConnector{
    @Override
    public void log(String text){
        System.out.println("[treefeller] "+text);
    }
}
