package com.thizthizzydizzy.treefeller.core.config.structure;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
/** Bukkit defaults and optional tree/tool overrides use the shared config reader/writer. */
public class BukkitConfiguration{
    @ConfigComment("Send cancellable Bukkit BlockBreakEvents for additional tree blocks. The original block already has its event.")
    @com.thizthizzydizzy.treefeller.core.config.globaldefault.ConfigGlobalDefaultBoolean(false)
    @com.thizthizzydizzy.treefeller.core.config.legacy.LegacyBukkitOption(value = "send-block-break-events",
        scopes = {com.thizthizzydizzy.treefeller.core.config.legacy.LegacyBukkitOption.Scope.GLOBAL,
            com.thizthizzydizzy.treefeller.core.config.legacy.LegacyBukkitOption.Scope.TOOL,
            com.thizthizzydizzy.treefeller.core.config.legacy.LegacyBukkitOption.Scope.TREE})
    public Boolean send_block_break_events;
    @com.thizthizzydizzy.treefeller.core.config.globaldefault.ConfigGlobalDefaultNewInstance
    public BukkitCompatibilityConfiguration compatibility;
}
