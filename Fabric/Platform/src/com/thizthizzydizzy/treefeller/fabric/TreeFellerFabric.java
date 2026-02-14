package com.thizthizzydizzy.treefeller.fabric;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import net.fabricmc.api.ModInitializer;
public class TreeFellerFabric implements ModInitializer{
    public static final String MOD_ID = "treefeller";
    public FabricConnector connector;
    @Override
    public void onInitialize(){
        TreeFellerCore.initialize(connector = new FabricConnector());
    }
}
