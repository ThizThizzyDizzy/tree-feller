package com.thizthizzydizzy.treefeller.compat;
import org.bukkit.block.Block;
public class TestResult{
    public final String plugin;
    public final Block block;
    TestResult(String pugin, Block block){
        this.plugin = pugin;
        this.block = block;
    }
}