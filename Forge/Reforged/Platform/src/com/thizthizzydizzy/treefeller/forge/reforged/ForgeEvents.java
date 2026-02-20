package com.thizthizzydizzy.treefeller.forge.reforged;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
@EventBusSubscriber(modid = TreeFellerForgeReforged.MOD_ID)
public class ForgeEvents{
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event){
        TreeFellerCore.initialize(TreeFellerForgeReforged.connector = new ForgeConnector());
    }
}
