package com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.ecojobs;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.compatibility.BukkitPluginCompatibility;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.TreeFellerBukkit;
import com.thizthizzydizzy.treefeller.core.compatibility.CompatibilityContext;
public final class EcoJobsCompat extends BukkitPluginCompatibility{
    public EcoJobsCompat(TreeFellerBukkit plugin){super(plugin);}
    @Override public String getPluginName(){return "EcoJobs";}
    @Override public void experience(CompatibilityContext context,String name,double amount,boolean modifiers){
        com.willfp.ecojobs.jobs.Job job=com.willfp.ecojobs.jobs.Jobs.getByID(name);
        if(job!=null)com.willfp.ecojobs.api.EcoJobsAPI.instance.giveJobExperience(player(context),job,amount,modifiers);
    }
}
