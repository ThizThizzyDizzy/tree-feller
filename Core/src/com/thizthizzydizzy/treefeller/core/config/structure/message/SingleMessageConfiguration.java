package com.thizthizzydizzy.treefeller.core.config.structure.message;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
public class SingleMessageConfiguration{
    @ConfigComment("Chat text; null disables this channel. Positional placeholders use {0}, {1}, etc.")
    public String chat;
    @ConfigComment("Actionbar text; null disables this channel independently of chat.")
    public String actionbar;
}
