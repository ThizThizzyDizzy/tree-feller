package com.thizthizzydizzy.treefeller;
import com.thizthizzydizzy.vanillify.Vanillify;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
public enum FellBehavior{
    BREAK(Material.COBBLESTONE, "blocks will break and fall as items"){
        @Override
        void breakBlock(DetectedTree detectedTree, TreeFeller plugin, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed, ArrayList<Modifier> modifiers, DirectionalFallBehavior directionalFallBehavior, boolean lockCardinal, double directionalFallVelocity, boolean rotate, ArrayList<Material> overridables, double randomFallVelocity, double explosiveFallVelocity, double verticalFallVelocity){
            if(dropItems){
                int[] xp = new int[]{0};
                for(ItemStack s : plugin.getDropsWithBonus(block, tool, tree, axe, xp, modifiers)){
                    plugin.dropItem(detectedTree, player, block.getWorld().dropItemNaturally(block.getLocation(), s));
                }
                plugin.dropExp(block.getWorld(), block.getLocation(), xp[0]);
            }
            block.setType(Material.AIR);
        }
        @Override
        public FellBehavior getDecorationBehavior(){
            return BREAK;
        }
    },
    INVENTORY(Material.CHEST, "blocks will appear in the player's inventory as items, or fall as items if the player inventory is full"){
        @Override
        void breakBlock(DetectedTree detectedTree, TreeFeller plugin, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed, ArrayList<Modifier> modifiers, DirectionalFallBehavior directionalFallBehavior, boolean lockCardinal, double directionalFallVelocity, boolean rotate, ArrayList<Material> overridables, double randomFallVelocity, double explosiveFallVelocity, double verticalFallVelocity){
            if(player==null)BREAK.breakBlock(detectedTree, plugin, dropItems, tree, tool, axe, block, origin, lowest, player, seed, modifiers, directionalFallBehavior, lockCardinal, directionalFallVelocity, rotate, overridables, randomFallVelocity, explosiveFallVelocity, verticalFallVelocity);
            if(dropItems){
                int[] xp = new int[]{0};
                for(ItemStack s : plugin.getDropsWithBonus(block, tool, tree, axe, xp, modifiers)){
                    for(ItemStack st : player.getInventory().addItem(s).values()){
                        plugin.dropItem(detectedTree, player, block.getWorld().dropItemNaturally(block.getLocation(), st));
                    }
                }
                player.setTotalExperience(player.getTotalExperience()+xp[0]);
            }
            block.setType(Material.AIR);
        }
        @Override
        public FellBehavior getDecorationBehavior(){
            return INVENTORY;
        }
    },
    NATURAL(Material.OAK_SAPLING, "blocks will instantly fall in a more natural way (May not work with cutting-animation)"){
        @Override
        void breakBlock(DetectedTree detectedTree, TreeFeller plugin, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed, ArrayList<Modifier> modifiers, DirectionalFallBehavior directionalFallBehavior, boolean lockCardinal, double directionalFallVelocity, boolean rotate, ArrayList<Material> overridables, double randomFallVelocity, double explosiveFallVelocity, double verticalFallVelocity){
            Vector v = directionalFallBehavior.getDirectionalVel(seed, player, block, lockCardinal, directionalFallVelocity).normalize();
            plugin.naturalFalls.add(new NaturalFall(player, v, origin, block, block.getY()-lowest, rotate, overridables));
            block.setType(Material.AIR);
        }
        @Override
        public FellBehavior getDecorationBehavior(){
            return BREAK;
        }
    },
    FALL(Material.SAND, "blocks will fall as falling blocks"){
        @Override
        void breakBlock(DetectedTree detectedTree, TreeFeller plugin, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed, ArrayList<Modifier> modifiers, DirectionalFallBehavior directionalFallBehavior, boolean lockCardinal, double directionalFallVelocity, boolean rotate, ArrayList<Material> overridables, double randomFallVelocity, double explosiveFallVelocity, double verticalFallVelocity){
            processDelayedFallBehavior(detectedTree, false, false, false, plugin, dropItems, tree, tool, axe, block, origin, lowest, player, seed, modifiers, directionalFallBehavior, lockCardinal, directionalFallVelocity, rotate, overridables, randomFallVelocity, explosiveFallVelocity, verticalFallVelocity);
        }
        @Override
        public FellBehavior getDecorationBehavior(){
            return BREAK;
        }
    },
    FALL_HURT(Material.ANVIL, "blocks will fall as falling blocks and hurt any entity they land on"){
        @Override
        void breakBlock(DetectedTree detectedTree, TreeFeller plugin, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed, ArrayList<Modifier> modifiers, DirectionalFallBehavior directionalFallBehavior, boolean lockCardinal, double directionalFallVelocity, boolean rotate, ArrayList<Material> overridables, double randomFallVelocity, double explosiveFallVelocity, double verticalFallVelocity){
            processDelayedFallBehavior(detectedTree, true, false, false, plugin, dropItems, tree, tool, axe, block, origin, lowest, player, seed, modifiers, directionalFallBehavior, lockCardinal, directionalFallVelocity, rotate, overridables, randomFallVelocity, explosiveFallVelocity, verticalFallVelocity);
        }
        @Override
        public FellBehavior getDecorationBehavior(){
            return BREAK;
        }
    },
    FALL_BREAK(Material.GRAVEL, "blocks will fall as falling blocks and break when they reach the ground"){
        @Override
        void breakBlock(DetectedTree detectedTree, TreeFeller plugin, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed, ArrayList<Modifier> modifiers, DirectionalFallBehavior directionalFallBehavior, boolean lockCardinal, double directionalFallVelocity, boolean rotate, ArrayList<Material> overridables, double randomFallVelocity, double explosiveFallVelocity, double verticalFallVelocity){
            processDelayedFallBehavior(detectedTree, false, true, false, plugin, dropItems, tree, tool, axe, block, origin, lowest, player, seed, modifiers, directionalFallBehavior, lockCardinal, directionalFallVelocity, rotate, overridables, randomFallVelocity, explosiveFallVelocity, verticalFallVelocity);
        }
        @Override
        public FellBehavior getDecorationBehavior(){
            return BREAK;
        }
    },
    FALL_HURT_BREAK(Material.DAMAGED_ANVIL, "blocks will fall as falling blocks, hurt entities they land on, and break when they reach the ground"){
        @Override
        void breakBlock(DetectedTree detectedTree, TreeFeller plugin, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed, ArrayList<Modifier> modifiers, DirectionalFallBehavior directionalFallBehavior, boolean lockCardinal, double directionalFallVelocity, boolean rotate, ArrayList<Material> overridables, double randomFallVelocity, double explosiveFallVelocity, double verticalFallVelocity){
            processDelayedFallBehavior(detectedTree, true, true, false, plugin, dropItems, tree, tool, axe, block, origin, lowest, player, seed, modifiers, directionalFallBehavior, lockCardinal, directionalFallVelocity, rotate, overridables, randomFallVelocity, explosiveFallVelocity, verticalFallVelocity);
        }
        @Override
        public FellBehavior getDecorationBehavior(){
            return BREAK;
        }
    },
    FALL_INVENTORY(Material.ENDER_CHEST, "blocks will fall as falling blocks, break, and appear in the player's inventory upon reaching the ground"){
        @Override
        void breakBlock(DetectedTree detectedTree, TreeFeller plugin, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed, ArrayList<Modifier> modifiers, DirectionalFallBehavior directionalFallBehavior, boolean lockCardinal, double directionalFallVelocity, boolean rotate, ArrayList<Material> overridables, double randomFallVelocity, double explosiveFallVelocity, double verticalFallVelocity){
            processDelayedFallBehavior(detectedTree, false, false, true, plugin, dropItems, tree, tool, axe, block, origin, lowest, player, seed, modifiers, directionalFallBehavior, lockCardinal, directionalFallVelocity, rotate, overridables, randomFallVelocity, explosiveFallVelocity, verticalFallVelocity);
        }
        @Override
        public FellBehavior getDecorationBehavior(){
            return INVENTORY;
        }
    },
    FALL_HURT_INVENTORY(Material.TRAPPED_CHEST, "blocks will fall as falling blocks, break, hurt entities they land on, and appear in the player's inventory upon reaching the ground"){
        @Override
        void breakBlock(DetectedTree detectedTree, TreeFeller plugin, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed, ArrayList<Modifier> modifiers, DirectionalFallBehavior directionalFallBehavior, boolean lockCardinal, double directionalFallVelocity, boolean rotate, ArrayList<Material> overridables, double randomFallVelocity, double explosiveFallVelocity, double verticalFallVelocity){
            processDelayedFallBehavior(detectedTree, true, false, true, plugin, dropItems, tree, tool, axe, block, origin, lowest, player, seed, modifiers, directionalFallBehavior, lockCardinal, directionalFallVelocity, rotate, overridables, randomFallVelocity, explosiveFallVelocity, verticalFallVelocity);
        }
        @Override
        public FellBehavior getDecorationBehavior(){
            return INVENTORY;
        }
    },
    FALL_NATURAL(Material.SAND, "blocks will fall as falling blocks. Blocks will attempt to target a position to land as if the tree fell over realistically"){
        @Override
        void breakBlock(DetectedTree detectedTree, TreeFeller plugin, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed, ArrayList<Modifier> modifiers, DirectionalFallBehavior directionalFallBehavior, boolean lockCardinal, double directionalFallVelocity, boolean rotate, ArrayList<Material> overridables, double randomFallVelocity, double explosiveFallVelocity, double verticalFallVelocity){
            processDelayedNaturalFallBehavior(detectedTree, false, false, false, plugin, dropItems, tree, tool, axe, block, origin, lowest, player, seed, modifiers, directionalFallBehavior, lockCardinal, rotate, overridables, verticalFallVelocity);
        }
        @Override
        public FellBehavior getDecorationBehavior(){
            return BREAK;
        }
    },
    FALL_NATURAL_HURT(Material.ANVIL, "blocks will fall as falling blocks and hurt any entity they land on. Blocks will attempt to target a position to land as if the tree fell over realistically"){
        @Override
        void breakBlock(DetectedTree detectedTree, TreeFeller plugin, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed, ArrayList<Modifier> modifiers, DirectionalFallBehavior directionalFallBehavior, boolean lockCardinal, double directionalFallVelocity, boolean rotate, ArrayList<Material> overridables, double randomFallVelocity, double explosiveFallVelocity, double verticalFallVelocity){
            processDelayedNaturalFallBehavior(detectedTree, true, false, false, plugin, dropItems, tree, tool, axe, block, origin, lowest, player, seed, modifiers, directionalFallBehavior, lockCardinal, rotate, overridables, verticalFallVelocity);
        }
        @Override
        public FellBehavior getDecorationBehavior(){
            return BREAK;
        }
    },
    FALL_NATURAL_BREAK(Material.GRAVEL, "blocks will fall as falling blocks and break when they reach the ground. Blocks will attempt to target a position to land as if the tree fell over realistically"){
        @Override
        void breakBlock(DetectedTree detectedTree, TreeFeller plugin, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed, ArrayList<Modifier> modifiers, DirectionalFallBehavior directionalFallBehavior, boolean lockCardinal, double directionalFallVelocity, boolean rotate, ArrayList<Material> overridables, double randomFallVelocity, double explosiveFallVelocity, double verticalFallVelocity){
            processDelayedNaturalFallBehavior(detectedTree, false, true, false, plugin, dropItems, tree, tool, axe, block, origin, lowest, player, seed, modifiers, directionalFallBehavior, lockCardinal, rotate, overridables, verticalFallVelocity);
        }
        @Override
        public FellBehavior getDecorationBehavior(){
            return BREAK;
        }
    },
    FALL_NATURAL_HURT_BREAK(Material.DAMAGED_ANVIL, "blocks will fall as falling blocks, hurt entities they land on, and break when they reach the ground. Blocks will attempt to target a position to land as if the tree fell over realistically"){
        @Override
        void breakBlock(DetectedTree detectedTree, TreeFeller plugin, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed, ArrayList<Modifier> modifiers, DirectionalFallBehavior directionalFallBehavior, boolean lockCardinal, double directionalFallVelocity, boolean rotate, ArrayList<Material> overridables, double randomFallVelocity, double explosiveFallVelocity, double verticalFallVelocity){
            processDelayedNaturalFallBehavior(detectedTree, true, true, false, plugin, dropItems, tree, tool, axe, block, origin, lowest, player, seed, modifiers, directionalFallBehavior, lockCardinal, rotate, overridables, verticalFallVelocity);
        }
        @Override
        public FellBehavior getDecorationBehavior(){
            return BREAK;
        }
    },
    FALL_NATURAL_INVENTORY(Material.ENDER_CHEST, "blocks will fall as falling blocks, break, and appear in the player's inventory upon reaching the ground. Blocks will attempt to target a position to land as if the tree fell over realistically"){
        @Override
        void breakBlock(DetectedTree detectedTree, TreeFeller plugin, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed, ArrayList<Modifier> modifiers, DirectionalFallBehavior directionalFallBehavior, boolean lockCardinal, double directionalFallVelocity, boolean rotate, ArrayList<Material> overridables, double randomFallVelocity, double explosiveFallVelocity, double verticalFallVelocity){
            processDelayedNaturalFallBehavior(detectedTree, false, false, true, plugin, dropItems, tree, tool, axe, block, origin, lowest, player, seed, modifiers, directionalFallBehavior, lockCardinal, rotate, overridables, verticalFallVelocity);
        }
        @Override
        public FellBehavior getDecorationBehavior(){
            return INVENTORY;
        }
    },
    FALL_NATURAL_HURT_INVENTORY(Material.TRAPPED_CHEST, "blocks will fall as falling blocks, break, hurt entities they land on, and appear in the player's inventory upon reaching the ground. Blocks will attempt to target a position to land as if the tree fell over realistically"){
        @Override
        void breakBlock(DetectedTree detectedTree, TreeFeller plugin, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed, ArrayList<Modifier> modifiers, DirectionalFallBehavior directionalFallBehavior, boolean lockCardinal, double directionalFallVelocity, boolean rotate, ArrayList<Material> overridables, double randomFallVelocity, double explosiveFallVelocity, double verticalFallVelocity){
            processDelayedNaturalFallBehavior(detectedTree, true, false, true, plugin, dropItems, tree, tool, axe, block, origin, lowest, player, seed, modifiers, directionalFallBehavior, lockCardinal, rotate, overridables, verticalFallVelocity);
        }
        @Override
        public FellBehavior getDecorationBehavior(){
            return INVENTORY;
        }
    };
    private final Material item;
    private final String description;
    private FellBehavior(Material item, String description){
        this.item = item;
        this.description = description;
    }
    public static FellBehavior match(String s){
        return valueOf(s.toUpperCase().trim().replace("-", "_"));
    }
    public Material getItem(){
        return item;
    }
    abstract void breakBlock(DetectedTree detectedTree, TreeFeller plugin, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed, ArrayList<Modifier> modifiers, DirectionalFallBehavior directionalFallBehavior, boolean lockCardinal, double directionalFallVelocity, boolean rotate, ArrayList<Material> overridables, double randomFallVelocity, double explosiveFallVelocity, double verticalFallVelocity);
    public abstract FellBehavior getDecorationBehavior();
    private static void processDelayedFallBehavior(DetectedTree detectedTree, boolean hurt, boolean doBreak, boolean inventory, TreeFeller plugin, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed, ArrayList<Modifier> modifiers, DirectionalFallBehavior directionalFallBehavior, boolean lockCardinal, double directionalFallVelocity, boolean rotate, ArrayList<Material> overridables, double randomFallVelocity, double explosiveFallVelocity, double verticalFallVelocity){
        BlockData data = block.getBlockData();
        block.setType(Material.AIR);
        int delay = Option.FALL_DELAY.get(tool, tree);
        if(delay>0){
            new BukkitRunnable() {
                @Override
                public void run() {
                    processFallBehavior(data, detectedTree, hurt, doBreak, inventory, plugin, dropItems, tree, tool, axe, block, origin, lowest, player, seed, modifiers, directionalFallBehavior, lockCardinal, directionalFallVelocity, rotate, overridables, randomFallVelocity, explosiveFallVelocity, verticalFallVelocity);
                }
            }.runTaskLater(plugin, delay);
        }
        else processFallBehavior(data, detectedTree, hurt, doBreak, inventory, plugin, dropItems, tree, tool, axe, block, origin, lowest, player, seed, modifiers, directionalFallBehavior, lockCardinal, directionalFallVelocity, rotate, overridables, randomFallVelocity, explosiveFallVelocity, verticalFallVelocity);
    }
    private static void processFallBehavior(BlockData data, DetectedTree detectedTree, boolean hurt, boolean doBreak, boolean inventory, TreeFeller plugin, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed, ArrayList<Modifier> modifiers, DirectionalFallBehavior directionalFallBehavior, boolean lockCardinal, double directionalFallVelocity, boolean rotate, ArrayList<Material> overridables, double randomFallVelocity, double explosiveFallVelocity, double verticalFallVelocity){
        FallingBlock falling = block.getWorld().spawnFallingBlock(block.getLocation().add(.5,.5,.5), data);
        falling.addScoreboardTag("tree_feller");
        Vector v = falling.getVelocity();
        if(directionalFallVelocity>0){
            v.add(directionalFallBehavior.getDirectionalVel(seed, player, origin, lockCardinal, directionalFallVelocity).setY(0));
        }
        v.add(new Vector((Math.random()*2-1)*randomFallVelocity, verticalFallVelocity, (Math.random()*2-1)*randomFallVelocity));
        if(block.getX()!=origin.getX()||block.getZ()!=origin.getZ()){
            Vector explosive = new Vector(block.getLocation().getX()-origin.getLocation().getX(),0,block.getLocation().getZ()-origin.getLocation().getZ());
            if(lockCardinal){
                if(Math.abs(explosive.getX())>Math.abs(explosive.getZ())){
                    if(explosive.getX()>0)explosive = new Vector(1, 0, 0);
                    else explosive = new Vector(-1, 0, 0);
                }else{
                    if(explosive.getZ()>0)explosive = new Vector(0, 0, 1);
                    else explosive = new Vector(0, 0, -1);
                }
            }
            explosive = explosive.normalize();
            explosive = explosive.multiply(explosiveFallVelocity);
            v.add(explosive);
        }
        falling.setVelocity(v);
        falling.setHurtEntities(hurt);
        Vanillify.modifyEntityNBT(falling, "FallHurtAmount", Option.FALL_HURT_AMOUNT.get(tool, tree));
        Vanillify.modifyEntityNBT(falling, "FallHurtMax", Option.FALL_HURT_MAX.get(tool, tree));
        Player inv = null;
        if(inventory){
            if(player==null)doBreak = true;
            else inv = player;
        }
        RotationData rot = null;
        if(falling.getBlockData() instanceof Orientable&&rotate){
            rot = new RotationData((Orientable)falling.getBlockData(), origin);
        }
        plugin.fallingBlocks.add(new FallingTreeBlock(detectedTree, falling, tool, tree, axe, doBreak, inv, rot, dropItems, modifiers));
    }
    private static void processDelayedNaturalFallBehavior(DetectedTree detectedTree, boolean hurt, boolean doBreak, boolean inventory, TreeFeller plugin, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed, ArrayList<Modifier> modifiers, DirectionalFallBehavior directionalFallBehavior, boolean lockCardinal, boolean rotate, ArrayList<Material> overridables, double verticalFallVelocity){
        BlockData data = block.getBlockData();
        block.setType(Material.AIR);
        int delay = Option.FALL_DELAY.get(tool, tree);
        if(delay>0){
            new BukkitRunnable() {
                @Override
                public void run() {
                    processNaturalFallBehavior(data, detectedTree, hurt, doBreak, inventory, plugin, dropItems, tree, tool, axe, block, origin, lowest, player, seed, modifiers, directionalFallBehavior, lockCardinal, rotate, overridables, verticalFallVelocity);
                }
            }.runTaskLater(plugin, delay);
        }
        else processNaturalFallBehavior(data, detectedTree, hurt, doBreak, inventory, plugin, dropItems, tree, tool, axe, block, origin, lowest, player, seed, modifiers, directionalFallBehavior, lockCardinal, rotate, overridables, verticalFallVelocity);
    }
    private static void processNaturalFallBehavior(BlockData data, DetectedTree detectedTree, boolean hurt, boolean doBreak, boolean inventory, TreeFeller plugin, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed, ArrayList<Modifier> modifiers, DirectionalFallBehavior directionalFallBehavior, boolean lockCardinal, boolean rotate, ArrayList<Material> overridables, double verticalFallVelocity){
        FallingBlock falling = block.getWorld().spawnFallingBlock(block.getLocation().add(.5,.5,.5), data);
        falling.addScoreboardTag("tree_feller");
//        Vector v = directionalFallBehavior.getDirectionalVel(seed, player, origin, lockCardinal, 1).setY(0).normalize();
//        int height = block.getY()-lowest;
//        double vel = calculateInitialVelocity(block.getLocation().toVector(), block.getLocation().add(height, -height, 0).toVector(), verticalFallVelocity).length();
//        falling.setVelocity(v.multiply(vel).setY(verticalFallVelocity));
        try{
            Vector v = calculateVelocityForBlock(block, lowest, directionalFallBehavior.getDirectionalVel(seed, player, origin, lockCardinal, 1).setY(0).normalize(), verticalFallVelocity).setY(verticalFallVelocity);
            if(!Double.isFinite(v.getX())||!Double.isFinite(v.getY())||!Double.isFinite(v.getZ()))v = new Vector(0, verticalFallVelocity, 0);
            falling.setVelocity(v);
        }catch(IllegalArgumentException ex){}
        falling.setHurtEntities(hurt);
        Vanillify.modifyEntityNBT(falling, "FallHurtAmount", Option.FALL_HURT_AMOUNT.get(tool, tree));
        Vanillify.modifyEntityNBT(falling, "FallHurtMax", Option.FALL_HURT_MAX.get(tool, tree));
        Player inv = null;
        if(inventory){
            if(player==null)doBreak = true;
            else inv = player;
        }
        RotationData rot = null;
        if(falling.getBlockData() instanceof Orientable&&rotate){
            rot = new RotationData((Orientable)falling.getBlockData(), origin);
        }
        plugin.fallingBlocks.add(new FallingTreeBlock(detectedTree, falling, tool, tree, axe, doBreak, inv, rot, dropItems, modifiers));
    }
    public String getDescription(){
        return description;
    }
    private static final double GRAVITY = .04d;
    private static final double DRAG = .02d;
    private static final Vector UP = new Vector(0, 1, 0);
    
