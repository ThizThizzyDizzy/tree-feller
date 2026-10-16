package com.thizthizzydizzy.treefeller.core.config.structure.section.result;

public class EffectConfiguration{
    public float chance = 1;
    public EffectLocation location;
    public EffectType type;
    // Empty means all definitions; TOOL/TOOL_BREAK use item definitions, other locations use block definitions.
    public com.thizthizzydizzy.treefeller.core.config.ISpecialConfigObject[] filter = new com.thizthizzydizzy.treefeller.core.config.structure.special.IBlockDefinition[0];
    
    // particles
    public String particle;
    public float x, y, z, dx, dy, dz, speed;
    public int count = 1;
    public float r, g, b;
    public float size = 1;
    public com.thizthizzydizzy.treefeller.core.config.structure.special.IItemDefinition item;
    public String block;
    
    // sounds
    public String sound;
    public float volume = 1, pitch = 1;
    
    // explosions
    public float power;
    public boolean fire;
    
    // markers
    public boolean permanent;
    public String[] tags = new String[0];
}
