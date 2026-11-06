package com.thizthizzydizzy.treefeller.platform.bukkit.core.compatibility;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.TreeFellerBukkit;
import com.thizthizzydizzy.treefeller.core.compatibility.TreeFellerCompatibility;
/** Direct construction, compiled after compatibility projects, like version connectors. */
public final class BukkitCompatibilities{
    private BukkitCompatibilities(){}
    public static void register(TreeFellerBukkit plugin){
        if(plugin.getServer().getPluginManager().isPluginEnabled("Drop2Inventory"))register(plugin,() -> new com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.drop2inventory.Drop2InventoryCompat(plugin));
        if(plugin.getServer().getPluginManager().isPluginEnabled("AureliumSkills"))register(plugin,() -> new com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.aureliumskills.AureliumSkillsCompat(plugin));
        if(plugin.getServer().getPluginManager().isPluginEnabled("WorldGuard"))register(plugin,() -> new com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.worldguard.WorldGuardCompat(plugin));
        if(plugin.getServer().getPluginManager().isPluginEnabled("OreRegenerator"))register(plugin,() -> new com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.oreregenerator.OreRegeneratorCompat(plugin));
        if(plugin.getServer().getPluginManager().isPluginEnabled("Towny"))register(plugin,() -> new com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.towny.TownyCompat(plugin));
        if(plugin.getServer().getPluginManager().isPluginEnabled("LogBlock"))register(plugin,() -> new com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.logblock.version.v1_16_5_1.LogBlockCompatibilityV1_16_5_1(plugin));
        if(plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI"))register(plugin,() -> new com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.placeholderapi.PlaceholderAPICompat(plugin));
        if(plugin.getServer().getPluginManager().isPluginEnabled("Jobs"))register(plugin,() -> new com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.jobsreborn.JobsRebornCompat(plugin));
        if(plugin.getServer().getPluginManager().isPluginEnabled("MMOCore"))register(plugin,() -> new com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.mmocore.MMOCoreCompat(plugin));
        if(plugin.getServer().getPluginManager().isPluginEnabled("CoreProtect"))register(plugin,() -> new com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.coreprotect.CoreProtectCompat(plugin));
        if(plugin.getServer().getPluginManager().isPluginEnabled("EcoSkills"))register(plugin,() -> new com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.ecoskills.EcoSkillsCompat(plugin));
        if(plugin.getServer().getPluginManager().isPluginEnabled("BlockRegen"))register(plugin,() -> new com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.blockregen.version.v3_25.BlockRegenCompatibilityV3_25(plugin));
        if(plugin.getServer().getPluginManager().isPluginEnabled("LogBlock"))register(plugin,() -> new com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.logblock.version.v1_19_0_0.LogBlockCompatibilityV1_19_0_0(plugin));
        if(plugin.getServer().getPluginManager().isPluginEnabled("SaberFactions"))register(plugin,() -> new com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.saberfactions.SaberFactionsCompat(plugin));
        if(plugin.getServer().getPluginManager().isPluginEnabled("BlockRegen"))register(plugin,() -> new com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.blockregen.version.v3_10_1.BlockRegenCompatibilityV3_10_1(plugin));
        if(plugin.getServer().getPluginManager().isPluginEnabled("Prism"))register(plugin,() -> new com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.prism.version.v3.PrismCompatibilityV3(plugin));
        if(plugin.getServer().getPluginManager().isPluginEnabled("mcMMO"))register(plugin,() -> new com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.mcmmo.version.v2_2_007.McMMOCompatibilityV2_2_007(plugin));
        if(plugin.getServer().getPluginManager().isPluginEnabled("AuraSkills"))register(plugin,() -> new com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.auraskills.AuraSkillsCompat(plugin));
        if(plugin.getServer().getPluginManager().isPluginEnabled("Prism"))register(plugin,() -> new com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.prism.version.v2.PrismCompatibilityV2(plugin));
        if(plugin.getServer().getPluginManager().isPluginEnabled("AuroraCollections"))register(plugin,() -> new com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.auroracollections.AuroraCollectionsCompat(plugin));
        if(plugin.getServer().getPluginManager().isPluginEnabled("EcoJobs"))register(plugin,() -> new com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.ecojobs.EcoJobsCompat(plugin));
        if(plugin.getServer().getPluginManager().isPluginEnabled("GriefPrevention"))register(plugin,() -> new com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.griefprevention.GriefPreventionCompat(plugin));
        if(plugin.getServer().getPluginManager().isPluginEnabled("mcMMO"))register(plugin,() -> new com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.mcmmo.version.classic.McMMOCompatibilityClassic(plugin));
        if(plugin.getServer().getPluginManager().isPluginEnabled("Quests"))register(plugin,() -> new com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.quests.QuestsCompat(plugin));
        if(plugin.getServer().getPluginManager().isPluginEnabled("Lands"))register(plugin,() -> new com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.lands.LandsCompat(plugin));
        if(plugin.getServer().getPluginManager().isPluginEnabled("prism"))register(plugin,() -> new com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.prism.version.v4.PrismCompatibilityV4(plugin));
    }
    private static void register(TreeFellerBukkit plugin,java.util.function.Supplier<BukkitPluginCompatibility> factory){
        try{TreeFellerCompatibility.register(factory.get());}
        catch(RuntimeException|LinkageError ex){plugin.getLogger().log(java.util.logging.Level.SEVERE,"Could not load compatibility connector",ex);}
    }
}
