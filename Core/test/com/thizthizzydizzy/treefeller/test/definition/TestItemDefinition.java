package com.thizthizzydizzy.treefeller.test.definition;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IItemDefinition;
public class TestItemDefinition implements IItemDefinition{
    public String material;
    public TestItemDefinition(){
    }
    public TestItemDefinition(String str){
        material = str;
    }
    @Override
    public Object asSimplified(){
        if(material!=null&&material.contains("["))return this;
        return material;
    }
}
