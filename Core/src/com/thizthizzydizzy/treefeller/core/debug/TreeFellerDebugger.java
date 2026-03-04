package com.thizthizzydizzy.treefeller.core.debug;
public class TreeFellerDebugger{
    public static DebuggerContext begin(Object... log){
        DebuggerContext context = new DebuggerContext();
        context.info(log);
        return context;
    }
}
