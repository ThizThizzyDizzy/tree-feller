package com.thizthizzydizzy.treefeller.config;
import com.thizthizzydizzy.treefeller.config.hjson.SerializedComment;
public class CascadeConfiguration {
    public boolean enable = false;
    
    @SerializedComment("The maximum number of trees that may be felling at oncefrom a single cascade")
    public int parallel_cascade_limit = 1;
    
    @SerializedComment("The maximum number of cascade checks that may happen in a single tick")
    public int cascade_check_limit = 64;
}
