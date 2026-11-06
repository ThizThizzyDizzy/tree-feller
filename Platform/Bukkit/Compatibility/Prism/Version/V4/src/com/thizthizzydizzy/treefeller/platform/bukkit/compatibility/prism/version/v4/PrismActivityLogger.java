package com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.prism.version.v4;
import com.thizthizzydizzy.treefeller.core.compatibility.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.prism_mc.prism.api.Prism;
import org.prism_mc.prism.api.storage.ActivityBatch;
import org.prism_mc.prism.paper.actions.PaperBlockAction;
import org.prism_mc.prism.paper.actions.PaperItemStackAction;
import org.prism_mc.prism.paper.actions.types.PaperActionTypeRegistry;
import org.prism_mc.prism.paper.api.activities.PaperActivity;
public class PrismActivityLogger{
    public static Prism prism;
    static void init(Plugin plugin){
        prism = (Prism)plugin;
    }
    static void addBlock(Player player, Block block, BlockState was){
        try{
            ActivityBatch batch = prism.storageAdapter().createActivityBatch();
            batch.startBatch();
            batch.add(PaperActivity.builder()
                .action(new PaperBlockAction(PaperActionTypeRegistry.BLOCK_PLACE, block.getState(), was))
                .location(block.getLocation())
                .cause(player)
                .build()
            );
            batch.commitBatch();
        }catch(Exception ex){
            // These will be caught & handledlater
            throw new RuntimeException(ex);
        }
    }
    static void breakBlock(Player player, Block block){
        try{
            ActivityBatch batch = prism.storageAdapter().createActivityBatch();
            batch.startBatch();
            batch.add(PaperActivity.builder()
                .action(new PaperBlockAction(PaperActionTypeRegistry.BLOCK_BREAK, block.getState()))
                .location(block.getLocation())
                .cause(player)
                .build()
            );
            batch.commitBatch();
        }catch(Exception ex){
            // These will be caught & handledlater
            throw new RuntimeException(ex);
        }
    }
    static void dropItem(Player player, Item item){
        try{
            ActivityBatch batch = prism.storageAdapter().createActivityBatch();
            batch.startBatch();
            batch.add(PaperActivity.builder()
                .action(new PaperItemStackAction(PaperActionTypeRegistry.ITEM_DROP, item.getItemStack()))
                .location(item.getLocation())
                .cause(player) //TODO This probably makes it think the player dropped it; this is block drops
                .build()
            );
            batch.commitBatch();
        }catch(Exception ex){
            // These will be caught & handledlater
            throw new RuntimeException(ex);
        }
    }
}

