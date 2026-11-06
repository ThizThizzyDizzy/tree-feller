package com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.quests;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.compatibility.BukkitPluginCompatibility;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.TreeFellerBukkit;
import com.thizthizzydizzy.treefeller.core.compatibility.*;
import com.leonardobishop.quests.bukkit.BukkitQuestsPlugin;
import com.leonardobishop.quests.bukkit.tasktype.type.MiningTaskType;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
public class QuestsCompat extends BukkitPluginCompatibility{
    public QuestsCompat(TreeFellerBukkit plugin){super(plugin);}
    private BukkitQuestsPlugin quests;
    @Override
    public String getPluginName(){
        return "Quests";
    }
    @Override
    public void initialize(){
        quests = (BukkitQuestsPlugin)getPlugin();
    }
    @Override
    public double breakBlock(CompatibilityContext context, Player player, ItemStack axe, Block block){
        // There is a lot of internal logic, but it only uses the block/player from the event, so I can leave Quests to do the heavy lifting
        ((MiningTaskType)quests.getTaskTypeManager().getTaskType("blockbreak")).onBlockBreak(new BlockBreakEvent(block, player));

        return 1;
    }
    @Override public TestCounter testQuest(com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector player,java.util.Map<String,Object> options){
        BukkitQuestsPlugin target=(BukkitQuestsPlugin)getPlugin();
        com.leonardobishop.quests.common.quest.Quest quest=new com.leonardobishop.quests.common.quest.Quest.Builder("treefeller_"+java.util.UUID.randomUUID().toString()).build();
        com.leonardobishop.quests.common.quest.Task task=new com.leonardobishop.quests.common.quest.Task("logs","blockbreak");
        options.forEach(task::addConfigValue);quest.registerTask(task);target.getQuestManager().registerQuest(quest);target.getTaskTypeManager().getTaskType("blockbreak").registerQuest(quest);
        com.leonardobishop.quests.common.player.QPlayer user=target.getPlayerManager().getPlayer(((com.thizthizzydizzy.treefeller.platform.bukkit.core.connector.BukkitPlayerConnector)player).getPlayer().getUniqueId());
        com.leonardobishop.quests.common.player.questprogressfile.QuestProgress progress=user.getQuestProgressFile().generateBlankQuestProgress(quest);
        progress.setStarted(true);user.getQuestProgressFile().addQuestProgress(progress);
        return new TestCounter(){
            @Override public int count(){Object value=progress.getTaskProgress("logs").getProgress();return value==null?0:((Number)value).intValue();}
            @Override public void close(){user.getQuestProgressFile().getQuestProgressMap().remove(quest.getId());target.reloadQuests();}
        };
    }
}

