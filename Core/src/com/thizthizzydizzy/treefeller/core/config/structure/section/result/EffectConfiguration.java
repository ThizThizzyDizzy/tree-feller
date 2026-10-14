package com.thizthizzydizzy.treefeller.core.config.structure.section.result;

public class EffectConfiguration{
    public float chance = 1;
    public EffectLocation location;
    public EffectType type;
    // Empty means all materials; tool locations match the initiating tool.
    public String[] filter = new String[0];
    
    // particles
    public String particle;
    public float x, y, z, dx, dy, dz, speed;
    public int count = 1;
    public float r, g, b;
    public float size = 1;
    public String item;
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
