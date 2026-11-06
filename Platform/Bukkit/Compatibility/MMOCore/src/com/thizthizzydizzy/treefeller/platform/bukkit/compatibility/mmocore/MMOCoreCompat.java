package com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.mmocore;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.compatibility.BukkitPluginCompatibility;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.TreeFellerBukkit;
import com.thizthizzydizzy.treefeller.core.compatibility.*;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;

public class MMOCoreCompat extends BukkitPluginCompatibility{
    public MMOCoreCompat(TreeFellerBukkit plugin){super(plugin);}
    private static boolean installed;

    @Override
    public String getPluginName(){
        return "MMOCore";
    }

    @Override
    public void reload() {
        installed = installed();
    }

    @Override public void experience(CompatibilityContext context,String profession,double amount,boolean modifiers){
        net.Indyuce.mmocore.api.player.PlayerData data=net.Indyuce.mmocore.api.player.PlayerData.get(player(context));
        if(profession.equals("global"))data.giveExperience((int)amount,net.Indyuce.mmocore.experience.EXPSource.SOURCE);
        else data.getCollectionSkills().giveExperience(net.Indyuce.mmocore.MMOCore.plugin.professionManager.get(profession),(int)amount,net.Indyuce.mmocore.experience.EXPSource.SOURCE);
    }
    @Override public int professionLevel(CompatibilityContext context,String profession){
        net.Indyuce.mmocore.api.player.PlayerData data=net.Indyuce.mmocore.api.player.PlayerData.get(player(context));
        return profession.equals("global")?data.getLevel():data.getCollectionSkills().getLevel(profession);
    }
    @Override public void regenerate(CompatibilityContext context,long pos){
        Block block=block(context.world,pos);
        net.Indyuce.mmocore.api.block.BlockInfo info=net.Indyuce.mmocore.MMOCore.plugin.mineManager.getInfo(block);
        String saved=block.getBlockData().getAsString();
        if(info!=null&&info.hasRegen())Bukkit.getScheduler().runTaskLater(net.Indyuce.mmocore.MMOCore.plugin,
            () -> net.Indyuce.mmocore.MMOCore.plugin.mineManager.initialize(info.startRegeneration(Bukkit.createBlockData(saved),block.getLocation()),true),1);
    }
}
