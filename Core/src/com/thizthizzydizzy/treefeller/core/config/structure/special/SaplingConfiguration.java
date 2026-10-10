package com.thizthizzydizzy.treefeller.core.config.structure.special;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
import com.thizthizzydizzy.treefeller.core.config.ISpecialConfigObject;
import java.util.LinkedHashMap;
import java.util.Map;
public class SaplingConfiguration implements ISpecialConfigObject{
    @ConfigComment("The sapling block to plant")
    public IBlockDefinition block;

    @ConfigComment("The sapling item to consume")
    public IItemDefinition item;

    @Override
    public Object asSimplified(){
        Object simplifiedBlock = block==null?null:block.asSimplified();
        Object simplifiedItem = item==null?null:item.asSimplified();
        if(simplifiedBlock instanceof String&&simplifiedBlock.equals(simplifiedItem))return simplifiedBlock;
        Map<String, Object> pair = new LinkedHashMap<>();
        pair.put("block", block);
        pair.put("item", item);
        return pair;
    }
}
