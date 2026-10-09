package com.thizthizzydizzy.treefeller.core.config.structure.section.result;
import com.thizthizzydizzy.treefeller.core.config.legacy.LegacyBukkitOption;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
import com.thizthizzydizzy.treefeller.core.config.globaldefault.ConfigGlobalDefaultBoolean;
import com.thizthizzydizzy.treefeller.core.config.globaldefault.ConfigGlobalDefaultInteger;
public class CascadeConfiguration{
    @ConfigGlobalDefaultBoolean(false)
    @LegacyBukkitOption(value = "cascade",
            scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE})
    public Boolean enable;

    @ConfigComment("The maximum number of trees that may be felling at once from a single cascade")
    @ConfigGlobalDefaultInteger(1)
    @LegacyBukkitOption(value = "parallel-cascade-limit",
            scopes = {LegacyBukkitOption.Scope.GLOBAL})
    public Integer parallel_cascade_limit;

    @ConfigComment("The maximum number of cascade checks that may happen in a single tick")
    @ConfigGlobalDefaultInteger(64)
    @LegacyBukkitOption(value = "cascade-check-limit",
            scopes = {LegacyBukkitOption.Scope.GLOBAL})
    public Integer cascade_check_limit;
}
