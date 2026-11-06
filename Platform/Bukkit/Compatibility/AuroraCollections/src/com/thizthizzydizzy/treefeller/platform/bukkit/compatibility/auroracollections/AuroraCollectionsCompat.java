package com.thizthizzydizzy.treefeller.platform.bukkit.compatibility.auroracollections;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.compatibility.BukkitPluginCompatibility;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.TreeFellerBukkit;
import com.thizthizzydizzy.treefeller.core.compatibility.*;
import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.item.ItemManager;
import gg.auroramc.collections.api.AuroraCollectionsProvider;
import gg.auroramc.collections.collection.CollectionManager;
import gg.auroramc.collections.collection.Trigger;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
public class AuroraCollectionsCompat extends BukkitPluginCompatibility{
    public AuroraCollectionsCompat(TreeFellerBukkit plugin){super(plugin);}
    private CollectionManager collectionManager;
    private ItemManager itemManager;
    @Override
    public String getPluginName(){
        return "AuroraCollections";
    }
    @Override
    public void initialize(){
        collectionManager = AuroraCollectionsProvider.getCollectionManager();
        itemManager = AuroraAPI.getItemManager();
    }
    @Override
    public void dropItem(Player player, Item item){
        collectionManager.progressCollections(player, itemManager.resolveId(item.getItemStack()), 1, Trigger.BLOCK_LOOT);
    }
}
