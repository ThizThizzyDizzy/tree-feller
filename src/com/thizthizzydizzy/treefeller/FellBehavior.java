package com.thizthizzydizzy.treefeller;
import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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
    },
    NATURAL(Material.OAK_SAPLING, "blocks will instantly fall in a more natural way (May not work with cutting-animation)"){
        @Override
        void breakBlock(DetectedTree detectedTree, TreeFeller plugin, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed, ArrayList<Modifier> modifiers, DirectionalFallBehavior directionalFallBehavior, boolean lockCardinal, double directionalFallVelocity, boolean rotate, ArrayList<Material> overridables, double randomFallVelocity, double explosiveFallVelocity, double verticalFallVelocity){
            Vector v = directionalFallBehavior.getDirectionalVel(seed, player, block, lockCardinal, directionalFallVelocity).normalize();
            plugin.naturalFalls.add(new NaturalFall(player, v, origin, block, block.getY()-lowest, rotate, overridables));
            block.setType(Material.AIR);
        }
    },
    FALL(Material.SAND, "blocks will fall as falling blocks"){
        @Override
        void breakBlock(DetectedTree detectedTree, TreeFeller plugin, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed, ArrayList<Modifier> modifiers, DirectionalFallBehavior directionalFallBehavior, boolean lockCardinal, double directionalFallVelocity, boolean rotate, ArrayList<Material> overridables, double randomFallVelocity, double explosiveFallVelocity, double verticalFallVelocity){
            processFallBehavior(detectedTree, false, false, false, plugin, dropItems, tree, tool, axe, block, origin, lowest, player, seed, modifiers, directionalFallBehavior, lockCardinal, directionalFallVelocity, rotate, overridables, randomFallVelocity, explosiveFallVelocity, verticalFallVelocity);
        }
    },
    FALL_HURT(Material.ANVIL, "blocks will fall as falling blocks and hurt any entity they land on"){
        @Override
        void breakBlock(DetectedTree detectedTree, TreeFeller plugin, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed, ArrayList<Modifier> modifiers, DirectionalFallBehavior directionalFallBehavior, boolean lockCardinal, double directionalFallVelocity, boolean rotate, ArrayList<Material> overridables, double randomFallVelocity, double explosiveFallVelocity, double verticalFallVelocity){
            processFallBehavior(detectedTree, true, false, false, plugin, dropItems, tree, tool, axe, block, origin, lowest, player, seed, modifiers, directionalFallBehavior, lockCardinal, directionalFallVelocity, rotate, overridables, randomFallVelocity, explosiveFallVelocity, verticalFallVelocity);
        }
    },
    FALL_BREAK(Material.GRAVEL, "blocks will fall as falling blocks and break when they reach the ground"){
        @Override
        void breakBlock(DetectedTree detectedTree, TreeFeller plugin, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed, ArrayList<Modifier> modifiers, DirectionalFallBehavior directionalFallBehavior, boolean lockCardinal, double directionalFallVelocity, boolean rotate, ArrayList<Material> overridables, double randomFallVelocity, double explosiveFallVelocity, double verticalFallVelocity){
            processFallBehavior(detectedTree, false, true, false, plugin, dropItems, tree, tool, axe, block, origin, lowest, player, seed, modifiers, directionalFallBehavior, lockCardinal, directionalFallVelocity, rotate, overridables, randomFallVelocity, explosiveFallVelocity, verticalFallVelocity);
        }
    },
    FALL_HURT_BREAK(Material.DAMAGED_ANVIL, "blocks will fall as falling brocks, hurt entities they land on, and break when they reach the ground"){
        @Override
        void breakBlock(DetectedTree detectedTree, TreeFeller plugin, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed, ArrayList<Modifier> modifiers, DirectionalFallBehavior directionalFallBehavior, boolean lockCardinal, double directionalFallVelocity, boolean rotate, ArrayList<Material> overridables, double randomFallVelocity, double explosiveFallVelocity, double verticalFallVelocity){
            processFallBehavior(detectedTree, true, true, false, plugin, dropItems, tree, tool, axe, block, origin, lowest, player, seed, modifiers, directionalFallBehavior, lockCardinal, directionalFallVelocity, rotate, overridables, randomFallVelocity, explosiveFallVelocity, verticalFallVelocity);
        }
    },
    FALL_INVENTORY(Material.ENDER_CHEST, "blocks will fall as falling blocks, break, and appear in the player's inventory upon reaching the ground"){
        @Override
        void breakBlock(DetectedTree detectedTree, TreeFeller plugin, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed, ArrayList<Modifier> modifiers, DirectionalFallBehavior directionalFallBehavior, boolean lockCardinal, double directionalFallVelocity, boolean rotate, ArrayList<Material> overridables, double randomFallVelocity, double explosiveFallVelocity, double verticalFallVelocity){
            processFallBehavior(detectedTree, false, false, true, plugin, dropItems, tree, tool, axe, block, origin, lowest, player, seed, modifiers, directionalFallBehavior, lockCardinal, directionalFallVelocity, rotate, overridables, randomFallVelocity, explosiveFallVelocity, verticalFallVelocity);
        }
    },
    FALL_HURT_INVENTORY(Material.TRAPPED_CHEST, "blocks will fall as falling blocks, break, hurt entities they land on, and appear in the player's inventory upon reaching the ground"){
        @Override
        void breakBlock(DetectedTree detectedTree, TreeFeller plugin, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed, ArrayList<Modifier> modifiers, DirectionalFallBehavior directionalFallBehavior, boolean lockCardinal, double directionalFallVelocity, boolean rotate, ArrayList<Material> overridables, double randomFallVelocity, double explosiveFallVelocity, double verticalFallVelocity){
            processFallBehavior(detectedTree, true, false, true, plugin, dropItems, tree, tool, axe, block, origin, lowest, player, seed, modifiers, directionalFallBehavior, lockCardinal, directionalFallVelocity, rotate, overridables, randomFallVelocity, explosiveFallVelocity, verticalFallVelocity);
        }
    },
    FALL_NATURAL(Material.SAND, "blocks will fall as falling blocks. Blocks will attempt to target a position to land as if the tree fell over realistically"){
        @Override
        void breakBlock(DetectedTree detectedTree, TreeFeller plugin, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed, ArrayList<Modifier> modifiers, DirectionalFallBehavior directionalFallBehavior, boolean lockCardinal, double directionalFallVelocity, boolean rotate, ArrayList<Material> overridables, double randomFallVelocity, double explosiveFallVelocity, double verticalFallVelocity){
            processNaturalFallBehavior(detectedTree, false, false, false, plugin, dropItems, tree, tool, axe, block, origin, lowest, player, seed, modifiers, directionalFallBehavior, lockCardinal, rotate, overridables, verticalFallVelocity);
        }
    },
    FALL_NATURAL_HURT(Material.ANVIL, "blocks will fall as falling blocks and hurt any entity they land on. Blocks will attempt to target a position to land as if the tree fell over realistically"){
        @Override
        void breakBlock(DetectedTree detectedTree, TreeFeller plugin, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed, ArrayList<Modifier> modifiers, DirectionalFallBehavior directionalFallBehavior, boolean lockCardinal, double directionalFallVelocity, boolean rotate, ArrayList<Material> overridables, double randomFallVelocity, double explosiveFallVelocity, double verticalFallVelocity){
            processNaturalFallBehavior(detectedTree, true, false, false, plugin, dropItems, tree, tool, axe, block, origin, lowest, player, seed, modifiers, directionalFallBehavior, lockCardinal, rotate, overridables, verticalFallVelocity);
        }
    },
    FALL_NATURAL_BREAK(Material.GRAVEL, "blocks will fall as falling blocks and break when they reach the ground. Blocks will attempt to target a position to land as if the tree fell over realistically"){
        @Override
        void breakBlock(DetectedTree detectedTree, TreeFeller plugin, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed, ArrayList<Modifier> modifiers, DirectionalFallBehavior directionalFallBehavior, boolean lockCardinal, double directionalFallVelocity, boolean rotate, ArrayList<Material> overridables, double randomFallVelocity, double explosiveFallVelocity, double verticalFallVelocity){
            processNaturalFallBehavior(detectedTree, false, true, false, plugin, dropItems, tree, tool, axe, block, origin, lowest, player, seed, modifiers, directionalFallBehavior, lockCardinal, rotate, overridables, verticalFallVelocity);
        }
    },
    FALL_NATURAL_HURT_BREAK(Material.DAMAGED_ANVIL, "blocks will fall as falling brocks, hurt entities they land on, and break when they reach the ground. Blocks will attempt to target a position to land as if the tree fell over realistically"){
        @Override
        void breakBlock(DetectedTree detectedTree, TreeFeller plugin, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed, ArrayList<Modifier> modifiers, DirectionalFallBehavior directionalFallBehavior, boolean lockCardinal, double directionalFallVelocity, boolean rotate, ArrayList<Material> overridables, double randomFallVelocity, double explosiveFallVelocity, double verticalFallVelocity){
            processNaturalFallBehavior(detectedTree, true, true, false, plugin, dropItems, tree, tool, axe, block, origin, lowest, player, seed, modifiers, directionalFallBehavior, lockCardinal, rotate, overridables, verticalFallVelocity);
        }
    },
    FALL_NATURAL_INVENTORY(Material.ENDER_CHEST, "blocks will fall as falling blocks, break, and appear in the player's inventory upon reaching the ground. Blocks will attempt to target a position to land as if the tree fell over realistically"){
        @Override
        void breakBlock(DetectedTree detectedTree, TreeFeller plugin, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed, ArrayList<Modifier> modifiers, DirectionalFallBehavior directionalFallBehavior, boolean lockCardinal, double directionalFallVelocity, boolean rotate, ArrayList<Material> overridables, double randomFallVelocity, double explosiveFallVelocity, double verticalFallVelocity){
            processNaturalFallBehavior(detectedTree, false, false, true, plugin, dropItems, tree, tool, axe, block, origin, lowest, player, seed, modifiers, directionalFallBehavior, lockCardinal, rotate, overridables, verticalFallVelocity);
        }
    },
    FALL_NATURAL_HURT_INVENTORY(Material.TRAPPED_CHEST, "blocks will fall as falling blocks, break, hurt entities they land on, and appear in the player's inventory upon reaching the ground. Blocks will attempt to target a position to land as if the tree fell over realistically"){
        @Override
        void breakBlock(DetectedTree detectedTree, TreeFeller plugin, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed, ArrayList<Modifier> modifiers, DirectionalFallBehavior directionalFallBehavior, boolean lockCardinal, double directionalFallVelocity, boolean rotate, ArrayList<Material> overridables, double randomFallVelocity, double explosiveFallVelocity, double verticalFallVelocity){
            processNaturalFallBehavior(detectedTree, true, false, true, plugin, dropItems, tree, tool, axe, block, origin, lowest, player, seed, modifiers, directionalFallBehavior, lockCardinal, rotate, overridables, verticalFallVelocity);
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
    private static void processFallBehavior(DetectedTree detectedTree, boolean hurt, boolean doBreak, boolean inventory, TreeFeller plugin, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed, ArrayList<Modifier> modifiers, DirectionalFallBehavior directionalFallBehavior, boolean lockCardinal, double directionalFallVelocity, boolean rotate, ArrayList<Material> overridables, double randomFallVelocity, double explosiveFallVelocity, double verticalFallVelocity){
        FallingBlock falling = block.getWorld().spawnFallingBlock(block.getLocation().add(.5,.5,.5), block.getBlockData());
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
        Player inv = null;
        if(inventory){
            if(player==null)doBreak = true;
            else inv = player;
        }
        RotationData rot = null;
        if(falling.getBlockData() instanceof Orientable&&rotate){
            rot = new RotationData((Orientable)falling.getBlockData(), origin);
        }
        block.setType(Material.AIR);
        plugin.fallingBlocks.add(new FallingTreeBlock(detectedTree, falling, tool, tree, axe, doBreak, inv, rot, dropItems, modifiers));
    }
    private static void processNaturalFallBehavior(DetectedTree detectedTree, boolean hurt, boolean doBreak, boolean inventory, TreeFeller plugin, boolean dropItems, Tree tree, Tool tool, ItemStack axe, Block block, Block origin, int lowest, Player player, long seed, ArrayList<Modifier> modifiers, DirectionalFallBehavior directionalFallBehavior, boolean lockCardinal, boolean rotate, ArrayList<Material> overridables, double verticalFallVelocity){
        FallingBlock falling = block.getWorld().spawnFallingBlock(block.getLocation().add(.5,.5,.5), block.getBlockData());
        falling.addScoreboardTag("tree_feller");
        Vector v = directionalFallBehavior.getDirectionalVel(seed, player, origin, lockCardinal, 1).setY(0).normalize();
        int height = block.getY()-lowest;
        double velocity = findNaturalVelocity(verticalFallVelocity, height+.3, height, .015625);//at least quarter-of-a-pixel accuracy
        falling.setVelocity(v.multiply(velocity).setY(verticalFallVelocity));
        falling.setHurtEntities(hurt);
        Player inv = null;
        if(inventory){
            if(player==null)doBreak = true;
            else inv = player;
        }
        RotationData rot = null;
        if(falling.getBlockData() instanceof Orientable&&rotate){
            rot = new RotationData((Orientable)falling.getBlockData(), origin);
        }
        block.setType(Material.AIR);
        plugin.fallingBlocks.add(new FallingTreeBlock(detectedTree, falling, tool, tree, axe, doBreak, inv, rot, dropItems, modifiers));
    }
    public String getDescription(){
        return description;
    }
    private static double findNaturalVelocity(double verticalVelocity, double targetDistance, int altitude, double tolerance){
        double vel = 0;
        double result = 0;
        int iteration = 0;
        while(Math.abs(result-targetDistance)>tolerance){
            if(iteration==0){
                vel++;
                result = testNaturalVelocity(verticalVelocity, vel, altitude);
                if(result>targetDistance)iteration++;
                if(vel>100)return 0;//a hundered blocks a tick is way too far already; this is a lost cause
            }else{
                double step = Math.pow(0.5, iteration);
                System.out.println("Iteration "+iteration+", vert "+verticalVelocity+", alt "+altitude+", value "+vel+", result "+result+", target "+targetDistance+", step "+step+", difference "+Math.abs(result-targetDistance));
                //at iteration 1, the target value is between vel-1 and vel
                if(result>targetDistance)vel-=step;
                else vel+=step;
                result = testNaturalVelocity(verticalVelocity, vel, altitude);
                iteration++;
            }
        }
        return vel;
    }
    private static double testNaturalVelocity(double verticalVelocity, double velocity, int altitude){
        double xVel = velocity, yVel = verticalVelocity, x = 0, y = altitude;
        int iteration = 0;
        while(y>0){
            iteration++;
            xVel*=.98;
            yVel-=.04;
            yVel*=.98;
            x+=xVel;
            y+=yVel;
        }
        return x;
    }
}