    /**
     * @param b The block in question
     * @param bottomLog Bottommost log of the tree
     * @param fallDirection Normalised direction the block should fall. Y component must be zero.
     * @param yVelocity The initial y velocity this block should be given.
     * @return The velocity that should be applied to the falling block
     */
    private static Vector calculateVelocityForBlock(Block b, int bottomLogY, Vector fallDirection, double yVelocity) {
        Vector start = b.getLocation().toVector();
        Vector bottomLogPos = start.clone().setY(bottomLogY);
        Vector end = getEndPosition(start, bottomLogPos, fallDirection, yVelocity);
        return calculateInitialVelocity(start, end.add(start), yVelocity);
    }

    /**
     * Gets the position a felled block should land at given its starting position, the bottommost log of the tree, the direction it should fall and its initial upwards velocity
     * @param start Where the block is positioned before being felled
     * @param bottomLog Where the bottommost log of the tree is
     * @param fallDirection Normalised horizontal falling direction. Y component must be zero.
     * @param yVelocity The initial y velocity (upwards) this block should have
     */
    private static Vector getEndPosition(Vector start, Vector bottomLog, Vector fallDirection, double yVelocity) {
        return start.clone().subtract(bottomLog).rotateAroundAxis(fallDirection.crossProduct(UP), -90);
    }

