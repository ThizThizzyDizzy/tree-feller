package com.thizthizzydizzy.treefeller.forge.reforged;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
@Mod(TreeFellerForgeReforged.MOD_ID)
public class TreeFellerForgeReforged{
    public static final String MOD_ID = "treefeller";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static ForgeConnector connector;
}
