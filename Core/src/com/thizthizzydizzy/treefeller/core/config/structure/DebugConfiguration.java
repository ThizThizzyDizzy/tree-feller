package com.thizthizzydizzy.treefeller.core.config.structure;
import com.thizthizzydizzy.treefeller.core.config.legacy.LegacyBukkitOption;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
public class DebugConfiguration {
    @ConfigComment("List all loaded configuration values in the console during startup")
    @LegacyBukkitOption(value = "startup-logs",
            scopes = {LegacyBukkitOption.Scope.GLOBAL})
    public boolean startup_logs = true;
}
