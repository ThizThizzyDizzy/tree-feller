package com.thizthizzydizzy.treefeller.forge;
import com.thizthizzydizzy.treefeller.connector.TreeFellerConnector;

public class ForgeConnector implements TreeFellerConnector{
    @Override
    public void log(String text){
        System.out.println("[treefeller] "+text);
    }
}
