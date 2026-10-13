package com.thizthizzydizzy.treefeller.core.connector.world;
/** Immutable platform block data, kept opaque to Core. */
public interface BlockSnapshot{
    BlockAxis axis();
    BlockSnapshot withAxis(BlockAxis axis);
}
