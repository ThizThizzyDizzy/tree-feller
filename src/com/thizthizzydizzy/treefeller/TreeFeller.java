package com.thizthizzydizzy.treefeller;
import com.thizthizzydizzy.treefeller.compat.TestResult;
import com.thizthizzydizzy.treefeller.compat.TreeFellerCompat;
import com.thizthizzydizzy.treefeller.decoration.DecorationDetector;
import com.thizthizzydizzy.treefeller.menu.MenuTreesConfiguration;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
public class TreeFeller extends JavaPlugin{
    public static ArrayList<Tool> tools = new ArrayList<>();
    public static ArrayList<Tree> trees = new ArrayList<>();
    public static ArrayList<Effect> effects = new ArrayList<>();
    public static HashMap<UUID, Cooldown> cooldowns = new HashMap<>();
    public static HashMap<Player, MenuTreesConfiguration> detectingTrees = new HashMap<>();
    public HashSet<UUID> toggledPlayers = new HashSet<>();
    public ArrayList<FallingTreeBlock> fallingBlocks = new ArrayList<>();
    public ArrayList<Sapling> saplings = new ArrayList<>();
    public boolean debug = false;
    ArrayList<NaturalFall> naturalFalls = new ArrayList<>();
    ArrayList<Cascade> pendingCascades = new ArrayList<>();
    private static final HashMap<Material, int[]> exp = new HashMap<>();
    public final ArrayList<String> patrons = new ArrayList<>();
    private BukkitTask cascadeTask;
    {//fallback list in case downloading fails
        patrons.add("Thalzamar");
        patrons.add("Mstk");
        patrons.add("ZathrusWriter");
    }
    static{//Perhaps this should be in the config rather than hard-coded...
        exp.put(Material.COAL_ORE, new int[]{0, 2});
        exp.put(Material.NETHER_GOLD_ORE, new int[]{0, 1});
        exp.put(Material.DIAMOND_ORE, new int[]{3, 7});
        exp.put(Material.EMERALD_ORE, new int[]{3, 7});
        exp.put(Material.LAPIS_ORE, new int[]{2, 5});
        exp.put(Material.NETHER_QUARTZ_ORE, new int[]{2, 5});
        exp.put(Material.REDSTONE_ORE, new int[]{1, 5});
        exp.put(Material.SPAWNER, new int[]{15, 43});
    }
    public void fellTree(BlockBreakEvent event){
        if(fellTree(event.getBlock(), event.getPlayer()))event.setCancelled(true);
    }
    public boolean fellTree(Block block, Player player){
        return fellTree(block, player.getInventory().getItemInMainHand(), player);
    }
    public boolean fellTree(Block block, ItemStack axe, Player player){
        return fellTree(block, player, axe);
    }
    public boolean fellTree(Block block, Player player, ItemStack axe){
        return fellTree(block, player, axe, true)!=null;
    }
    /**
     * Fells a tree
     * @param block     the block that was broken
     * @param player    the player whose permissions are to be used. CAN BE NULL
     * @param axe       the tool used to break the block
     * @param dropItems weather or not to drop items
     * @return the items that would have been dropped. <b>This is not the actual dropped items, but possible dropped items</b> Returns null if the tree was not felled.
     */
    public ArrayList<ItemStack> fellTree(Block block, Player player, ItemStack axe, boolean dropItems){
        ItemMeta meta = axe.hasItemMeta()?axe.getItemMeta():null;
        boolean unbreakable = meta!=null&&meta.isUnbreakable();
        DetectedTree detectedTree = detectTree(block, player, axe, (testTree) -> {
            Tool tool = testTree.tool;
            Tree tree = testTree.tree;
            int durability = axe.getType().getMaxDurability()-axe.getDurability();
            if(Option.STACKED_TOOLS.get(testTree.tool, testTree.tree)){
                durability+=axe.getType().getMaxDurability()*(axe.getAmount()-1);
            }
            int durabilityCost = getTotal(testTree.trunk);
            if(Option.DAMAGE_MULT.globalValue!=null)durabilityCost*=Option.DAMAGE_MULT.globalValue;
            if(Option.DAMAGE_MULT.treeValues.containsKey(tree))durabilityCost*=Option.DAMAGE_MULT.treeValues.get(tree);
            if(Option.DAMAGE_MULT.toolValues.containsKey(tool))durabilityCost*=Option.DAMAGE_MULT.toolValues.get(tool);
            if(Option.RESPECT_UNBREAKING.get(tool, tree)){
                durabilityCost/=(axe.getEnchantmentLevel(Enchantment.DURABILITY)+1);
                if(durabilityCost<1)durabilityCost++;
            }
            if(Option.RESPECT_UNBREAKABLE.get(tool, tree)&&unbreakable)durabilityCost = 0;
            if(axe.getType().getMaxDurability()==0)durabilityCost = 0;//there is no durability
            if(player!=null&&player.getGameMode()==GameMode.CREATIVE)durabilityCost = 0;//Don't cost durability
            if(durabilityCost>durability&&Option.ALLOW_PARTIAL_TOOL.get(tool, tree)){
                debug(player, "partial-tool", false);
                durabilityCost = durability;
            }
            if(Option.PREVENT_BREAKAGE.get(tool, tree)){
                if(durabilityCost==durability){
                    debug(player, false, false, "prevent-breakage");
                    return null;
                }
                debug(player, false, true, "prevent-breakage-success");
            }
            if(durabilityCost>durability){
                if(!Option.ALLOW_PARTIAL.get(tool, tree)){
                    debug(player, false, false, "durability-low", durability, durabilityCost);
                    return null;
                }
                debug(player, "partial", false);
            }
            return true;
        });
        if(detectedTree==null)return null;
        Tool tool = detectedTree.tool;
        Tree tree = detectedTree.tree;
        int durability = axe.getType().getMaxDurability()-axe.getDurability();
        if(Option.STACKED_TOOLS.get(detectedTree.tool, detectedTree.tree)){
            durability+=axe.getType().getMaxDurability()*(axe.getAmount()-1);
        }
        int total = getTotal(detectedTree.trunk);
        int durabilityCost = total;
        if(Option.DAMAGE_MULT.globalValue!=null)durabilityCost*=Option.DAMAGE_MULT.globalValue;
        if(Option.DAMAGE_MULT.treeValues.containsKey(tree))durabilityCost*=Option.DAMAGE_MULT.treeValues.get(tree);
        if(Option.DAMAGE_MULT.toolValues.containsKey(tool))durabilityCost*=Option.DAMAGE_MULT.toolValues.get(tool);
        if(Option.RESPECT_UNBREAKING.get(tool, tree)){
            durabilityCost/=(axe.getEnchantmentLevel(Enchantment.DURABILITY)+1);
            if(durabilityCost<1)durabilityCost++;
        }
        if(Option.RESPECT_UNBREAKABLE.get(tool, tree)&&unbreakable)durabilityCost = 0;
        if(durabilityCost>durability&&Option.ALLOW_PARTIAL_TOOL.get(tool, tree)){
            durabilityCost = durability;
        }
        if(durabilityCost>durability&&Option.ALLOW_PARTIAL.get(tool, tree)){
            durabilityCost = total = durability;
        }
        if(axe.getType().getMaxDurability()==0)durabilityCost = 0;//there is no durability
        if(player!=null&&player.getGameMode()==GameMode.CREATIVE)durabilityCost = 0;//Don't cost durability
        debug(player, true, true, "success");
        TreeFellerCompat.fellTree(this, block, player, axe, tool, tree, detectedTree.trunk);
        if(Option.LEAVE_STUMP.get(tool, tree)){
            for(int i : detectedTree.trunk.keySet()){
                for(Iterator<Block> it = detectedTree.trunk.get(i).iterator(); it.hasNext();){
                    Block b = it.next();
                    if(b.getY()<block.getY())it.remove();
                }
            }
        }
        int lower = block.getY();
        for(Block b : toList(detectedTree.trunk)){
            if(b.getY()<lower)lower = b.getY();
        }
        int lowest = lower;
        if(player!=null&&player.getGameMode()!=GameMode.CREATIVE&&player.getGameMode()!=GameMode.SPECTATOR){
            int logCount = 0, leafCount = 0;
            for(int i : detectedTree.trunk.keySet())logCount+=detectedTree.trunk.get(i).size();
            for(int i : detectedTree.leaves.keySet())leafCount+=detectedTree.leaves.get(i).size();
            double consumeFood = Option.CONSUMED_FOOD_BASE.get(tool, tree)+Option.CONSUMED_FOOD_LOGS.get(tool, tree)*logCount+Option.CONSUMED_FOOD_LEAVES.get(tool, tree)*leafCount;
            double consumeHealth = Option.CONSUMED_HEALTH_BASE.get(tool, tree)+Option.CONSUMED_HEALTH_LOGS.get(tool, tree)*logCount+Option.CONSUMED_HEALTH_LEAVES.get(tool, tree)*leafCount;
            if(consumeFood>0){
                float satCost = (float)Math.min(player.getSaturation(), consumeFood);
                int foodCost = (int)(consumeFood-satCost);
                player.setSaturation(Math.max(0, player.getSaturation()-satCost));
                player.setFoodLevel(Math.max(0, player.getFoodLevel()-foodCost));
            }
            if(consumeFood<0){
                int foodBonus = Math.min((int)-consumeFood, 20-player.getFoodLevel());
                player.setFoodLevel(player.getFoodLevel()+foodBonus);
                player.setSaturation((float)(player.getSaturation()+(-consumeFood-foodBonus)));
            }
            if(consumeHealth>0){
                player.damage(consumeHealth);
            }
            if(consumeHealth<0){
                player.setHealth(Math.min(player.getMaxHealth(), Math.max(0, player.getHealth()-consumeHealth)));
            }
            if(axe.getType().getMaxDurability()>0){
                if(Option.STACKED_TOOLS.get(tool, tree)){
                    int amt = axe.getAmount();
                    while(durabilityCost>axe.getType().getMaxDurability()-axe.getDurability()){
                        amt--;
                        durabilityCost-=axe.getType().getMaxDurability();
                    }
                    playToolBreakEffect(tool, tree, axe, player, block);
                    axe.setAmount(amt);
                }
                if(durability==durabilityCost){
                    playToolBreakEffect(tool, tree, axe, player, block);
                }
                axe.setDurability((short)(axe.getDurability()+durabilityCost));
                if(durability==durabilityCost){
                    axe.setAmount(0);
                }
            }
        }
        //now the blocks
        ArrayList<ItemStack> droppedItems = new ArrayList<>();
        final int t = total;
        long seed = new Random().nextLong();
        ArrayList<Integer> distances = new ArrayList<>(detectedTree.trunk.keySet());
        Collections.sort(distances);
        saplings.addAll(detectedTree.saplings);
        if(Option.CUTTING_ANIMATION.get(tool, tree)){
            int delay = 0;
            int ttl = t;
            int tTL = t;
            int Ttl = 0;
            for(int i : distances){
                int TTL = tTL - Ttl;
                delay+=Option.ANIM_DELAY.get(tool, tree);
                for(Block b : detectedTree.trunk.get(i)){
                    if(ttl<=0)break;
                    for(Block leaf : toList(getBlocksWithLeafCheck(tree.trunk, tree.leaves, b, Option.LEAF_BREAK_RANGE.get(tool, tree), Option.DIAGONAL_LEAVES.get(tool, tree), Option.PLAYER_LEAVES.get(tool, tree), Option.IGNORE_LEAF_DATA.get(tool, tree), Option.FORCE_DISTANCE_CHECK.get(tool, tree)))){
                        droppedItems.addAll(getDrops(leaf, tool, tree, axe, new int[1]));
                        for(Block d : detectedTree.getDecorations(leaf))droppedItems.addAll(getDrops(d, tool, tree, axe, new int[1]));
                    }
                    droppedItems.addAll(getDrops(b, tool, tree, axe, new int[1]));
                    for(Block d : detectedTree.getDecorations(b))droppedItems.addAll(getDrops(d, tool, tree, axe, new int[1]));
                    ttl--;
                }
                new BukkitRunnable() {
                    @Override
                    public void run(){
                        int tTl = TTL;
                        for(Block b : detectedTree.trunk.get(i)){
                            if(tTl<=0)break;
                            for(Block leaf : toList(getBlocksWithLeafCheck(tree.trunk, tree.leaves, b, Option.LEAF_BREAK_RANGE.get(tool, tree), Option.DIAGONAL_LEAVES.get(tool, tree), Option.PLAYER_LEAVES.get(tool, tree), Option.IGNORE_LEAF_DATA.get(tool, tree), Option.FORCE_DISTANCE_CHECK.get(tool, tree)))){
                                breakBlock(detectedTree, dropItems, tree, tool, axe, leaf, block, lowest, player, seed, true);
                                for(Block d : detectedTree.getDecorations(leaf))breakBlock(detectedTree, dropItems, tree, tool, axe, d, block, lowest, player, seed, true);
                            }
                            breakBlock(detectedTree, dropItems, tree, tool, axe, b, block, lowest, player, seed, false);
                            for(Block d : detectedTree.getDecorations(b))breakBlock(detectedTree, dropItems, tree, tool, axe, d, block, lowest, player, seed, false);
                            tTl--;
                        }
                        processNaturalFalls();
                    }
                }.runTaskLater(this, delay);
                Ttl += detectedTree.trunk.get(i).size();
            }
            Integer maxSaplings = Option.MAX_SAPLINGS.get(tool, tree);
            if(maxSaplings!=null&&maxSaplings>=1){
                if(Option.SPAWN_SAPLINGS.get(tool, tree)==2){
                    new BukkitRunnable() {
                        @Override
                        public void run(){
                            for(Sapling s : detectedTree.saplings){
                                s.place(null);
                            }
                        }
                    }.runTaskLater(this, delay+1);
                }
            }
        }else{//TODO what about dropped items?
            for(int i : distances){
                for(Block b : detectedTree.trunk.get(i)){
                    if(total<=0)break;
                    for(Block leaf : toList(getBlocksWithLeafCheck(tree.trunk, tree.leaves, b, Option.LEAF_BREAK_RANGE.get(tool, tree), Option.DIAGONAL_LEAVES.get(tool, tree), Option.PLAYER_LEAVES.get(tool, tree), Option.IGNORE_LEAF_DATA.get(tool, tree), Option.FORCE_DISTANCE_CHECK.get(tool, tree)))){
                        breakBlock(detectedTree, dropItems, tree, tool, axe, leaf, block, lowest, player, seed, true);
                        for(Block d : detectedTree.getDecorations(leaf))breakBlock(detectedTree, dropItems, tree, tool, axe, d, block, lowest, player, seed, true);
                    }
                    breakBlock(detectedTree, dropItems, tree, tool, axe, b, block, lowest, player, seed, false);
                    for(Block d : detectedTree.getDecorations(b))breakBlock(detectedTree, dropItems, tree, tool, axe, d, block, lowest, player, seed, false);
                    total--;
                }
            }
            if(Option.SPAWN_SAPLINGS.get(tool, tree)==2){
                for(Sapling s : detectedTree.saplings){
                    s.place(null);
                }
            }
            processNaturalFalls();
        }
        if(player!=null){
            long time = System.currentTimeMillis();
            Cooldown cooldown = cooldowns.get(player.getUniqueId());
            if(cooldown==null)cooldown = new Cooldown();
            cooldown.globalCooldown = time;
            cooldown.treeCooldowns.put(tree, time);
            cooldown.toolCooldowns.put(tool, time);
            cooldowns.put(player.getUniqueId(), cooldown);
        }
        for(Effect e : Option.EFFECTS.get(tool, tree)){
            if(e.location==Effect.EffectLocation.TOOL){
                if(new Random().nextDouble()<e.chance)e.play(block);
            }
        }
        createSaplingHandler();
        return droppedItems;
    }
    /**
     * Detect any type of tree from a source block
     * @param block the block to search for the tree from
     * @param player The player to use while detecting the tree; can be null
     * @param axe The ItemStack to use while detecting the tree; can be null. (Used to find the Tool; tool durability for partial trees is not considered)
     * @return a DetectedTree object, or null if no tree was found
     */
    public DetectedTree detectTree(Block block, Player player, ItemStack axe){
        return detectTree(block, player, axe, (t) -> {
            return true;
        });
    }
    /**
     * Detect any type of tree from a source block
     * @param block the block to search for the tree from
     * @param player The player to use while detecting the tree; can be null
     * @param axe The ItemStack to use while detecting the tree; can be null. (Used to find the Tool; tool durability for partial trees is not considered)
     * @param checkFunc A function to perform any additional checks to ensure a tree is valid. returns true if the tree is valid, null if the tool is invalid, or false if the tree is invalid.
     * @return a DetectedTree object, or null if no tree was found
     */
    public DetectedTree detectTree(Block block, Player player, ItemStack axe, Function<DetectedTree, Boolean> checkFunc){
        if(player!=null&&player.getGameMode()==GameMode.SPECTATOR)return null;
        debugIndent = 0;
        Material material = block.getType();
        TREE:for(Tree tree : trees){
            if(!tree.trunk.contains(material)){
                HashSet<Material> roots = Option.ROOTS.getValue(tree);
                if(roots!=null&&roots.contains(material)){
                    Block b = getNearest(block, tree.trunk, roots, Option.ROOT_DISTANCE.get(null, tree), true, Option.PLAYER_LEAVES.get(null, tree));
                    if(b!=null)return detectTree(b, player, axe, checkFunc);
                }
                continue;
            }
            TOOL:for(Tool tool : tools){
                if(player!=null&&!isToggledOn(player)){
                    debug(player, true, false, "toggle");
                    return null;
                }
                debug(player, "checking", false, trees.indexOf(tree), tools.indexOf(tool));
                if(axe!=null&&tool.material!=Material.AIR&&axe.getType()!=tool.material)continue;
                for(Option o : Option.options){
                    DebugResult result = o.check(this, tool, tree, block, player, axe);
                    if(result==null)continue;
                    debug(player, false, result);
                    if(!result.isSuccess())continue TOOL;
                }
                int scanDistance = Option.SCAN_DISTANCE.get(tool, tree);
                Integer maxBlocks = Option.MAX_LOGS.get(tool, tree);
                HashMap<Integer, ArrayList<Block>> blocks = getBlocks(tree.trunk, block, scanDistance, maxBlocks==null?Integer.MAX_VALUE:(maxBlocks*2), true, false, false);//TODO what if the trunk is made of leaves?
                for(Option o : Option.options){
                    DebugResult result = o.checkTrunk(this, tool, tree, blocks, block);
                    if(result==null)continue;
                    debug(player, false, result);
                    if(!result.isSuccess())continue TOOL;
                }
                int minY = block.getY();
                for(int i : blocks.keySet()){
                    for(Block b : blocks.get(i)){
                        minY = Math.min(minY, b.getY());
                    }
                }
                ArrayList<Integer> distances = new ArrayList<>(blocks.keySet());
                Collections.sort(distances);
                int leaves = 0;
                HashMap<Integer, ArrayList<Block>> allLeaves = new HashMap<>();
                FOR:for(int i : distances){
                    for(Block b : blocks.get(i)){
                        HashMap<Integer, ArrayList<Block>> someLeaves = getBlocksWithLeafCheck(tree.trunk, tree.leaves, b, Option.LEAF_DETECT_RANGE.get(tool, tree), Option.DIAGONAL_LEAVES.get(tool, tree), Option.PLAYER_LEAVES.get(tool, tree), Option.IGNORE_LEAF_DATA.get(tool, tree), Option.FORCE_DISTANCE_CHECK.get(tool, tree));
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
                ArrayList<Block> everything = new ArrayList<>();
                everything.addAll(toList(blocks));
                everything.addAll(toList(allLeaves));
                TestResult res = TreeFellerCompat.test(this, player, everything);
                if(res!=null){
                    debug(player, false, false, "protected", res.plugin, res.block.getX(), res.block.getY(), res.block.getZ());
                    continue TREE;
                }
                for(Option o : Option.options){
                    DebugResult result = o.checkTree(this, tool, tree, blocks, leaves);
                    if(result==null)continue;
                    debug(player, false, result);
                    if(!result.isSuccess())continue TOOL;
                }
                HashMap<Block, ArrayList<Block>> decorations = new HashMap<>();
                for(Block b : everything){
                    ArrayList<Block> decor = new ArrayList<>();
                    for(DecorationDetector detector : Option.DECORATIONS.get(tool, tree)){
                        detector.detect(b, decor);
                    }
                    if(!decor.isEmpty())decorations.put(b, decor);
                }
                DetectedTree detected = new DetectedTree(tool, tree, blocks, allLeaves, decorations);
                Boolean result = checkFunc.apply(detected);
                if(result==null)continue;
                if(Objects.equals(result, false))continue TREE;
                debug(player, true, true, "success");
                if(Option.LEAVE_STUMP.get(tool, tree)){
                    for(int i : blocks.keySet()){
                        for(Iterator<Block> it = blocks.get(i).iterator(); it.hasNext();){
                            Block b = it.next();
                            if(b.getY()<block.getY()){
                                detected.stump.add(b);
                            }
                        }
                    }
                }
                HashMap<Block, Integer> possibleSaplings = new HashMap<>();
                if(Option.SAPLING.get(tool, tree)!=null&&Option.REPLANT_SAPLINGS.get(tool, tree)){
                    ArrayList<Block> logs = toList(blocks);
                    for(Block log : logs){
                        if(Option.GRASS.get(tool, tree).contains(log.getRelative(0, -1, 0).getType())){
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
                    Integer maxSaplings = Option.MAX_SAPLINGS.get(tool, tree);
                    if(maxSaplings!=null){
                        while(possibleSaplings.size()>maxSaplings){
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
                    }
                    for(Block b : possibleSaplings.keySet()){
                        detected.addSapling(this, Option.USE_INVENTORY_SAPLINGS.get(tool, tree)?player:null, b, Option.SAPLING.get(tool, tree));
                    }
                }
                return detected;
            }
        }
        return null;
    }
    private static HashMap<Integer, ArrayList<Block>> getBlocks(Collection<Material> materialTypes, Block startingBlock, int maxDistance, int maxBlocks, boolean diagonal, boolean playerLeaves, boolean ignoreLeafData){
        return getBlocks(materialTypes, startingBlock, maxDistance, maxBlocks, diagonal, playerLeaves, ignoreLeafData, false);
    }
    private static HashMap<Integer, ArrayList<Block>> getBlocks(Collection<Material> materialTypes, Block startingBlock, int maxDistance, int maxBlocks, boolean diagonal, boolean playerLeaves, boolean ignoreLeafData, boolean invertLeafDirection){
        //layer zero
        HashMap<Integer, ArrayList<Block>> results = new HashMap<>();
        int total = 0;
        ArrayList<Block> zero = new ArrayList<>();
        if(materialTypes.contains(startingBlock.getType())){
            zero.add(startingBlock);
        }
        results.put(0, zero);
        total+=zero.size();
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
                                    if(!ignoreLeafData){
                                        if(block.getBlockData() instanceof Leaves){
                                            Leaves oldLeaf = (Leaves)block.getBlockData();
                                            if(invertLeafDirection){
                                                if(newLeaf.getDistance()>=oldLeaf.getDistance())continue;
                                            }else{
                                                if(newLeaf.getDistance()<=oldLeaf.getDistance())continue;
                                            }
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
                            if(!ignoreLeafData){
                                if(block.getBlockData() instanceof Leaves){
                                    Leaves oldLeaf = (Leaves)block.getBlockData();
                                    if(invertLeafDirection){
                                        if(newLeaf.getDistance()>=oldLeaf.getDistance())continue;
                                    }else{
                                        if(newLeaf.getDistance()<=oldLeaf.getDistance())continue;
                                    }
                                }
                            }
                        }
                        layer.add(newBlock);
                    }
                }
            }
            if(layer.isEmpty())break;
            results.put(i+1, layer);
            total+=layer.size();
            if(total>maxBlocks)return results;
        }
        return results;
    }
    private HashMap<Integer, ArrayList<Block>> getBlocksWithLeafCheck(ArrayList<Material> trunk, ArrayList<Material> leaves, Block startingBlock, int maxDistance, boolean diagonal, boolean playerLeaves, boolean ignoreLeafData, boolean forceDistanceCheck){
        HashMap<Integer, ArrayList<Block>> blocks = getBlocks(leaves, startingBlock, maxDistance, Integer.MAX_VALUE, diagonal, playerLeaves, ignoreLeafData);
        if(forceDistanceCheck)leafCheck(blocks, trunk, leaves, diagonal, playerLeaves, ignoreLeafData);
        return blocks;
    }
    private static int getTotal(HashMap<Integer, ArrayList<Block>> blocks){
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
    private Block getNearest(Block from, ArrayList<Material> to, Collection<Material> materialTypes, int max, boolean diagonal, boolean playerLeaves){
        materialTypes.add(from.getType());
        materialTypes.addAll(to);
        for(int d = 0; d<max; d++){
            for(Block b : toList(getBlocks(materialTypes, from, d, Integer.MAX_VALUE, diagonal, playerLeaves, true))){
                if(to.contains(b.getType()))return b;
            }
        }
        return null;
    }
    private int distance(Block from, ArrayList<Material> to, ArrayList<Material> materialTypes, int max, boolean diagonal, boolean playerLeaves){
        materialTypes.add(from.getType());
        materialTypes.addAll(to);
        for(int d = 0; d<max; d++){
            for(Block b : toList(getBlocks(materialTypes, from, d, Integer.MAX_VALUE, diagonal, playerLeaves, true))){
                if(to.contains(b.getType()))return d;
            }
        }
        return max;
    }
    @Override
    public void onEnable(){
        PluginDescriptionFile pdfFile = getDescription();
        Logger logger = getLogger();
        //<editor-fold defaultstate="collapsed" desc="Register Events">
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new TreeFellerEventListener(this), this);
//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Register Config">
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
//</editor-fold>
        try{
            pm.addPermission(new Permission("treefeller.help"));
            pm.addPermission(new Permission("treefeller.toggle"));
            pm.addPermission(new Permission("treefeller.reload"));
            pm.addPermission(new Permission("treefeller.debug"));
            pm.addPermission(new Permission("treefeller.config"));
        }catch(IllegalArgumentException ex){
            logger.log(Level.WARNING, "Failed to add permissions! Did you reload the plugin? (If you just want to reload the config, use /treefeller reload)");
        }
        getCommand("treefeller").setExecutor(new CommandTreeFeller(this));
        logger.log(Level.INFO, "{0} has been enabled! (Version {1}) by ThizThizzyDizzy", new Object[]{pdfFile.getName(), pdfFile.getVersion()});
        reload();
        new BukkitRunnable() {
            @Override
            public void run(){
                refreshPatronsList();
            }
        }.runTaskAsynchronously(this);
    }
    @Override
    public void onDisable(){
        PluginDescriptionFile pdfFile = getDescription();
        Logger logger = getLogger();
        logger.log(Level.INFO, "{0} has been disabled! (Version {1}) by ThizThizzyDizzy", new Object[]{pdfFile.getName(), pdfFile.getVersion()});
    }
    public static Particle getParticle(String string){
        Particle p = null;
        try{
            p = Particle.valueOf(string.toUpperCase().replace(" ", "-").replace("-", "_"));
        }catch(IllegalArgumentException ex){}
        if(p!=null)return p;
        switch(string.toLowerCase().replace("_", " ").replace("-", " ")){
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
    public void reload(){
        reload(null);
    }
    public void reload(CommandSender source){
        Logger logger = getLogger();
        trees.clear();
        tools.clear();
        effects.clear();
        saplings.clear();
        if(saplingHandler!=null)saplingHandler.cancel();
        saplingHandler = null;
        fallingBlocks.clear();
        cooldowns.clear();
        //<editor-fold defaultstate="collapsed" desc="Effects">
        ArrayList<Object> effects = null;
        try{
            effects = new ArrayList<>(getConfig().getList("effects"));
        }catch(NullPointerException ex){
            if(getConfig().get("effects")!=null)log(logger, source, Level.WARNING, "Failed to load effects!");
        }
        if(effects!=null){
            for(Object o : effects){
                if(o instanceof LinkedHashMap){
                    LinkedHashMap map = (LinkedHashMap) o;
                    if(!map.containsKey("name")||!(map.get("name") instanceof String)){
                        log(logger, source, Level.WARNING, "Cannot find effect name! Skipping...");
                        continue;
                    }
                    String name = (String)map.get("name");
                    String typ = (String) map.get("type");
                    Effect.EffectType type = Effect.EffectType.valueOf(typ.toUpperCase().trim());
                    if(type==null){
                        log(logger, source, Level.WARNING, "Invalid effect type: {0}! Skipping...", typ);
                        continue;
                    }
                    String loc = (String) map.get("location");
                    Effect.EffectLocation location = Effect.EffectLocation.valueOf(loc.toUpperCase().trim());
                    if(location==null){
                        log(logger, source, Level.WARNING, "Invalid effect location: {0}! Skipping...", loc);
                        continue;
                    }
                    double chance = 1;
                    if(map.containsKey("chance")){
                        chance = ((Number)map.get("chance")).doubleValue();
                    }
                    Effect effect = type.loadEffect(name, location, chance, map);
                    if(Option.STARTUP_LOGS.isTrue())effect.print(logger);
                    this.effects.add(effect);
                }else if(o instanceof String){
                    Material m = Material.matchMaterial((String)o);
                    if(m==null){
                        log(logger, source, Level.WARNING, "Unknown material: {0}; Skipping...", o);
                    }
                    Tool tool = new Tool(m);
                    if(Option.STARTUP_LOGS.isTrue())tool.print(logger);
                    this.tools.add(tool);
                }else{
                    log(logger, source, Level.INFO, "Unknown tool declaration: {0} | {1}", new Object[]{o.getClass().getName(), o.toString()});
                }
            }
        }
//</editor-fold>
        TreeFellerCompat.init(null);//don't actually initialize the compatibilities, just their settings
        for(Option option : Option.options){
            if(option.global){
                option.setValue(option.loadFromConfig(getConfig()));
            }
        }
        TreeFellerCompat.init(this);//now initialize the compatibilities
        for(Message message : Message.messages){
            message.load(getConfig());
        }
        if(Option.STARTUP_LOGS.isTrue()){
            log(logger, source, Level.INFO, "Server version: {0}", Bukkit.getServer().getBukkitVersion());
            log(logger, source, Level.INFO, "Loaded global values:");
            for(Option option : Option.options){
                Object value = option.getValue();
                if(value!=null){
                    log(logger, source, Level.INFO, "- {0}: {1}", new Object[]{option.name, option.makeReadable(value)});
                }
            }
        }
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
                    log(logger, source, Level.WARNING, "Cannot load tree: {0}", o);
                    continue;
                }
                Tree tree = new Tree(trunk, leaves);
                if(((ArrayList) o).size()>2){
                    LinkedHashMap map = (LinkedHashMap) ((ArrayList) o).get(2);
                    for(Object key : map.keySet()){
                        if(!(key instanceof String)){
                            log(logger, source, Level.WARNING, "invalid tree option: {0}", key);
                            continue;
                        }
                        String s = ((String)key).toLowerCase().replace("-", "").replace("_", "").replace(" ", "");
                        boolean found = false;
                        for(Option option : Option.options){
                            if(!option.tree)continue;
                            if(option.getLocalName().equals(s)){
                                found = true;
                                option.setValue(tree, option.load(map.get(key)));
                            }
                        }
                        if(!found)log(logger, source, Level.WARNING, "Found unknown tree option: {0}", key);
                    }
                }
                if(Option.STARTUP_LOGS.isTrue())tree.print(logger);
                this.trees.add(tree);
            }else if(o instanceof String){
                ArrayList<Material> trunk = new ArrayList<>();
                ArrayList<Material> leaves = new ArrayList<>();
                Material t = Material.matchMaterial((String) o);
                Material l = Material.matchMaterial(((String) o).replace("STRIPPED_", "").replace("LOG", "LEAVES").replace("WOOD", "LEAVES"));
                if(t!=null)trunk.add(t);
                if(l!=null)leaves.add(l);
                if(trunk.isEmpty()||leaves.isEmpty()){
                    log(logger, source, Level.WARNING, "Cannot load tree: {0}", o);
                    continue;
                }
                Tree tree = new Tree(trunk, leaves);
                if(Option.STARTUP_LOGS.isTrue())tree.print(logger);
                this.trees.add(tree);
            }else{
                log(logger, source, Level.WARNING, "Cannot load tree: {0}", o);
            }
        }
//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Tools">
        ArrayList<Object> tools = new ArrayList<>(getConfig().getList("tools"));
        for(Object o : tools){
            if(o instanceof LinkedHashMap){
                LinkedHashMap map = (LinkedHashMap) o;
                if(!map.containsKey("type")||!(map.get("type") instanceof String)){
                    log(logger, source, Level.WARNING, "Cannot find tool material! Skipping...");
                    continue;
                }
                String typ = (String) map.get("type");
                Material type = Material.matchMaterial(typ.trim());
                if(type==null){
                    log(logger, source, Level.WARNING, "Unknown tool material: {0}! Skipping...", map.get("type"));
                    continue;
                }
                Tool tool = new Tool(type);
                for(Object key : map.keySet()){
                    if(key.equals("type"))continue;//already got that
                    if(!(key instanceof String)){
                        log(logger, source, Level.WARNING, "Unknown tool property: {0}; Skipping...", key);
                        continue;
                    }
                    String s = ((String)key).toLowerCase().replace("-", "").replace("_", "").replace(" ", "");
                    boolean found = false;
                    for(Option option : Option.options){
                        if(!option.tool)continue;
                        if(option.getLocalName().equals(s)){
                            found = true;
                            option.setValue(tool, option.load(map.get(key)));
                        }
                    }
                    if(!found)log(logger, source, Level.WARNING, "Found unknown tool option: {0}", key);
                }
                if(Option.STARTUP_LOGS.isTrue())tool.print(logger);
                this.tools.add(tool);
            }else if(o instanceof String){
                Material m = Material.matchMaterial((String)o);
                if(m==null){
                    log(logger, source, Level.WARNING, "Unknown material: {0}; Skipping...", o);
                }
                Tool tool = new Tool(m);
                if(Option.STARTUP_LOGS.isTrue())tool.print(logger);
                this.tools.add(tool);
            }else{
                log(logger, source, Level.INFO, "Unknown tool declaration: {0} | {1}", new Object[]{o.getClass().getName(), o.toString()});
            }
        }
//</editor-fold>
        TreeFellerCompat.reload();
    }
    private void breakBlock(DetectedTree detectedTree, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed, boolean isLeaves){
        ArrayList<Material> overridables = new ArrayList<>(Option.OVERRIDABLES.get(tool, tree));
        ArrayList<Effect> effects = new ArrayList<>();
        Effect.EffectLocation type = Effect.EffectLocation.DECORATION;//TODO use a special enum for this?
        if(tree.leaves.contains(block.getType()))type = Effect.EffectLocation.LEAVES;
        if(tree.trunk.contains(block.getType()))type = Effect.EffectLocation.LOGS;
        if(Option.CASCADE.get(tool, tree)){
            cascade(detectedTree, dropItems, tree, tool, axe, block, player);
        }
        for(Effect e : Option.EFFECTS.get(tool, tree)){
            if(e.location==Effect.EffectLocation.TREE)effects.add(e);
            if(type==e.location)effects.add(e);
        }
        FellBehavior behavior = isLeaves?Option.LEAF_BEHAVIOR.get(tool, tree):Option.LOG_BEHAVIOR.get(tool, tree);
        if(type==Effect.EffectLocation.DECORATION)behavior = behavior.getDecorationBehavior();
//        double dropChance = dropItems?(isLeaf?Option.LEAF_DROP_CHANCE.get(tool, tree):Option.LOG_DROP_CHANCE.get(tool, tree)):0;
        double directionalFallVelocity = Option.DIRECTIONAL_FALL_VELOCITY.get(tool, tree);
        double verticalFallVelocity = Option.VERTICAL_FALL_VELOCITY.get(tool, tree);
        double explosiveFallVelocity = Option.EXPLOSIVE_FALL_VELOCITY.get(tool, tree);
        double randomFallVelocity = Option.RANDOM_FALL_VELOCITY.get(tool, tree);
        boolean rotate = Option.ROTATE_LOGS.get(tool, tree);
        DirectionalFallBehavior directionalFallBehavior = Option.DIRECTIONAL_FALL_BEHAVIOR.get(tool, tree);
        boolean lockCardinal = Option.LOCK_FALL_CARDINAL.get(tool, tree);
        HashMap<Material, Material> conversions = Option.BLOCK_CONVERSIONS.get(tool, tree);
        if(conversions.containsKey(block.getType())){
            BlockState state = block.getState();
            TreeFellerCompat.removeBlock(this, player, block);
            block.setType(conversions.get(block.getType()));
            TreeFellerCompat.addBlock(this, player, block, state);
        }else{
            ArrayList<Modifier> modifiers = new ArrayList<>();
            if(behavior==FellBehavior.FALL||behavior==FellBehavior.FALL_HURT||behavior==FellBehavior.NATURAL){
                TreeFellerCompat.removeBlock(this, player, block);
            }else{
                TreeFellerCompat.breakBlock(this, tree, tool, player, axe, block, modifiers);
            }
            behavior.breakBlock(detectedTree, this, dropItems, tree, tool, axe, block, origin, lowest, player, seed, modifiers, directionalFallBehavior, lockCardinal, directionalFallVelocity, rotate, overridables, randomFallVelocity, explosiveFallVelocity, verticalFallVelocity);
        }
        Random rand = new Random();
        for(Effect e : effects){
            if(rand.nextDouble()<e.chance)e.play(block);
        }
    }
    private void cascade(DetectedTree detectedTree, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Player player){
        pendingCascades.add(new Cascade(detectedTree, dropItems, tree, tool, axe, block, player));
        if(cascadeTask==null){
            cascadeTask = new BukkitRunnable(){
                @Override
                public void run(){
                    int maxChecks = Option.CASCADE_CHECK_LIMIT.getValue();
                    int maxCascades = Option.PARALLEL_CASCADE_LIMIT.getValue();
                    int checks = 0;
                    int cascades = 0;
                    while(checks<maxChecks&&cascades<maxCascades&&!pendingCascades.isEmpty()){
                        checks++;
                        Cascade cascade = pendingCascades.remove(0);
                        ArrayList<Tree> trees = Option.CASCADE_TREES.get(cascade.tool, cascade.tree);
                        if(trees==null){
                            trees = new ArrayList<>();
                            trees.add(cascade.tree);
                        }
                        HashSet<Material> allLeaves = new HashSet<>();
                        HashSet<Material> allTrunks = new HashSet<>();
                        int maxRange = 0;
                        boolean diagonal = false, player = false, ignoreData = false;
                        for(Tree tree : trees){
                            allLeaves.addAll(tree.leaves);
                            allTrunks.addAll(tree.trunk);
                            int range = Option.LEAF_DETECT_RANGE.get(cascade.tool, tree);
                            if(range>maxRange)maxRange = range;
                            if(Option.DIAGONAL_LEAVES.get(cascade.tool, tree))diagonal = true;
                            if(Option.PLAYER_LEAVES.get(cascade.tool, cascade.tree))player = true;
                            if(Option.IGNORE_LEAF_DATA.get(cascade.tool, cascade.tree))ignoreData = true;
                        }
                        HashSet<Block> prevTrunks = new HashSet<>(toList(cascade.detectedTree.trunk));
                        HashSet<Block> prevLeaves = new HashSet<>(toList(cascade.detectedTree.leaves));
                        BlockFace[] directions = new BlockFace[]{BlockFace.NORTH,BlockFace.EAST,BlockFace.SOUTH,BlockFace.WEST,BlockFace.UP,BlockFace.DOWN};
                        for(BlockFace dir : directions){
                            Block start = cascade.block.getRelative(dir);
                            if(prevLeaves.contains(start)||prevTrunks.contains(start))continue;
                            ArrayList<Block> detectedLeaves = toList(getBlocks(new ArrayList<>(allLeaves), start, maxRange, 64, diagonal, player, ignoreData, true));
                            HashSet<Block> trunks = new HashSet<>();
                            for(Block b : detectedLeaves){
                                if(prevLeaves.contains(b))continue;
                                for(BlockFace direction : directions){
                                    Block bl = b.getRelative(direction);
                                    if(allTrunks.contains(bl.getType())){
                                        if(prevTrunks.contains(bl))continue;
                                        if(tryCascade(bl, cascade.player, cascade.axe, cascade.dropItems)!=null){
                                            cascades++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if(pendingCascades.isEmpty()){
                        cascadeTask.cancel();
                        cascadeTask = null;
                    }
                }
            }.runTaskTimer(this, 1, 1);
        }
    }
    public boolean cascading = false;
    public ArrayList<ItemStack> tryCascade(Block block, Player player, ItemStack axe, boolean dropItems){
        cascading = true;
        ArrayList<ItemStack> ret = fellTree(block, player, axe, dropItems);
        cascading = false;
        return ret;
    }
    int randbetween(int[] minmax){
        return randbetween(minmax[0], minmax[1]);
    }
    int randbetween(int min, int max){
        return new Random().nextInt(max-min+1)+min;
    }
    Collection<? extends ItemStack> getDropsWithBonus(Block block, Tool tool, Tree tree, ItemStack axe, int[] xp, List<Modifier> modifiers){
        if(xp.length!=1)throw new IllegalArgumentException("xp must be an array of size 1!");
        ArrayList<ItemStack> drops = new ArrayList<>();
        double dropChance = tree.trunk.contains(block.getType())?Option.LOG_DROP_CHANCE.get(tool, tree):Option.LEAF_DROP_CHANCE.get(tool, tree);
        for(Modifier mod : modifiers){
            dropChance = mod.apply(dropChance, tree, block);
        }
        int numDrops = 0;
        while(dropChance>=1){
            dropChance--;
            numDrops++;
        }
        if(dropChance>0&&new Random().nextDouble()<dropChance)numDrops++;
        if(numDrops>0){
            for(int i = 0; i<numDrops; i++){
                int[] blockXP = new int[1];
                drops.addAll(getDrops(block, tool, tree, axe, blockXP));
                xp[0]+=blockXP[0];
            }
        }
        return drops;
    }
    Collection<? extends ItemStack> getDrops(Block block, Tool tool, Tree tree, ItemStack axe, int[] xp){
        if(xp.length!=1)throw new IllegalArgumentException("blockXP must be an array of length 1!");
        if(exp.containsKey(block.getType())){
            xp[0] += randbetween(exp.get(block.getType()));
        }
        ArrayList<ItemStack> drops = new ArrayList<>();
        boolean fortune, silk;
        if(tree.trunk.contains(block.getType())){
            fortune = Option.LOG_FORTUNE.get(tool, tree);
            silk = Option.LOG_SILK_TOUCH.get(tool, tree);
        }else{
            fortune = Option.LEAF_FORTUNE.get(tool, tree);
            silk = Option.LEAF_SILK_TOUCH.get(tool, tree);
        }
        Material type = block.getType();
        if(silk&&axe.containsEnchantment(Enchantment.SILK_TOUCH)){
            List<ItemStack> drop = new ArrayList<>(block.getDrops(axe));
            //Test if silk touch drops the base item, and if so, use it.
            if(drop.size()==1&&drop.get(0).getType()==block.getType()){
                drops.addAll(drop);
                return drops;
            }
        }
        if(fortune&&!axe.containsEnchantment(Enchantment.SILK_TOUCH))drops.addAll(block.getDrops(axe));
        else drops.addAll(block.getDrops());
        HashMap<Material, Material> conversions = Option.DROP_CONVERSIONS.get(tool, tree);
        if(!conversions.isEmpty()){
            for(ItemStack s : drops){
                if(conversions.containsKey(s.getType()))s.setType(conversions.get(s.getType()));
            }
        }
        return drops;
    }
    private void leafCheck(HashMap<Integer, ArrayList<Block>> someLeaves, ArrayList<Material> trunk, ArrayList<Material> leaves, Boolean diagonal, Boolean playerLeaves, Boolean ignoreLeafData){
        if(ignoreLeafData)return;
        ArrayList<Integer> ints = new ArrayList<>();
        ints.addAll(someLeaves.keySet());
        Collections.sort(ints);
        int done = -1;
        for(int i = 0; i<ints.size(); i++){
            int d = ints.get(i);
            for(Iterator<Block> it = someLeaves.get(d).iterator(); it.hasNext();){
                Block leaf = it.next();
                if(distance(leaf, trunk, leaves, d, diagonal, playerLeaves)<d){
                    it.remove();
                }
            }
            if(i>0&&someLeaves.get(d).isEmpty()){
                done = i;
                break;
            }
        }
        if(done>-1){
            for(int i = done; i<ints.size(); i++){
                someLeaves.remove(ints.get(i));
            }
        }
    }
    ArrayList<ItemStack> getDrops(Material m, Tool tool, Tree tree, ItemStack axe, Block location, int[] xp, List<Modifier> modifiers){
        ArrayList<ItemStack> drops = new ArrayList<>();
        if(!m.isBlock())return drops;
        Block block = findAir(location);
        if(block==null){
            drops.add(new ItemStack(m));
            return drops;
        }
        block.setType(m);
        drops.addAll(getDropsWithBonus(block, tool, tree, axe, xp, modifiers));
        block.setType(Material.AIR);
        return drops;
    }
    private Block findAir(Block location){
        if(location.getType()==Material.AIR)return location;
        for(int x = -8; x<=8; x++){
            for(int y = 255; y>0; y--){
                for(int z = -1; z<=8; z++){
                    Block b = location.getWorld().getBlockAt(x,y,z);
                    if(b.getType()==Material.AIR)return b;
                }
            }
        }
        getLogger().log(Level.SEVERE, "Could not find any nearby air blocks to simulate drops!");
        return null;
    }
    private void processNaturalFalls(){
        for(NaturalFall fall : naturalFalls){
            fall.fall(this);
        }
        naturalFalls.clear();
    }
    private int debugIndent = 0;
    private void debug(Player player, String text, boolean indent, Object... vars){
        Message message = Message.getMessage(text);
        if(message!=null){
            message.send(player, vars);
            text = message.getDebugText();
        }
        for(int i = 0; i<vars.length; i++){
            text = text.replace("{"+i+"}", vars[i].toString());
        }
        if(!debug)return;
        if(indent)debugIndent++;
        text = "[TreeFeller] "+getDebugIndent()+" "+text;
        getLogger().log(Level.INFO, text);
        if(player!=null)player.sendMessage(text);
    }
    private void debug(Player player, boolean critical, DebugResult result){
        Message message = Message.getMessage(result.message+result.type.suffix);
        if(message!=null){
            message.send(player, result.args);
            if(!debug)return;
            String text = message.getDebugText();
            for(int i = 0; i<result.args.length; i++){
                text = text.replace("{"+i+"}", result.args[i].toString());
            }
            if((critical||!result.isSuccess())&&debugIndent>0)debugIndent--;
            String icon;
            if(result.isSuccess())icon = (critical?ChatColor.DARK_GREEN:ChatColor.GREEN)+"O";
            else icon = (critical?ChatColor.DARK_RED:ChatColor.RED)+"X";
            text = "[TreeFeller] "+getDebugIndent(1)+icon+ChatColor.RESET+" "+text;
            getLogger().log(Level.INFO, text);
            if(player!=null)player.sendMessage(text);
        }
    }
    private void debug(Player player, boolean critical, boolean success, String text, Object... vars){
        Message message = Message.getMessage(text);
        if(message!=null){
            message.send(player, vars);
            text = message.getDebugText();
        }
        for(int i = 0; i<vars.length; i++){
            text = text.replace("{"+i+"}", vars[i].toString());
        }
        if(!debug)return;
        if((critical||!success)&&debugIndent>0)debugIndent--;
        String icon;
        if(success)icon = (critical?ChatColor.DARK_GREEN:ChatColor.GREEN)+"O";
        else icon = (critical?ChatColor.DARK_RED:ChatColor.RED)+"X";
        text = "[TreeFeller] "+getDebugIndent(1)+icon+ChatColor.RESET+" "+text;
        getLogger().log(Level.INFO, text);
        if(player!=null)player.sendMessage(text);
    }
    private String getDebugIndent(){
        return getDebugIndent(0);
    }
    private String getDebugIndent(int end){
        String indent = "";
        for(int i = 0; i<debugIndent-end+1; i++){
            indent = indent + "-";
        }
        return indent;
    }
    void dropExp(World world, Location location, int xp){
        while(xp>2477){
            dropExpOrb(world, location, 2477);
            xp-=2477;
        }
        while(xp>1237){
            dropExpOrb(world, location, 1237);
            xp-=1237;
        }
        while(xp>617){
            dropExpOrb(world, location, 617);
            xp-=617;
        }
        while(xp>307){
            dropExpOrb(world, location, 307);
            xp-=307;
        }
        while(xp>149){
            dropExpOrb(world, location, 149);
            xp-=149;
        }
        while(xp>73){
            dropExpOrb(world, location, 73);
            xp-=73;
        }
        while(xp>37){
            dropExpOrb(world, location, 37);
            xp-=37;
        }
        while(xp>17){
            dropExpOrb(world, location, 17);
            xp-=17;
        }
        while(xp>7){
            dropExpOrb(world, location, 7);
            xp-=7;
        }
        while(xp>3){
            dropExpOrb(world, location, 3);
            xp-=3;
        }
        while(xp>1){
            dropExpOrb(world, location, 1);
            xp--;
        }
    }
    public void dropExpOrb(World world, Location location, int xp){
        ExperienceOrb orb = (ExperienceOrb) world.spawnEntity(location, EntityType.EXPERIENCE_ORB);
        orb.setExperience(orb.getExperience()+xp);
    }
    public static Tree detect(Block clickedBlock, Player player){
        ArrayList<Material> allLogs = new ArrayList<>();
        allLogs.add(Material.OAK_LOG);
        allLogs.add(Material.BIRCH_LOG);
        allLogs.add(Material.SPRUCE_LOG);
        allLogs.add(Material.DARK_OAK_LOG);
        allLogs.add(Material.ACACIA_LOG);
        allLogs.add(Material.JUNGLE_LOG);
        allLogs.add(Material.WARPED_STEM);
        allLogs.add(Material.CRIMSON_STEM);
        allLogs.add(Material.OAK_WOOD);
        allLogs.add(Material.BIRCH_WOOD);
        allLogs.add(Material.SPRUCE_WOOD);
        allLogs.add(Material.DARK_OAK_WOOD);
        allLogs.add(Material.ACACIA_WOOD);
        allLogs.add(Material.JUNGLE_WOOD);
        allLogs.add(Material.WARPED_HYPHAE);
        allLogs.add(Material.CRIMSON_HYPHAE);
        allLogs.add(Material.MUSHROOM_STEM);
        HashMap<Integer, ArrayList<Block>> trunk = getBlocks(allLogs, clickedBlock, Option.SCAN_DISTANCE.getValue(), Integer.MAX_VALUE, true, true, true);
        HashSet<Material> logs = new HashSet<>();
        for(int i : trunk.keySet()){
            for(Block b : trunk.get(i))logs.add(b.getType());
        }
        if(logs.isEmpty()){
            player.sendMessage(ChatColor.RED+"Failed to detect tree trunk");
            return null;
        }
        ArrayList<Material> allLeaves = new ArrayList<>();
        allLeaves.add(Material.OAK_LEAVES);
        allLeaves.add(Material.BIRCH_LEAVES);
        allLeaves.add(Material.SPRUCE_LEAVES);
        allLeaves.add(Material.DARK_OAK_LEAVES);
        allLeaves.add(Material.ACACIA_LEAVES);
        allLeaves.add(Material.JUNGLE_LEAVES);
        allLeaves.add(Material.WARPED_WART_BLOCK);
        allLeaves.add(Material.NETHER_WART_BLOCK);
        allLeaves.add(Material.SHROOMLIGHT);
        allLeaves.add(Material.RED_MUSHROOM_BLOCK);
        allLeaves.add(Material.BROWN_MUSHROOM_BLOCK);
        ArrayList<Material> allBlocks = new ArrayList<>(logs);
        allBlocks.addAll(allLeaves);
        HashMap<Integer, ArrayList<Block>> properLeaves;
        HashMap<Integer, ArrayList<Block>> badLeaves;
        HashMap<Integer, ArrayList<Block>> terribleLeaves;
        int proper = getTotal(properLeaves = getBlocks(allBlocks, clickedBlock, Option.SCAN_DISTANCE.getValue(), Integer.MAX_VALUE, false, false, false));
        int bad = getTotal(badLeaves = getBlocks(allBlocks, clickedBlock, Option.SCAN_DISTANCE.getValue(), Integer.MAX_VALUE, false, true, true));
        int terrible = getTotal(terribleLeaves = getBlocks(allBlocks, clickedBlock, Option.SCAN_DISTANCE.getValue(), Integer.MAX_VALUE, true, true, true));
        int numLogs = getTotal(trunk);
        int numLeaves;
        HashSet<Material> leaves = new HashSet<>();
        boolean diagonalLeaves = false;
        boolean playerLeaves = false;
        boolean ignoreLeafData = false;
        if(terrible>bad||terrible>proper){
            numLeaves = terrible-numLogs;
            for(int i : terribleLeaves.keySet()){
                for(Block b : terribleLeaves.get(i))leaves.add(b.getType());
            }
            diagonalLeaves = playerLeaves = ignoreLeafData = true;
        }else if(bad>proper){
            numLeaves = bad-numLogs;
            for(int i : badLeaves.keySet()){
                for(Block b : badLeaves.get(i))leaves.add(b.getType());
            }
            playerLeaves = ignoreLeafData = true;
        }else{
            numLeaves = proper-numLogs;
            for(int i : properLeaves.keySet()){
                for(Block b : properLeaves.get(i))leaves.add(b.getType());
            }
        }
        leaves.removeAll(logs);
        Tree tree = new Tree(new ArrayList<>(logs), new ArrayList<>(leaves));
        if(numLogs<Option.REQUIRED_LOGS.getValue()){
            Option.REQUIRED_LOGS.treeValues.put(tree, numLogs/4);
        }
        if(numLogs>Option.MAX_LOGS.getValue()){
            Option.MAX_LOGS.treeValues.put(tree, numLogs*2);
        }
        if(numLeaves<Option.REQUIRED_LEAVES.getValue()){
            Option.REQUIRED_LEAVES.treeValues.put(tree, numLeaves/4);
        }
        if(diagonalLeaves||Objects.equals(Option.DIAGONAL_LEAVES.getValue(),true))Option.DIAGONAL_LEAVES.treeValues.put(tree, diagonalLeaves);
        if(playerLeaves||Objects.equals(Option.PLAYER_LEAVES.getValue(),true))Option.PLAYER_LEAVES.treeValues.put(tree, playerLeaves);
        if(ignoreLeafData||Objects.equals(Option.IGNORE_LEAF_DATA.getValue(),true))Option.IGNORE_LEAF_DATA.treeValues.put(tree, ignoreLeafData);
        //now to find the leaf range...
        ArrayList<Integer> distances = new ArrayList<>(trunk.keySet());
        Collections.sort(distances);
        int theLeafRange = 0;
        int lastCount = 0;
        for(int leafRange = 1; leafRange<Option.SCAN_DISTANCE.getValue(); leafRange++){
            HashSet<Block> allDaLeaves = new HashSet<>();
            FOR:for(int i : distances){
                for(Block b : trunk.get(i)){
                    HashMap<Integer, ArrayList<Block>> someLeaves = getBlocks(tree.leaves, b, leafRange, Integer.MAX_VALUE, diagonalLeaves, playerLeaves, ignoreLeafData);
                    for(int in : someLeaves.keySet()){
                        allDaLeaves.addAll(someLeaves.get(in));
                    }
                }
            }
            if(allDaLeaves.size()==lastCount)break;
            theLeafRange = leafRange;
            lastCount = allDaLeaves.size();
        }
        if(theLeafRange>Option.LEAF_DETECT_RANGE.getValue())Option.LEAF_DETECT_RANGE.treeValues.put(tree, theLeafRange);
        return tree;
    }
    BukkitTask saplingHandler;
    private void createSaplingHandler(){
        if(saplingHandler!=null)return;
        saplingHandler = new BukkitRunnable() {
            @Override
            public void run(){
                for(Iterator<Sapling> it = saplings.iterator(); it.hasNext();){
                    Sapling sapling = it.next();
                    sapling.tick();
                    if(sapling.isDead())it.remove();
                }
                if(saplings.isEmpty()){
                    saplingHandler = null;
                    cancel();
                }
            }
        }.runTaskTimer(this, 0, 1);
    }
    /**
     * Handle dropped item. Item has already dropped; this handles it for sapling replant & compatibilities
     * @param detectedTree the tree that was cut down
     * @param player the player who dropped the item
     * @param item the item that was dropped
     */
    public void dropItem(DetectedTree detectedTree, Player player, Item item){
        ItemStack stack = item.getItemStack();
        if(Option.USE_TREE_SAPLINGS.get(detectedTree.tool, detectedTree.tree)){
            for(Sapling sapling : saplings){
                if(sapling.detectedTree==detectedTree){
                    if(sapling.tryPlace(stack)){
                        if(stack.getAmount()==0){
                            item.remove();
                            return;
                        }
                    }
                }
            }
        }
        item.setItemStack(stack);
        TreeFellerCompat.dropItem(this, player, item);
    }
    public boolean isToggledOn(Player player){
        boolean inverted = Option.DEFAULT_ENABLED.isTrue();
        boolean toggled = toggledPlayers.contains(player.getUniqueId());
        return inverted?!toggled:toggled;
    }
    public void toggle(Player player){
        boolean on = isToggledOn(player);
        toggle(player, !on);
    }
    public void toggle(Player player, boolean state){
        boolean inverted = Option.DEFAULT_ENABLED.isTrue();
        boolean shouldBeToggled = inverted?!state:state;
        UUID uuid = player.getUniqueId();
        if(toggledPlayers.contains(uuid)){
            if(!shouldBeToggled)toggledPlayers.remove(uuid);
        }else if(shouldBeToggled)toggledPlayers.add(uuid);
        Message.getMessage("toggle-"+(state?"on":"off")).send(player);
    }
    private void refreshPatronsList(){
        try{
            File file = new File(getDataFolder(), "patrons.txt");
            file.delete();
            downloadFile("https://raw.githubusercontent.com/ThizThizzyDizzy/nc-reactor-generator/overhaul/patrons.txt", file.getAbsoluteFile());
            ArrayList<String> patrons = new ArrayList<>();
            try(BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file)))){
                String line;
                while((line = in.readLine())!=null){
                    if(line.isEmpty())continue;
                    patrons.add(line);
                }
            }catch(Exception ex){}
            if(!patrons.isEmpty()){
                this.patrons.clear();
                this.patrons.addAll(patrons);
            }
            file.delete();
        }catch(Exception ex){}//don't crash if the patrons list fails to download
    }
    public static File downloadFile(String link, File destinationFile){
        if(destinationFile.exists()||link==null){
            return destinationFile;
        }
        if(destinationFile.getParentFile()!=null)destinationFile.getParentFile().mkdirs();
        try {
            URL url = new URL(link);
            int fileSize;
            URLConnection connection = url.openConnection();
            connection.setDefaultUseCaches(false);
            if ((connection instanceof HttpURLConnection)) {
                ((HttpURLConnection)connection).setRequestMethod("HEAD");
                int code = ((HttpURLConnection)connection).getResponseCode();
                if (code / 100 == 3) {
                    return null;
                }
            }
            fileSize = connection.getContentLength();
            byte[] buffer = new byte[65535];
            int unsuccessfulAttempts = 0;
            int maxUnsuccessfulAttempts = 3;
            boolean downloadFile = true;
            while (downloadFile) {
                downloadFile = false;
                URLConnection urlconnection = url.openConnection();
                if ((urlconnection instanceof HttpURLConnection)) {
                    urlconnection.setRequestProperty("Cache-Control", "no-cache");
                    urlconnection.connect();
                }
                String targetFile = destinationFile.getName();
                FileOutputStream fos;
                int downloadedFileSize;
                try (InputStream inputstream=getRemoteInputStream(targetFile, urlconnection)) {
                    fos=new FileOutputStream(destinationFile);
                    downloadedFileSize=0;
                    int read;
                    while ((read = inputstream.read(buffer)) != -1) {
                        fos.write(buffer, 0, read);
                        downloadedFileSize += read;
                    }
                }
                fos.close();
                if (((urlconnection instanceof HttpURLConnection)) && 
                    ((downloadedFileSize != fileSize) && (fileSize > 0))){
                    unsuccessfulAttempts++;
                    if (unsuccessfulAttempts < maxUnsuccessfulAttempts){
                        downloadFile = true;
                    }else{
                        throw new Exception("failed to download "+targetFile);
                    }
                }
            }
            return destinationFile;
        }catch (Exception ex){
            return null;
        }
    }
    public static InputStream getRemoteInputStream(String currentFile, final URLConnection urlconnection) throws Exception {
        final InputStream[] is = new InputStream[1];
        for (int j = 0; (j < 3) && (is[0] == null); j++) {
            Thread t = new Thread() {
                public void run() {
                    try {
                        is[0] = urlconnection.getInputStream();
                    }catch (IOException localIOException){}
                }
            };
            t.setName("FileDownloadStreamThread");
            t.start();
            int iterationCount = 0;
            while ((is[0] == null) && (iterationCount++ < 5)){
                try {
                    t.join(1000L);
                } catch (InterruptedException localInterruptedException) {
                }
            }
            if (is[0] != null){
                continue;
            }
            try {
                t.interrupt();
                t.join();
            } catch (InterruptedException localInterruptedException1) {
            }
        }
        if (is[0] == null) {
            throw new Exception("Unable to download "+currentFile);
        }
        return is[0];
    }
    private void playToolBreakEffect(Tool tool, Tree tree, ItemStack axe, Player player, Block block){
        player.playEffect(EntityEffect.BREAK_EQUIPMENT_MAIN_HAND);
        for(Effect e : Option.EFFECTS.get(tool, tree)){
            if(e.location==Effect.EffectLocation.TOOL_BREAK){
                if(new Random().nextDouble()<e.chance)e.play(block);
            }
        }
    }
    private void log(Logger logger, CommandSender source, Level level, String message, Object... params) {
        logger.log(level, message, params);
        if(source!=null){
            ChatColor color = ChatColor.RESET;
            if(level==Level.INFO)return;
            if(level==Level.WARNING)color = ChatColor.YELLOW;
            if(level==Level.SEVERE)color = ChatColor.RED;
            for(int i = 0; i<params.length; i++){
                message = message.replace("{"+i+"}", Objects.toString(params[i]));
            }
            source.sendMessage("[TreeFeller] "+color+message);
        }
    }
}