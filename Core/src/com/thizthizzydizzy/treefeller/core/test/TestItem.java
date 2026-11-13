package com.thizthizzydizzy.treefeller.core.test;
import com.thizthizzydizzy.treefeller.core.nbt.NbtAccess;
import com.thizthizzydizzy.treefeller.core.nbt.NbtCompound;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.Config;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.ConfigFactory;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.ConfigRenderOptions;
import java.util.ArrayList;
import java.util.List;

/** Semantic fixture properties; native item serialization belongs to the platform. */
public final class TestItem{
    public final String material,customData;
    public final Config components;
    public TestItem(String definition){
        int bracket=definition.indexOf('[');
        material=bracket<0?definition:definition.substring(0,bracket);
        String properties=bracket<0?"":definition.substring(bracket+1,definition.length()-1);
        if(properties.startsWith("custom_data=")){
            customData=properties.substring("custom_data=".length());
            components=ConfigFactory.empty();
        }else{
            customData=null;
            // Namespaced component keys use ':' as part of the key, not a separator.
            properties=properties.replaceAll("(?<![\"a-zA-Z0-9_])minecraft:([a-z_]+)=", "$1=");
            components=ConfigFactory.parseString(properties.replace('\'', '"'));
        }
    }
    public static final class AttributeModifier{
        public final String id;
        public final double amount;
        private AttributeModifier(String id,double amount){this.id=id;this.amount=amount;}
    }
    public List<AttributeModifier> attributeModifiers(){
        List<AttributeModifier> result=new ArrayList<>();
        for(Config entry:components.getConfigList("attribute_modifiers"))
            result.add(new AttributeModifier(entry.getString("id"),entry.getDouble("amount")));
        return result;
    }
    public String canBreakBlocks(){return components.getValue("can_break.blocks").render(ConfigRenderOptions.concise());}
    /** Fixture semantics stay in Core; the version connector only applies native NBT. */
    public byte[] legacyCanDestroy(NbtAccess nbt,byte[] original){
        NbtCompound data=NbtCompound.read(original);
        NbtCompound tag=NbtCompound.read(nbt.parse("{CanDestroy:"+canBreakBlocks()+"}"));
        return data.withChild("tag",data.child("tag").merge(tag)).bytes();
    }

}
