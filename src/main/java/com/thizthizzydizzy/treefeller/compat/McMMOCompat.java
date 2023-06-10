package com.thizthizzydizzy.treefeller.compat;

import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.SubSkillType;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.Permissions;
import com.gmail.nossr50.util.player.UserManager;
import com.gmail.nossr50.util.random.RandomChanceUtil;
import com.gmail.nossr50.util.skills.RankUtils;
import com.gmail.nossr50.util.skills.SkillActivationType;
import com.thizthizzydizzy.simplegui.ItemBuilder;
import com.thizthizzydizzy.treefeller.Modifier;
import com.thizthizzydizzy.treefeller.OptionBoolean;
import com.thizthizzydizzy.treefeller.Tool;
import com.thizthizzydizzy.treefeller.Tree;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;

public class McMMOCompat extends InternalCompatibility {
    public static OptionBoolean MCMMO_DOUBLE_DROPS = new OptionBoolean("MCMMO Double Drops", true, true, true, true,
            true) {
        @Override
        public String getDesc(boolean ingame) {
            return "Should the mcMMO Double Drops feature apply when cutting down trees?";
        }

        @Override
        public ItemBuilder getConfigurationDisplayItem(Boolean value) {
            return new ItemBuilder(Material.OAK_LOG).setCount(Objects.equals(value, true) ? 2 : 1);
        }
    };

    @Override
    public String getPluginName() {
        return "mcMMO";
    }

    @Override
    public void breakBlock(Tree tree, Tool tool, Player player, ItemStack axe, Block block, List<Modifier> modifiers) {
        if (player == null) return;
        try {
            McMMOPlayer mcmmoPlayer =
                    UserManager.getPlayer(player);
            ExperienceAPI.addXpFromBlock(block.getState(), mcmmoPlayer);
            //canGetDoubleDrops and checkForDoubleDrop are private, so I'll just do it myself
            if (Permissions.isSubSkillEnabled(player,
                    SubSkillType.WOODCUTTING_HARVEST_LUMBER)
                    && RankUtils.hasReachedRank(1, player,
                    SubSkillType.WOODCUTTING_HARVEST_LUMBER)
                    && RandomChanceUtil.isActivationSuccessful(SkillActivationType.RANDOM_LINEAR_100_SCALE_WITH_CAP,
                    SubSkillType.WOODCUTTING_HARVEST_LUMBER, player)) {
                BlockState blockState = block.getState();
                if (MCMMO_DOUBLE_DROPS.get(tool, tree)) {
                    if (mcMMO.getModManager().isCustomLog(blockState) && mcMMO.getModManager().getBlock(blockState).isDoubleDropEnabled()) {
                        modifiers.add(new Modifier(Modifier.Type.LOG_MULT, 2));
                    } else {
                        if (mcMMO.p.getGeneralConfig().getWoodcuttingDoubleDropsEnabled(blockState.getBlockData())) {
                            modifiers.add(new Modifier(Modifier.Type.LOG_MULT, 2));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            //TODO some sort of logging should happen here
        }
    }
}