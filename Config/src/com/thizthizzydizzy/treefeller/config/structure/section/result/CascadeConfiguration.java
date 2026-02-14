package com.thizthizzydizzy.treefeller.config.structure.section.result;
import com.thizthizzydizzy.treefeller.config.ConfigComment;
public class CascadeConfiguration{
    public boolean enable = false;

    @ConfigComment("The maximum number of trees that may be felling at oncefrom a single cascade")
    public int parallel_cascade_limit = 1;

    @ConfigComment("The maximum number of cascade checks that may happen in a single tick")
    public int cascade_check_limit = 64;
}
