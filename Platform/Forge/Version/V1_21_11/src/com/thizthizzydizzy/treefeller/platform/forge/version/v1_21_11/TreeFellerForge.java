package com.thizthizzydizzy.treefeller.platform.forge.version.v1_21_11;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
@Mod(TreeFellerForge.MOD_ID)
public class TreeFellerForge{
    public static final String MOD_ID = "treefeller";
    public static ForgeConnector connector;
    public TreeFellerForge(FMLJavaModLoadingContext context){
        FMLCommonSetupEvent.getBus(context.getModBusGroup()).addListener(this::onCommonSetup);
    }
    private void onCommonSetup(FMLCommonSetupEvent event){
        TreeFellerCore.initialize(connector = new ForgeConnector());
    }
}
