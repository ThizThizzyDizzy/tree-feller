package com.thizthizzydizzy.treefeller;
import com.thizthizzydizzy.treefeller.compat.TestResult;
import com.thizthizzydizzy.treefeller.compat.TreeFellerCompat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.HashSet;
import java.util.List;
import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
public class TreeFeller extends JavaPlugin{
    public static ArrayList<Tool> tools = new ArrayList<>();
    public static ArrayList<Tree> trees = new ArrayList<>();
    public static ArrayList<Effect> effects = new ArrayList<>();
    public static HashMap<UUID, Cooldown> cooldowns = new HashMap<>();
    public HashSet<UUID> disabledPlayers = new HashSet<>();
    public ArrayList<FallingTreeBlock> fallingBlocks = new ArrayList<>();
    public ArrayList<Sapling> saplings = new ArrayList<>();
    boolean debug = false;
    private ArrayList<NaturalFall> naturalFalls = new ArrayList<>();
    private static final HashMap<Material, int[]> exp = new HashMap<>();
    static{//Perhaps this should be in the config rather than hard-coded...
        exp.put(Material.COAL_ORE, new int[]{0, 2});
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
        return fellTree(block, player, axe, player.getGameMode(), player.isSneaking());
    }
    public boolean fellTree(Block block, Player player, ItemStack axe, GameMode gamemode, boolean sneaking){
        return fellTree(block, player, axe, gamemode, sneaking, true)!=null;
    }
    /**
     * Fells a tree
     * @param block     the block that was broken
     * @param player    the player whose permissions are to be used. CAN BE NULL
     * @param axe       the tool used to break the block
     * @param gamemode  the player's gamemode
     * @param sneaking  weather or not the player was sneaking
     * @param dropItems weather or not to drop items
     * @return the items that would have been dropped. <b>This is not the actual dropped items, but possible dropped items</b> Returns null if the tree was not felled.
     */
    public ArrayList<ItemStack> fellTree(Block block, Player player, ItemStack axe, GameMode gamemode, boolean sneaking, boolean dropItems){
        if(gamemode==GameMode.SPECTATOR)return null;
        debugIndent = 0;
        Material material = block.getType();
        TREE:for(Tree tree : trees){
            if(!tree.trunk.contains(material))continue;
            TOOL:for(Tool tool : tools){
                if(player!=null&&disabledPlayers.contains(player.getUniqueId())){
                    debug(player, true, false, "toggle");
                    return null;
                }
                debug(player, "checking", false, trees.indexOf(tree), tools.indexOf(tool));
                if(tool.material!=Material.AIR&&axe.getType()!=tool.material)continue;
                for(Option o : Option.options){
                    DebugResult result = o.check(this, tool, tree, block, player, axe, gamemode, sneaking, dropItems);
                    if(result==null)continue;
                    debug(player, false, result);
                    if(!result.isSuccess())continue TOOL;
                }
                int durability = axe.getType().getMaxDurability()-axe.getDurability();
                int scanDistance = Option.SCAN_DISTANCE.get(tool, tree);
                HashMap<Integer, ArrayList<Block>> blocks = getBlocks(tree.trunk, block, scanDistance, true, false, false);//TODO what if the trunk is made of leaves?
                for(Option o : Option.options){
                    DebugResult result = o.checkTrunk(this, tool, tree, blocks, block);
                    if(result==null)continue;
                    debug(player, false, result);
                    if(!result.isSuccess())continue TOOL;
                }
                int total = getTotal(blocks);
                int minY = block.getY();
                for(int i : blocks.keySet()){
                    for(Block b : blocks.get(i)){
                        minY = Math.min(minY, b.getY());
                    }
                }
                int durabilityCost = total;
                if(Option.DAMAGE_MULT.globalValue!=null)durabilityCost*=Option.DAMAGE_MULT.globalValue;
                if(Option.DAMAGE_MULT.treeValues.containsKey(tree))durabilityCost*=Option.DAMAGE_MULT.treeValues.get(tree);
                if(Option.DAMAGE_MULT.toolValues.containsKey(tool))durabilityCost*=Option.DAMAGE_MULT.toolValues.get(tool);
                if(Option.RESPECT_UNBREAKING.get(tool, tree)){
                    durabilityCost/=(axe.getEnchantmentLevel(Enchantment.DURABILITY)+1);
                    if(durabilityCost<1)durabilityCost++;
                }
                if(axe.getType().getMaxDurability()==0)durabilityCost = 0;//there is no durability
                if(gamemode==GameMode.CREATIVE)durabilityCost = 0;//Don't cost durability
                if(Option.PREVENT_BREAKAGE.get(tool, tree)){
                    if(durabilityCost==durability){
                        debug(player, false, false, "prevent-breakage");
                        continue;
                    }
                    debug(player, false, true, "prevent-breakage-success");
                }
                if(durabilityCost>durability){
                    if(!Option.ALLOW_PARTIAL.get(tool, tree)){
                        debug(player, false, false, "durability-low", durability, durabilityCost);
                        continue;
                    }
                    debug(player, "partial", false);
                    durabilityCost = total = durability;
                }
                ArrayList<Integer> distances = new ArrayList<>(blocks.keySet());
                Collections.sort(distances);
                int leaves = 0;
                HashMap<Integer, ArrayList<Block>> allLeaves = new HashMap<>();
                FOR:for(int i : distances){
                    for(Block b : blocks.get(i)){
                        HashMap<Integer, ArrayList<Block>> someLeaves = getBlocksWithLeafCheck(tree.trunk, tree.leaves, b, Option.LEAF_RANGE.get(tool, tree), Option.DIAGONAL_LEAVES.get(tool, tree), Option.PLAYER_LEAVES.get(tool, tree), Option.IGNORE_LEAF_DATA.get(tool, tree), Option.FORCE_DISTANCE_CHECK.get(tool, tree));
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
                TestResult res = TreeFellerCompat.test(player, everything);
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
                debug(player, true, true, "success");
                if(Option.LEAVE_STUMP.get(tool, tree)){
                    for(int i : blocks.keySet()){
                        for(Iterator<Block> it = blocks.get(i).iterator(); it.hasNext();){
                            Block b = it.next();
                            if(b.getY()<block.getY())it.remove();
                        }
                    }
                }
                int lower = block.getY();
                for(Block b : toList(blocks)){
                    if(b.getY()<lower)lower = b.getY();
                }
                int lowest = lower;
                if(gamemode!=GameMode.CREATIVE){
                    if(axe.getType().getMaxDurability()>0){
                        axe.setDurability((short)(axe.getDurability()+durabilityCost));
                        if(durability==durabilityCost)axe.setAmount(0);
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
                        addSapling(b, Option.SAPLING.get(tool, tree), Option.SPAWN_SAPLINGS.get(tool, tree)!=2);
                    }
                }
                //now the blocks
                ArrayList<ItemStack> droppedItems = new ArrayList<>();
                final int t = total;
                long seed = new Random().nextLong();
                if(Option.CUTTING_ANIMATION.get(tool, tree)){
                    int delay = 0;
                    int ttl = t;
                    int tTL = t;
                    int Ttl = 0;
                    for(int i : distances){
                        int TTL = tTL - Ttl;
                        delay+=Option.ANIM_DELAY.get(tool, tree);
                        for(Block b : blocks.get(i)){
                            if(ttl<=0)break;
                            for(Block leaf : toList(getBlocksWithLeafCheck(tree.trunk, tree.leaves, b, Option.LEAF_RANGE.get(tool, tree), Option.DIAGONAL_LEAVES.get(tool, tree), Option.PLAYER_LEAVES.get(tool, tree), Option.IGNORE_LEAF_DATA.get(tool, tree), Option.FORCE_DISTANCE_CHECK.get(tool, tree)))){
                                droppedItems.addAll(getDrops(leaf, tool, tree, axe, new int[1]));
                            }
                            droppedItems.addAll(getDrops(b, tool, tree, axe, new int[1]));
                            ttl--;
                        }
                        new BukkitRunnable() {
                            @Override
                            public void run(){
                                int tTl = TTL;
                                for(Block b : blocks.get(i)){
                                    if(tTl<=0)break;
                                    for(Block leaf : toList(getBlocksWithLeafCheck(tree.trunk, tree.leaves, b, Option.LEAF_RANGE.get(tool, tree), Option.DIAGONAL_LEAVES.get(tool, tree), Option.PLAYER_LEAVES.get(tool, tree), Option.IGNORE_LEAF_DATA.get(tool, tree), Option.FORCE_DISTANCE_CHECK.get(tool, tree)))){
                                        breakBlock(dropItems, tree, tool, axe, leaf, block, lowest, player, seed);
                                    }
                                    breakBlock(dropItems, tree, tool, axe, b, block, lowest, player, seed);
                                    tTl--;
                                }
                                processNaturalFalls();
                            }
                        }.runTaskLater(this, delay);
                        Ttl += blocks.get(i).size();
                    }
                    Integer maxSaplings = Option.MAX_SAPLINGS.get(tool, tree);
                    if(maxSaplings!=null&&maxSaplings>=1){
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
                            for(Block leaf : toList(getBlocksWithLeafCheck(tree.trunk, tree.leaves, b, Option.LEAF_RANGE.get(tool, tree), Option.DIAGONAL_LEAVES.get(tool, tree), Option.PLAYER_LEAVES.get(tool, tree), Option.IGNORE_LEAF_DATA.get(tool, tree), Option.FORCE_DISTANCE_CHECK.get(tool, tree)))){
                                breakBlock(dropItems, tree, tool, axe, leaf, block, lowest, player, seed);
                            }
                            breakBlock(dropItems, tree, tool, axe, b, block, lowest, player, seed);
                            total--;
                        }
                    }
                    if(Option.SPAWN_SAPLINGS.get(tool, tree)>=1){
                        for(Block b : possibleSaplings.keySet()){
                            Sapling s = getSapling(b);
                            if(s!=null)s.place();
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
                return droppedItems;
            }
        }
        return null;
    }
    /**
     * Gets the size (number of logs) of a tree (Only for use by other plugins)
     * @param block the block that was broken
     * @param axe the tool used to break it
     * @return the size of the tree, in logs (0 if no tree can be felled)
     * @deprecated This does not behave exactly like fellTree, although no replacement is currently available
     * @throws UnsupportedOperationException when run, as it is highly broken
     */
    @Deprecated
    public int getTreeSize(Block block, ItemStack axe){
        throw new UnsupportedOperationException("This feature is not done yet!");//TODO fix this
    }
    private HashMap<Integer, ArrayList<Block>> getBlocks(ArrayList<Material> materialTypes, Block startingBlock, int maxDistance, boolean diagonal, boolean playerLeaves, boolean ignoreLeafData){
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
                                    if(!ignoreLeafData){
                                        if(block.getBlockData() instanceof Leaves){
                                            Leaves oldLeaf = (Leaves)block.getBlockData();
                                            if(newLeaf.getDistance()<=oldLeaf.getDistance()){
                                                continue;
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
                                    if(newLeaf.getDistance()<=oldLeaf.getDistance()){
                                        continue;
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
        }
        return results;
    }
    private HashMap<Integer, ArrayList<Block>> getBlocksWithLeafCheck(ArrayList<Material> trunk, ArrayList<Material> leaves, Block startingBlock, int maxDistance, boolean diagonal, boolean playerLeaves, boolean ignoreLeafData, boolean forceDistanceCheck){
        HashMap<Integer, ArrayList<Block>> blocks = getBlocks(leaves, startingBlock, maxDistance, diagonal, playerLeaves, ignoreLeafData);
        if(forceDistanceCheck)leafCheck(blocks, trunk, leaves, diagonal, playerLeaves, ignoreLeafData);
        return blocks;
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
    private int distance(Block from, ArrayList<Material> to, ArrayList<Material> materialTypes, int max, boolean diagonal, boolean playerLeaves){
        materialTypes.add(from.getType());
        materialTypes.addAll(to);
        for(int d = 0; d<max; d++){
            for(Block b : toList(getBlocks(materialTypes, from, d, diagonal, playerLeaves, true))){
                if(to.contains(b.getType()))return d;
            }
        }
        return max;
    }
    @Override
    public void onEnable(){
        PluginDescriptionFile pdfFile = getDescription();
        Logger logger = getLogger();
        TreeFellerCompat.init();
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
    @Override
    public void onDisable(){
        PluginDescriptionFile pdfFile = getDescription();
        Logger logger = getLogger();
        logger.log(Level.INFO, "{0} has been disabled! (Version {1}) by ThizThizzyDizzy", new Object[]{pdfFile.getName(), pdfFile.getVersion()});
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
    public void addSapling(Block b, Material sapling, boolean autofill){
        saplings.add(new Sapling(b, sapling, autofill, System.currentTimeMillis()));
    }
    public void reload(){
        Logger logger = getLogger();
        trees.clear();
        tools.clear();
        effects.clear();
        saplings.clear();
        fallingBlocks.clear();
        cooldowns.clear();
        //<editor-fold defaultstate="collapsed" desc="Effects">
        ArrayList<Object> effects = null;
        try{
            effects = new ArrayList<>(getConfig().getList("effects"));
        }catch(NullPointerException ex){
            if(getConfig().get("effects")!=null)logger.log(Level.WARNING, "Failed to load effects!");
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
                    if(Option.STARTUP_LOGS.isTrue())effect.print(logger);
                    this.effects.add(effect);
                }else if(o instanceof String){
                    Material m = Material.matchMaterial((String)o);
                    if(m==null){
                        logger.log(Level.WARNING, "Unknown enchantment: {0}; Skipping...", o);
                    }
                    Tool tool = new Tool(m);
                    if(Option.STARTUP_LOGS.isTrue())tool.print(logger);
                    this.tools.add(tool);
                }else{
                    logger.log(Level.INFO, "Unknown tool declaration: {0} | {1}", new Object[]{o.getClass().getName(), o.toString()});
                }
            }
        }
//</editor-fold>
        for(Option option : Option.options){
            if(option.global){
                option.setValue(option.loadFromConfig(getConfig()));
            }
        }
        for(Message message : Message.messages){
            message.load(getConfig());
        }
        if(Option.STARTUP_LOGS.isTrue()){
            logger.log(Level.INFO, "Server version: {0}", Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].substring(1));
            logger.log(Level.INFO, "Loaded global values:");
            for(Option option : Option.options){
                Object value = option.getValue();
                if(value!=null){
                    logger.log(Level.INFO, "- {0}: {1}", new Object[]{option.name, option.makeReadable(value)});
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
                        String s = ((String)key).toLowerCase().replace("-", "").replace("_", "").replace(" ", "");
                        boolean found = false;
                        for(Option option : Option.options){
                            if(!option.tree)continue;
                            if(option.getLocalName().equals(s)){
                                found = true;
                                option.setValue(tree, option.load(map.get(key)));
                            }
                        }
                        if(!found)logger.log(Level.WARNING, "Found unknown tree option: {0}", key);
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
                    logger.log(Level.WARNING, "Cannot load tree: {0}", o);
                    continue;
                }
                Tree tree = new Tree(trunk, leaves);
                if(Option.STARTUP_LOGS.isTrue())tree.print(logger);
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
                Material type = Material.matchMaterial(typ.trim());
                if(type==null){
                    logger.log(Level.WARNING, "Unknown tool material: {0}! Skipping...", map.get("type"));
                    continue;
                }
                Tool tool = new Tool(type);
                for(Object key : map.keySet()){
                    if(key.equals("type"))continue;//already got that
                    if(!(key instanceof String)){
                        logger.log(Level.WARNING, "Unknown tool property: {0}; Skipping...", key);
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
                    if(!found)logger.log(Level.WARNING, "Found unknown tool option: {0}", key);
                }
                if(Option.STARTUP_LOGS.isTrue())tool.print(logger);
                this.tools.add(tool);
            }else if(o instanceof String){
                Material m = Material.matchMaterial((String)o);
                if(m==null){
                    logger.log(Level.WARNING, "Unknown enchantment: {0}; Skipping...", o);
                }
                Tool tool = new Tool(m);
                if(Option.STARTUP_LOGS.isTrue())tool.print(logger);
                this.tools.add(tool);
            }else{
                logger.log(Level.INFO, "Unknown tool declaration: {0} | {1}", new Object[]{o.getClass().getName(), o.toString()});
            }
        }
//</editor-fold>
        TreeFellerCompat.reload();
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
    private void breakBlock(boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed){
        ArrayList<Material> overridables = new ArrayList<>(Option.OVERRIDABLES.get(tool, tree));
        ArrayList<Effect> effects = new ArrayList<>();
        boolean isLeaf = !tree.trunk.contains(block.getType());
        for(Effect e : Option.EFFECTS.get(tool, tree)){
            if(e.location==Effect.EffectLocation.TREE)effects.add(e);
            if(isLeaf){
                if(e.location==Effect.EffectLocation.LEAVES)effects.add(e);
            }else{
                if(e.location==Effect.EffectLocation.LOGS)effects.add(e);
            }
        }
        FellBehavior behavior = isLeaf?Option.LEAF_BEHAVIOR.get(tool, tree):Option.LOG_BEHAVIOR.get(tool, tree);
//        double dropChance = dropItems?(isLeaf?Option.LEAF_DROP_CHANCE.get(tool, tree):Option.LOG_DROP_CHANCE.get(tool, tree)):0;
        double directionalFallVelocity = Option.DIRECTIONAL_FALL_VELOCITY.get(tool, tree);
        double verticalFallVelocity = Option.VERTICAL_FALL_VELOCITY.get(tool, tree);
        double randomFallVelocity = Option.RANDOM_FALL_VELOCITY.get(tool, tree);
        boolean rotate = Option.ROTATE_LOGS.get(tool, tree);
        DirectionalFallBehavior directionalFallBehavior = Option.DIRECTIONAL_FALL_BEHAVIOR.get(tool, tree);
        boolean lockCardinal = Option.LOCK_FALL_CARDINAL.get(tool, tree);
        ArrayList<Modifier> modifiers = new ArrayList<>();
        if(behavior==FellBehavior.FALL||behavior==FellBehavior.FALL_HURT||behavior==FellBehavior.NATURAL){
            TreeFellerCompat.removeBlock(player, block);
        }else{
            TreeFellerCompat.breakBlock(tree, tool, player, axe, block, modifiers);
        }
        switch(behavior){
            case INVENTORY:
                if(player!=null){
                    if(dropItems){
                        int[] xp = new int[]{0};
                        for(ItemStack s : getDropsWithBonus(block, tool, tree, axe, xp, modifiers)){
                            for(ItemStack st : player.getInventory().addItem(s).values()){
                                block.getWorld().dropItemNaturally(block.getLocation(), st);
                            }
                        }
                        player.setTotalExperience(player.getTotalExperience()+xp[0]);
                    }
                    block.setType(Material.AIR);
                    break;
                }
            case BREAK:
                if(dropItems){
                    int[] xp = new int[]{0};
                    for(ItemStack s : getDropsWithBonus(block, tool, tree, axe, xp, modifiers)){
                        block.getWorld().dropItemNaturally(block.getLocation(), s);
                    }
                    dropExp(block.getWorld(), block.getLocation(), xp[0]);
                }
                block.setType(Material.AIR);
                break;
            case FALL_HURT:
            case FALL:
            case FALL_BREAK:
            case FALL_HURT_BREAK:
            case FALL_INVENTORY:
            case FALL_HURT_INVENTORY:
                FallingBlock falling = block.getWorld().spawnFallingBlock(block.getLocation().add(.5,.5,.5), block.getBlockData());
                falling.addScoreboardTag("tree_feller");
                Vector v = falling.getVelocity();
                if(directionalFallVelocity>0){
                    v.add(directionalFallBehavior.getDirectionalVel(seed, player, block, lockCardinal, directionalFallVelocity));
                }
                v.add(new Vector((Math.random()*2-1)*randomFallVelocity, verticalFallVelocity, (Math.random()*2-1)*randomFallVelocity));
                falling.setVelocity(v);
                falling.setHurtEntities(behavior.name().contains("HURT"));
                boolean doBreak = behavior.name().contains("BREAK");
                Player inv = null;
                if(behavior.name().contains("INVENTORY")){
                    if(player==null)doBreak = true;
                    else inv = player;
                }
                RotationData rot = null;
                if(falling.getBlockData() instanceof Orientable&&rotate){
                    rot = new RotationData((Orientable)falling.getBlockData(), origin);
                }
                block.setType(Material.AIR);
                fallingBlocks.add(new FallingTreeBlock(falling, tool, tree, axe, doBreak, inv, rot, dropItems, modifiers));
                break;
            case NATURAL:
                v = directionalFallBehavior.getDirectionalVel(seed, player, block, lockCardinal, directionalFallVelocity).normalize();
                naturalFalls.add(new NaturalFall(player, v, origin, block, block.getY()-lowest, rotate, overridables));
                block.setType(Material.AIR);
                break;
            default:
                throw new IllegalArgumentException("Invalid block behavior: "+behavior);
        }
        for(Effect e : effects){
            if(new Random().nextDouble()<e.chance)e.play(block);
        }
    }
    public int randbetween(int[] minmax){
        return randbetween(minmax[0], minmax[1]);
    }
    public int randbetween(int min, int max){
        return new Random().nextInt(max-min+1)+min;
    }
    private Collection<? extends ItemStack> getDropsWithBonus(Block block, Tool tool, Tree tree, ItemStack axe, int[] xp, List<Modifier> modifiers){
        if(xp.length!=1)throw new IllegalArgumentException("xp must be an array of size 1!");
        ArrayList<ItemStack> drops = new ArrayList<>();
        double dropChance = tree.trunk.contains(block.getType())?Option.LOG_DROP_CHANCE.get(tool, tree):Option.LEAF_DROP_CHANCE.get(tool, tree);
        for(Modifier mod : modifiers){
            switch(mod.type){
                case LOG_MULT:
                    if(tree.trunk.contains(block.getType()))dropChance*=mod.value;
                    break;
                case LEAF_MULT:
                    if(!tree.trunk.contains(block.getType()))dropChance*=mod.value;
                    break;
                case DROPS_MULT:
                    dropChance*=mod.value;
                    break;
                default:
                    getLogger().log(Level.WARNING, "Unhandled modifier: {0}! Please report this on github! (https://github.com/ThizThizzyDizzy/tree-feller)!", mod.toString());
                    break;
            }
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
                int[] blockXP = new int[1];
                drops.addAll(getDrops(block, tool, tree, axe, blockXP));
                xp[0]+=blockXP[0];
            }
        }
        return drops;
    }
    private Collection<? extends ItemStack> getDrops(Block block, Tool tool, Tree tree, ItemStack axe, int[] xp){
        if(xp.length!=1)throw new IllegalArgumentException("blockXP must be an array of length 1!");
        if(exp.containsKey(block.getType())){
            xp[0] += randbetween(exp.get(block.getType()));
        }
        ArrayList<ItemStack> drops = new ArrayList<>();
        boolean convert = Option.CONVERT_WOOD_TO_LOG.get(tool, tree);
        boolean fortune, silk;
        if(tree.trunk.contains(block.getType())){
            fortune = Option.LOG_FORTUNE.get(tool, tree);
            silk = Option.LOG_SILK_TOUCH.get(tool, tree);
        }else{
            fortune = Option.LEAF_FORTUNE.get(tool, tree);
            silk = Option.LEAF_SILK_TOUCH.get(tool, tree);
        }
        Material type = block.getType();
        drops.addAll(block.getDrops());
        for(Iterator<ItemStack> it = drops.iterator(); it.hasNext();){
            ItemStack next = it.next();
            if(next.getType().isAir())it.remove();//don't try to drop air
        }
        if(axe.containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS)&&fortune)applyFortune(type, drops, axe, axe.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS), xp);
        if(axe.containsEnchantment(Enchantment.SILK_TOUCH)&&silk)applySilkTouch(type, drops, axe, axe.getEnchantmentLevel(Enchantment.SILK_TOUCH), xp);
        if(convert){
            for(ItemStack s : drops){
                if(s.getType().name().endsWith("_WOOD")){
                    s.setType(Material.matchMaterial(s.getType().name().replace("_WOOD", "_LOG")));
                }
                if(s.getType().name().endsWith("_HYPHAE")){
                    s.setType(Material.matchMaterial(s.getType().name().replace("_HYPHAE", "_STEM")));
                }
            }
        }
        return drops;
    }
    //Bukkit API lacks fortune/silk touch handling, so I have to do it the hard way...
    private void applyFortune(Material type, ArrayList<ItemStack> drops, ItemStack axe, int enchantmentLevel, int[] xp){
        if(enchantmentLevel==0)return;
        switch(type){
            case COAL_ORE:
            case DIAMOND_ORE:
            case EMERALD_ORE:
            case LAPIS_ORE:
            case NETHER_QUARTZ_ORE:
            case REDSTONE_ORE://incorrect
            case NETHER_GOLD_ORE://might be incorrect
                ArrayList<Integer> mults = new ArrayList<>();
                mults.add(1);
                mults.add(1);
                for(int i = 0; i<enchantmentLevel; i++){
                    mults.add(i+2);
                }
                Random rand = new Random();
                int mult = mults.get(rand.nextInt(mults.size()));
                for(ItemStack s : drops){
                    s.setAmount(s.getAmount()*mult);
                }
                break;
            case OAK_LEAVES:
            case BIRCH_LEAVES:
            case SPRUCE_LEAVES:
            case ACACIA_LEAVES:
            case DARK_OAK_LEAVES:
            case JUNGLE_LEAVES:
                Random r = new Random();
                int fortune = Math.min(4,enchantmentLevel);
                Material sapling = Material.matchMaterial(type.name().replace("LEAVES", "SAPLING"));
                if(type==Material.JUNGLE_LEAVES){
                    switch(fortune){
                        case 1:
                            if(r.nextDouble()<.0023)drops.add(new ItemStack(sapling));
                            break;
                        case 2:
                            if(r.nextDouble()<.00625)drops.add(new ItemStack(sapling));
                            break;
                        case 3:
                        case 4:
                            if(r.nextDouble()<.0167)drops.add(new ItemStack(sapling));
                            break;
                    }
                }else{
                    switch(fortune){
                        case 1:
                            if(r.nextDouble()<.0125)drops.add(new ItemStack(sapling));
                            break;
                        case 2:
                            if(r.nextDouble()<.0333)drops.add(new ItemStack(sapling));
                            break;
                        case 3:
                        case 4:
                            if(r.nextDouble()<.05)drops.add(new ItemStack(sapling));
                            break;
                    }
                }
                switch(fortune){
                    case 1:
                        if(r.nextDouble()<.0022)drops.add(new ItemStack(Material.STICK));
                        break;
                    case 2:
                        if(r.nextDouble()<.005)drops.add(new ItemStack(Material.STICK));
                        break;
                    case 3:
                        if(r.nextDouble()<.0133)drops.add(new ItemStack(Material.STICK));
                        break;
                    case 4:
                        if(r.nextDouble()<.08)drops.add(new ItemStack(Material.STICK));//why, minecraft, why?
                        break;
                }
                if(type==Material.OAK_LEAVES){
                    switch(fortune){
                        case 1:
                            if(r.nextDouble()<.00056)drops.add(new ItemStack(Material.APPLE));
                            break;
                        case 2:
                            if(r.nextDouble()<.00125)drops.add(new ItemStack(Material.APPLE));
                            break;
                        case 3:
                        case 4:
                            if(r.nextDouble()<.00333)drops.add(new ItemStack(Material.APPLE));
                            break;
                    }
                }
                break;
        }
    }
    private void applySilkTouch(Material type, ArrayList<ItemStack> drops, ItemStack axe, int enchantmentLevel, int[] xp){
        if(enchantmentLevel==0)return;
        switch(type){
            case BEEHIVE:
            case BEE_NEST:
            case CAMPFIRE:
            case BLUE_ICE:
            case BOOKSHELF:
            case CLAY:
            case BUBBLE_CORAL:
            case HORN_CORAL:
            case FIRE_CORAL:
            case TUBE_CORAL:
            case BRAIN_CORAL:
            case BUBBLE_CORAL_FAN:
            case HORN_CORAL_FAN:
            case FIRE_CORAL_FAN:
            case TUBE_CORAL_FAN:
            case BRAIN_CORAL_FAN:
            case BUBBLE_CORAL_WALL_FAN:
            case HORN_CORAL_WALL_FAN:
            case FIRE_CORAL_WALL_FAN:
            case TUBE_CORAL_WALL_FAN:
            case BRAIN_CORAL_WALL_FAN:
            case GLASS:
            case BLUE_STAINED_GLASS:
            case RED_STAINED_GLASS:
            case ORANGE_STAINED_GLASS:
            case PINK_STAINED_GLASS:
            case YELLOW_STAINED_GLASS:
            case LIME_STAINED_GLASS:
            case GREEN_STAINED_GLASS:
            case CYAN_STAINED_GLASS:
            case LIGHT_BLUE_STAINED_GLASS:
            case MAGENTA_STAINED_GLASS:
            case PURPLE_STAINED_GLASS:
            case GRAY_STAINED_GLASS:
            case LIGHT_GRAY_STAINED_GLASS:
            case BLACK_STAINED_GLASS:
            case WHITE_STAINED_GLASS:
            case BROWN_STAINED_GLASS:
            case BLUE_STAINED_GLASS_PANE:
            case RED_STAINED_GLASS_PANE:
            case ORANGE_STAINED_GLASS_PANE:
            case PINK_STAINED_GLASS_PANE:
            case YELLOW_STAINED_GLASS_PANE:
            case LIME_STAINED_GLASS_PANE:
            case GREEN_STAINED_GLASS_PANE:
            case CYAN_STAINED_GLASS_PANE:
            case LIGHT_BLUE_STAINED_GLASS_PANE:
            case MAGENTA_STAINED_GLASS_PANE:
            case PURPLE_STAINED_GLASS_PANE:
            case GRAY_STAINED_GLASS_PANE:
            case LIGHT_GRAY_STAINED_GLASS_PANE:
            case BLACK_STAINED_GLASS_PANE:
            case WHITE_STAINED_GLASS_PANE:
            case BROWN_STAINED_GLASS_PANE:
            case GLOWSTONE:
            case GRASS_BLOCK:
            case GRAVEL:
            case ICE:
            case OAK_LEAVES:
            case BIRCH_LEAVES:
            case SPRUCE_LEAVES:
            case JUNGLE_LEAVES:
            case ACACIA_LEAVES:
            case DARK_OAK_LEAVES:
            case MELON:
            case MUSHROOM_STEM:
            case BROWN_MUSHROOM_BLOCK:
            case RED_MUSHROOM_BLOCK:
            case MYCELIUM:
            case PODZOL:
            case SEA_LANTERN:
            case TURTLE_EGG:
            case SOUL_CAMPFIRE:
                drops.clear();
                drops.add(new ItemStack(type));
                xp[0] = 0;
                break;
            //pickaxe only (conditional too!)
            case COAL_ORE:
            case BUBBLE_CORAL_BLOCK:
            case HORN_CORAL_BLOCK:
            case FIRE_CORAL_BLOCK:
            case TUBE_CORAL_BLOCK:
            case BRAIN_CORAL_BLOCK:
            case DIAMOND_ORE:
            case EMERALD_ORE:
            case ENDER_CHEST:
            case LAPIS_ORE:
            case NETHER_QUARTZ_ORE:
            case REDSTONE_ORE:
            case NETHER_GOLD_ORE:
            case GILDED_BLACKSTONE:
            case STONE:
                if(axe.getType().name().toLowerCase().contains("pickaxe")){
                    xp[0] = 0;
                    if(drops.isEmpty())return;
                    drops.clear();
                    drops.add(new ItemStack(type));
                }
                break;
            //shovel only
            case SNOW:
            case SNOW_BLOCK:
                if(axe.getType().name().toLowerCase().contains("shovel")){
                    drops.clear();
                    drops.add(new ItemStack(type));
                    xp[0] = 0;
                }
                break;
        }
    }
    private void leafCheck(HashMap<Integer, ArrayList<Block>> someLeaves, ArrayList<Material> trunk, ArrayList<Material> leaves, Boolean diagonal, Boolean playerLeaves, Boolean ignoreLeafData){
        if(ignoreLeafData)return;
        ArrayList<Integer> ints = new ArrayList<>();
        ints.addAll(someLeaves.keySet());
        Collections.sort(ints);
        for(int d : ints){
            for(Iterator<Block> it = someLeaves.get(d).iterator(); it.hasNext();){
                Block leaf = it.next();
                if(distance(leaf, trunk, leaves, d, diagonal, playerLeaves)<d){
                    it.remove();
                }
            }
        }
    }
    private static class RotationData{
        private final Axis axis;
        private final int x;
        private final int y;
        private final int z;
        public RotationData(Orientable data, Block origin){
            this(data.getAxis(), origin.getX(), origin.getY(), origin.getZ());
        }
        public RotationData(Axis axis, int x, int y, int z){
            this.axis = axis;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
    public class FallingTreeBlock{
        public FallingBlock entity;
        private final Tool tool;
        private final Tree tree;
        private final ItemStack axe;
        private final boolean doBreak;
        private final Player player;
        private final RotationData rot;
        private final boolean dropItems;
        private final List<Modifier> modifiers;
        public FallingTreeBlock(FallingBlock entity, Tool tool, Tree tree, ItemStack axe, boolean doBreak, Player player, RotationData rot, boolean dropItems, List<Modifier> modifiers){
            this.entity = entity;
            this.tool = tool;
            this.tree = tree;
            this.axe = axe;
            this.doBreak = doBreak;
            this.player = player;
            this.rot = rot;
            this.dropItems = dropItems;
            this.modifiers = modifiers;
        }
        public void land(EntityChangeBlockEvent event){
            if(event.getTo()==Material.AIR)return;
            if(event.getBlock().getRelative(0, -1, 0).isPassable()){
                event.setCancelled(true);
                FallingBlock falling = event.getBlock().getWorld().spawnFallingBlock(event.getBlock().getLocation().add(.5,.5,.5), event.getBlockData());
                entity = falling;
                falling.setVelocity(new Vector(0, event.getEntity().getVelocity().getY(), 0));
                falling.setHurtEntities(((FallingBlock)event.getEntity()).canHurtEntities());
                for(String s : event.getEntity().getScoreboardTags()){
                    falling.addScoreboardTag(s);
                }
            }else{
                int[] xp = new int[]{0};
                if(!dropItems){
                    event.setCancelled(true);
                    fallingBlocks.remove(this);
                    return;
                }
                ArrayList<ItemStack> drops = getDrops(event.getTo(), tool, tree, axe, event.getBlock(), xp, modifiers);
                if(doBreak){
                    event.setCancelled(true);
                    for(ItemStack drop : drops){
                        //fake item
                        ItemStack fakeDrop = new ItemStack(drop);
                        ItemMeta meta = fakeDrop.getItemMeta();
                        meta.getPersistentDataContainer().set(new NamespacedKey(TreeFeller.this, "fakeItemTag"), PersistentDataType.STRING, UUID.randomUUID().toString());
                        fakeDrop.setItemMeta(meta);
                        Item fake = event.getBlock().getWorld().dropItemNaturally(event.getEntity().getLocation(), fakeDrop);
                        fake.setTicksLived(5960);//40 ticks to despawn
                        fake.setPickupDelay(32767);//cannot be picked up
                        //real item
                        Item item = event.getBlock().getWorld().dropItemNaturally(event.getEntity().getLocation().add(randbetween(-8, 8), randbetween(1000, 10000), randbetween(-8, 8)), drop);//If I spawn items near others, some will get deleted
                        Vector velocity = item.getVelocity();
                        new BukkitRunnable() {
                            @Override
                            public void run(){
                                item.teleport(event.getEntity().getLocation());
                                item.setVelocity(velocity);
                            }
                        }.runTaskLater(TreeFeller.this, 40);
                    }
                    dropExp(event.getBlock().getWorld(), event.getEntity().getLocation(), xp[0]);
                }
                if(player!=null){
                    event.setCancelled(true);
                    for(ItemStack drop : drops){
                        for(ItemStack stack : player.getInventory().addItem(drop).values())event.getBlock().getWorld().dropItemNaturally(event.getEntity().getLocation(), stack);
                    }
                    player.setTotalExperience(player.getTotalExperience()+xp[0]);
                }
                fallingBlocks.remove(this);
                if(event.isCancelled())return;
                if(rot!=null){
                    Axis axis = rot.axis;
                    double xDiff = Math.abs(rot.x-event.getEntity().getLocation().getX());
                    double yDiff = Math.abs(rot.y-event.getEntity().getLocation().getY());
                    double zDiff = Math.abs(rot.z-event.getEntity().getLocation().getZ());
                    Axis newAxis = Axis.Y;
                    if(Math.max(Math.max(xDiff, yDiff), zDiff)==xDiff)newAxis = Axis.X;
                    if(Math.max(Math.max(xDiff, yDiff), zDiff)==zDiff)newAxis = Axis.Z;
                    if(newAxis==Axis.X){
                        switch(axis){
                            case X:
                                axis = Axis.Y;
                                break;
                            case Y:
                                axis = Axis.X;
                                break;
                            case Z:
                                break;
                        }
                    }
                    if(newAxis==Axis.Z){
                        switch(axis){
                            case X:
                                break;
                            case Y:
                                axis = Axis.Z;
                                break;
                            case Z:
                                axis = Axis.X;
                                break;
                        }
                    }
                    Orientable data = (Orientable)event.getBlockData();
                    data.setAxis(axis);
                    event.setCancelled(true);
                    event.getBlock().setType(event.getTo());
                    event.getBlock().setBlockData(data);
                }
            }
        }
    }
    private ArrayList<ItemStack> getDrops(Material m, Tool tool, Tree tree, ItemStack axe, Block location, int[] xp, List<Modifier> modifiers){
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
    private class NaturalFall{
        private static final double interval = 0.1;
        private final Player player;
        private final Vector v;
        private final Block origin;
        private final Block block;
        private final int height;
        private final Material material;
        private Axis axis = null;
        private final ArrayList<Material> overridables;
        private boolean fell = false;
        public NaturalFall(Player player, Vector v, Block origin, Block block, int height, boolean rotate, ArrayList<Material> overridables){
            this.player = player;
            this.v = v.multiply(interval);
            this.origin = origin;
            this.block = block;
            this.height = height;
            this.material = block.getType();
            if(rotate&&block.getBlockData() instanceof Orientable){
                axis = ((Orientable)block.getBlockData()).getAxis();
            }
            this.overridables = overridables;
        }
        public void fall(){
            if(fell)return;
            fell = true;
            double dist = 0;
            Block target = block;
            Location l = block.getLocation().add(.5,.5,.5);
            while(dist<height){
                dist+=interval;
                l = l.add(v);
                Block b = l.getBlock();
                triggerNaturalFall(b);
                if(overridables.contains(b.getType()))target = b;
                else break;
            }
            Block b;
            while(overridables.contains((b = target.getRelative(0, -1, 0)).getType())){
                triggerNaturalFall(b);
                target = b;
            }
            target.setType(material);
            if(axis!=null){
                double xDiff = Math.abs(origin.getX()-target.getX());
                double yDiff = Math.abs(origin.getY()-target.getY());
                double zDiff = Math.abs(origin.getZ()-target.getZ());
                Axis newAxis = Axis.Y;
                if(Math.max(Math.max(xDiff, yDiff), zDiff)==xDiff)newAxis = Axis.X;
                if(Math.max(Math.max(xDiff, yDiff), zDiff)==zDiff)newAxis = Axis.Z;
                if(newAxis==Axis.X){
                    switch(axis){
                        case X:
                            axis = Axis.Y;
                            break;
                        case Y:
                            axis = Axis.X;
                            break;
                        case Z:
                            break;
                    }
                }
                if(newAxis==Axis.Z){
                    switch(axis){
                        case X:
                            break;
                        case Y:
                            axis = Axis.Z;
                            break;
                        case Z:
                            axis = Axis.X;
                            break;
                    }
                }
                BlockData data = target.getBlockData();
                ((Orientable)data).setAxis(axis);
                target.setBlockData(data);
            }
            TreeFellerCompat.addBlock(player, target);
        }
        private void triggerNaturalFall(Block b){
            for(NaturalFall fall : naturalFalls){
                if(fall==this)continue;
                if(fall.block.equals(b))fall.fall();
            }
        }
    }
    private void processNaturalFalls(){
        for(NaturalFall fall : naturalFalls){
            fall.fall();
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
    private void dropExp(World world, Location location, int xp){
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
}