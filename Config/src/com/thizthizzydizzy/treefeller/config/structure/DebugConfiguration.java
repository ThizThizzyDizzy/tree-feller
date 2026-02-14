package com.thizthizzydizzy.treefeller.config.structure;
import com.thizthizzydizzy.treefeller.config.ConfigComment;
public class DebugConfiguration {
    @ConfigComment("List all loaded configuration values in the console during startup")
    public boolean startup_logs = true;
}
