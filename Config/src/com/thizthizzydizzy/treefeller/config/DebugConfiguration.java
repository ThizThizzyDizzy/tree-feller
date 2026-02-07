package com.thizthizzydizzy.treefeller.config;
import com.thizthizzydizzy.treefeller.config.hjson.SerializedComment;
public class DebugConfiguration {
    @SerializedComment("List all loaded configuration values in the console during startup")
    public boolean startup_logs = true;
}
