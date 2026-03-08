package com.thizthizzydizzy.treefeller.core.config.structure.section.detection;
import com.thizthizzydizzy.treefeller.core.config.structure.general.SimpleDirection;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IBlockDefinition;
public class DecorationConfiguration{
    public DecorationConfiguration(){
    }
    public DecorationConfiguration(SimpleDirection direction, boolean column, IBlockDefinition... blocks){
        this.column = column;
        this.blocks = blocks;
        this.direction = direction;
    }
    public IBlockDefinition[] blocks;
    public SimpleDirection direction;
    public boolean column = false;
}
