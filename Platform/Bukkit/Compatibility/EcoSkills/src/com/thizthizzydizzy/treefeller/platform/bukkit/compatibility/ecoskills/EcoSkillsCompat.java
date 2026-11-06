package com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.ecoskills;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.compatibility.BukkitPluginCompatibility;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.TreeFellerBukkit;
import com.thizthizzydizzy.treefeller.core.compatibility.CompatibilityContext;
public final class EcoSkillsCompat extends BukkitPluginCompatibility{
    public EcoSkillsCompat(TreeFellerBukkit plugin){super(plugin);}
    @Override public String getPluginName(){return "EcoSkills";}
    @Override public void experience(CompatibilityContext context,String name,double amount,boolean modifiers){
        com.willfp.ecoskills.api.EcoSkillsAPI.gainSkillXP(player(context),com.willfp.ecoskills.skills.Skills.INSTANCE.getByID(name),amount);
    }
}
