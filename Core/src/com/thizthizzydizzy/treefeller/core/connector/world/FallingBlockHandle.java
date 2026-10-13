package com.thizthizzydizzy.treefeller.core.connector.world;
import com.thizthizzydizzy.treefeller.core.breaking.FallVector;
/** Native entity operations only. Core decides what happens on impact. */
public interface FallingBlockHandle{
    boolean alive();
    FallVector position();
    FallVector velocity();
    void remove();
    void damageOverlappingEntities(float damage);
}
