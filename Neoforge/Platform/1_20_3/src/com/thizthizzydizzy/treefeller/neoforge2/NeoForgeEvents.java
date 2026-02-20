package com.thizthizzydizzy.treefeller.neoforge2;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
@Mod.EventBusSubscriber(modid = TreeFellerNeoforge.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NeoForgeEvents{
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event){
        TreeFellerCore.initialize(new NeoforgeConnector());
    }
}
