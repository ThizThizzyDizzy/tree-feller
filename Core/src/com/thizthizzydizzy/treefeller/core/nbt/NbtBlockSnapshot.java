package com.thizthizzydizzy.treefeller.core.nbt;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockSnapshot;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockAxis;
/** Block entity metadata travels with a block through conversion or falling placement. */
public final class NbtBlockSnapshot implements BlockSnapshot{
    public final BlockSnapshot state;
    private final NbtCompound nbt;
    public NbtBlockSnapshot(BlockSnapshot state,byte[] serialized){this(state,NbtCompound.read(serialized));}
    private NbtBlockSnapshot(BlockSnapshot state,NbtCompound nbt){this.state=state;this.nbt=nbt;}
    public boolean hasNbt(){return !nbt.isEmpty();}
    public byte[] at(byte[] destination){return nbt.preserving(NbtCompound.read(destination),"id","x","y","z").bytes();}
    @Override public BlockAxis axis(){return state.axis();}
    @Override public BlockSnapshot withAxis(BlockAxis axis){return new NbtBlockSnapshot(state.withAxis(axis),nbt);}
}
