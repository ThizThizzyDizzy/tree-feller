package com.thizthizzydizzy.treefeller.neoforge;
import com.thizthizzydizzy.treefeller.connector.TreeFellerConnector;
public class NeoforgeConnector implements TreeFellerConnector{
    @Override
    public void log(String text){
        System.out.println("[treefeller] "+text);
    }
}
