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
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
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
                    long dayTime = block.getWorld().getTime();
                    long gameTime = block.getWorld().getFullTime();
                    long day = gameTime/24000;
                    long phase = day%8;
                    if(tool.minTime!=-1&&tool.maxTime!=-1&&tool.maxTime<tool.minTime){
                        if(dayTime>tool.maxTime&&dayTime<tool.minTime){
                            debug(player, false, false, "Day time does not fit into tool's allowed range: "+tool.minTime+" - "+tool.maxTime+" (currently "+dayTime+")");
                            continue;
                        }
                    }else{
                        if(tool.minTime!=-1&&dayTime<tool.minTime){
                            debug(player, false, false, "Day time does not fit into tool's allowed range: "+tool.minTime+" - "+tool.maxTime+" (currently "+dayTime+")");
                            continue;
                        }
                        if(tool.maxTime!=-1&&dayTime>tool.maxTime){
                            debug(player, false, false, "Day time does not fit into tool's allowed range: "+tool.minTime+" - "+tool.maxTime+" (currently "+dayTime+")");
                            continue;
                        }
                    }
                    if(tool.minTime!=-1||tool.maxTime!=-1){
                        debug(player, false, true, "Day time is valid for tool");
                    }
                    if(tool.minPhase!=-1&&tool.maxPhase!=-1&&tool.maxPhase<tool.minPhase){
                        if(phase>tool.maxPhase&&phase<tool.minPhase){
                            debug(player, false, false, "Moon phase does not fit into tool's allowed range: "+tool.minPhase+" - "+tool.maxPhase+" (currently "+phase+")");
                            continue;
                        }
                    }else{
                        if(tool.minPhase!=-1&&phase<tool.minPhase){
                            debug(player, false, false, "Moon phase does not fit into tool's allowed range: "+tool.minPhase+" - "+tool.maxPhase+" (currently "+phase+")");
                            continue;
                        }
                        if(tool.maxPhase!=-1&&phase>tool.maxPhase){
                            debug(player, false, false, "Moon phase does not fit into tool's allowed range: "+tool.minPhase+" - "+tool.maxPhase+" (currently "+phase+")");
                            continue;
                        }
                    }
                    if(tool.minPhase!=-1||tool.maxPhase!=-1){
                        debug(player, false, true, "Moon phase is valid for tool");
                    }
                    debug(player, true, true, "Tool is valid! (Tool #"+tools.indexOf(tool)+") Beinning tree felling checks...");
                    if(tree.minTime!=-1&&tree.maxTime!=-1&&tree.maxTime<tree.minTime){
                        if(dayTime>tree.maxTime&&dayTime<tree.minTime){
                            debug(player, false, false, "Day time does not fit into tree's allowed range: "+tree.minTime+" - "+tree.maxTime+" (currently "+dayTime+")");
                            continue;
                        }
                    }else{
                        if(tree.minTime!=-1&&dayTime<tree.minTime){
                            debug(player, false, false, "Day time does not fit into tree's allowed range: "+tree.minTime+" - "+tree.maxTime+" (currently "+dayTime+")");
                            continue;
                        }
                        if(tree.maxTime!=-1&&dayTime>tree.maxTime){
                            debug(player, false, false, "Day time does not fit into tree's allowed range: "+tree.minTime+" - "+tree.maxTime+" (currently "+dayTime+")");
                            continue;
                        }
                    }
                    if(tree.minTime!=-1||tree.maxTime!=-1){
                        debug(player, false, true, "Day time is valid for tree");
                    }
                    if(tree.minPhase!=-1&&tree.maxPhase!=-1&&tree.maxPhase<tree.minPhase){
                        if(phase>tree.maxPhase&&phase<tree.minPhase){
                            debug(player, false, false, "Moon phase does not fit into tree's allowed range: "+tree.minPhase+" - "+tree.maxPhase+" (currently "+phase+")");
                            continue;
                        }
                    }else{
                        if(tree.minPhase!=-1&&phase<tree.minPhase){
                            debug(player, false, false, "Moon phase does not fit into tree's allowed range: "+tree.minPhase+" - "+tree.maxPhase+" (currently "+phase+")");
                            continue;
                        }
                        if(tree.maxPhase!=-1&&phase>tree.maxPhase){
                            debug(player, false, false, "Moon phase does not fit into tree's allowed range: "+tree.minPhase+" - "+tree.maxPhase+" (currently "+phase+")");
                            continue;
                        }
                    }
                    if(tree.minPhase!=-1||tree.maxPhase!=-1){
                        debug(player, false, true, "Moon phase is valid for tree");
                    }
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
                    if(tool.requireCrossSection){
                        for(int x = -1; x<=1; x++){
                            for(int z = -1; z<=1; z++){
                                if(x==0&&z==0)continue;
                                if(tree.trunk.contains(block.getRelative(x, 0, z).getType())){
                                    debug(player, true, false, "A full cross-section has not been cut for tool!");
                                    return null;
                                }
                            }
                        }
                    }
                    if(tree.requireCrossSection){
                        for(int x = -1; x<=1; x++){
                            for(int z = -1; z<=1; z++){
                                if(x==0&&z==0)continue;
                                if(tree.trunk.contains(block.getRelative(x, 0, z).getType())){
                                    debug(player, true, false, "A full cross-section has not been cut!");
                                    return null;
                                }
                            }
                        }
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
                    if(Bukkit.getServer().getPluginManager().getPlugin("GriefPrevention")!=null){
                        try{
                            for(Block b : toList(blocks)){
                                String s = me.ryanhamshire.GriefPrevention.GriefPrevention.instance.allowBreak(player, b, b.getLocation());
                                if(s!=null){
                                    debug(player, true, false, "This tree is protected by GriefPrevention at "+b.getX()+" "+b.getY()+" "+b.getZ());
                                    player.sendMessage(s);
                                    return null;
                                }
                            }
                            for(Block b : toList(allLeaves)){
                                String s = me.ryanhamshire.GriefPrevention.GriefPrevention.instance.allowBreak(player, b, b.getLocation());
                                if(s!=null){
                                    debug(player, true, false, "This tree is protected by GriefPrevention at "+b.getX()+" "+b.getY()+" "+b.getZ());
                                    player.sendMessage(s);
                                    return null;
                                }
                            }
                        }catch(Exception ex){}
                    }
                    if(Bukkit.getServer().getPluginManager().getPlugin("WorldGuard")!=null){
                        try{
                            Block b = WorldGuardCompat.test(player, toList(blocks), toList(allLeaves));
                            if(b!=null){
                                debug(player, true, false, "This tree is protected by WorldGuard at "+b.getX()+" "+b.getY()+" "+b.getZ());
                                return null;
                            }
                        }catch(Exception ex){}
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
                                                
                                                breakLeaf(tree, tool, axe, leaf, block, player, seed);
                                            }else leaf.setType(Material.AIR);
                                        }
                                        if(dropItems)breakLog(tree, tool, axe, b, block, player, seed);
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
                                            breakLeaf(tree, tool, axe, leaf, block, player, seed);
                                        }else{
                                            droppedItems.addAll(tool.leafEnchantments?leaf.getDrops(axe):leaf.getDrops());
                                            leaf.setType(Material.AIR);
                                        }
                                    }
                                if(dropItems)breakLog(tree, tool, axe, b, block, player, seed);
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
                    ArrayList<Effect> effects = new ArrayList<>();
                    effects.addAll(tree.effects);
                    effects.addAll(tool.effects);
                    for(Effect e : effects){
                        if(e.location==Effect.EffectLocation.TOOL){
                            if(new Random().nextDouble()<e.chance)e.play(block);
                        }
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
    public ArrayList<Effect> effects = new ArrayList<>();
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
        pm.addPermission(new Permission("treefeller.reload"));
        pm.addPermission(new Permission("treefeller.debug"));
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
    private Particle getParticle(String string){
        Particle p = null;
        try{
            p = Particle.valueOf(string.toUpperCase().replace(" ", "-").replace("-", "_"));
        }catch(IllegalArgumentException ex){}
        if(p!=null)return p;
        switch(string.toLowerCase().replaceAll("_", " ")){
            case "barrier":
                return Particle.BARRIER;
            case "block":
//                return Particle.BLOCK_CRACK;
                return Particle.BLOCK_DUST;
            case "enchanted hit":
                return Particle.CRIT_MAGIC;
            case "dripping lava":
                return Particle.DRIP_LAVA;
            case "dripping water":
                return Particle.DRIP_WATER;
            case "enchant":
                return Particle.ENCHANTMENT_TABLE;
            case "explosion emitter":
                return Particle.EXPLOSION_HUGE;
            case "explode":
                return Particle.EXPLOSION_LARGE;
            case "poof":
                return Particle.EXPLOSION_NORMAL;
            case "firework":
                return Particle.FIREWORKS_SPARK;
            case "item":
                return Particle.ITEM_CRACK;
            case "elder guardian":
                return Particle.MOB_APPEARANCE;
            case "dust":
                return Particle.REDSTONE;
            case "item slime":
                return Particle.SLIME;
            case "large smoke":
                return Particle.SMOKE_LARGE;
            case "smoke":
                return Particle.SMOKE_NORMAL;
            case "item snowball":
                return Particle.SNOWBALL;
            case "effect":
                return Particle.SPELL;
            case "instant effect":
                return Particle.SPELL_INSTANT;
            case "entity effect":
                return Particle.SPELL_MOB;
            case "mob spell ambient":
            case "ambient entity effect":
                return Particle.SPELL_MOB_AMBIENT;
            case "witch":
                return Particle.SPELL_WITCH;
            case "underwater":
                return Particle.SUSPENDED;
            case "totem of undying":
                return Particle.TOTEM;
            case "mycelium":
                return Particle.TOWN_AURA;
            case "angry villager":
                return Particle.VILLAGER_ANGRY;
            case "happy villager":
                return Particle.VILLAGER_HAPPY;
            case "bubble":
                return Particle.WATER_BUBBLE;
            case "rain":
                return Particle.WATER_DROP;
            case "splash":
                return Particle.WATER_SPLASH;
            case "fishing":
                return Particle.WATER_WAKE;
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
        effects.clear();
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
        Tool.DEFAULT.requireCrossSection = getConfig().getBoolean("require-cross-section");
        Tool.DEFAULT.rotateLogs = getConfig().getBoolean("rotate-logs");
        Tool.DEFAULT.minTime = getConfig().getInt("min-time");
        Tool.DEFAULT.maxTime = getConfig().getInt("max-time");
        Tool.DEFAULT.minPhase = getConfig().getInt("min-phase");
        Tool.DEFAULT.maxPhase = getConfig().getInt("max-phase");
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
        Tree.DEFAULT.requireCrossSection = false;
        Tree.DEFAULT.rotateLogs = false;
        Tree.DEFAULT.minTime = Tree.DEFAULT.maxTime = Tree.DEFAULT.minPhase = Tree.DEFAULT.maxPhase = -1;
        ArrayList<Material> grass = new ArrayList<>();
        grass.add(Material.DIRT);
        grass.add(Material.GRASS_BLOCK);
        grass.add(Material.PODZOL);
        Tree.DEFAULT.grasses = grass;
        startupLogs = getConfig().getBoolean("startup-logs");
        diagonalLeaves = getConfig().getBoolean("diagonal-leaves");
        ArrayList<Object> effects = null;
        //<editor-fold defaultstate="collapsed" desc="Effects">
        try{
            effects = new ArrayList<>(getConfig().getList("effects"));
        }catch(NullPointerException ex){
            logger.log(Level.WARNING, "Failed to load effects!");
        }
        if(effects!=null){
            for(Object o : effects){
                if(o instanceof LinkedHashMap){
                    LinkedHashMap map = (LinkedHashMap) o;
                    if(!map.containsKey("name")||!(map.get("name") instanceof String)){
                        logger.log(Level.WARNING, "Cannot find effect name! Skipping...");
                        continue;
                    }
                    String name = (String)map.get("name");
                    String typ = (String) map.get("type");
                    Effect.EffectType type = Effect.EffectType.valueOf(typ.toUpperCase().trim());
                    if(type==null){
                        logger.log(Level.WARNING, "Invalid effect type: {0}! Skipping...", typ);
                        continue;
                    }
                    String loc = (String) map.get("location");
                    Effect.EffectLocation location = Effect.EffectLocation.valueOf(loc.toUpperCase().trim());
                    if(location==null){
                        logger.log(Level.WARNING, "Invalid effect location: {0}! Skipping...", loc);
                        continue;
                    }
                    double chance = 1;
                    if(map.containsKey("chance")){
                        chance = ((Number)map.get("chance")).doubleValue();
                    }
                    Effect effect;
                    switch(type){
                        case PARTICLE:
                            Particle particle = getParticle((String) map.get("particle"));
                            double x = 0;
                            if(map.containsKey("x")){
                                x = ((Number)map.get("x")).doubleValue();
                            }
                            double y = 0;
                            if(map.containsKey("y")){
                                y = ((Number)map.get("y")).doubleValue();
                            }
                            double z = 0;
                            if(map.containsKey("z")){
                                z = ((Number)map.get("z")).doubleValue();
                            }
                            double dx = 0;
                            if(map.containsKey("dx")){
                                dx = ((Number)map.get("dx")).doubleValue();
                            }
                            double dy = 0;
                            if(map.containsKey("dy")){
                                dy = ((Number)map.get("dy")).doubleValue();
                            }
                            double dz = 0;
                            if(map.containsKey("dz")){
                                dz = ((Number)map.get("dz")).doubleValue();
                            }
                            double speed = 0;
                            if(map.containsKey("speed")){
                                speed = ((Number)map.get("speed")).doubleValue();
                            }
                            int count = 1;
                            if(map.containsKey("count")){
                                count = ((Number)map.get("count")).intValue();
                            }
                            Object extra = null;
                            switch(particle){
                                case REDSTONE:
                                    extra = new Particle.DustOptions(Color.fromRGB(((Number)map.get("r")).intValue(), ((Number)map.get("g")).intValue(), ((Number)map.get("b")).intValue()), ((Number)map.get("size")).floatValue());
                                    break;
                                case ITEM_CRACK:
                                    extra = new ItemStack(Material.matchMaterial((String)map.get("item")));
                                    break;
                                case BLOCK_CRACK:
                                case BLOCK_DUST:
                                case FALLING_DUST:
                                    extra = Bukkit.createBlockData(Material.matchMaterial((String)map.get("block")));
                                    break;
                            }
                            effect = new Effect(name, location, chance, particle, x, y, z, dx, dy, dz, speed, count, extra);
                            break;
                        case SOUND:
                            String sound = (String)map.get("sound");
                            float volume = 1;
                            if(map.containsKey("volume")){
                                volume = ((Number)map.get("volume")).floatValue();
                            }
                            float pitch = 1;
                            if(map.containsKey("pitch")){
                                pitch = ((Number)map.get("pitch")).floatValue();
                            }
                            effect = new Effect(name, location, chance, sound, volume, pitch);
                            break;
                        case EXPLOSION:
                            float power = ((Number)map.get("power")).floatValue();
                            boolean fire = false;
                            if(map.containsKey("fire")){
                                fire = (boolean)map.get("fire");
                            }
                            effect = new Effect(name, location, chance, power, fire);
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown effect typpe: "+type+"!");
                    }
                    if(startupLogs)effect.print(logger);
                    this.effects.add(effect);
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
        }
//</editor-fold>
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
                            case "requirecrosssection":
                                tree.requireCrossSection = (boolean)map.get(key);
                                break;
                            case "rotatelogs":
                                tree.rotateLogs = (boolean)map.get(key);
                                break;
                            case "mintime":
                                tree.minTime = ((Number)map.get(key)).intValue();
                                break;
                            case "maxtime":
                                tree.maxTime = ((Number)map.get(key)).intValue();
                                break;
                            case "minphase":
                                tree.minPhase = ((Number)map.get(key)).intValue();
                                break;
                            case "maxphase":
                                tree.maxPhase = ((Number)map.get(key)).intValue();
                                break;
                            case "effects":
                                ArrayList<Effect> effectses = new ArrayList<>();
                                if(map.get(key) instanceof String){
                                    effectses.add(getEffect((String)map.get(key)));
                                }else{
                                    ArrayList theEffects = (ArrayList) map.get(key);
                                    for(Object ob : theEffects){
                                        effectses.add(getEffect((String)ob));
                                    }
                                }
                                tree.effects = effectses;
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
                        case "requirecrosssection":
                            tool.requireCrossSection = (boolean)map.get(key);
                            break;
                        case "rotatelogs":
                            tool.rotateLogs = (boolean)map.get(key);
                            break;
                        case "mintime":
                            tool.minTime = ((Number)map.get(key)).intValue();
                            break;
                        case "maxtime":
                            tool.maxTime = ((Number)map.get(key)).intValue();
                            break;
                        case "minphase":
                            tool.minPhase = ((Number)map.get(key)).intValue();
                            break;
                        case "maxphase":
                            tool.maxPhase = ((Number)map.get(key)).intValue();
                            break;
                        case "effects":
                            ArrayList<Effect> effectses = new ArrayList<>();
                            if(map.get(key) instanceof String){
                                effectses.add(getEffect((String)map.get(key)));
                            }else{
                                ArrayList theEffects = (ArrayList) map.get(key);
                                for(Object obj : theEffects){
                                    effectses.add(getEffect((String)obj));
                                }
                            }
                            tool.effects = effectses;
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
    private Effect getEffect(String string){
        for(Effect e : effects){
            if(e.name.equals(string))return e;
        }
        for(Effect e : effects){
            if(e.name.trim().equals(string))return e;
        }
        for(Effect e : effects){
            if(e.name.trim().equalsIgnoreCase(string))return e;
        }
        return null;
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
        public boolean requireCrossSection;
        public boolean rotateLogs;
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
        public int minTime, maxTime, minPhase, maxPhase;
        public ArrayList<Effect> effects = new ArrayList<>();
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
                requireCrossSection = DEFAULT.requireCrossSection;
                rotateLogs = DEFAULT.rotateLogs;
                minTime = DEFAULT.minTime;
                maxTime = DEFAULT.maxTime;
                minPhase = DEFAULT.minPhase;
                maxPhase = DEFAULT.maxPhase;
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
            logger.log(Level.INFO, "- Random fall velocity: {0}", randomFallVelocity);
            logger.log(Level.INFO, "- Directional fall velocity: {0}", directionalFallVelocity);
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
            logger.log(Level.INFO, "- Require cross section: {0}", requireCrossSection);
            logger.log(Level.INFO, "- Rotate logs: {0}", rotateLogs);
            logger.log(Level.INFO, "- Minimum time: {0}", minTime);
            logger.log(Level.INFO, "- Maximum time: {0}", maxTime);
            logger.log(Level.INFO, "- Minimum phase: {0}", minPhase);
            logger.log(Level.INFO, "- Maximum phase: {0}", maxPhase);
            String effects = "";//<editor-fold defaultstate="collapsed">
            for(Effect e : this.effects){
                effects+=e.name+", ";
            }
            if(!effects.isEmpty())effects = effects.substring(0, effects.length()-2);
//</editor-fold>
            logger.log(Level.INFO, "- Effects: {0}", effects);
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
        public boolean requireCrossSection;
        public boolean rotateLogs;
        public int minTime, maxTime, minPhase, maxPhase;
        public ArrayList<Effect> effects = new ArrayList<>();
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
                requireCrossSection = DEFAULT.requireCrossSection;
                rotateLogs = DEFAULT.rotateLogs;
                minTime = DEFAULT.minTime;
                maxTime = DEFAULT.maxTime;
                minPhase = DEFAULT.minPhase;
                maxPhase = DEFAULT.maxPhase;
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
            logger.log(Level.INFO, "- Require cross section: {0}", requireCrossSection);
            logger.log(Level.INFO, "- Rotate logs: {0}", rotateLogs);
            logger.log(Level.INFO, "- Minimum time: {0}", minTime);
            logger.log(Level.INFO, "- Maximum time: {0}", maxTime);
            logger.log(Level.INFO, "- Minimum phase: {0}", minPhase);
            logger.log(Level.INFO, "- Maximum phase: {0}", maxPhase);
            String effects = "";//<editor-fold defaultstate="collapsed">
            for(Effect e : this.effects){
                effects+=e.name+", ";
            }
            if(!effects.isEmpty())effects = effects.substring(0, effects.length()-2);
//</editor-fold>
            logger.log(Level.INFO, "- Effects: {0}", effects);
        }
    }
    public static class Effect{
        private final String name;
        private final EffectLocation location;
        private final EffectType type;
        private final double chance;
        private Particle particle;
        private double x;
        private double y;
        private double z;
        private double dx;
        private double dy;
        private double dz;
        private double speed;
        private int count;
        private Object extra;
        private String sound;
        private float volume;
        private float pitch;
        private float power;
        private boolean fire;
        private Effect(String name, EffectLocation location, EffectType type, double chance){
            this.name = name;
            this.location = location;
            this.type = type;
            this.chance = chance;
        }
        public Effect(String name, EffectLocation location, double chance, Particle particle, double x, double y, double z, double dx, double dy, double dz, double speed, int count, Object extra){
            this(name, location, EffectType.PARTICLE, chance);
            this.particle = particle;
            this.x = x;
            this.y = y;
            this.z = z;
            this.dx = dx;
            this.dy = dy;
            this.dz = dz;
            this.speed = speed;
            this.count = count;
            this.extra = extra;
        }
        public Effect(String name, EffectLocation location, double chance, String sound, float volume, float pitch){
            this(name, location, EffectType.SOUND, chance);
            this.sound = sound;
            this.volume = volume;
            this.pitch = pitch;
        }
        public Effect(String name, EffectLocation location, double chance, float power, boolean fire){
            this(name, location, EffectType.EXPLOSION, chance);
            this.power = power;
            this.fire = fire;
        }
        private void print(Logger logger){
            logger.log(Level.INFO, "Loaded effect: {0}", name);
            logger.log(Level.INFO, "- Location: {0}", location);
            logger.log(Level.INFO, "- Type: {0}", type);
            logger.log(Level.INFO, "- Chance: {0}", chance);
            switch(type){
                case PARTICLE:
                    logger.log(Level.INFO, "- Particle: {0}", particle);
                    logger.log(Level.INFO, "- x: {0}", x);
                    logger.log(Level.INFO, "- y: {0}", y);
                    logger.log(Level.INFO, "- z: {0}", z);
                    logger.log(Level.INFO, "- dx: {0}", dx);
                    logger.log(Level.INFO, "- dy: {0}", dy);
                    logger.log(Level.INFO, "- dz: {0}", dz);
                    logger.log(Level.INFO, "- Speed: {0}", speed);
                    logger.log(Level.INFO, "- Count: {0}", count);
                    logger.log(Level.INFO, "- Extra: {0}", extra);
                    break;
                case SOUND:
                    logger.log(Level.INFO, "- Sound: {0}", sound);
                    logger.log(Level.INFO, "- Volume: {0}", volume);
                    logger.log(Level.INFO, "- Pitch: {0}", pitch);
                    break;
                case EXPLOSION:
                    logger.log(Level.INFO, "- Power: {0}", power);
                    logger.log(Level.INFO, "- Fire: {0}", fire);
                    break;
            }
        }
        private void play(Block block){
            switch(type){
                case EXPLOSION:
                    block.getWorld().createExplosion(block.getLocation().add(0.5, 0.5, 0.5), power, fire);
                    break;
                case SOUND:
                    block.getWorld().playSound(block.getLocation().add(0.5,0.5,0.5), sound, SoundCategory.BLOCKS, volume, pitch);
                    break;
                case PARTICLE:
                    block.getWorld().spawnParticle(particle, block.getLocation().add(x+.5,y+.5,z+.5), count, dx, dy, dz, speed, extra);
                    break;
            }
        }
        public static enum EffectLocation{
            LOGS,LEAVES,TREE,TOOL;
        }
        public static enum EffectType{
            PARTICLE,SOUND,EXPLOSION;
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
        BREAK,FALL,FALL_HURT,FALL_BREAK,FALL_HURT_BREAK,INVENTORY,FALL_INVENTORY,FALL_HURT_INVENTORY,NATURAL;
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
        ArrayList<Effect> effects = new ArrayList<>();
        for(Effect e : tree.effects){
            if(e.location==Effect.EffectLocation.LOGS||e.location==Effect.EffectLocation.TREE)effects.add(e);
        }
        breakBlock(tree.logBehavior, tree.convertWoodToLog||tool.convertWoodToLog, tree.logDropChance*tool.logDropChance, tree.directionalFallVelocity+tool.directionalFallVelocity, tree.randomFallVelocity+tool.randomFallVelocity, tree.rotateLogs||tool.rotateLogs, tree.directionalFallBehavior, true, axe, log, origin, player, seed, effects);
    }
    private void breakLeaf(Tree tree, Tool tool, ItemStack axe, Block leaf, Block origin, Player player, long seed){
        ArrayList<Effect> effects = new ArrayList<>();
        for(Effect e : tree.effects){
            if(e.location==Effect.EffectLocation.LEAVES||e.location==Effect.EffectLocation.TREE)effects.add(e);
        }
        breakBlock(tree.leafBehavior, tree.convertWoodToLog||tool.convertWoodToLog, tree.leafDropChance*tool.leafDropChance, tree.directionalFallVelocity+tool.directionalFallVelocity, tree.randomFallVelocity+tool.randomFallVelocity, tree.rotateLogs||tool.rotateLogs, tree.directionalFallBehavior, tool.leafEnchantments, axe, leaf, origin, player, seed, effects);
    }
    private void breakBlock(FellBehavior behavior, boolean convert, double dropChance, double directionalFallVelocity, double randomFallVelocity, boolean rotate, DirectionalFallBehavior directionalFallBehavior, boolean applyEnchantments, ItemStack axe, Block block, Block origin, Player player, long seed, Iterable<Effect> effects){
        switch(behavior){
            case INVENTORY:
                if(player!=null){
                    do{
                        if(Bukkit.getServer().getPluginManager().getPlugin("McMMO")!=null){
                            try{
                                if(block!=origin)com.gmail.nossr50.api.ExperienceAPI.addXpFromBlock(block.getState(), com.gmail.nossr50.util.player.UserManager.getPlayer(player));
                            }catch(Exception ex){}
                        }
                        boolean drop = true;
                        int bonus = 0;
                        if(dropChance<=1){
                            drop = new Random().nextDouble()<dropChance;
                        }else{
                            while(dropChance>1){
                                dropChance--;
                                bonus++;
                            }
                            if(new Random().nextDouble()<dropChance)bonus++;
                        }
                        if(drop){
                            for(int i = 0; i<bonus+1; i++){
                                for(ItemStack s : applyEnchantments?block.getDrops(axe):block.getDrops()){
                                    if(convert){
                                        if(s.getType().name().contains("_WOOD")){
                                            s.setType(Material.matchMaterial(s.getType().name().replace("_WOOD", "_LOG")));
                                        }
                                    }
                                    for(ItemStack st : player.getInventory().addItem(s).values()){
                                        block.getWorld().dropItemNaturally(block.getLocation(), st);
                                    }
                                }
                            }
                        }
                        block.setType(Material.AIR);
                    }while(false);
                }
            case BREAK:
                if(Bukkit.getServer().getPluginManager().getPlugin("McMMO")!=null){
                    try{
                        if(block!=origin)com.gmail.nossr50.api.ExperienceAPI.addXpFromBlock(block.getState(), com.gmail.nossr50.util.player.UserManager.getPlayer(player));
                    }catch(Exception ex){}
                }
                boolean drop = true;
                int bonus = 0;
                if(dropChance<=1){
                    drop = new Random().nextDouble()<dropChance;
                }else{
                    while(dropChance>1){
                        dropChance--;
                        bonus++;
                    }
                    if(new Random().nextDouble()<dropChance)bonus++;
                }
                if(convert){
                    if(drop){
                        for(int i = 0; i<bonus+1; i++){
                            for(ItemStack s : applyEnchantments?block.getDrops(axe):block.getDrops()){
                                if(s.getType().name().contains("_WOOD")){
                                    s.setType(Material.matchMaterial(s.getType().name().replace("_WOOD", "_LOG")));
                                }
                                block.getWorld().dropItemNaturally(block.getLocation(), s);
                            }
                        }
                    }
                    block.setType(Material.AIR);
                }else{
                    for(int i = 0; i<bonus; i++){
                        for(ItemStack s : applyEnchantments?block.getDrops(axe):block.getDrops()){
                            block.getWorld().dropItemNaturally(block.getLocation(), s);
                        }
                    }
                    if(drop){
                        if(applyEnchantments)block.breakNaturally(axe);
                        else block.breakNaturally();
                    }
                    else block.setType(Material.AIR);
                }
                break;
            case FALL_HURT:
            case FALL:
            case FALL_BREAK:
            case FALL_HURT_BREAK:
            case FALL_INVENTORY:
            case FALL_HURT_INVENTORY:
                FallingBlock falling = block.getWorld().spawnFallingBlock(block.getLocation().add(.5,.5,.5), block.getBlockData());
                Vector v = falling.getVelocity();
                if(directionalFallVelocity>0){
                    Vector directionalVel = new Vector(0, 0, 0);
                    switch(directionalFallBehavior){
                        case RANDOM:
                            double angle = new Random(seed).nextDouble()*Math.PI*2;
                            directionalVel = new Vector(Math.cos(angle),0,Math.sin(angle));
                            break;
                        case TOWARD:
                            if(player!=null){
                                directionalVel = new Vector(player.getLocation().getX()-block.getLocation().getX(),player.getLocation().getY()-block.getLocation().getY(),player.getLocation().getZ()-block.getLocation().getZ());
                            }
                            break;
                        case AWAY:
                            if(player!=null){
                                directionalVel = new Vector(player.getLocation().getX()-block.getLocation().getX(),player.getLocation().getY()-block.getLocation().getY(),player.getLocation().getZ()-block.getLocation().getZ()).multiply(-1);
                            }
                            break;
                        case LEFT:
                            if(player!=null){
                                directionalVel = new Vector(-(player.getLocation().getZ()-block.getLocation().getZ()),player.getLocation().getY()-block.getLocation().getY(),player.getLocation().getX()-block.getLocation().getX());
                            }
                            break;
                        case RIGHT:
                            if(player!=null){
                                directionalVel = new Vector(-(player.getLocation().getZ()-block.getLocation().getZ()),player.getLocation().getY()-block.getLocation().getY(),player.getLocation().getX()-block.getLocation().getX()).multiply(-1);
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
                            throw new IllegalArgumentException("Invalid fall behavior: "+directionalFallBehavior);
                    }
                    directionalVel = new Vector(directionalVel.getX()/Math.abs(directionalVel.length()), directionalVel.getY()/Math.abs(directionalVel.length()), directionalVel.getZ()/Math.abs(directionalVel.length()));
                    directionalVel = directionalVel.multiply(directionalFallVelocity);
                    v.add(directionalVel);
                }
                v.add(new Vector((Math.random()*2-1)*randomFallVelocity, randomFallVelocity/5, (Math.random()-.5)*randomFallVelocity));
                falling.setVelocity(v);
                falling.setHurtEntities(behavior.name().contains("HURT"));
                if(behavior.name().contains("BREAK"))falling.addScoreboardTag("TreeFeller_Break");
                if(behavior.name().contains("INVENTORY")){
                    if(player==null)falling.addScoreboardTag("TreeFeller_Break");
                    else falling.addScoreboardTag("TreeFeller_Inventory_"+player.getUniqueId().toString());
                }
                if(convert)falling.addScoreboardTag("TreeFeller_Convert");
                if(falling.getBlockData() instanceof Orientable&&rotate){
                    falling.addScoreboardTag("TreeFeller_R"+((Orientable)falling.getBlockData()).getAxis().name()+"_"+origin.getX()+"_"+origin.getY()+"_"+origin.getZ());
                }
                block.setType(Material.AIR);
                fallingBlocks.add(falling.getUniqueId());
                break;
            default:
                throw new IllegalArgumentException("Invalid block behavior: "+behavior);
        }
        for(Effect e : effects){
            if(new Random().nextDouble()<e.chance)e.play(block);
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