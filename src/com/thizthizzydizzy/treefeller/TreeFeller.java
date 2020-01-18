package com.thizthizzydizzy.treefeller;
import com.thizthizzydizzy.treefeller.compat.TestResult;
import com.thizthizzydizzy.treefeller.compat.TreeFellerCompat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
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
    public static ArrayList<Tool> tools = new ArrayList<>();
    public static ArrayList<Tree> trees = new ArrayList<>();
    public static ArrayList<Effect> effects = new ArrayList<>();
    public static HashMap<UUID, Cooldown> cooldowns = new HashMap<>();
    public ArrayList<UUID> fallingBlocks = new ArrayList<>();
    public ArrayList<Sapling> saplings = new ArrayList<>();
    boolean debug = false;
    private ArrayList<NaturalFall> naturalFalls = new ArrayList<>();
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
     * @return the items that would have been dropped, only <code>dropItems</code> is false. Returns null if the tree was not felled.
     */
    public ArrayList<ItemStack> fellTree(Block block, Player player, ItemStack axe, GameMode gamemode, boolean sneaking, boolean dropItems){
        if(gamemode==GameMode.SPECTATOR)return null;
        debugIndent = 0;
        Material material = block.getType();
        TREE:for(Tree tree : trees){
            if(!tree.trunk.contains(material))continue;
            TOOL:for(Tool tool : tools){
                if(tool.material!=Material.AIR&&axe.getType()!=tool.material)continue;
                debug(player, "Checking tree #"+trees.indexOf(tree)+" with tool #"+tools.indexOf(tool)+"...", true);
                for(Option o : Option.options){
                    DebugResult result = o.check(tool, tree, block, player, axe, gamemode, sneaking, dropItems);
                    if(result==null)continue;
                    debug(player, false, result.success, result.message);
                    if(!result.success)continue TOOL;
                }
                int durability = axe.getType().getMaxDurability()-axe.getDurability();
                int scanDistance = Option.SCAN_DISTANCE.get(tool, tree);
                HashMap<Integer, ArrayList<Block>> blocks = getBlocks(tree.trunk, block, scanDistance, true, false, false);//TODO what if the trunk is made of leaves?
                for(Option o : Option.options){
                    DebugResult result = o.checkTrunk(tool, tree, blocks, block);
                    if(result==null)continue;
                    debug(player, false, result.success, result.message);
                    if(!result.success)continue TOOL;
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
                if(gamemode==GameMode.CREATIVE)durabilityCost = 0;//Don't cost durability
                if(durabilityCost>durability){
                    if(!Option.ALLOW_PARTIAL.get(tool, tree)){
                        debug(player, false, false, "Tool durability is too low: "+durability+"<"+durabilityCost);
                        continue;
                    }
                    debug(player, "Tool is cutting partial tree!", false);
                    durabilityCost = total = durability;
                }
                ArrayList<Integer> distances = new ArrayList<>(blocks.keySet());
                Collections.sort(distances);
                int leaves = 0;
                HashMap<Integer, ArrayList<Block>> allLeaves = new HashMap<>();
                FOR:for(int i : distances){
                    for(Block b : blocks.get(i)){
                        HashMap<Integer, ArrayList<Block>> someLeaves = getBlocks(tree.leaves, b, Option.LEAF_RANGE.get(tool, tree), Option.DIAGONAL_LEAVES.get(tool, tree), Option.PLAYER_LEAVES.get(tool, tree), Option.IGNORE_LEAF_DATA.get(tool, tree));
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
                    debug(player, false, false, "This tree is protected by "+res.plugin+" at "+res.block.getX()+" "+res.block.getY()+" "+res.block.getZ());
                    continue TREE;
                }
                for(Option o : Option.options){
                    DebugResult result = o.checkTree(tool, tree, blocks, leaves);
                    if(result==null)continue;
                    debug(player, false, result.success, result.message);
                    if(!result.success)continue TOOL;
                }
                debug(player, true, true, "Success! Felling tree...");
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
                    while(possibleSaplings.size()>Option.MAX_SAPLINGS.get(tool, tree)){
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
                        if(!dropItems){
                            for(Block b : blocks.get(i)){
                                if(ttl<=0)break;
                                for(Block leaf : toList(getBlocks(tree.leaves, b, Option.LEAF_RANGE.get(tool, tree), Option.DIAGONAL_LEAVES.get(tool, tree), Option.PLAYER_LEAVES.get(tool, tree), Option.IGNORE_LEAF_DATA.get(tool, tree)))){
                                    droppedItems.addAll(Option.LEAF_ENCHANTMENTS.get(tool, tree)?leaf.getDrops(axe):leaf.getDrops());
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
                                    for(Block leaf : toList(getBlocks(tree.leaves, b, Option.LEAF_RANGE.get(tool, tree), Option.DIAGONAL_LEAVES.get(tool, tree), Option.PLAYER_LEAVES.get(tool, tree), Option.IGNORE_LEAF_DATA.get(tool, tree)))){
                                        if(dropItems){
                                            breakLeaf(tree, tool, axe, leaf, block, lowest, player, seed, toList(blocks));
                                        }else leaf.setType(Material.AIR);
                                    }
                                    if(dropItems)breakLog(tree, tool, axe, b, block, lowest, player, seed, toList(blocks));
                                    else b.setType(Material.AIR);
                                    tTl--;
                                }
                                for(NaturalFall fall : naturalFalls){
                                    fall.fall();
                                }
                                naturalFalls.clear();
                            }
                        }.runTaskLater(this, delay);
                        Ttl += blocks.get(i).size();
                    }
                    if(Option.MAX_SAPLINGS.get(tool, tree)>=1){
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
                                for(Block leaf : toList(getBlocks(tree.leaves, b, Option.LEAF_RANGE.get(tool, tree), Option.DIAGONAL_LEAVES.get(tool, tree), Option.PLAYER_LEAVES.get(tool, tree), Option.IGNORE_LEAF_DATA.get(tool, tree)))){
                                    if(dropItems){
                                        breakLeaf(tree, tool, axe, leaf, block, lowest, player, seed, toList(blocks));
                                    }else{
                                        droppedItems.addAll(Option.LEAF_ENCHANTMENTS.get(tool, tree)?leaf.getDrops(axe):leaf.getDrops());
                                        leaf.setType(Material.AIR);
                                    }
                                }
                            if(dropItems)breakLog(tree, tool, axe, b, block, lowest, player, seed, toList(blocks));
                            else{
                                droppedItems.addAll(b.getDrops(axe));
                                b.setType(Material.AIR);
                            }
                            total--;
                        }
                    }
                    if(Option.SPAWN_SAPLINGS.get(tool, tree)>=1){
                        for(Block b : possibleSaplings.keySet()){
                            Sapling s = getSapling(b);
                            if(s!=null)s.place();
                        }
                    }
                    for(NaturalFall fall : naturalFalls){
                        fall.fall();
                    }
                    naturalFalls.clear();
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
                option.setValue(option.loadFromconfig(getConfig()));
            }
        }
        if(Option.STARTUP_LOGS.isTrue()){
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
                        for(Option option : Option.options){
                            if(!option.tree)continue;
                            if(option.getLocalName().equals(s)){
                                option.setValue(tree, option.load(map.get(key)));
                            }
                        }
                    }
                }
                if(Option.STARTUP_LOGS.isTrue())tree.print(logger);
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
                    if(!(key instanceof String)){
                        logger.log(Level.WARNING, "Unknown tool property: {0}; Skipping...", key);
                        continue;
                    }
                    String s = ((String)key).toLowerCase().replace("-", "").replace("_", "").replace(" ", "");
                    for(Option option : Option.options){
                        if(!option.tool)continue;
                        if(option.getLocalName().equals(s)){
                            option.setValue(tool, option.load(map.get(key)));
                        }
                    }
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
    private void breakLog(Tree tree, Tool tool, ItemStack axe, Block log, Block origin, int lowest, Player player, long seed, List<Block> blocks){
        ArrayList<Material> overridables = new ArrayList<>(Option.OVERRIDABLES.get(tool, tree));
        ArrayList<Effect> effects = new ArrayList<>();
        for(Effect e : Option.EFFECTS.get(tool, tree)){
            if(e.location==Effect.EffectLocation.LOGS||e.location==Effect.EffectLocation.TREE)effects.add(e);
        }
        breakBlock(Option.LOG_BEHAVIOR.get(tool, tree), Option.CONVERT_WOOD_TO_LOG.get(tool, tree), Option.LOG_DROP_CHANCE.get(tool, tree), Option.DIRECTIONAL_FALL_VELOCITY.get(tool, tree), Option.RANDOM_FALL_VELOCITY.get(tool, tree), Option.ROTATE_LOGS.get(tool, tree), Option.DIRECTIONAL_FALL_BEHAVIOR.get(tool, tree), true, Option.LOCK_FALL_CARDINAL.get(tool, tree), axe, log, origin, lowest, player, seed, effects, blocks, overridables);
    }
    private void breakLeaf(Tree tree, Tool tool, ItemStack axe, Block leaf, Block origin, int lowest, Player player, long seed, List<Block> blocks){
        ArrayList<Material> overridables = new ArrayList<>(Option.OVERRIDABLES.get(tool, tree));
        ArrayList<Effect> effects = new ArrayList<>();
        for(Effect e : Option.EFFECTS.get(tool, tree)){
            if(e.location==Effect.EffectLocation.LOGS||e.location==Effect.EffectLocation.TREE)effects.add(e);
        }
        breakBlock(Option.LEAF_BEHAVIOR.get(tool, tree), Option.CONVERT_WOOD_TO_LOG.get(tool, tree), Option.LEAF_DROP_CHANCE.get(tool, tree), Option.DIRECTIONAL_FALL_VELOCITY.get(tool, tree), Option.RANDOM_FALL_VELOCITY.get(tool, tree), Option.ROTATE_LOGS.get(tool, tree), Option.DIRECTIONAL_FALL_BEHAVIOR.get(tool, tree), Option.LEAF_ENCHANTMENTS.get(tool, tree), Option.LOCK_FALL_CARDINAL.get(tool, tree), axe, leaf, origin, lowest, player, seed, effects, blocks, overridables);
    }
    private void breakBlock(FellBehavior behavior, boolean convert, double dropChance, double directionalFallVelocity, double randomFallVelocity, boolean rotate, DirectionalFallBehavior directionalFallBehavior, boolean applyEnchantments, boolean lockCardinal, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed, Iterable<Effect> effects, List<Block> blocks, ArrayList<Material> overridables){
        if(behavior==FellBehavior.FALL||behavior==FellBehavior.FALL_HURT||behavior==FellBehavior.NATURAL){
            TreeFellerCompat.removeBlock(player, block);
        }else{
            TreeFellerCompat.breakBlock(player, block);
        }
        switch(behavior){
            case INVENTORY:
                if(player!=null){
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
                }
            case BREAK:
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
                    v.add(directionalFallBehavior.getDirectionalVel(seed, player, block, lockCardinal, directionalFallVelocity));
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
            double dist = 0;
            Block target = block;
            Location l = block.getLocation().add(.5,.5,.5);
            while(dist<height){
                dist+=interval;
                l = l.add(v);
                Block b = l.getBlock();
                if(overridables.contains(b.getType()))target = b;
                else break;
            }
            while(overridables.contains(target.getRelative(0, -1, 0)))target = target.getRelative(0,-1,0);
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
    }
    private int debugIndent = 0;
    private void debug(Player player, String text, boolean indent){
        if(!debug)return;
        if(indent)debugIndent++;
        text = "[TreeFeller] "+getDebugIndent()+" "+text;
        getLogger().log(Level.FINEST, text);
        if(player!=null)player.sendMessage(text);
    }
    private void debug(Player player, boolean critical, boolean success, String text){
        if(!debug)return;
        if((critical||!success)&&debugIndent>0)debugIndent--;
        String icon;
        if(success)icon = (critical?ChatColor.DARK_GREEN:ChatColor.GREEN)+"O";
        else icon = (critical?ChatColor.DARK_RED:ChatColor.RED)+"X";
        text = "[TreeFeller] "+getDebugIndent(1)+icon+ChatColor.RESET+" "+text;
        getLogger().log(Level.FINEST, text);
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
}