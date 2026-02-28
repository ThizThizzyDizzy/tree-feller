package com.thizthizzydizzy.treefeller.core.connector.world;
public interface IWorldConnector{
    public float getDayTime();
    public float getMoonPhase();
    public String getDimension();
    public String getBiome(BlockPos pos);
}
