package com.thizthizzydizzy.treefeller.forge.classic;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
@Mod(modid=TreeFellerForgeClassic.MOD_ID)
public class TreeFellerForgeClassic{
    public static final String MOD_ID = "treefeller";
    public static ForgeLegacyConnector connector;

    @Mod.EventHandler
    public static void onPreInit(FMLPreInitializationEvent event){
        TreeFellerCore.initialize(TreeFellerForgeClassic.connector = new ForgeLegacyConnector());
    }
}
