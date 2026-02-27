package com.thizthizzydizzy.treefeller.test.definition;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IBlockDefinition;
public class TestBlockDefinition implements IBlockDefinition{
    public String material;
    public TestBlockDefinition(){
    }
    public TestBlockDefinition(String str){
        material = str;
    }
    @Override
    public Object asSimplified(){
        if(material!=null&&material.contains("["))return this;
        return material;
    }
}
