package com.thizthizzydizzy.treefeller.core.test;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockAxis;
/** Integration hooks only. All calls are made on the server thread. */
public interface ITestPlatform{
    /** Observe native output during a real client action. Close removes every hook. */
    EffectCapture captureEffects(String player);
    interface EffectCapture extends AutoCloseable{
        java.util.List<ParticleObservation> particles();
        java.util.List<SoundObservation> sounds();
        java.util.List<MarkerObservation> markers();
        java.util.List<ExplosionObservation> explosions();
        @Override void close();
    }
    final class ParticleObservation{
        public final double x,y,z,dx,dy,dz,speed,r,g,b,size;
        public final int count;
        public final String data;
        public ParticleObservation(double x,double y,double z,double dx,double dy,double dz,double speed,int count,String data,double r,double g,double b,double size){
            this.x=x;this.y=y;this.z=z;this.dx=dx;this.dy=dy;this.dz=dz;this.speed=speed;this.count=count;this.data=data;this.r=r;this.g=g;this.b=b;this.size=size;
        }
    }
    final class SoundObservation{
        public final double x,y,z,volume,pitch;
        public final String sound,category;
        public SoundObservation(double x,double y,double z,double volume,double pitch,String sound,String category){
            this.x=x;this.y=y;this.z=z;this.volume=volume;this.pitch=pitch;this.sound=sound;this.category=category;
        }
    }
    final class MarkerObservation{
        public final double x,y,z;
        public final boolean permanent,alive,invisible,invulnerable,gravity,marker;
        public final java.util.Set<String> tags;
        public MarkerObservation(double x,double y,double z,boolean permanent,boolean alive,boolean invisible,boolean invulnerable,boolean gravity,boolean marker,java.util.Set<String> tags){
            this.x=x;this.y=y;this.z=z;this.permanent=permanent;this.alive=alive;this.invisible=invisible;this.invulnerable=invulnerable;this.gravity=gravity;this.marker=marker;this.tags=tags;
        }
    }
    final class ExplosionObservation{
        public final double x,y,z;
        public final int blocks;
        public ExplosionObservation(double x,double y,double z,int blocks){this.x=x;this.y=y;this.z=z;this.blocks=blocks;}
    }
    ITestWorld world(String player);
    void preparePlayer(String player, boolean survival);
    void setPlayerGameMode(String player, com.thizthizzydizzy.treefeller.core.connector.player.PlayerGameMode mode);
    void setPlayerSneaking(String player, boolean sneaking);
    /** Install explicit permission values; the returned hook removes the attachment. */
    Runnable permissions(String player, java.util.Map<String, Boolean> values);
    /** Equip an item using Minecraft's item definition syntax. */
    void equipItem(String player, String definition);
    void setToolCount(String player, int count);
    void setPlayerLevels(String player, int food, float health, float saturation);
    com.thizthizzydizzy.treefeller.core.connector.item.IItemConnector tool(String player);
    com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector playerState(String player);
    int inventoryItemCount(String player, String material);
    void setInventoryItem(String player, int slot, String material, int count);
    void prepareCamera(String player);
    /** Announce the current test in chat. */
    void showTestStatus(String player, String message);
    final class FallingObservation{
        public final double x,y,z,vx,vy,vz;
        public final String material;
        public FallingObservation(double x,double y,double z,double vx,double vy,double vz,String material){
            this.x=x;this.y=y;this.z=z;this.vx=vx;this.vy=vy;this.vz=vz;this.material=material;
        }
    }
    interface TestTarget{
        float health();
        void remove();
    }
    interface ITestWorld{
        java.util.List<FallingObservation> fallingBlocks();
        void removeFallingBlocks();
        int fireCount(int minX,int minY,int minZ,int maxX,int maxY,int maxZ);
        TestTarget spawnTarget(double x,double y,double z);

        int maxHeight(); // Exclusive build ceiling; arenas deliberately use positive Y.
        boolean supports(String material);
        void setBlock(int x, int y, int z, String material, BlockAxis axis, int leafDistance);
        String blockState(int x, int y, int z);
        boolean isMaterial(int x, int y, int z, String material);
        int droppedItemCount(String material, int minX, int minY, int minZ, int maxX, int maxY, int maxZ);
        void removeDroppedItems(int minX, int minY, int minZ, int maxX, int maxY, int maxZ);
        void loadChunk(int x, int z);
        void daylight();
        long fullTime();
        void setFullTime(long ticks);
        boolean daylightCycle();
        void setDaylightCycle(boolean enabled);
        String dimension();
        String biome(int x, int z);
        void setBiome(int x, int z, String biome);
    }
}
