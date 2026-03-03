package com.thizthizzydizzy.treefeller.core.connector.world;
public class BlockPos{
    private static final int X_BITS = 26;
    private static final int Z_BITS = 26;
    private static final int Y_BITS = 12;

    private static final long X_MASK = (1L<<X_BITS)-1;
    private static final long Y_MASK = (1L<<Y_BITS)-1;
    private static final long Z_MASK = (1L<<Z_BITS)-1;
    public static long toPos(int x, int y, int z){
        long pos = 0L;
        pos |= ((long)x&X_MASK)<<(Y_BITS+Z_BITS);
        pos |= ((long)y&Y_MASK)<<Z_BITS;
        pos |= ((long)z&Z_MASK);
        return pos;
    }
    public static int getX(long pos){
        return (int)(pos>>(Y_BITS+Z_BITS));
    }
    public static int getY(long pos){
        return (int)((pos<<X_BITS)>>(X_BITS+Z_BITS));
    }
    public static int getZ(long pos){
        return (int)((pos<<(64-Z_BITS))>>(64-Z_BITS));
    }
}
