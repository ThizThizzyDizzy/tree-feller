package com.thizthizzydizzy.treefeller.core.config.structure;
import com.thizthizzydizzy.treefeller.core.config.legacy.LegacyBukkitOption;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
import com.thizthizzydizzy.treefeller.core.debug.*;
import java.util.LinkedHashMap;
import java.util.Map;
public class DebugConfiguration {
    @ConfigComment("Send debug text to the initiating player")
    public boolean chat = false;
    @ConfigComment("Send debug text to the server console")
    public boolean console = false;
    @ConfigComment("Enable the world overlay (client-only block changes on Bukkit)")
    public boolean block_changes = false;
    @ConfigComment("OFF, USER (plain outcomes), SUMMARY (phase totals), DETAIL (decisions/blocks), TRACE (every scan candidate)")
    public DebugLevel text_level = DebugLevel.DETAIL;
    public DebugLevel overlay_level = DebugLevel.DETAIL;
    @ConfigComment("Per-phase text/overlay overrides; null inherits the channel level")
    public DebugPhasesConfiguration phases = new DebugPhasesConfiguration();
    @ConfigComment("Overlay lifetime in server ticks; 0 keeps it until cleared or replaced")
    public int overlay_duration = 100;
    @ConfigComment("Reapply active markers every N ticks after Minecraft world/chunk updates; 0 disables refresh")
    public int overlay_refresh = 10;
    @ConfigComment("Maximum displayed positions per player; bounds packet volume")
    public int overlay_limit = 4096;
    @ConfigComment("Optional semantic marker to Minecraft material mappings, e.g. REJECTED = red_stained_glass")
    public Map<String, String> overlay_palette = new LinkedHashMap<>();
    @ConfigComment("Step through debug output with the platform next-step trigger")
    public boolean step_through = false;
    @ConfigComment("Maximum retained steps per player")
    public int step_limit = 20000;
    @ConfigComment("List loaded configuration values during startup")
    @LegacyBukkitOption(value = "startup-logs", scopes = {LegacyBukkitOption.Scope.GLOBAL})
    public boolean startup_logs = true;
    public DebugLevel level(DebugPhase phase, boolean overlay){
        DebugPhaseConfiguration override = phases==null?null:phases.get(phase);
        DebugLevel value = override==null?null:overlay?override.overlay:override.text;
        if(value!=null)return value;
        DebugLevel inherited=overlay?overlay_level:text_level;
        return inherited==null?DebugLevel.DETAIL:inherited;
    }
}
