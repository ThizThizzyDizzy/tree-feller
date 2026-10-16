package com.thizthizzydizzy.treefeller.core.nbt;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
/** Shared metadata for block and item definitions. */
public abstract class NbtDefinition{
    @ConfigComment("SNBT compound. Matches listed tags recursively (extra tags are allowed); conversions recursively merge these tags. Item data uses tag contents before 1.20.5 and serialized components after 1.20.5. Blocks use block entity data; ordinary blocks have an empty compound.")
    public String nbt;
    private String parsedSource;
    private NbtCompound parsed;
    private NbtCompound compound(NbtAccess access){
        if(parsed==null||!nbt.equals(parsedSource)){
            parsed=NbtCompound.read(access.parse(nbt));parsedSource=nbt;
        }
        return parsed;
    }
    public final boolean matchesNbt(NbtAccess access,byte[] actual){
        return nbt==null||compound(access).matches(NbtCompound.read(actual));
    }
    public final boolean appliesNbt(NbtAccess access){return nbt!=null&&!compound(access).isEmpty();}
    public final boolean matchesItemNbt(NbtAccess access,byte[] serialized,String dataKey){
        return nbt==null||compound(access).matches(NbtCompound.read(serialized).child(dataKey));
    }
    public final byte[] mergeItemNbt(NbtAccess access,byte[] serialized,String dataKey){
        NbtCompound original=NbtCompound.read(serialized);
        return original.withChild(dataKey,original.child(dataKey).merge(compound(access))).bytes();
    }
    public final byte[] mergeBlockNbt(NbtAccess access,byte[] serialized){
        NbtCompound original=NbtCompound.read(serialized);
        return original.merge(compound(access)).preserving(original,"id","x","y","z").bytes();
    }
    /** Map keys and material shorthands may append a compound: chest{Lock:"key"}. */
    protected final String readMaterial(String definition){
        int start=definition.indexOf('{');
        if(start<0)return definition.trim();
        nbt=definition.substring(start).trim();
        return definition.substring(0,start).trim();
    }
}
