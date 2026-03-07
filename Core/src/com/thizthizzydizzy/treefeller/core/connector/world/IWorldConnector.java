package com.thizthizzydizzy.treefeller.core.connector.world;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IBlockDefinition;
public interface IWorldConnector{
    public float getDayTime();
    public float getMoonPhase();
    public String getDimension();
    public String getBiome(long pos);
    public boolean matches(long pos, IBlockDefinition block);
    public BlockAxis getBlockAxis(long pos);
    public int getLeafDistance(long pos);
}
