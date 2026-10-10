package com.thizthizzydizzy.treefeller.core.connector.item;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IItemDefinition;
public interface IItemConnector{
    public IItemConnector copyForDrops(boolean fortune, boolean silkTouch);
    public IItemConnector convert(IItemDefinition definition);
    public void setCount(int count);
    public void setDamage(int damage);
    public boolean matches(IItemDefinition definition);
    public Object getItemId();
    public int getMaxDurability();
    public int getCurrentDurability();
    public int getCount();
    public int getUnbreakingLevel();
    public boolean isUnbreakable();
}
