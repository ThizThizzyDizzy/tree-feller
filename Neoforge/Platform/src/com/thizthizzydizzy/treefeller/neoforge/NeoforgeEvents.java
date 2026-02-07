package com.thizthizzydizzy.treefeller.neoforge;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
@EventBusSubscriber(modid = "treefeller")
public class NeoforgeEvents{
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event){
        TreeFellerCore.initialize(TreeFellerNeoforge.connector = new NeoforgeConnector());
    }
}
