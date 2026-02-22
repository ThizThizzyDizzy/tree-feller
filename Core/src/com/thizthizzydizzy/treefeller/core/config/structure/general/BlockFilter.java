package com.thizthizzydizzy.treefeller.core.config.structure.general;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
public class BlockFilter {
    @ConfigComment("Blocks that MUST be present")
    public String[] required;
    
    @ConfigComment("Blocks that MUST NOT be present")
    public String[] blacklist;
}
