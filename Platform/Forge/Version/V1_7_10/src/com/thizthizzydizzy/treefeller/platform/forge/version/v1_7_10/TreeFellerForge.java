package com.thizthizzydizzy.treefeller.platform.forge.version.v1_7_10;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
@Mod(modid = TreeFellerForge.MOD_ID)
public class TreeFellerForge{
    public static final String MOD_ID = "treefeller";
    public static ForgeConnector connector;

    @Mod.EventHandler
    public static void onPreInit(FMLPreInitializationEvent event){
        TreeFellerCore.initialize(TreeFellerForge.connector = new ForgeConnector());
    }
}
