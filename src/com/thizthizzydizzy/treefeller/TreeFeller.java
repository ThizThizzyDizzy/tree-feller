package com.thizthizzydizzy.treefeller;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
public class TreeFeller extends JavaPlugin{
    public HashMap<UUID, HashMap<Tree, Long>> treeCooldowns = new HashMap<>();
    public HashMap<UUID, HashMap<Tool, Long>> itemCooldowns = new HashMap<>();
    public ArrayList<UUID> fallingBlocks = new ArrayList<>();
    public int spawnSaplings;
    public boolean replantSaplings;
    public boolean respectUnbreaking;
    public int scanDistance;
    public boolean cuttingAnimation;
    public int animDelay;
    public boolean ignoreLeafData;
    public boolean startupLogs;
    public boolean diagonalLeaves;
    boolean debug = false;
    public void fellTree(BlockBreakEvent event){
        if(fellTree(event.getBlock(), event.getPlayer()))event.setCancelled(true);
    }
    public boolean fellTree(Block block, Player player){
        return fellTree(block, player.getInventory().getItemInMainHand(), player);
    }
    public boolean fellTree(Block block, ItemStack axe, Player player){
        return fellTree(block, player, axe, player.getGameMode(), player.isSneaking());
    }
    public boolean fellTree(Block block, Player player, ItemStack axe, GameMode gamemode, boolean sneaking){
        return fellTree(block, player, axe, gamemode, sneaking, true)!=null;
    }
    /**
     * Fells a tree
     * @param block the block that was broken
     * @param player the player whose permissions are to be used. CAN BE NULL
     * @param axe the tool used to break the block
     * @param gamemode the player's gamemode
     * @param sneaking weather or not the player was sneaking
     * @param dropItems weather or not to drop items
     * @return the items that would have been dropped, only <code>dropItems</code> is false. Returns null if the tree was not felled.
     */
    public ArrayList<ItemStack> fellTree(Block block, Player player, ItemStack axe, GameMode gamemode, boolean sneaking, boolean dropItems){
        ArrayList<ItemStack> droppedItems = new ArrayList<>();
        Material material = block.getType();
        for(Tree tree : trees){
            if(tree.trunk.contains(material)){
                debug(player, "Attempting to fell tree #"+trees.indexOf(tree)+" ("+material.toString()+")");
                if(tree.worlds!=null){
                    boolean isInWorld = false;
                    for(String world : tree.worlds){
                        if(world.equalsIgnoreCase(block.getWorld().getName())||world.equalsIgnoreCase(block.getWorld().getUID().toString())){
                            debug(player, false, true, "World is valid for this tree!");
                            isInWorld = true;
                            break;
                        }
                    }
                    if(!isInWorld){
                        debug(player, false, false, "World "+block.getWorld().getName()+" ("+block.getWorld().getUID().toString()+") is invalid for this tree!");
                        continue;
                    }
                }
                if(player!=null){
                    if(tree.cooldown>0){
                        long last = getLastTime(player, tree);
                        long now = System.currentTimeMillis();
                        long diff = (now-last)/50;
                        if(diff<=tree.cooldown){
                            debug(player, false, false, "Tree cooldown remaining: "+(tree.cooldown-diff)+"ms");
                            continue;
                        }
                        debug(player, false, true, "Tree cooldown ready!");
                    }
                }
                TOOL:for(Tool tool : tools){
                    if(tool.material!=Material.AIR&&axe.getType()!=tool.material){
                        debug(player, false, false, "Tool item does not match: "+tool.material);
                        continue;
                    }
                    debug(player, false, true, "Tool item matches: "+tool.material);
                    if(tool.worlds!=null){
                        boolean isInWorld = false;
                        for(String world : tool.worlds){
                            if(world.equalsIgnoreCase(block.getWorld().getName())||world.equalsIgnoreCase(block.getWorld().getUID().toString())){
                                debug(player, false, true, "World is valid for this tool!");
                                isInWorld = true;
                                break;
                            }
                        }
                        if(!isInWorld){
                            debug(player, false, false, "World "+block.getWorld().getName()+" ("+block.getWorld().getUID().toString()+") is invalid for this tree!");
                            continue;
                        }
                    }
                    if(player!=null){
                        if(tool.cooldown>0){
                            long last = getLastTime(player, tool);
                            long now = System.currentTimeMillis();
                            long diff = (now-last)/50;
                            if(diff<=tool.cooldown){
                                debug(player, false, false, "Tool cooldown remaining: "+(tool.cooldown-diff)+"ms");
                                continue;
                            }
                            debug(player, false, true, "Tool cooldown ready!");
                        }
                    }
                    switch(gamemode){
                        case ADVENTURE:
                            if(!tool.enableAdventure){
                                debug(player, false, false, "This tool does not work in adventure mode!");
                                continue;
                            }
                            break;
                        case CREATIVE:
                            if(!tool.enableCreative){
                                debug(player, false, false, "This tool does not work in creative mode!");
                                continue;
                            }
                            break;
                        case SPECTATOR:
                            debug(player, true, false, "The Tree Feller does not work in spectator mode!");
                            return null;
                        case SURVIVAL:
                            if(!tool.enableSurvival){
                                debug(player, false, false, "This tool does not work in survival mode!");
                                continue;
                            }
                            break;
                    }
                    debug(player, false, true, "Gamemode valid!");
                    if(sneaking){
                        if(!tool.withSneak){
                            debug(player, false, false, "This tool does not work when sneaking!");
                            continue;
                        }
                    }else{
                        if(!tool.withoutSneak){
                            debug(player, false, false, "This tool does not work when not sneaking!");
                            continue;
                        }
                    }
                    debug(player, false, true, "Sneaking state valid!");
                    if(!tool.allowedTrees.isEmpty()&&!tool.allowedTrees.contains(tree)){
                        debug(player, false, false, "This tool cannot cut down this tree!");
                        continue;
                    }
                    debug(player, false, true, "This tool can cut down this tree!");
                    if(tool.name!=null&&axe.hasItemMeta()&&!axe.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', tool.name))){
                        debug(player, false, false, "Tool name does not match: "+ChatColor.translateAlternateColorCodes('&', tool.name));
                        continue;
                    }
                    debug(player, false, true, "Tool name matches!");
                    ArrayList<String> pendingLores = new ArrayList<>(tool.requiredLore);
                    if(axe.hasItemMeta()&&axe.getItemMeta().hasLore()){
                        for(String loreStr : axe.getItemMeta().getLore()){
                            for(Iterator<String> it = pendingLores.iterator(); it.hasNext();){
                                String lore = ChatColor.translateAlternateColorCodes('&', it.next());
                                if(loreStr.contains(lore)){
                                    debug(player, false, true, "Tool contains required lore: "+lore);
                                    it.remove();
                                }
                            }
                        }
                    }
                    if(!pendingLores.isEmpty()){
                        debug(player, false, false, "Tool is missing required lore: "+pendingLores.get(0));
                        continue;
                    }
                    if(player!=null){
                        ArrayList<String> pendingPermissions = new ArrayList<>(tool.requiredPermissions);
                        for(Iterator<String> it = pendingPermissions.iterator(); it.hasNext();){
                            String perm = it.next();
                            if(player.hasPermission(perm)){
                                debug(player, false, true, "Player has required permission: "+perm);
                                it.remove();
                            }
                        }
                        if(!pendingPermissions.isEmpty()){
                            debug(player, false, false, "Player is missing required permission: "+pendingPermissions.get(0));
                            continue;
                        }
                    }
                    for(Enchantment enchant : tool.requiredEnchantments.keySet()){
                        if(!axe.containsEnchantment(enchant)){
                            debug(player, false, false, "Tool is missing required enchantment: "+enchant.toString());
                            continue TOOL;
                        }
                        if(axe.getEnchantmentLevel(enchant)<tool.requiredEnchantments.get(enchant)){
                            debug(player, false, false, "Tool is missing "+enchant.toString()+" at minimum level: "+tool.requiredEnchantments.get(enchant));
                            continue TOOL;
                        }
                        debug(player, false, true, "Tool contains required enchantment: "+enchant.toString()+" at minimum level: "+tool.requiredEnchantments.get(enchant));
                    }
                    for(Enchantment enchant : tool.bannedEnchantments.keySet()){
                        if(axe.containsEnchantment(enchant)&&axe.getEnchantmentLevel(enchant)>=tool.bannedEnchantments.get(enchant)){
                            debug(player, false, false, "Tool contains banned enchantment: "+enchant.toString()+" above level: "+(tool.bannedEnchantments.get(enchant)-1));
                            continue TOOL;
                        }
                        debug(player, false, true, "Tool does not contain banned enchantment: "+enchant.toString()+" above level "+(tool.bannedEnchantments.get(enchant)-1));
                    }
                    int durability = axe.getType().getMaxDurability()-axe.getDurability();
                    double durabilityPercent = durability/(double)axe.getType().getMaxDurability();
                    if(axe.getType().getMaxDurability()>0){
                        if(durability>tool.maxDurability){
                            debug(player, false, false, "Tool durability is greater than maximum allowed: "+tool.maxDurability);
                            continue;
                        }
                        if(durability<tool.minDurability){
                            debug(player, false, false, "Tool durability is less than minimum allowed: "+tool.minDurability);
                            continue;
                        }
                        if(durabilityPercent>tool.maxDurabilityPercent){
                            debug(player, false, false, "Tool durability is greater than maximum allowed: "+tool.maxDurabilityPercent*100+"%");
                            continue;
                        }
                        if(durabilityPercent<tool.minDurabilityPercent){
                            debug(player, false, false, "Tool durability is less than minimum allowed: "+tool.minDurabilityPercent*100+"%");
                            continue;
                        }
                        debug(player, false, true, "Tool durability is valid!");
                    }
                    debug(player, true, true, "Tool is valid! (Tool #"+tools.indexOf(tool)+") Beinning tree felling checks...");
                    //do calculations and stuff here
                    HashMap<Integer, ArrayList<Block>> blocks = getBlocks(tree.trunk, block, scanDistance, true, false);
                    int total = getTotal(blocks);
                    int minY = block.getY();
                    for(int i : blocks.keySet()){
                        for(Block b : blocks.get(i)){
                            minY = Math.min(minY, b.getY());
                        }
                    }
                    if(total<tree.requiredLogs){
                        debug(player, true, false, "Tree has too few logs: "+total+"<"+tree.requiredLogs);
                        return null;
                    }
                    if(total<tool.requiredLogs){
                        debug(player, true, false, "Tree has too few logs for tool: "+total+"<"+tool.requiredLogs);
                        return null;
                    }
                    if(total>tree.maxLogs){
                        debug(player, true, false, "Tree is too big: "+total+">"+tree.maxLogs);
                        return null;
                    }
                    if(total>tool.maxLogs){
                        debug(player, true, false, "Tree is too big for tool: "+total+">"+tool.maxLogs);
                        return null;
                    }
                    debug(player, true, true, "Tree size is valid!");
                    if(block.getY()-minY>tree.maxHeight-1){
                        int i = block.getY()-minY-(tree.maxHeight-1);
                        debug(player, true, false, "Tree was cut "+i+" blocks too high!");
                        return null;
                    }
                    if(block.getY()-minY>tool.maxHeight-1){
                        int i = block.getY()-minY-(tool.maxHeight-1);
                        debug(player, true, false, "Tree was cut "+i+" blocks too high for tool!");
                        return null;
                    }
                    debug(player, true, true, "Tree cut position is valid!");
                    int durabilityCost = total;
                    durabilityCost*=tool.material==Material.AIR?0:tool.damageMult;
                    durabilityCost*=tree.damageMult;
                    if(respectUnbreaking&&axe.containsEnchantment(Enchantment.DURABILITY)){
                        durabilityCost/=(axe.getEnchantmentLevel(Enchantment.DURABILITY)+1);
                        if(durabilityCost<1)durabilityCost++;
                    }
                    if(gamemode==GameMode.CREATIVE)durability = durabilityCost+10;//always has enough durability
                    if(durabilityCost>durability){
                        if(!tool.allowPartial||!tree.allowPartial){
                            debug(player, false, false, "Tool durability is too low: "+durability+"<"+durabilityCost);
                            return null;
                        }
                        debug(player, "Tool is cutting partial tree!");
                        durabilityCost = total = durability;
                    }
                    ArrayList<Integer> distances = new ArrayList<>(blocks.keySet());
                    Collections.sort(distances);
                    int leaves = 0;
                    HashMap<Integer, ArrayList<Block>> allLeaves = new HashMap<>();
                    FOR:for(int i : distances){
                        for(Block b : blocks.get(i)){
                            HashMap<Integer, ArrayList<Block>> someLeaves = getBlocks(tree.leaves, b, tool.leafRange, diagonalLeaves, tool.playerLeaves&&tree.playerLeaves);
                            leaves+=toList(someLeaves).size();
                            for(int in : someLeaves.keySet()){
                                if(allLeaves.containsKey(in)){
                                    allLeaves.get(in).addAll(someLeaves.get(in));
                                }else{
                                    allLeaves.put(in, someLeaves.get(in));
                                }
                            }
                        }
                    }
                    if(leaves<tree.requiredLeaves){
                        debug(player, true, false, "Tree has too few leaves: "+leaves+"<"+tree.requiredLeaves);
                        return null;
                    }
                    if(leaves<tool.requiredLeaves){
                        debug(player, true, false, "Tree has too few leaves for tool: "+leaves+"<"+tool.requiredLeaves);
                        return null;
                    }
                    debug(player, true, true, "Success! Felling tree...");
                    if(tree.leaveStump||tool.leaveStump){
                        for(int i : blocks.keySet()){
                            for(Iterator<Block> it = blocks.get(i).iterator(); it.hasNext();){
                                Block b = it.next();
                                if(b.getY()<block.getY())it.remove();
                            }
                        }
                    }
                    if(gamemode!=GameMode.CREATIVE){
                        if(axe.getType().getMaxDurability()>0){
                            axe.setDurability((short)(axe.getDurability()+durabilityCost));
                            if(durability==durabilityCost)axe.setAmount(0);
                        }
                    }
                    HashMap<Block, Integer> possibleSaplings = new HashMap<>();
                    if(tree.sapling!=null&&replantSaplings){
                        ArrayList<Block> logs = toList(blocks);
                        for(Block log : logs){
                            if(tree.grasses.contains(log.getRelative(0, -1, 0).getType())){
                                possibleSaplings.put(log, -1);
                            }
                        }
                        for(Block b : possibleSaplings.keySet()){
                            int above = -1;
                            Block b1 = b;
                            while(tree.trunk.contains(b1.getType())){
                                above++;
                                b1 = b1.getRelative(0, 1, 0);
                            }
                            possibleSaplings.put(b, above);
                        }
                        while(possibleSaplings.size()>tree.maxSaplings){
                            ArrayList<Integer> ints = new ArrayList<>(possibleSaplings.values());
                            Collections.sort(ints);
                            int i = ints.get(0);
                            for(Block b : possibleSaplings.keySet()){
                                if(possibleSaplings.get(b)==i){
                                    possibleSaplings.remove(b);
                                    break;
                                }
                            }
                        }
                        for(Block b : possibleSaplings.keySet()){
                            addSapling(b, tree.sapling, spawnSaplings!=2);
                        }
                    }
                    //now the blocks
                    final int t = total;
                    long seed = new Random().nextLong();
                    if(cuttingAnimation){
                        int delay = 0;
                        int ttl = t;
                        int tTL = t;
                        int Ttl = 0;
                        for(int i : distances){
                            int TTL = tTL - Ttl;
                            delay+=animDelay;
                            if(!dropItems){
                                for(Block b : blocks.get(i)){
                                    if(ttl<=0)break;
                                    for(Block leaf : toList(getBlocks(tree.leaves, b, tool.leafRange, diagonalLeaves, tool.playerLeaves&&tree.playerLeaves))){
                                        droppedItems.addAll(tool.leafEnchantments?leaf.getDrops(axe):leaf.getDrops());
                                    }
                                    droppedItems.addAll(b.getDrops(axe));
                                    ttl--;
                                }
                            }
                            new BukkitRunnable() {
                                @Override
                                public void run(){
                                    int tTl = TTL;
                                    for(Block b : blocks.get(i)){
                                        if(tTl<=0)break;
                                        for(Block leaf : toList(getBlocks(tree.leaves, b, tool.leafRange, diagonalLeaves, tool.playerLeaves&&tree.playerLeaves))){
                                            if(dropItems){
                                                breakLeaf(tree, tool, axe, leaf, block, player,seed);
                                            }else leaf.setType(Material.AIR);
                                        }
                                        if(dropItems)breakLog(tree, tool, axe, b, block, player,seed);
                                        else b.setType(Material.AIR);
                                        tTl--;
                                    }
                                }
                            }.runTaskLater(this, delay);
                            Ttl += blocks.get(i).size();
                        }
                        if(spawnSaplings>=1){
                            new BukkitRunnable() {
                                @Override
                                public void run(){
                                    for(Block b : possibleSaplings.keySet()){
                                        Sapling s = getSapling(b);
                                        if(s!=null)s.place();
                                    }
                                }
                            }.runTaskLater(this, delay+1);
                        }
                    }else{
                        for(int i : distances){
                            for(Block b : blocks.get(i)){
                                if(total<=0)break;
                                    for(Block leaf : toList(getBlocks(tree.leaves, b, tool.leafRange, diagonalLeaves, tool.playerLeaves&&tree.playerLeaves))){
                                        if(dropItems){
                                            breakLeaf(tree, tool, axe, leaf, block, player,seed);
                                        }else{
                                            droppedItems.addAll(tool.leafEnchantments?leaf.getDrops(axe):leaf.getDrops());
                                            leaf.setType(Material.AIR);
                                        }
                                    }
                                if(dropItems)breakLog(tree, tool, axe, b, block, player,seed);
                                else{
                                    droppedItems.addAll(b.getDrops(axe));
                                    b.setType(Material.AIR);
                                }
                                total--;
                            }
                        }
                        if(spawnSaplings>=1){
                            for(Block b : possibleSaplings.keySet()){
                                Sapling s = getSapling(b);
                                if(s!=null)s.place();
                            }
                        }
                    }
                    if(player!=null){
                        setLastTime(player, tree, System.currentTimeMillis());
                        setLastTime(player, tool, System.currentTimeMillis());
                    }
                    return droppedItems;
                }
            }
        }
        return null;
    }
    /**
     * Gets the size (number of logs) of a tree (Only for use by other plugins)
     * @param block the block that was broken
     * @param axe the tool used to break it
     * @return the size of the tree, in logs (0 if no tree can be felled)
     */
    public int getTreeSize(Block block, ItemStack axe){
        Material material = block.getType();
        for(Tree tree : trees){
            if(tree.trunk.contains(material)){
                if(tree.worlds!=null){
                    boolean isInWorld = false;
                    for(String world : tree.worlds){
                        if(world.equalsIgnoreCase(block.getWorld().getName())||world.equalsIgnoreCase(block.getWorld().getUID().toString())){
                            isInWorld = true;
                            break;
                        }
                    }
                    if(!isInWorld)continue;
                }
                TOOL:for(Tool tool : tools){
                    if(tool.worlds!=null){
                        boolean isInWorld = false;
                        for(String world : tool.worlds){
                            if(world.equalsIgnoreCase(block.getWorld().getName())||world.equalsIgnoreCase(block.getWorld().getUID().toString())){
                                isInWorld = true;
                                break;
                            }
                        }
                        if(!isInWorld)continue;
                    }
                    if(!tool.allowedTrees.isEmpty()&&!tool.allowedTrees.contains(tree))continue;
                    if(axe.getType()==tool.material){
                        if(tool.name!=null&&axe.hasItemMeta()&&!axe.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', tool.name))){
                            continue;
                        }
                        ArrayList<String> pendingLores = new ArrayList<>(tool.requiredLore);
                        if(axe.hasItemMeta()&&axe.getItemMeta().hasLore()){
                            for(String loreStr : axe.getItemMeta().getLore()){
                                for(Iterator<String> it = pendingLores.iterator(); it.hasNext();){
                                    String lore = it.next();
                                    if(loreStr.contains(lore))it.remove();
                                }
                            }
                        }
                        if(!pendingLores.isEmpty())continue;
                        for(Enchantment enchant : tool.requiredEnchantments.keySet()){
                            if(!axe.containsEnchantment(enchant)||axe.getEnchantmentLevel(enchant)<tool.requiredEnchantments.get(enchant)){
                                continue TOOL;
                            }
                        }
                        for(Enchantment enchant : tool.bannedEnchantments.keySet()){
                            if(axe.containsEnchantment(enchant)&&axe.getEnchantmentLevel(enchant)>=tool.bannedEnchantments.get(enchant)){
                                continue TOOL;
                            }
                        }
                        int durability = axe.getType().getMaxDurability()-axe.getDurability();
                        double durabilityPercent = durability/(double)axe.getType().getMaxDurability();
                        if(durability>tool.maxDurability)continue;
                        if(durability<tool.minDurability)continue;
                        if(durabilityPercent>tool.maxDurabilityPercent)continue;
                        if(durabilityPercent<tool.minDurabilityPercent)continue;
                        //do calculations and stuff here
                        HashMap<Integer, ArrayList<Block>> blocks = getBlocks(tree.trunk, block, scanDistance, true, false);
                        int total = getTotal(blocks);
                        int minY = block.getY();
                        for(int i : blocks.keySet()){
                            for(Block b : blocks.get(i)){
                                minY = Math.min(minY, b.getY());
                            }
                        }
                        if(total<Math.max(tool.requiredLogs,tree.requiredLogs))return 0;
                        if(block.getY()-minY>tool.maxHeight-1)return 0;
                        if(block.getY()-minY>tree.maxHeight-1)return 0;
                        if(total>tool.maxLogs)return 0;
                        if(total>tree.maxLogs)return 0;
                        ArrayList<Integer> distances = new ArrayList<>(blocks.keySet());
                        Collections.sort(distances);
                        int leaves = 0;
                        HashMap<Integer, ArrayList<Block>> allLeaves = new HashMap<>();
                        FOR:for(int i : distances){
                            for(Block b : blocks.get(i)){
                                HashMap<Integer, ArrayList<Block>> someLeaves = getBlocks(tree.leaves, b, tool.leafRange, diagonalLeaves, tool.playerLeaves&&tree.playerLeaves);
                                leaves+=toList(someLeaves).size();
                                for(int in : someLeaves.keySet()){
                                    if(allLeaves.containsKey(in)){
                                        allLeaves.get(in).addAll(someLeaves.get(in));
                                    }else{
                                        allLeaves.put(in, someLeaves.get(in));
                                    }
                                }
                            }
                        }
                        if(leaves<Math.max(tool.requiredLeaves, tree.requiredLeaves))return 0;
                        return total;
                    }
                }
            }
        }
        return 0;
    }
    private HashMap<Integer, ArrayList<Block>> getBlocks(ArrayList<Material> materialTypes, Block startingBlock, int maxDistance, boolean diagonal, boolean playerLeaves){
        //layer zero
        HashMap<Integer, ArrayList<Block>> results = new HashMap<>();
        ArrayList<Block> zero = new ArrayList<>();
        if(materialTypes.contains(startingBlock.getType())){
            zero.add(startingBlock);
        }
        results.put(0, zero);
        //all the other layers
        for(int i = 0; i<maxDistance; i++){
            ArrayList<Block> layer = new ArrayList<>();
            ArrayList<Block> lastLayer = new ArrayList<>(results.get(i));
            if(i==0&&lastLayer.isEmpty()){
                lastLayer.add(startingBlock);
            }
            for(Block block : lastLayer){
                if(diagonal){
                    for(int x = -1; x<=1; x++){
                        for(int y = -1; y<=1; y++){
                            for(int z = -1; z<=1; z++){
                                if(x==0&&y==0&&z==0)continue;//same block
                                Block newBlock = block.getRelative(x,y,z);
                                if(!materialTypes.contains(newBlock.getType())){
                                    continue;
                                }
                                if(lastLayer.contains(newBlock))continue;//if the new block is on the same layer, ignore
                                if(i>0&&results.get(i-1).contains(newBlock))continue;//if the new block is on the previous layer, ignore
                                if(layer.contains(newBlock))continue;//if the new block is on the next layer, but already processed, ignore
                                if(newBlock.getBlockData() instanceof Leaves){
                                    Leaves newLeaf = (Leaves)newBlock.getBlockData();
                                    if(!playerLeaves&&newLeaf.isPersistent())continue;
                                    if(block.getBlockData() instanceof Leaves&&!ignoreLeafData){
                                        Leaves oldLeaf = (Leaves)block.getBlockData();
                                        if(newLeaf.getDistance()<=oldLeaf.getDistance()){
                                            continue;
                                        }
                                    }
                                }
                                layer.add(newBlock);
                            }
                        }
                    }
                }else{
                    for(int j = 0; j<6; j++){
                        int x=0,y=0,z=0;
                        switch(j){
                            case 0:
                                x = -1;
                                break;
                            case 1:
                                x = 1;
                                break;
                            case 2:
                                y = -1;
                                break;
                            case 3:
                                y = 1;
                                break;
                            case 4:
                                z = -1;
                                break;
                            case 5:
                                z = 1;
                                break;
                            default:
                                throw new IllegalArgumentException("How did this happen?");
                        }
                        Block newBlock = block.getRelative(x,y,z);
                        if(!materialTypes.contains(newBlock.getType())){
                            continue;
                        }
                        if(lastLayer.contains(newBlock))continue;//if the new block is on the same layer, ignore
                        if(i>0&&results.get(i-1).contains(newBlock))continue;//if the new block is on the previous layer, ignore
                        if(layer.contains(newBlock))continue;//if the new block is on the next layer, but already processed, ignore
                        if(newBlock.getState().getBlockData() instanceof Leaves){
                            Leaves newLeaf = (Leaves)newBlock.getBlockData();
                            if(!playerLeaves&&newLeaf.isPersistent())continue;
                            if(block.getBlockData() instanceof Leaves&&!ignoreLeafData){
                                Leaves oldLeaf = (Leaves)block.getBlockData();
                                if(newLeaf.getDistance()<=oldLeaf.getDistance()){
                                    continue;
                                }
                            }
                        }
                        layer.add(newBlock);
                    }
                }
            }
            if(layer.isEmpty())break;
            results.put(i+1, layer);
        }
        return results;
    }
    private int getTotal(HashMap<Integer, ArrayList<Block>> blocks){
        int total = 0;
        for(int i : blocks.keySet()){
            total+=blocks.get(i).size();
        }
        return total;
    }
    private ArrayList<Block> toList(HashMap<Integer, ArrayList<Block>> blocks){
        ArrayList<Block> list = new ArrayList<>();
        for(int i : blocks.keySet()){
            list.addAll(blocks.get(i));
        }
        return list;
    }
    public ArrayList<Tool> tools = new ArrayList<>();
    public ArrayList<Tree> trees = new ArrayList<>();
    public void onEnable(){
        PluginDescriptionFile pdfFile = getDescription();
        Logger logger = getLogger();
        //<editor-fold defaultstate="collapsed" desc="Register Events">
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new BlockBreak(this), this);
//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Register Config">
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
//</editor-fold>
        getCommand("treefeller").setExecutor(new CommandTreeFeller(this));
        logger.log(Level.INFO, "{0} has been enabled! (Version {1}) by ThizThizzyDizzy", new Object[]{pdfFile.getName(), pdfFile.getVersion()});
        reload();
    }
    public void onDisable(){
        PluginDescriptionFile pdfFile = getDescription();
        Logger logger = getLogger();
        logger.log(Level.INFO, "{0} has been disabled! (Version {1}) by ThizThizzyDizzy", new Object[]{pdfFile.getName(), pdfFile.getVersion()});
    }
    private Enchantment getEnchantment(String string){
        switch(string.toLowerCase().replaceAll("_", " ")){
            case "power":
            case "arrow damage":
                return Enchantment.ARROW_DAMAGE;
            case "flame":
            case "arrow fire":
                return Enchantment.ARROW_FIRE;
            case "arrow infinite":
            case "infinity":
                return Enchantment.ARROW_INFINITE;
            case "arrow knockback":
            case "punch":
                return Enchantment.ARROW_KNOCKBACK;
            case "binding":
            case "binding curse":
            case "curse of binding":
                return Enchantment.BINDING_CURSE;
            case "channeling":
                return Enchantment.CHANNELING;
            case "sharpness":
            case "damage all":
                return Enchantment.DAMAGE_ALL;
            case "damage arthropods":
            case "bane of arthropods":
                return Enchantment.DAMAGE_ARTHROPODS;
            case "damage undead":
            case "smite":
                return Enchantment.DAMAGE_UNDEAD;
            case "depth strider":
                return Enchantment.DEPTH_STRIDER;
            case "efficiency":
            case "dig speed":
                return Enchantment.DIG_SPEED;
            case "durability":
            case "unbreaking":
                return Enchantment.DURABILITY;
            case "fire aspect":
                return Enchantment.FIRE_ASPECT;
            case "frost walker":
                return Enchantment.FROST_WALKER;
            case "impaling":
                return Enchantment.IMPALING;
            case "knockback":
                return Enchantment.KNOCKBACK;
            case "fortune":
            case "loot bonus blocks":
                return Enchantment.LOOT_BONUS_BLOCKS;
            case "looting":
            case "loot bonus mobs":
                return Enchantment.LOOT_BONUS_MOBS;
            case "loyalty":
                return Enchantment.LOYALTY;
            case "luck":
            case "luck of the sea":
                return Enchantment.LUCK;
            case "lure":
                return Enchantment.LURE;
            case "mending":
                return Enchantment.MENDING;
            case "oxygen":
            case "respiration":
                return Enchantment.OXYGEN;
            case "protection environmental":
            case "protection":
                return Enchantment.PROTECTION_ENVIRONMENTAL;
            case "protection explosions":
            case "blast protection":
                return Enchantment.PROTECTION_EXPLOSIONS;
            case "protection fall":
            case "feather falling":
            case "feather fall":
                return Enchantment.PROTECTION_FALL;
            case "protection fire":
            case "fire protection":
                return Enchantment.PROTECTION_FIRE;
            case "protection projectile":
            case "projectile protection":
                return Enchantment.PROTECTION_PROJECTILE;
            case "riptide":
                return Enchantment.RIPTIDE;
            case "silk touch":
                return Enchantment.SILK_TOUCH;
            case "sweeping":
            case "sweeping edge":
                return Enchantment.SWEEPING_EDGE;
            case "thorns":
                return Enchantment.THORNS;
            case "vanishing":
            case "vanishing curse":
            case "curse of vanishing":
                return Enchantment.VANISHING_CURSE;
            case "water worker":
            case "aqua affinity":
                return Enchantment.WATER_WORKER;
            default:
                return null;
        }
    }
    public ArrayList<Sapling> saplings = new ArrayList<>();
    public void addSapling(Block b, Material sapling, boolean autofill){
        saplings.add(new Sapling(b, sapling, autofill, System.currentTimeMillis()));
    }
    private void setLastTime(Player player, Tree tree, long time){
        if(treeCooldowns.containsKey(player.getUniqueId())){
            treeCooldowns.get(player.getUniqueId()).put(tree, time);
        }else{
            HashMap<Tree, Long> map = new HashMap<>();
            map.put(tree, time);
            treeCooldowns.put(player.getUniqueId(), map);
        }
    }
    private void setLastTime(Player player, Tool axe, long time){
        if(itemCooldowns.containsKey(player.getUniqueId())){
            itemCooldowns.get(player.getUniqueId()).put(axe, time);
        }else{
            HashMap<Tool, Long> map = new HashMap<>();
            map.put(axe, time);
            itemCooldowns.put(player.getUniqueId(), map);
        }
    }
    private long getLastTime(Player player, Tree tree){
        if(treeCooldowns.containsKey(player.getUniqueId())){
            HashMap<Tree, Long> map = treeCooldowns.get(player.getUniqueId());
            if(map.containsKey(tree)){
                return map.get(tree);
            }
            return 0;
        }
        return 0;
    }
    private long getLastTime(Player player, Tool axe){
        if(itemCooldowns.containsKey(player.getUniqueId())){
            HashMap<Tool, Long> map = itemCooldowns.get(player.getUniqueId());
            if(map.containsKey(axe)){
                return map.get(axe);
            }
            return 0;
        }
        return 0;
    }
    public void reload(){
        Logger logger = getLogger();
        trees.clear();
        tools.clear();
        saplings.clear();
        fallingBlocks.clear();
        itemCooldowns.clear();
        treeCooldowns.clear();
        replantSaplings = getConfig().getBoolean("replant-saplings");
        respectUnbreaking = getConfig().getBoolean("respect-unbreaking");
        scanDistance = getConfig().getInt("scan-distance");
        cuttingAnimation = getConfig().getBoolean("cutting-animation");
        animDelay = getConfig().getInt("anim-delay");
        spawnSaplings = getConfig().getInt("spawn-saplings");
        Tool.DEFAULT = new Tool(null);
        Tool.DEFAULT.maxLogs = getConfig().getInt("max-logs");
        Tool.DEFAULT.damageMult = getConfig().getDouble("damage-mult");
        Tool.DEFAULT.maxDurabilityPercent = 1;
        Tool.DEFAULT.minDurabilityPercent = 0;
        Tool.DEFAULT.maxDurability = Integer.MAX_VALUE;
        Tool.DEFAULT.minDurability = 0;
        Tool.DEFAULT.respectUnbreaking = getConfig().getBoolean("respect-unbreaking");
        Tool.DEFAULT.leafRange = getConfig().getInt("leaf-range");
        Tool.DEFAULT.allowPartial = getConfig().getBoolean("allow-partial");
        Tool.DEFAULT.maxHeight = getConfig().getInt("max-height");
        Tool.DEFAULT.playerLeaves = getConfig().getBoolean("player-leaves");
        Tool.DEFAULT.requiredLogs = getConfig().getInt("required-logs");
        Tool.DEFAULT.requiredLeaves = getConfig().getInt("required-leaves");
        Tool.DEFAULT.enableAdventure = getConfig().getBoolean("enable-adventure");
        Tool.DEFAULT.enableSurvival = getConfig().getBoolean("enable-survival");
        Tool.DEFAULT.enableCreative = getConfig().getBoolean("enable-creative");
        Tool.DEFAULT.withoutSneak = getConfig().getBoolean("without-sneak");
        Tool.DEFAULT.withSneak = getConfig().getBoolean("with-sneak");
        Tool.DEFAULT.damageMult = getConfig().getDouble("damage-mult");
        Tool.DEFAULT.cooldown = getConfig().getInt("cooldown");
        Tool.DEFAULT.leafEnchantments = getConfig().getBoolean("leaf-enchantments");
        Tool.DEFAULT.maxDurability = Integer.MAX_VALUE;
        Tool.DEFAULT.maxDurabilityPercent = 1;
        Tool.DEFAULT.minDurability = 0;
        Tool.DEFAULT.minDurabilityPercent = 0;
        Tool.DEFAULT.maxLogs = getConfig().getInt("max-logs");
        Tool.DEFAULT.randomFallVelocity = 0;
        Tool.DEFAULT.directionalFallVelocity = 0;
        Tool.DEFAULT.worlds = null;
        Tool.DEFAULT.leafDropChance = getConfig().getDouble("leaf-drop-chance");
        Tool.DEFAULT.logDropChance = getConfig().getDouble("log-drop-chance");
        Tool.DEFAULT.leaveStump = getConfig().getBoolean("leave-stump");
        Tree.DEFAULT.allowPartial = true;
        Tree.DEFAULT.damageMult = 1;
        Tree.DEFAULT.maxHeight = Integer.MAX_VALUE;
        Tree.DEFAULT.maxLogs = Integer.MAX_VALUE;
        Tree.DEFAULT.playerLeaves = true;
        Tree.DEFAULT.requiredLeaves = 0;
        Tree.DEFAULT.requiredLogs = 0;
        Tree.DEFAULT.sapling = null;
        Tree.DEFAULT.maxSaplings = 1;
        Tree.DEFAULT.cooldown = 0;
        Tree.DEFAULT.logBehavior = FellBehavior.match(getConfig().getString("log-behavior"));
        Tree.DEFAULT.leafBehavior = FellBehavior.match(getConfig().getString("leaf-behavior"));
        Tree.DEFAULT.randomFallVelocity = getConfig().getDouble("random-fall-velocity");
        Tree.DEFAULT.directionalFallVelocity = getConfig().getDouble("directional-fall-velocity");
        Tree.DEFAULT.directionalFallBehavior = DirectionalFallBehavior.match(getConfig().getString("directional-fall-behavior"));
        Tree.DEFAULT.convertWoodToLog = Tool.DEFAULT.convertWoodToLog = getConfig().getBoolean("convert-wood-to-log");
        Tree.DEFAULT.leafDropChance = 1;
        Tree.DEFAULT.logDropChance = 1;
        Tree.DEFAULT.leaveStump = false;
        ArrayList<Material> grass = new ArrayList<>();
        grass.add(Material.DIRT);
        grass.add(Material.GRASS_BLOCK);
        grass.add(Material.PODZOL);
        Tree.DEFAULT.grasses = grass;
        startupLogs = getConfig().getBoolean("startup-logs");
        diagonalLeaves = getConfig().getBoolean("diagonal-leaves");
        //<editor-fold defaultstate="collapsed" desc="Trees">
        ArrayList<Object> trees = new ArrayList<>(getConfig().getList("trees"));
        for(Object o : trees){
            if(o instanceof ArrayList){
                ArrayList<Material> trunk = new ArrayList<>();
                ArrayList<Material> leaves = new ArrayList<>();
                if(((ArrayList) o).get(0) instanceof String){
                    Material t = Material.matchMaterial((String) ((ArrayList) o).get(0));
                    if(t!=null)trunk.add(t);
                }else{
                    for(Object obj : (ArrayList)((ArrayList) o).get(0)){
                        if(obj instanceof String){
                            Material t = Material.matchMaterial((String)obj);
                            if(t!=null)trunk.add(t);
                        }
                    }
                }
                if(((ArrayList) o).get(1) instanceof String){
                    Material l = Material.matchMaterial((String) ((ArrayList) o).get(1));
                    if(l!=null)leaves.add(l);
                }else{
                    for(Object obj : (ArrayList)((ArrayList) o).get(1)){
                        if(obj instanceof String){
                            Material l = Material.matchMaterial((String)obj);
                            if(l!=null)leaves.add(l);
                        }
                    }
                }
                if(trunk.isEmpty()||leaves.isEmpty()){
                    logger.log(Level.WARNING, "Cannot load tree: {0}", o);
                    continue;
                }
                Tree tree = new Tree(trunk, leaves);
                if(((ArrayList) o).size()>2){
                    LinkedHashMap map = (LinkedHashMap) ((ArrayList) o).get(2);
                    for(Object key : map.keySet()){
                        if(!(key instanceof String)){
                            logger.log(Level.WARNING, "invalid tree option: {0}", key);
                            continue;
                        }
                        switch(((String)key).toLowerCase()){
                            case "grass":
                                ArrayList<Material> grasses = new ArrayList<>();
                                if(map.get(key) instanceof String){
                                    grasses.add(Material.matchMaterial((String)map.get(key)));
                                }else{
                                    ArrayList theGrasses = (ArrayList) map.get(key);
                                    grasses.addAll(theGrasses);
                                }
                                tree.grasses = grasses;
                                break;
                            case "sapling":
                                tree.sapling = Material.matchMaterial((String) map.get(key));
                                break;
                            case "maxsaplings":
                                tree.maxSaplings = ((Number)map.get(key)).intValue();
                                break;
                            case "cooldown":
                                tree.cooldown = ((Number)map.get(key)).intValue();
                                break;
                            case "damagemult":
                                tree.damageMult = ((Number)map.get(key)).doubleValue();
                                break;
                            case "maxlogs":
                                tree.maxLogs = ((Number)map.get(key)).intValue();
                                break;
                            case "allowpartial":
                                tree.allowPartial = (boolean)map.get(key);
                                break;
                            case "maxheight":
                                tree.maxHeight = ((Number)map.get(key)).intValue();
                                break;
                            case "playerleaves":
                                tree.playerLeaves = (boolean)map.get(key);
                                break;
                            case "requiredlogs":
                                tree.requiredLogs = ((Number)map.get(key)).intValue();
                                break;
                            case "requiredleaves":
                                tree.requiredLeaves = ((Number)map.get(key)).intValue();
                                break;
                            case "logbehavior":
                                tree.logBehavior = FellBehavior.match((String)map.get(key));
                                break;
                            case "leafbehavior":
                                tree.leafBehavior = FellBehavior.match((String)map.get(key));
                                break;
                            case "randomFallVelocity":
                                tree.randomFallVelocity = ((Number)map.get(key)).doubleValue();
                                break;
                            case "directionalFallVelocity":
                                tree.directionalFallVelocity = ((Number)map.get(key)).doubleValue();
                                break;
                            case "directionalFallBehavior":
                                tree.directionalFallBehavior = DirectionalFallBehavior.match((String)map.get(key));
                                break;
                            case "worlds":
                                ArrayList<String> worlds = new ArrayList<>();
                                if(map.get(key) instanceof String){
                                    worlds.add((String)map.get(key));
                                }else{
                                    ArrayList theGrasses = (ArrayList) map.get(key);
                                    worlds.addAll(theGrasses);
                                }
                                tree.worlds = worlds;
                                break;
                            case "leafdropchance":
                                tree.leafDropChance = ((Number)map.get(key)).doubleValue();
                                break;
                            case "logdropchance":
                                tree.logDropChance = ((Number)map.get(key)).doubleValue();
                                break;
                            case "leavestump":
                                tree.leaveStump = (boolean)map.get(key);
                                break;
                            default:
                                logger.log(Level.WARNING, "Unknown tree setting: {0}", key);
                        }
                    }
                }
                if(startupLogs)tree.print(logger);
                this.trees.add(tree);
            }else if(o instanceof String){
                ArrayList<Material> trunk = new ArrayList<>();
                ArrayList<Material> leaves = new ArrayList<>();
                Material t = Material.matchMaterial((String) o);
                Material l = Material.matchMaterial(((String) o).replaceAll("STRIPPED_", "").replaceAll("LOG", "LEAVES").replaceAll("WOOD", "LEAVES"));
                if(t!=null)trunk.add(t);
                if(l!=null)leaves.add(l);
                if(trunk.isEmpty()||leaves.isEmpty()){
                    logger.log(Level.WARNING, "Cannot load tree: {0}", o);
                    continue;
                }
                Tree tree = new Tree(trunk, leaves);
                if(startupLogs)tree.print(logger);
                this.trees.add(tree);
            }else{
                logger.log(Level.WARNING, "Cannot load tree: {0}", o);
            }
        }
//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Tools">
        ArrayList<Object> tools = new ArrayList<>(getConfig().getList("tools"));
        for(Object o : tools){
            if(o instanceof LinkedHashMap){
                LinkedHashMap map = (LinkedHashMap) o;
                if(!map.containsKey("type")||!(map.get("type") instanceof String)){
                    logger.log(Level.WARNING, "Cannot find tool material! Skipping...");
                    continue;
                }
                String typ = (String) map.get("type");
                Material type = typ.trim().equals("*")?Material.AIR:Material.matchMaterial(typ.trim());
                if(type==null){
                    logger.log(Level.WARNING, "Unknown tool material: {0}! Skipping...", map.get("type"));
                    continue;
                }
                Tool tool = new Tool(type);
                for(Object ob : map.keySet()){
                    if(!(ob instanceof String)){
                        logger.log(Level.WARNING, "Unknown tool property: {0}; Skipping...", ob);
                        continue;
                    }
                    String key = (String) ob;
                    switch(key.toLowerCase()){
                        case "allowedtrees":
                            ArrayList indicies = (ArrayList) map.get(ob);
                            for(Object obj : indicies){
                                if(obj instanceof Integer){
                                    int i = (int) obj;
                                    if(i<0||i>=this.trees.size()){
                                        logger.log(Level.WARNING, "Invalid tree index: {0}! Valid indexes range from 0-{1}", new Object[]{i, this.trees.size()-1});
                                        continue;
                                    }
                                    tool.allowedTrees.add(this.trees.get(i));
                                }else{
                                    logger.log(Level.WARNING, "Unknown tree index: {0}", obj);
                                    continue;
                                }
                            }
                            break;
                        case "requiredname":
                            tool.name = (String) map.get(ob);
                            break;
                        case "requiredlore":
                            ArrayList strs = (ArrayList) map.get(ob);
                            for(Object obj : strs){
                                tool.requiredLore.add(obj.toString());
                            }
                            break;
                        case "requiredpermissions":
                            ArrayList perms = (ArrayList) map.get(ob);
                            for(Object obj : perms){
                                tool.requiredPermissions.add(obj.toString());
                            }
                            break;
                        case "requiredenchantments":
                            LinkedHashMap enchantments = (LinkedHashMap) map.get(ob);
                            for(Object obj : enchantments.keySet()){
                                if(!(obj instanceof String)){
                                    logger.log(Level.WARNING, "Unknown enchantment: {0}; Skipping...", obj);
                                    continue;
                                }
                                Enchantment e = getEnchantment((String)obj);
                                if(e==null){
                                    logger.log(Level.WARNING, "Unknown enchantment: {0}; Skipping...", obj);
                                }
                                tool.requiredEnchantments.put(e, (Integer)enchantments.get(obj));
                            }
                            break;
                        case "bannedenchantments":
                            enchantments = (LinkedHashMap) map.get(ob);
                            for(Object ench : enchantments.keySet()){
                                if(!(ench instanceof String)){
                                    logger.log(Level.WARNING, "Unknown enchantment: {0}; Skipping...", ench);
                                    continue;
                                }
                                Enchantment e = getEnchantment((String)ench);
                                if(e==null){
                                    logger.log(Level.WARNING, "Unknown enchantment: {0}; Skipping...", ench);
                                }
                                tool.bannedEnchantments.put(e, (Integer)enchantments.get(ench));
                            }
                            break;
                        case "mindurability":
                            tool.minDurability = ((Number)map.get(ob)).intValue();
                            break;
                        case "maxdurability":
                            tool.maxDurability = ((Number)map.get(ob)).intValue();
                            break;
                        case "mindurabilitypercent":
                            tool.minDurabilityPercent = ((Number)map.get(ob)).doubleValue();
                            break;
                        case "maxdurabilitypercent":
                            tool.maxDurabilityPercent = ((Number)map.get(ob)).doubleValue();
                            break;
                        case "damagemult":
                            tool.damageMult = ((Number)map.get(ob)).doubleValue();
                            break;
                        case "maxlogs":
                            tool.maxLogs = ((Number)map.get(ob)).intValue();
                            break;
                        case "respectunbreaking":
                            tool.respectUnbreaking = (boolean)map.get(ob);
                            break;
                        case "leafrange":
                            tool.leafRange = ((Number)map.get(ob)).intValue();
                            break;
                        case "allowpartial":
                            tool.allowPartial = (boolean)map.get(ob);
                            break;
                        case "maxheight":
                            tool.maxHeight = ((Number)map.get(ob)).intValue();
                            break;
                        case "cooldown":
                            tool.cooldown = ((Number)map.get(ob)).intValue();
                            break;
                        case "playerleaves":
                            tool.playerLeaves = (boolean)map.get(ob);
                            break;
                        case "requiredlogs":
                            tool.requiredLogs = ((Number)map.get(ob)).intValue();
                            break;
                        case "requiredleaves":
                            tool.requiredLeaves = ((Number)map.get(ob)).intValue();
                            break;
                        case "enableadventure":
                            tool.enableAdventure = (boolean)map.get(ob);
                            break;
                        case "enablesurvival":
                            tool.enableSurvival = (boolean)map.get(ob);
                            break;
                        case "enablecreative":
                            tool.enableCreative = (boolean)map.get(ob);
                            break;
                        case "withsneak":
                            tool.withSneak = (boolean)map.get(ob);
                            break;
                        case "withoutsneak":
                            tool.withoutSneak = (boolean)map.get(ob);
                            break;
                        case "leafenchantments":
                            tool.leafEnchantments = (boolean)map.get(ob);
                            break;
                        case "randomFallVelocity":
                            tool.randomFallVelocity = ((Number)map.get(key)).doubleValue();
                            break;
                        case "directionalFallVelocity":
                            tool.directionalFallVelocity = ((Number)map.get(key)).doubleValue();
                            break;
                        case "worlds":
                            ArrayList<String> worlds = new ArrayList<>();
                            if(map.get(key) instanceof String){
                                worlds.add((String)map.get(key));
                            }else{
                                ArrayList theGrasses = (ArrayList) map.get(key);
                                worlds.addAll(theGrasses);
                            }
                            tool.worlds = worlds;
                            break;
                        case "leafdropchance":
                            tool.leafDropChance = ((Number)map.get(key)).doubleValue();
                            break;
                        case "logdropchance":
                            tool.logDropChance = ((Number)map.get(key)).doubleValue();
                            break;
                        case "leavestump":
                            tool.leaveStump = (boolean)map.get(key);
                            break;
                    }
                }
                if(startupLogs)tool.print(logger);
                this.tools.add(tool);
            }else if(o instanceof String){
                Material m = Material.matchMaterial((String)o);
                if(m==null){
                    logger.log(Level.WARNING, "Unknown enchantment: {0}; Skipping...", o);
                }
                Tool tool = new Tool(m);
                if(startupLogs)tool.print(logger);
                this.tools.add(tool);
            }else{
                logger.log(Level.INFO, "Unknown tool declaration: {0} | {1}", new Object[]{o.getClass().getName(), o.toString()});
            }
        }
//</editor-fold>
    }
    public static class Tool{
        public static Tool DEFAULT;
        public final Material material;
        public int maxLogs;
        public double damageMult;
        public double maxDurabilityPercent;
        public double minDurabilityPercent;
        public int maxDurability;
        public int minDurability;
        public boolean respectUnbreaking;
        public int leafRange;
        public boolean allowPartial;
        public int maxHeight;
        public boolean playerLeaves;
        public int requiredLogs;
        public int requiredLeaves;
        public boolean enableAdventure;
        public boolean enableSurvival;
        public boolean enableCreative;
        public boolean withoutSneak;
        public boolean withSneak;
        public HashMap<Enchantment, Integer> requiredEnchantments = new HashMap<>();
        public HashMap<Enchantment, Integer> bannedEnchantments = new HashMap<>();
        public ArrayList<Tree> allowedTrees = new ArrayList<>();
        public ArrayList<String> requiredLore = new ArrayList<>();
        public String name = null;
        public ArrayList<String> requiredPermissions = new ArrayList<>();
        public int cooldown;
        public boolean leafEnchantments;
        public double randomFallVelocity;
        public double directionalFallVelocity;
        public ArrayList<String> worlds = null;
        public boolean convertWoodToLog;
        public double leafDropChance;
        public double logDropChance;
        public boolean leaveStump;
        public Tool(Material material){
            this.material = material;
            if(DEFAULT!=null){
                maxLogs = DEFAULT.maxLogs;
                damageMult = DEFAULT.damageMult;
                maxDurabilityPercent = DEFAULT.maxDurabilityPercent;
                minDurabilityPercent = DEFAULT.minDurabilityPercent;
                maxDurability = DEFAULT.maxDurability;
                minDurability = DEFAULT.minDurability;
                respectUnbreaking = DEFAULT.respectUnbreaking;
                leafRange = DEFAULT.leafRange;
                allowPartial = DEFAULT.allowPartial;
                maxHeight = DEFAULT.maxHeight;
                cooldown = DEFAULT.cooldown;
                playerLeaves = DEFAULT.playerLeaves;
                requiredLogs = DEFAULT.requiredLogs;
                requiredLeaves = DEFAULT.requiredLeaves;
                enableAdventure = DEFAULT.enableAdventure;
                enableSurvival = DEFAULT.enableSurvival;
                enableCreative = DEFAULT.enableCreative;
                withoutSneak = DEFAULT.withoutSneak;
                withSneak = DEFAULT.withSneak;
                allowedTrees = new ArrayList<>(DEFAULT.allowedTrees);
                requiredLore = new ArrayList<>(DEFAULT.requiredLore);
                name = DEFAULT.name;
                requiredPermissions = new ArrayList<>(DEFAULT.requiredPermissions);
                cooldown = DEFAULT.cooldown;
                leafEnchantments = DEFAULT.leafEnchantments;
                convertWoodToLog = DEFAULT.convertWoodToLog;
                leafDropChance = DEFAULT.leafDropChance;
                logDropChance = DEFAULT.logDropChance;
                leaveStump = DEFAULT.leaveStump;
            }
        }
        private void print(Logger logger){
            String requiredEnchants = "";//<editor-fold defaultstate="collapsed">
            for(Enchantment e : requiredEnchantments.keySet()){
                requiredEnchants+=e.toString()+": "+requiredEnchantments.get(e)+", ";
            }
            if(!requiredEnchants.isEmpty())requiredEnchants = requiredEnchants.substring(0, requiredEnchants.length()-2);
//</editor-fold>
            String bannedEnchants = "";//<editor-fold defaultstate="collapsed">
            for(Enchantment e : bannedEnchantments.keySet()){
                bannedEnchants+=e.toString()+": "+bannedEnchantments.get(e)+", ";
            }
            if(!bannedEnchants.isEmpty())bannedEnchants = bannedEnchants.substring(0, bannedEnchants.length()-2);
//</editor-fold>
            logger.log(Level.INFO, "Loaded tool: {0}", material);
            logger.log(Level.INFO, "- Required enchantments: {0}", requiredEnchants);
            logger.log(Level.INFO, "- Banned enchantments: {0}", bannedEnchants);
            logger.log(Level.INFO, "- Maximum logs: {0}", maxLogs);
            logger.log(Level.INFO, "- Damage Multiplier: {0}", damageMult);
            logger.log(Level.INFO, "- Minimum durability: {0}", minDurability);
            logger.log(Level.INFO, "- Maximum durability: {0}", maxDurability);
            logger.log(Level.INFO, "- Minimum durability (%): {0}", Math.round(minDurabilityPercent*100_00)/100d+"%");
            logger.log(Level.INFO, "- Maximum durability (%): {0}", Math.round(maxDurabilityPercent*100_00)/100d+"%");
            logger.log(Level.INFO, "- Respect unbreaking: {0}", respectUnbreaking);
            logger.log(Level.INFO, "- Leaf Range: {0}", leafRange);
            logger.log(Level.INFO, "- Allow parital: {0}", allowPartial);
            logger.log(Level.INFO, "- Max Height: {0}", maxHeight);
            logger.log(Level.INFO, "- Player leaves: {0}", playerLeaves);
            logger.log(Level.INFO, "- Required Logs: {0}", requiredLogs);
            logger.log(Level.INFO, "- Required Leaves: {0}", requiredLeaves);
            logger.log(Level.INFO, "- Adventure mode: {0}", enableAdventure);
            logger.log(Level.INFO, "- Survival mode: {0}", enableSurvival);
            logger.log(Level.INFO, "- Creative mode: {0}", enableCreative);
            logger.log(Level.INFO, "- Without sneak: {0}", withoutSneak);
            logger.log(Level.INFO, "- With sneak: {0}", withSneak);
            logger.log(Level.INFO, "- Allowed trees: {0}", allowedTrees.isEmpty()?"ALL":allowedTrees.size());
            String requiredLores = "";//<editor-fold defaultstate="collapsed">
            for(String str : requiredLore){
                requiredLores+=", "+str;
            }
            if(!requiredLores.isEmpty())requiredLores = requiredLores.substring(2);
//</editor-fold>
            String requiredPerms = "";//<editor-fold defaultstate="collapsed">
            for(String str : requiredPermissions){
                requiredPerms+=", "+str;
            }
            if(!requiredPerms.isEmpty())requiredPerms = requiredPerms.substring(2);
//</editor-fold>
            logger.log(Level.INFO, "- Required lore: {0}", requiredLores);
            if(name!=null)logger.log(Level.INFO, "- Required name: {0}", name);
            logger.log(Level.INFO, "- Required permissions: {0}", requiredPerms);
            logger.log(Level.INFO, "- Cooldown: {0}", cooldown);
            logger.log(Level.INFO, "- Leaf enchantments: {0}", leafEnchantments);
            logger.log(Level.INFO, "- Random Fall Velocity: {0}", randomFallVelocity);
            logger.log(Level.INFO, "- Directional Fall Velocity: {0}", directionalFallVelocity);
            String worldses = "";//<editor-fold defaultstate="collapsed">
            if(worlds==null){
                worldses = "ANY";
            }else{
                for(String w : worlds){
                    worldses+=w+", ";
                }
                if(!worldses.isEmpty())worldses = worldses.substring(0, worldses.length()-2);
            }
//</editor-fold>
            logger.log(Level.INFO, "- Worlds: {0}", worldses);
            logger.log(Level.INFO, "- Leaf drop chance: {0}", leafDropChance);
            logger.log(Level.INFO, "- Log drop chance: {0}", logDropChance);
            logger.log(Level.INFO, "- Leave stump: {0}", leaveStump);
        }
    }
    public static class Tree{
        public static final Tree DEFAULT = new Tree(null, null);
        public final ArrayList<Material> trunk;
        public final ArrayList<Material> leaves;
        public double damageMult;
        public int maxLogs;
        public boolean allowPartial;
        public int maxHeight;
        public boolean playerLeaves;
        public int requiredLogs;
        public int requiredLeaves;
        public Material sapling;
        public int maxSaplings;
        public int cooldown;
        public ArrayList<Material> grasses = new ArrayList<>();
        public FellBehavior logBehavior;
        public FellBehavior leafBehavior;
        public double randomFallVelocity;
        public double directionalFallVelocity;
        public DirectionalFallBehavior directionalFallBehavior;
        public ArrayList<String> worlds = null;
        public boolean convertWoodToLog;
        public double leafDropChance;
        public double logDropChance;
        public boolean leaveStump;
        public Tree(ArrayList<Material> trunk, ArrayList<Material> leaves){
            this.trunk = trunk;
            this.leaves = leaves;
            if(trunk!=null){//if not default
                damageMult = DEFAULT.damageMult;
                maxLogs = DEFAULT.maxLogs;
                allowPartial = DEFAULT.allowPartial;
                maxHeight = DEFAULT.maxHeight;
                playerLeaves = DEFAULT.playerLeaves;
                requiredLogs = DEFAULT.requiredLogs;
                requiredLeaves = DEFAULT.requiredLeaves;
                sapling = DEFAULT.sapling;
                maxSaplings = DEFAULT.maxSaplings;
                cooldown = DEFAULT.cooldown;
                if(grasses.isEmpty())grasses.addAll(DEFAULT.grasses);
                logBehavior = DEFAULT.logBehavior;
                leafBehavior = DEFAULT.leafBehavior;
                randomFallVelocity = DEFAULT.randomFallVelocity;
                directionalFallVelocity = DEFAULT.directionalFallVelocity;
                directionalFallBehavior = DEFAULT.directionalFallBehavior;
                convertWoodToLog = DEFAULT.convertWoodToLog;
                leafDropChance = DEFAULT.leafDropChance;
                logDropChance = DEFAULT.logDropChance;
                leaveStump = DEFAULT.leaveStump;
            }
        }
        private void print(Logger logger){
            String trunks = "";//<editor-fold defaultstate="collapsed">
            for(Material m : trunk){
                trunks+=m+", ";
            }
            if(!trunks.isEmpty())trunks = trunks.substring(0, trunks.length()-2);
//</editor-fold>
            String leaveses = "";//<editor-fold defaultstate="collapsed">
            for(Material m : leaves){
                leaveses+=m+", ";
            }
            if(!leaveses.isEmpty())leaveses = leaveses.substring(0, leaveses.length()-2);
//</editor-fold>
            logger.log(Level.INFO, "Loaded Tree!");
            logger.log(Level.INFO, "- Trunk: {0}", trunks);
            logger.log(Level.INFO, "- Leaves: {0}", leaveses);
            logger.log(Level.INFO, "- Maximum logs: {0}", maxLogs);
            logger.log(Level.INFO, "- Damage Multiplier: {0}", damageMult);
            logger.log(Level.INFO, "- Allow parital: {0}", allowPartial);
            logger.log(Level.INFO, "- Max Height: {0}", maxHeight);
            logger.log(Level.INFO, "- Player leaves: {0}", playerLeaves);
            logger.log(Level.INFO, "- Required Logs: {0}", requiredLogs);
            logger.log(Level.INFO, "- Required Leaves: {0}", requiredLeaves);
            logger.log(Level.INFO, "- Log Behavior: {0}", logBehavior.toString());
            logger.log(Level.INFO, "- Leaf Behavior: {0}", leafBehavior.toString());
            logger.log(Level.INFO, "- Random Fall Velocity: {0}", randomFallVelocity);
            logger.log(Level.INFO, "- Directional Fall Velocity: {0}", directionalFallVelocity);
            logger.log(Level.INFO, "- Directional Fall Behavior: {0}", directionalFallBehavior.toString());
            String worldses = "";//<editor-fold defaultstate="collapsed">
            if(worlds==null){
                worldses = "ANY";
            }else{
                for(String w : worlds){
                    worldses+=w+", ";
                }
                if(!worldses.isEmpty())worldses = worldses.substring(0, worldses.length()-2);
            }
//</editor-fold>
            logger.log(Level.INFO, "- Worlds: {0}", worldses);
            logger.log(Level.INFO, "- Leaf drop chance: {0}", leafDropChance);
            logger.log(Level.INFO, "- Log drop chance: {0}", logDropChance);
            logger.log(Level.INFO, "- Leave stump: {0}", leaveStump);
        }
    }
    public static class Sapling{
        private final Block block;
        private final Material material;
        public final boolean autofill;
        private final long time;
        private boolean placed = false;
        private static final float timeout = 2.5f;//seconds
        public Sapling(Block block, Material material, boolean autofill, long time){
            this.block = block;
            this.material = material;
            this.autofill = autofill;
            this.time = time;
        }
        public boolean isDead(){
            if(block.getType()==material)placed = true;
            return placed||System.currentTimeMillis()>time+timeout*1000;
        }
        public boolean canPlace(){
            if(isDead())return false;
            return block.getType()==Material.AIR;
        }
        public boolean place(){
            if(!canPlace())return false;
            placed = true;
            block.setType(material);
            return true;
        }
        public Material getMaterial(){
            return material;
        }
    }
    public Sapling getSapling(Block b){
        for(Iterator<Sapling> it = saplings.iterator(); it.hasNext();){
            Sapling sapling = it.next();
            if(sapling.isDead()){
                it.remove();
                continue;
            }
            if(sapling.block.equals(b))return sapling;
        }
        return null;
    }
    public static enum FellBehavior{
        BREAK,FALL,FALL_HURT,FALL_BREAK,FALL_HURT_BREAK;
        public static FellBehavior match(String s){
            return valueOf(s.toUpperCase().trim().replace("-", "_"));
        }
    }
    public static enum DirectionalFallBehavior{
        RANDOM,TOWARD,AWAY,LEFT,RIGHT,NORTH,SOUTH,EAST,WEST,NORTH_EAST,SOUTH_EAST,NORTH_WEST,SOUTH_WEST;
        public static DirectionalFallBehavior match(String s){
            return valueOf(s.toUpperCase().trim().replace("-", "_"));
        }
    }
    private void breakLog(Tree tree, Tool tool, ItemStack axe, Block log, Block origin, Player player, long seed){
        switch(tree.logBehavior){
            case BREAK:
                if(Bukkit.getServer().getPluginManager().getPlugin("McMMO")!=null){
                    try{
                        if(log!=origin)com.gmail.nossr50.api.ExperienceAPI.addXpFromBlock(log.getState(), com.gmail.nossr50.util.player.UserManager.getPlayer(player));
                    }catch(Exception ex){}
                }
                double chance = tree.logDropChance*tool.logDropChance;
                boolean drop = true;
                int bonus = 0;
                if(chance<=1){
                    drop = new Random().nextDouble()<chance;
                }else{
                    while(chance>1){
                        chance--;
                        bonus++;
                    }
                    if(new Random().nextDouble()<chance)bonus++;
                }
                if(tree.convertWoodToLog||tool.convertWoodToLog){
                    if(drop){
                        for(int i = 0; i<bonus+1; i++){
                            for(ItemStack s : log.getDrops(axe)){
                                if(s.getType().name().contains("_WOOD")){
                                    s.setType(Material.matchMaterial(s.getType().name().replace("_WOOD", "_LOG")));
                                }
                                log.getWorld().dropItemNaturally(log.getLocation(), s);
                            }
                        }
                    }
                    log.setType(Material.AIR);
                }else{
                    for(int i = 0; i<bonus; i++){
                        for(ItemStack s : log.getDrops(axe)){
                            log.getWorld().dropItemNaturally(log.getLocation(), s);
                        }
                    }
                    if(drop)log.breakNaturally(axe);
                    else log.setType(Material.AIR);
                }
                break;
            case FALL_HURT:
            case FALL:
            case FALL_BREAK:
            case FALL_HURT_BREAK:
                FallingBlock falling = log.getWorld().spawnFallingBlock(log.getLocation().add(.5,.5,.5), log.getBlockData());
                Vector v = falling.getVelocity();
                if(tree.directionalFallVelocity+tool.directionalFallVelocity>0){
                    Vector directionalVel = new Vector(0, 0, 0);
                    switch(tree.directionalFallBehavior){
                        case RANDOM:
                            double angle = new Random(seed).nextDouble()*Math.PI*2;
                            directionalVel = new Vector(Math.cos(angle),0,Math.sin(angle));
                            break;
                        case TOWARD:
                            if(player!=null){
                                directionalVel = new Vector(player.getLocation().getX()-log.getLocation().getX(),player.getLocation().getY()-log.getLocation().getY(),player.getLocation().getZ()-log.getLocation().getZ());
                            }
                            break;
                        case AWAY:
                            if(player!=null){
                                directionalVel = new Vector(player.getLocation().getX()-log.getLocation().getX(),player.getLocation().getY()-log.getLocation().getY(),player.getLocation().getZ()-log.getLocation().getZ()).multiply(-1);
                            }
                            break;
                        case LEFT:
                            if(player!=null){
                                directionalVel = new Vector(-(player.getLocation().getZ()-log.getLocation().getZ()),player.getLocation().getY()-log.getLocation().getY(),player.getLocation().getX()-log.getLocation().getX());
                            }
                            break;
                        case RIGHT:
                            if(player!=null){
                                directionalVel = new Vector(-(player.getLocation().getZ()-log.getLocation().getZ()),player.getLocation().getY()-log.getLocation().getY(),player.getLocation().getX()-log.getLocation().getX()).multiply(-1);
                            }
                            break;
                        case NORTH:
                            directionalVel = new Vector(0, 0, -1);
                            break;
                        case SOUTH:
                            directionalVel = new Vector(0, 0, 1);
                            break;
                        case EAST:
                            directionalVel = new Vector(1, 0, 0);
                            break;
                        case WEST:
                            directionalVel = new Vector(-1, 0, 0);
                            break;
                        case NORTH_EAST:
                            directionalVel = new Vector(1, 0, -1);
                            break;
                        case SOUTH_EAST:
                            directionalVel = new Vector(1, 0, 1);
                            break;
                        case SOUTH_WEST:
                            directionalVel = new Vector(-1, 0, 1);
                            break;
                        case NORTH_WEST:
                            directionalVel = new Vector(-1, 0, -1);
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid fall behavior: "+tree.directionalFallBehavior);
                    }
                    directionalVel = new Vector(directionalVel.getX()/Math.abs(directionalVel.length()), directionalVel.getY()/Math.abs(directionalVel.length()), directionalVel.getZ()/Math.abs(directionalVel.length()));
                    directionalVel = directionalVel.multiply(tree.directionalFallVelocity+tool.directionalFallVelocity);
                    v.add(directionalVel);
                }
                v.add(new Vector((Math.random()-.5)*(tree.randomFallVelocity+tool.randomFallVelocity), (tree.randomFallVelocity+tool.randomFallVelocity)/5, (Math.random()-.5)*(tree.randomFallVelocity+tool.randomFallVelocity)));
                falling.setVelocity(v);
                falling.setHurtEntities(tree.logBehavior==FellBehavior.FALL_HURT||tree.logBehavior==FellBehavior.FALL_HURT_BREAK);
                if(tree.logBehavior==FellBehavior.FALL_BREAK||tree.logBehavior==FellBehavior.FALL_HURT_BREAK)falling.addScoreboardTag("TreeFeller_Break");
                if(tree.convertWoodToLog||tool.convertWoodToLog)falling.addScoreboardTag("TreeFeller_Convert");
                log.setType(Material.AIR);
                fallingBlocks.add(falling.getUniqueId());
                break;
            default:
                throw new IllegalArgumentException("Invalid log behavior: "+tree.logBehavior);
        }
    }
    private void breakLeaf(Tree tree, Tool tool, ItemStack axe, Block leaf, Block origin, Player player, long seed){
        switch(tree.leafBehavior){
            case BREAK:
                if(Bukkit.getServer().getPluginManager().getPlugin("McMMO")!=null){
                    try{
                        if(leaf!=origin)com.gmail.nossr50.api.ExperienceAPI.addXpFromBlock(leaf.getState(), com.gmail.nossr50.util.player.UserManager.getPlayer(player));
                    }catch(Exception ex){}
                }
                double chance = tree.leafDropChance*tool.leafDropChance;
                boolean drop = true;
                int bonus = 0;
                if(chance<=1){
                    drop = new Random().nextDouble()<chance;
                }else{
                    while(chance>1){
                        chance--;
                        bonus++;
                    }
                    if(new Random().nextDouble()<chance)bonus++;
                }
                if(tree.convertWoodToLog||tool.convertWoodToLog){
                    if(drop){
                        for(int i = 0; i<bonus+1; i++){
                            for(ItemStack s : tool.leafEnchantments?leaf.getDrops(axe):leaf.getDrops()){
                                if(s.getType().name().contains("_WOOD")){
                                    s.setType(Material.matchMaterial(s.getType().name().replace("_WOOD", "_LOG")));
                                }
                                leaf.getWorld().dropItemNaturally(leaf.getLocation(), s);
                            }
                        }
                    }
                    leaf.setType(Material.AIR);
                }else{
                    for(int i = 0; i<bonus; i++){
                        for(ItemStack s : tool.leafEnchantments?leaf.getDrops(axe):leaf.getDrops()){
                            leaf.getWorld().dropItemNaturally(leaf.getLocation(), s);
                        }
                    }
                    if(drop){
                        if(tool.leafEnchantments)leaf.breakNaturally(axe);
                        else leaf.breakNaturally();
                    }
                    else leaf.setType(Material.AIR);
                }
                break;
            case FALL_HURT:
            case FALL:
            case FALL_BREAK:
            case FALL_HURT_BREAK:
                FallingBlock falling = leaf.getWorld().spawnFallingBlock(leaf.getLocation().add(.5,.5,.5), leaf.getBlockData());
                Vector v = falling.getVelocity();
                if(tree.directionalFallVelocity+tool.directionalFallVelocity>0){
                    Vector directionalVel = new Vector(0, 0, 0);
                    switch(tree.directionalFallBehavior){
                        case RANDOM:
                            double angle = new Random(seed).nextDouble()*Math.PI*2;
                            directionalVel = new Vector(Math.cos(angle),0,Math.sin(angle));
                            break;
                        case TOWARD:
                            if(player!=null){
                                directionalVel = new Vector(player.getLocation().getX()-leaf.getLocation().getX(),player.getLocation().getY()-leaf.getLocation().getY(),player.getLocation().getZ()-leaf.getLocation().getZ());
                            }
                            break;
                        case AWAY:
                            if(player!=null){
                                directionalVel = new Vector(player.getLocation().getX()-leaf.getLocation().getX(),player.getLocation().getY()-leaf.getLocation().getY(),player.getLocation().getZ()-leaf.getLocation().getZ()).multiply(-1);
                            }
                            break;
                        case LEFT:
                            if(player!=null){
                                directionalVel = new Vector(-(player.getLocation().getZ()-leaf.getLocation().getZ()),player.getLocation().getY()-leaf.getLocation().getY(),player.getLocation().getX()-leaf.getLocation().getX());
                            }
                            break;
                        case RIGHT:
                            if(player!=null){
                                directionalVel = new Vector(-(player.getLocation().getZ()-leaf.getLocation().getZ()),player.getLocation().getY()-leaf.getLocation().getY(),player.getLocation().getX()-leaf.getLocation().getX()).multiply(-1);
                            }
                            break;
                        case NORTH:
                            directionalVel = new Vector(0, 0, -1);
                            break;
                        case SOUTH:
                            directionalVel = new Vector(0, 0, 1);
                            break;
                        case EAST:
                            directionalVel = new Vector(1, 0, 0);
                            break;
                        case WEST:
                            directionalVel = new Vector(-1, 0, 0);
                            break;
                        case NORTH_EAST:
                            directionalVel = new Vector(1, 0, -1);
                            break;
                        case SOUTH_EAST:
                            directionalVel = new Vector(1, 0, 1);
                            break;
                        case SOUTH_WEST:
                            directionalVel = new Vector(-1, 0, 1);
                            break;
                        case NORTH_WEST:
                            directionalVel = new Vector(-1, 0, -1);
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid fall behavior: "+tree.directionalFallBehavior);
                    }
                    directionalVel = new Vector(directionalVel.getX()/Math.abs(directionalVel.length()), directionalVel.getY()/Math.abs(directionalVel.length()), directionalVel.getZ()/Math.abs(directionalVel.length()));
                    directionalVel = directionalVel.multiply(tree.directionalFallVelocity+tool.directionalFallVelocity);
                    v.add(directionalVel);
                }
                v.add(new Vector((Math.random()-.5)*(tree.randomFallVelocity+tool.randomFallVelocity), (tree.randomFallVelocity+tool.randomFallVelocity)/5, (Math.random()-.5)*(tree.randomFallVelocity+tool.randomFallVelocity)));
                falling.setVelocity(v);
                falling.setHurtEntities(tree.leafBehavior==FellBehavior.FALL_HURT||tree.leafBehavior==FellBehavior.FALL_HURT_BREAK);
                if(tree.leafBehavior==FellBehavior.FALL_BREAK||tree.leafBehavior==FellBehavior.FALL_HURT_BREAK)falling.addScoreboardTag("TreeFeller_Break");
                if(tree.convertWoodToLog||tool.convertWoodToLog)falling.addScoreboardTag("TreeFeller_Convert");
                leaf.setType(Material.AIR);
                fallingBlocks.add(falling.getUniqueId());
                break;
            default:
                throw new IllegalArgumentException("Invalid leaf behavior: "+tree.leafBehavior);
        }
    }
    private void debug(Player player, String text){
        if(!debug)return;
        if(!text.contains("[TreeFeller]"))text = "[TreeFeller] - "+text;
        getLogger().log(Level.FINEST, text);
        if(player!=null)player.sendMessage(text);
    }
    private void debug(Player player, boolean critical, boolean success, String text){
        if(!debug)return;
        if(success)debug(player, "[TreeFeller] "+(critical?ChatColor.DARK_GREEN:ChatColor.GREEN)+"O"+ChatColor.RESET+" "+text);
        else debug(player, "[TreeFeller] "+(critical?ChatColor.DARK_RED:ChatColor.RED)+"X"+ChatColor.RESET+" "+text);
    }
}