package com.thizthizzydizzy.treefeller;
public class DebugResult{
    public final boolean success;
    public final String message;
    public final Object[] args;
    private DebugResult(boolean success, String message, Object... args){
        this.success = success;
        this.message = message;
        this.args = args;
    }
    public DebugResult(Option option, Type type, Object... args){
        this(type==Type.SUCCESS, option.getDebugText()[type.ordinal()], args);
    }
    public static enum Type{
        GLOBAL,TOOL,TREE,SUCCESS;
    }
}