package com.thizthizzydizzy.treefeller.sponge;
import com.thizthizzydizzy.treefeller.connector.TreeFellerConnector;
public class SpongeConnector implements TreeFellerConnector{
    @Override
    public void log(String text){
        System.out.println("[treefeller] "+text);
    }
}
