package com.thizthizzydizzy.treefeller.core.connector.world;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IBlockDefinition;
import com.thizthizzydizzy.treefeller.core.connector.item.IItemConnector;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import java.util.Collection;
public interface IWorldConnector{
    public Collection<IItemConnector> getDrops(long pos, IItemConnector tool, IPlayerConnector player);
    public void removeBlock(long pos);
    public void setBlock(long pos, IBlockDefinition definition);
    public void dropItem(long pos, IItemConnector item);
    public float getDayTime();
    public float getMoonPhase();
    public String getDimension();
    public String getBiome(long pos);
    public boolean matches(long pos, IBlockDefinition block);
    public BlockAxis getBlockAxis(long pos);
    public int getLeafDistance(long pos);
}
