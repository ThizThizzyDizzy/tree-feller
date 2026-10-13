package com.thizthizzydizzy.treefeller.core.connector.world;
import com.thizthizzydizzy.treefeller.core.breaking.FallVector;
/** Native entity operations only. Core decides what happens on impact. */
public interface FallingBlockHandle{
    boolean alive();
    FallVector position();
    FallVector velocity();
    /** Disable gravity and clear motion before holding at an exact render position. */
    void hold(FallVector position);
    void release(FallVector velocity);
    /** Reset native expiry while Core is deliberately holding the entity. */
    void keepAlive();
    void remove();
    void damageOverlappingEntities(float damage);
}
