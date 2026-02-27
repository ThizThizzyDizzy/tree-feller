package com.thizthizzydizzy.treefeller.core;
public class BlockPos{
    private static final int X_BITS = 26;
    private static final int Z_BITS = 26;
    private static final int Y_BITS = 12;

    private static final long X_MASK = (1L<<X_BITS)-1;
    private static final long Y_MASK = (1L<<Y_BITS)-1;
    private static final long Z_MASK = (1L<<Z_BITS)-1;

    public static BlockPos fromLong(long packed){
        int x = (int)(packed>>(Y_BITS+Z_BITS));
        int y = (int)((packed>>Z_BITS)&Y_MASK);
        int z = (int)((packed<<(64-Z_BITS))>>(64-Z_BITS));

        if(y>=(1<<(Y_BITS-1)))y -= (1<<Y_BITS);

        return new BlockPos(x, y, z);
    }

    public final int x;
    public final int y;
    public final int z;

    public BlockPos(int x, int y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public long asLong(){
        long packed = 0L;
        packed |= ((long)x&X_MASK)<<(Y_BITS+Z_BITS);
        packed |= ((long)y&Y_MASK)<<Z_BITS;
        packed |= ((long)z&Z_MASK);
        return packed;
    }
}
