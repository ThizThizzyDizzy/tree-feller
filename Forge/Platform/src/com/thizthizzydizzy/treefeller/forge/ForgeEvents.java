package com.thizthizzydizzy.treefeller.forge;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
@EventBusSubscriber(modid = "treefeller")
public class ForgeEvents{
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event){
        TreeFellerCore.initialize(TreeFellerForge.connector = new ForgeConnector());
    }
}
