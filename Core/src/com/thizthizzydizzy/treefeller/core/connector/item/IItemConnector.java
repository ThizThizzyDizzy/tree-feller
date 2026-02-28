package com.thizthizzydizzy.treefeller.core.connector.item;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IItemDefinition;
public interface IItemConnector{
    public boolean matches(IItemDefinition definition);
}