    /**
     * @param height How far down are we falling
     * @param yVelocity The initial y velocity upwards
     * @return Ticks it takes to fall
     */
    private static int getFallTime(double height, double yVelocity) {
        double currHeight = height;
        double vel = -yVelocity;
        int ticks = 0;
        while(currHeight > 0) {
            vel += GRAVITY;
            vel *= (1 - DRAG);
            currHeight -= vel;
            ticks++;
        }
        return ticks;
    }

    /**
     * Calculates the required velocity for a felled block to land in a specific position
     * @param start Where the block starts at
     * @param end Where the block should land
     * @param yVelocity How fast upwards should the block be propelled
     * @return The velocity that should be applied to the falling block
     */
    private static Vector calculateInitialVelocity(Vector start, Vector end, double yVelocity) {
        int t = getFallTime(start.getY()-end.getY(), yVelocity);
        return new Vector(getAxisVelocity(end.getX() - start.getX(), t), 0, getAxisVelocity(end.getZ() - start.getZ(), t));
    }

    /**
     * Calculates the velocity required to travel a certain distance in a given time, accounting for drag
     * @param s The displacement (distance) that should be travelled along this axis
     * @param t The time that we have to travel that far
     * @return The velocity that should be applied to travel s blocks in t ticks along one axis
     */
    private static double getAxisVelocity(double s, int t) {
        return (s * 0.02d) / (1 - Math.pow(0.98, t));
    }
}