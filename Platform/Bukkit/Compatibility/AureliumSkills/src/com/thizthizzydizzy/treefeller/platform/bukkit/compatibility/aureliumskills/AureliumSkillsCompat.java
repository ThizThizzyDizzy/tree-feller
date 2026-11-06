package com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.aureliumskills;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.compatibility.BukkitPluginCompatibility;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.TreeFellerBukkit;
import com.thizthizzydizzy.treefeller.core.compatibility.CompatibilityContext;
public final class AureliumSkillsCompat extends BukkitPluginCompatibility{
    public AureliumSkillsCompat(TreeFellerBukkit plugin){super(plugin);}
    @Override public String getPluginName(){return "AureliumSkills";}
    @Override public void experience(CompatibilityContext context,String name,double amount,boolean modifiers){
        com.archyx.aureliumskills.skills.Skill skill=com.archyx.aureliumskills.api.AureliumAPI.getPlugin().getSkillRegistry().getSkill(name);
        if(skill==null)return;
        if(modifiers)com.archyx.aureliumskills.api.AureliumAPI.addXp(player(context),skill,amount);
        else com.archyx.aureliumskills.api.AureliumAPI.addXpRaw(player(context),skill,amount);
    }
}
