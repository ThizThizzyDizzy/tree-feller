package com.thizthizzydizzy.treefeller.core.config.structure;
public class TreeFellerConfiguration{
    public DebugConfiguration debug = new DebugConfiguration();
    public GlobalConfiguration global = new GlobalConfiguration();
    public ToolConfiguration[] tools;
    public TreeConfiguration[] trees;
    public MessagesConfiguration messages = new MessagesConfiguration();
}
