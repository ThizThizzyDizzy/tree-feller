package com.thizthizzydizzy.treefeller.core.config.structure;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
public class DebugConfiguration {
    @ConfigComment("List all loaded configuration values in the console during startup")
    public boolean startup_logs = true;
}
