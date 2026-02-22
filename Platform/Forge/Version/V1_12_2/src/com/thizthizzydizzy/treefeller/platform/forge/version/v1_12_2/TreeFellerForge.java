package com.thizthizzydizzy.treefeller.platform.forge.version.v1_12_2;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
@Mod(modid = TreeFellerForge.MOD_ID)
public class TreeFellerForge{
    public static final String MOD_ID = "treefeller";
    public static ForgeConnector connector;

    @Mod.EventHandler
    public static void onPreInit(FMLPreInitializationEvent event){
        TreeFellerCore.initialize(TreeFellerForge.connector = new ForgeConnector());
    }
}
