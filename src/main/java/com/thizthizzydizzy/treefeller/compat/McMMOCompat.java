package com.thizthizzydizzy.treefeller.compat;
import com.thizthizzydizzy.simplegui.ItemBuilder;
import com.thizthizzydizzy.treefeller.Modifier;
import com.thizthizzydizzy.treefeller.OptionBoolean;
import com.thizthizzydizzy.treefeller.Tool;
import com.thizthizzydizzy.treefeller.Tree;
import java.util.List;
import java.util.Objects;
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
            if(com.gmail.nossr50.util.Permissions.isSubSkillEnabled(player, com.gmail.nossr50.datatypes.skills.SubSkillType.WOODCUTTING_HARVEST_LUMBER)
                    &&com.gmail.nossr50.util.skills.RankUtils.hasReachedRank(1, player, com.gmail.nossr50.datatypes.skills.SubSkillType.WOODCUTTING_HARVEST_LUMBER)
                    &&com.gmail.nossr50.util.random.RandomChanceUtil.isActivationSuccessful(com.gmail.nossr50.util.skills.SkillActivationType.RANDOM_LINEAR_100_SCALE_WITH_CAP, com.gmail.nossr50.datatypes.skills.SubSkillType.WOODCUTTING_HARVEST_LUMBER, player)){
                BlockState blockState = block.getState();
                if(MCMMO_DOUBLE_DROPS.get(tool, tree)){
                    if(com.gmail.nossr50.mcMMO.getModManager().isCustomLog(blockState) && com.gmail.nossr50.mcMMO.getModManager().getBlock(blockState).isDoubleDropEnabled()){
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
}