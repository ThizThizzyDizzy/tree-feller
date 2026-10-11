package com.thizthizzydizzy.treefeller.core.config.structure;
import com.thizthizzydizzy.treefeller.core.config.legacy.LegacyBukkitOption;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
public class DebugConfiguration {
    @ConfigComment("Send debug information to players in chat")
    public boolean chat = false;

    @ConfigComment("Show scanned tree nodes to players using debug block changes")
    public boolean block_changes = false;

    @ConfigComment("List all loaded configuration values in the console during startup")
    @LegacyBukkitOption(value = "startup-logs",
            scopes = {LegacyBukkitOption.Scope.GLOBAL})
    public boolean startup_logs = true;
}
