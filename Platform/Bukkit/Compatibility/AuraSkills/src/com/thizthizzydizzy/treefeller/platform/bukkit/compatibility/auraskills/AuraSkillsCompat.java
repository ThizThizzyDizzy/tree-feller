package com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.auraskills;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.compatibility.BukkitPluginCompatibility;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.TreeFellerBukkit;
import com.thizthizzydizzy.treefeller.core.compatibility.CompatibilityContext;
public final class AuraSkillsCompat extends BukkitPluginCompatibility{
    public AuraSkillsCompat(TreeFellerBukkit plugin){super(plugin);}
    @Override public String getPluginName(){return "AuraSkills";}
    @Override public double testExperience(CompatibilityContext context,String name){
        dev.aurelium.auraskills.api.AuraSkillsApi api=dev.aurelium.auraskills.api.AuraSkillsApi.get();
        return api.getUser(player(context).getUniqueId()).getSkillXp(api.getGlobalRegistry().getSkill(dev.aurelium.auraskills.api.registry.NamespacedId.fromString(name)));
    }
    @Override public void experience(CompatibilityContext context,String name,double amount,boolean modifiers){
        dev.aurelium.auraskills.api.AuraSkillsApi api=dev.aurelium.auraskills.api.AuraSkillsApi.get();
        dev.aurelium.auraskills.api.skill.Skill skill=api.getGlobalRegistry().getSkill(dev.aurelium.auraskills.api.registry.NamespacedId.fromString(name));
        if(skill==null)return;
        if(modifiers)api.getUser(player(context).getUniqueId()).addSkillXp(skill,amount);
        else api.getUser(player(context).getUniqueId()).addSkillXpRaw(skill,amount);
    }
}
