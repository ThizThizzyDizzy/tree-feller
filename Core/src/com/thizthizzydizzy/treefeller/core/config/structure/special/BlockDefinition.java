package com.thizthizzydizzy.treefeller.core.config.structure.special;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
/** Shared block-state filters; platforms supply native state values. */
public abstract class BlockDefinition extends com.thizthizzydizzy.treefeller.core.nbt.NbtDefinition implements IBlockDefinition{
    @ConfigComment("Required leaf persistent state: false for natural leaves, true for player-placed leaves, omitted/null for either. Ignored on blocks without this state.")
    public Boolean persistent;

    protected final Object simplify(String material){
        if(persistent==null&&nbt==null)return material;
        java.util.Map<String,Object> definition=new java.util.LinkedHashMap<>();
        if(material!=null)definition.put("material",material);
        if(persistent!=null)definition.put("persistent",persistent);
        if(nbt!=null)definition.put("nbt",nbt);
        return definition;
    }

    public final boolean matchesPersistent(Boolean state){
        return persistent==null||state==null||persistent.equals(state);
    }
}
