package com.thizthizzydizzy.treefeller;

import org.bukkit.block.Block;

class TestResult {
    public final String plugin;
    public final Block block;

    TestResult(String pugin, Block block) {
        this.plugin = pugin;
        this.block = block;
    }
}