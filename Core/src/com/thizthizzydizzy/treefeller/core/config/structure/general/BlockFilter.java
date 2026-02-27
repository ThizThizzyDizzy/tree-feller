package com.thizthizzydizzy.treefeller.core.config.structure.general;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IBlockDefinition;
public class BlockFilter {
    @ConfigComment("Blocks that MUST be present")
    public IBlockDefinition[] required;
    
    @ConfigComment("Blocks that MUST NOT be present")
    public IBlockDefinition[] blacklist;
}
