package com.thizthizzydizzy.treefeller.core.test;
import com.thizthizzydizzy.mcautotester.mod.core.TestContext;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
/** Reusable sky arena and deterministic tree placement. Never executes console commands. */
public final class TestArena{
    public final int minX, minZ, size, floorY;
    public final ITestPlatform.ITestWorld world;
    private final TestContext ctx;
    private TestArena(TestContext ctx, ITestPlatform.ITestWorld world, int minX, int minZ, int size, int floorY){
        this.ctx = ctx;
        this.world = world;
        this.minX = minX;
        this.minZ = minZ;
        this.size = size;
        this.floorY = floorY;
    }
    public static TestArena setup(TestContext ctx, ITestPlatform platform, String player){
        return setup(ctx, platform, player, -8, -8, 16, 99);
    }
    /** Clears the entire column above the floor, including mountains and overhangs. */
    public static TestArena setup(TestContext ctx, ITestPlatform platform, String player, int minX, int minZ, int size, int floorY){
        ctx.log("Preparing a sky arena with clear columns to the build ceiling");
        TestArena arena = onServer(ctx, () -> {
            ITestPlatform.ITestWorld world = platform.world(player);
            if(size<8||size>256||floorY<1||floorY+34>=world.maxHeight())
                throw new IllegalArgumentException("Arena must fit a 34-block tree below the build ceiling; size must be 8..256");
            if(!world.supports("grass_block")||!world.supports("air"))
                throw new IllegalArgumentException("Platform lacks arena materials");
            platform.preparePlayer(player, false);
            for(int x = minX>>4; x<=(minX+size-1)>>4; x++)
                for(int z = minZ>>4; z<=(minZ+size-1)>>4; z++)world.loadChunk(x, z);
            world.daylight();
            for(int x = minX; x<minX+size; x++)for(int z = minZ; z<minZ+size; z++){
                for(int y = floorY+1; y<world.maxHeight(); y++)world.setBlock(x, y, z, "air", null, -1);
                world.setBlock(x, floorY, z, "grass_block", null, -1);
            }
            return new TestArena(ctx, world, minX, minZ, size, floorY);
        });
        ctx.teleportPlayer(player, minX+size/2+0.5, floorY+1, minZ+size/2+0.5, 0.0, 0.0);
        return arena;
    }
    /** Query on the server thread; callers can select only trees present in their target version. */
    public boolean supports(TestTrees.Kind kind){
        return onServer(ctx, () -> {
            for(TestTrees.Block block : TestTrees.specimen(kind).blocks.values())
                if(!world.supports(block.material))return false;
            return true;
        });
    }
    /** Preflights every material and coordinate before mutating any blocks. */
    public String spawn(TestTrees.Tree tree, int x, int z){
        return spawnAll(tree, new int[][]{{x, z}});
    }
    /** Place all specimens together and fingerprint the arena once. Origins are X/Z pairs. */
    public String spawnAll(TestTrees.Tree tree, int[][] origins){
        String fingerprint = onServer(ctx, () -> {
            for(int[] origin : origins){
                int x = origin[0], z = origin[1];
                for(Map.Entry<Long, TestTrees.Block> entry : tree.blocks.entrySet()){
                    long pos = entry.getKey();
                    int bx = x+BlockPos.getX(pos), by = floorY+1+BlockPos.getY(pos), bz = z+BlockPos.getZ(pos);
                    if(bx<minX||bx>=minX+size||bz<minZ||bz>=minZ+size||by<=floorY||by>=world.maxHeight())
                        throw new IllegalArgumentException("Tree extends outside the arena");
                    if(!world.supports(entry.getValue().material))
                        throw new IllegalArgumentException("This Minecraft version lacks "+entry.getValue().material+" for "+tree.kind);
                }
            }
            for(int[] origin : origins){
                int x = origin[0], z = origin[1];
                for(Map.Entry<Long, TestTrees.Block> entry : tree.blocks.entrySet()){
                    long pos = entry.getKey();
                    TestTrees.Block block = entry.getValue();
                    world.setBlock(x+BlockPos.getX(pos), floorY+1+BlockPos.getY(pos), z+BlockPos.getZ(pos), block.material, block.axis, block.leafDistance);
                }
            }
            try{
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                // Read every arena block, so the fingerprint also catches extra blocks/terrain.
                for(int bx = minX; bx<minX+size; bx++)for(int by = floorY; by<world.maxHeight(); by++)for(int bz = minZ; bz<minZ+size; bz++){
                    String state = world.blockState(bx, by, bz);
                    digest.update((bx+","+by+","+bz+":"+state+"\n").getBytes(StandardCharsets.UTF_8));
                }
                StringBuilder hash = new StringBuilder();
                for(byte value : digest.digest())hash.append(String.format("%02x", value&255));
                return hash.toString();
            }catch(java.security.NoSuchAlgorithmException ex){throw new AssertionError(ex);}
        });
        ctx.log(tree.kind+" fixtures: "+origins.length+" trees, "+tree.blocks.size()+" blocks each; arena SHA-256 "+fingerprint);
        return fingerprint;
    }
    public void assertLogsRemoved(TestTrees.Tree tree, int x, int z){
        onServer(ctx, () -> {
            for(Map.Entry<Long, TestTrees.Block> entry : tree.blocks.entrySet()){
                TestTrees.Block block = entry.getValue();
                if(block.axis==null)continue;
                long pos = entry.getKey();
                if(world.isMaterial(x+BlockPos.getX(pos), floorY+1+BlockPos.getY(pos), z+BlockPos.getZ(pos), block.material))
                    throw new AssertionError("Unfelled trunk/branch at relative "+BlockPos.getX(pos)+","+BlockPos.getY(pos)+","+BlockPos.getZ(pos));
            }
            return null;
        });
    }
    /** A refused fell still lets Minecraft break the targeted log normally. */
    public void assertOnlyLogRemoved(TestTrees.Tree tree, int x, int z, int cutX, int cutY, int cutZ){
        onServer(ctx, () -> {
            long cut = BlockPos.toPos(cutX, cutY, cutZ);
            for(Map.Entry<Long, TestTrees.Block> entry : tree.blocks.entrySet()){
                if(entry.getValue().axis==null)continue;
                long pos = entry.getKey();
                int bx = x+BlockPos.getX(pos), by = floorY+1+BlockPos.getY(pos), bz = z+BlockPos.getZ(pos);
                boolean correct = pos==cut?world.isMaterial(bx, by, bz, "air"):world.isMaterial(bx, by, bz, entry.getValue().material);
                if(!correct)throw new AssertionError("Expected only the targeted log removed; unexpected log state at relative "
                        +BlockPos.getX(pos)+","+BlockPos.getY(pos)+","+BlockPos.getZ(pos));
            }
            return null;
        });
    }
    /** Wait for completion and propagate main-thread failures to the test runner. */
    public static <T> T onServer(TestContext ctx, Supplier<T> action){
        CompletableFuture<T> result = new CompletableFuture<>();
        ctx.schedule(() -> {
            try{result.complete(action.get());}
            catch(Throwable ex){result.completeExceptionally(ex);}
        });
        try{return result.get(60, TimeUnit.SECONDS);}
        catch(InterruptedException ex){Thread.currentThread().interrupt(); throw new IllegalStateException(ex);}
        catch(Exception ex){throw new IllegalStateException("Test platform operation failed: "+(ex.getCause()==null?ex:ex.getCause()), ex);}
    }
}
