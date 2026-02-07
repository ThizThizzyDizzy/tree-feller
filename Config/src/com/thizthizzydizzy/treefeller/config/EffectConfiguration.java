package com.thizthizzydizzy.treefeller.config;

public class EffectConfiguration{
    public float chance;
    public EffectLocation location;
    public EffectType type;
    
    // particles
    public String particle;
    public float x, y, z, dx, dy, dz, speed;
    public int count;
    public float r, g, b, size;
    public String item;
    public String block;
    
    // sounds
    public String sound;
    public float volume, pitch;
    
    // explosions
    public float power;
    public boolean fire;
    
    // markers
    public boolean permanent;
    public String[] tags;
}
