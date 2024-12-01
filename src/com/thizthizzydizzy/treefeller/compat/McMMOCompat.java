package com.thizthizzydizzy.treefeller.compat;
import com.thizthizzydizzy.simplegui.ItemBuilder;
import com.thizthizzydizzy.treefeller.*;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
public class McMMOCompat extends InternalCompatibility{
    public static OptionBoolean MCMMO_DOUBLE_DROPS = new OptionBoolean("MCMMO Double Drops", true, true, true, true, true) {
        @Override
        public String getDesc(boolean ingame){
            return "Should the mcMMO Double Drops feature apply when cutting down trees?";
        }
        @Override
        public ItemBuilder getConfigurationDisplayItem(Boolean value){
            return new ItemBuilder(Material.OAK_LOG).setCount(Objects.equals(value, true)?2:1);
        }
    };
    private Logger logger;
    @Override
    public void init(TreeFeller treeFeller) {
        super.init(treeFeller);
        this.logger = treeFeller.getLogger();
    }
    @Override
    public String getPluginName(){
        return "mcMMO";
    }
    @Override
    public void breakBlock(Tree tree, Tool tool, Player player, ItemStack axe, Block block, List<Modifier> modifiers){
        if(player==null)return;
        try{
            com.gmail.nossr50.datatypes.player.McMMOPlayer mcmmoPlayer = com.gmail.nossr50.util.player.UserManager.getPlayer(player);
            com.gmail.nossr50.api.ExperienceAPI.addXpFromBlock(block.getState(), mcmmoPlayer);
            //canGetDoubleDrops and checkForDoubleDrop are private, so I'll just do it myself
            if(MCMMO_DOUBLE_DROPS.get(tool, tree)){
                if(com.gmail.nossr50.util.Permissions.isSubSkillEnabled(player, com.gmail.nossr50.datatypes.skills.SubSkillType.WOODCUTTING_HARVEST_LUMBER)
                        &&com.gmail.nossr50.util.skills.RankUtils.hasReachedRank(1, player, com.gmail.nossr50.datatypes.skills.SubSkillType.WOODCUTTING_HARVEST_LUMBER)
                        &&com.gmail.nossr50.util.random.ProbabilityUtil.isSkillRNGSuccessful(com.gmail.nossr50.datatypes.skills.SubSkillType.WOODCUTTING_HARVEST_LUMBER, player)){
                    BlockState blockState = block.getState();
                    if(isCustomLogWithDoubleDropEnabled(blockState)){
                        modifiers.add(new Modifier(Modifier.Type.LOG_MULT, 2));
                    }else{
                        if(com.gmail.nossr50.mcMMO.p.getGeneralConfig().getWoodcuttingDoubleDropsEnabled(blockState.getBlockData())){
                            modifiers.add(new Modifier(Modifier.Type.LOG_MULT, 2));
                        }
                    }
                }
            }
        }catch(Exception ex){}
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