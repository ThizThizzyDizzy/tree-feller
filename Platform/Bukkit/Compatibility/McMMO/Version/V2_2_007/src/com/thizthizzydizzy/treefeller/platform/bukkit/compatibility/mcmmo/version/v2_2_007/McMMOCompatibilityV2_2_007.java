package com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.mcmmo.version.v2_2_007;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.compatibility.BukkitPluginCompatibility;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.TreeFellerBukkit;
import com.thizthizzydizzy.treefeller.core.compatibility.*;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
public class McMMOCompatibilityV2_2_007 extends BukkitPluginCompatibility{
    public McMMOCompatibilityV2_2_007(TreeFellerBukkit plugin){super(plugin);}

    private Logger logger;
    @Override protected String requiredClass(){return "com.gmail.nossr50.datatypes.skills.SubSkillType";}
    @Override
    public void initialize() {

        this.logger = plugin.getLogger();
    }
    @Override
    public String getPluginName(){
        return "mcMMO";
    }
    @Override
    public double breakBlock(CompatibilityContext context, Player player, ItemStack axe, Block block){
        if(player==null)return 1;
        com.gmail.nossr50.datatypes.player.McMMOPlayer mcmmoPlayer=com.gmail.nossr50.util.player.UserManager.getPlayer(player);
        com.gmail.nossr50.api.ExperienceAPI.addXpFromBlock(block.getState(),mcmmoPlayer);
        return 1;
    }
    @Override public double bonusDrops(CompatibilityContext context,long pos){
        Player player=player(context);Block block=block(context.world,pos);
                if(com.gmail.nossr50.util.Permissions.isSubSkillEnabled(player, com.gmail.nossr50.datatypes.skills.SubSkillType.WOODCUTTING_HARVEST_LUMBER)
                        &&com.gmail.nossr50.util.skills.RankUtils.hasReachedRank(1, player, com.gmail.nossr50.datatypes.skills.SubSkillType.WOODCUTTING_HARVEST_LUMBER)
                        &&com.gmail.nossr50.util.random.ProbabilityUtil.isSkillRNGSuccessful(com.gmail.nossr50.datatypes.skills.SubSkillType.WOODCUTTING_HARVEST_LUMBER, player)){
                    BlockState blockState = block.getState();
                    if(isCustomLogWithDoubleDropEnabled(blockState)){
                        return 2;
                    }else{
                        if(com.gmail.nossr50.mcMMO.p.getGeneralConfig().getWoodcuttingDoubleDropsEnabled(blockState.getBlockData())){
                            return 2;
                        }
                    }
                }
        return 1;
    }
    public boolean isCustomLogWithDoubleDropEnabled(BlockState blockState){
        try{
            Class<?> modManagerClass = Class.forName("com.gmail.nossr50.util.ModManager");
            Object modManagerInstance = modManagerClass
                    .getMethod("getModManager")
                    .invoke(null);
            Object mcMmoBlock = modManagerClass
                    .getMethod("getBlock", BlockState.class)
                    .invoke(modManagerInstance, blockState);
            boolean isCustomLog = (Boolean) Class.forName("com.gmail.nossr50.util.ModManager")
                    .getMethod("isCustomLog", BlockState.class)
                    .invoke(modManagerInstance, blockState);
            boolean isDoubleDropEnabled = (Boolean) mcMmoBlock.getClass()
                    .getMethod("isDoubleDropEnabled")
                    .invoke(mcMmoBlock);
            return isCustomLog && isDoubleDropEnabled;
        }catch(NoSuchMethodError | ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e){
            logger.log(Level.FINE, "Unable to check for custom blocks in mcMMO.", e);
            return false;
        }
    }
}
