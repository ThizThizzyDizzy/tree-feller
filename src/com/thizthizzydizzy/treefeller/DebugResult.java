package com.thizthizzydizzy.treefeller;
public class DebugResult{
    public final Type type;
    public final String message;
    public final Object[] args;
    public DebugResult(Option option, Type type, Object... args){
        this(type, option.getGlobalName(), args);
    }
    public DebugResult(Type type, String message, Object... args){
        this.type = type;
        this.message = message;
        this.args = args;
    }
    public boolean isSuccess(){
        return type==Type.SUCCESS;
    }
    public static enum Type{
        GLOBAL(""),
        TOOL("-tool"),
        TREE("-tree"),
        SUCCESS("-success");
        public final String suffix;
        private Type(String suffix){
            this.suffix = suffix;
        }
    }
}