package com.thizthizzydizzy.treefeller.forge.reforged;
import com.mojang.logging.LogUtils;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
@Mod(TreeFellerForgeReforged.MOD_ID)
public class TreeFellerForgeReforged{
    public static final String MOD_ID = "treefeller";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static ForgeConnector connector;
    public TreeFellerForgeReforged(){
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
    }
    private void onCommonSetup(FMLCommonSetupEvent event){
        TreeFellerCore.initialize(connector = new ForgeConnector());
    }
}
