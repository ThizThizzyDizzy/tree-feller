package com.thizthizzydizzy.treefeller.config;
import com.thizthizzydizzy.treefeller.config.hjson.SerializedComment;
public class BlockFilter {
    @SerializedComment("Blocks that MUST be present")
    public String[] required;
    
    @SerializedComment("Blocks that MUST NOT be present")
    public String[] blacklist;
}
