package com.thizthizzydizzy.treefeller.core.config.structure.special;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
/** Shared block-state filters; platforms supply native state values. */
public abstract class BlockDefinition implements IBlockDefinition{
    @ConfigComment("Required leaf persistent state: false for natural leaves, true for player-placed leaves, omitted/null for either. Ignored on blocks without this state.")
    public Boolean persistent;

    protected final Object simplify(String material){
        if(persistent==null)return material;
        java.util.Map<String,Object> definition=new java.util.LinkedHashMap<>();
        if(material!=null)definition.put("material",material);
        definition.put("persistent",persistent);
        return definition;
    }

    public final boolean matchesPersistent(Boolean state){
        return persistent==null||state==null||persistent.equals(state);
    }
}
