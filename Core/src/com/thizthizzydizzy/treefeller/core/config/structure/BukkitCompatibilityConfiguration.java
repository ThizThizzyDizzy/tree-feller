package com.thizthizzydizzy.treefeller.core.config.structure;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
import com.thizthizzydizzy.treefeller.core.config.legacy.LegacyBukkitOption;
import com.thizthizzydizzy.treefeller.core.config.globaldefault.*;
public class BukkitCompatibilityConfiguration{
    @ConfigGlobalDefaultBoolean(true)
    @ConfigComment("Enable mmocore integration (global only).")
    @LegacyBukkitOption(value = "compatibility-mmocore", scopes = {LegacyBukkitOption.Scope.GLOBAL})
    public Boolean mmocore;
    @ConfigGlobalDefaultBoolean(true)
    @ConfigComment("Enable jobs integration (global only).")
    @LegacyBukkitOption(value = "compatibility-jobs", scopes = {LegacyBukkitOption.Scope.GLOBAL})
    public Boolean jobs;
    @ConfigGlobalDefaultBoolean(true)
    @ConfigComment("Enable mcmmo integration (global only).")
    @LegacyBukkitOption(value = "compatibility-mcmmo", scopes = {LegacyBukkitOption.Scope.GLOBAL})
    public Boolean mcmmo;
    @ConfigGlobalDefaultBoolean(true)
    @ConfigComment("Enable mcmmo classic integration (global only).")
    @LegacyBukkitOption(value = "compatibility-mcmmo-classic", scopes = {LegacyBukkitOption.Scope.GLOBAL})
    public Boolean mcmmo_classic;
    @ConfigGlobalDefaultBoolean(true)
    @ConfigComment("Enable coreprotect integration (global only).")
    @LegacyBukkitOption(value = "compatibility-coreprotect", scopes = {LegacyBukkitOption.Scope.GLOBAL})
    public Boolean coreprotect;
    @ConfigGlobalDefaultBoolean(true)
    @ConfigComment("Enable worldguard integration (global only).")
    @LegacyBukkitOption(value = "compatibility-worldguard", scopes = {LegacyBukkitOption.Scope.GLOBAL})
    public Boolean worldguard;
    @ConfigGlobalDefaultBoolean(true)
    @ConfigComment("Enable griefprevention integration (global only).")
    @LegacyBukkitOption(value = "compatibility-griefprevention", scopes = {LegacyBukkitOption.Scope.GLOBAL})
    public Boolean griefprevention;
    @ConfigGlobalDefaultBoolean(true)
    @ConfigComment("Enable towny integration (global only).")
    @LegacyBukkitOption(value = "compatibility-towny", scopes = {LegacyBukkitOption.Scope.GLOBAL})
    public Boolean towny;
    @ConfigGlobalDefaultBoolean(false)
    @ConfigComment("Enable oreregenerator integration (global only).")
    @LegacyBukkitOption(value = "compatibility-oreregenerator", scopes = {LegacyBukkitOption.Scope.GLOBAL})
    public Boolean oreregenerator;
    @ConfigGlobalDefaultBoolean(true)
    @ConfigComment("Enable drop2inventory integration (global only).")
    @LegacyBukkitOption(value = "compatibility-drop2inventory", scopes = {LegacyBukkitOption.Scope.GLOBAL})
    public Boolean drop2inventory;
    @ConfigGlobalDefaultBoolean(true)
    @ConfigComment("Enable ecoskills integration (global only).")
    @LegacyBukkitOption(value = "compatibility-ecoskills", scopes = {LegacyBukkitOption.Scope.GLOBAL})
    public Boolean ecoskills;
    @ConfigGlobalDefaultBoolean(true)
    @ConfigComment("Enable ecojobs integration (global only).")
    @LegacyBukkitOption(value = "compatibility-ecojobs", scopes = {LegacyBukkitOption.Scope.GLOBAL})
    public Boolean ecojobs;
    @ConfigGlobalDefaultBoolean(false)
    @ConfigComment("Enable logblock legacy integration (global only).")
    @LegacyBukkitOption(value = "compatibility-logblock-legacy", scopes = {LegacyBukkitOption.Scope.GLOBAL})
    public Boolean logblock_legacy;
    @ConfigGlobalDefaultBoolean(true)
    @ConfigComment("Enable logblock integration (global only).")
    @LegacyBukkitOption(value = "compatibility-logblock", scopes = {LegacyBukkitOption.Scope.GLOBAL})
    public Boolean logblock;
    @ConfigGlobalDefaultBoolean(true)
    @ConfigComment("Enable lands integration (global only).")
    @LegacyBukkitOption(value = "compatibility-lands", scopes = {LegacyBukkitOption.Scope.GLOBAL})
    public Boolean lands;
    @ConfigGlobalDefaultBoolean(true)
    @ConfigComment("Enable placeholderapi integration (global only).")
    @LegacyBukkitOption(value = "compatibility-placeholderapi", scopes = {LegacyBukkitOption.Scope.GLOBAL})
    public Boolean placeholderapi;
    @ConfigGlobalDefaultBoolean(true)
    @ConfigComment("Enable saberfactions integration (global only).")
    @LegacyBukkitOption(value = "compatibility-saberfactions", scopes = {LegacyBukkitOption.Scope.GLOBAL})
    public Boolean saberfactions;
    @ConfigGlobalDefaultBoolean(true)
    @ConfigComment("Enable aureliumskills integration (global only).")
    @LegacyBukkitOption(value = "compatibility-aureliumskills", scopes = {LegacyBukkitOption.Scope.GLOBAL})
    public Boolean aureliumskills;
    @ConfigGlobalDefaultBoolean(true)
    @ConfigComment("Enable auraskills integration (global only).")
    @LegacyBukkitOption(value = "compatibility-auraskills", scopes = {LegacyBukkitOption.Scope.GLOBAL})
    public Boolean auraskills;
    @ConfigGlobalDefaultBoolean(false)
    @ConfigComment("Enable blockregen integration (global only).")
    @LegacyBukkitOption(value = "compatibility-blockregen", scopes = {LegacyBukkitOption.Scope.GLOBAL})
    public Boolean blockregen;
    @ConfigGlobalDefaultBoolean(false)
    @ConfigComment("Enable blockregen v3 25 plus integration (global only).")
    @LegacyBukkitOption(value = "compatibility-blockregen-v3.25+", scopes = {LegacyBukkitOption.Scope.GLOBAL})
    public Boolean blockregen_v3_25_plus;
    @ConfigGlobalDefaultBoolean(true)
    @ConfigComment("Enable prism 2 integration (global only).")
    @LegacyBukkitOption(value = "compatibility-prism-2", scopes = {LegacyBukkitOption.Scope.GLOBAL})
    public Boolean prism_2;
    @ConfigGlobalDefaultBoolean(true)
    @ConfigComment("Enable prism 3 integration (global only).")
    @LegacyBukkitOption(value = "compatibility-prism-3", scopes = {LegacyBukkitOption.Scope.GLOBAL})
    public Boolean prism_3;
    @ConfigGlobalDefaultBoolean(true)
    @ConfigComment("Enable prism 4 integration (global only).")
    @LegacyBukkitOption(value = "compatibility-prism-4", scopes = {LegacyBukkitOption.Scope.GLOBAL})
    public Boolean prism_4;
    @ConfigGlobalDefaultBoolean(true)
    @ConfigComment("Enable auroracollections integration (global only).")
    @LegacyBukkitOption(value = "compatibility-auroracollections", scopes = {LegacyBukkitOption.Scope.GLOBAL})
    public Boolean auroracollections;
    @ConfigGlobalDefaultBoolean(true)
    @ConfigComment("Enable quests integration (global only).")
    @LegacyBukkitOption(value = "compatibility-quests", scopes = {LegacyBukkitOption.Scope.GLOBAL})
    public Boolean quests;
    @ConfigGlobalDefaultBoolean(true)
    @ConfigComment("Mcmmo double drops.")
    @LegacyBukkitOption(value = "mcmmo-double-drops", scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE})
    public Boolean mcmmo_double_drops;
    @ConfigGlobalDefaultBoolean(false)
    @ConfigComment("Mmocore emulate regen.")
    @LegacyBukkitOption(value = "mmocore-emulate-regen", scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TREE})
    public Boolean mmocore_emulate_regen;
    @ConfigGlobalDefaultBoolean(true)
    @ConfigComment("Auraskills apply modifiers.")
    @LegacyBukkitOption(value = "auraskills-apply-modifiers", scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE})
    public Boolean auraskills_apply_modifiers;
    @ConfigGlobalDefaultBoolean(true)
    @ConfigComment("Aureliumskills apply modifiers.")
    @LegacyBukkitOption(value = "aureliumskills-apply-modifiers", scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE})
    public Boolean aureliumskills_apply_modifiers;
    @ConfigGlobalDefaultBoolean(true)
    @ConfigComment("Ecojobs apply multipliers.")
    @LegacyBukkitOption(value = "ecojobs-apply-multipliers", scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE})
    public Boolean ecojobs_apply_multipliers;
    @ConfigGlobalDefaultInteger(1200)
    @ConfigComment("Oreregenerator regen delay.")
    @LegacyBukkitOption(value = "oreregenerator-regen-delay", scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TREE})
    public Integer oreregenerator_regen_delay;
    @ConfigGlobalDefaultNewInstance
    @ConfigComment("Per-block XP by skill/job/profession ID.")
    @LegacyBukkitOption(value = "mmocore-trunk-xp", scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TREE})
    public java.util.HashMap<String,Double> mmocore_trunk_xp;
    @ConfigGlobalDefaultNewInstance
    @ConfigComment("Per-block XP by skill/job/profession ID.")
    @LegacyBukkitOption(value = "mmocore-leaves-xp", scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TREE})
    public java.util.HashMap<String,Double> mmocore_leaves_xp;
    @ConfigGlobalDefaultNewInstance
    @ConfigComment("Per-tree XP by skill/profession ID.")
    @LegacyBukkitOption(value = "mmocore-tree-xp", scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TREE})
    public java.util.HashMap<String,Double> mmocore_tree_xp;
    @ConfigGlobalDefaultNewInstance
    @ConfigComment("Per-block XP by skill/job/profession ID.")
    @LegacyBukkitOption(value = "ecoskills-trunk-xp", scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TREE})
    public java.util.HashMap<String,Double> ecoskills_trunk_xp;
    @ConfigGlobalDefaultNewInstance
    @ConfigComment("Per-block XP by skill/job/profession ID.")
    @LegacyBukkitOption(value = "ecoskills-leaves-xp", scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TREE})
    public java.util.HashMap<String,Double> ecoskills_leaves_xp;
    @ConfigGlobalDefaultNewInstance
    @ConfigComment("Per-tree XP by skill/profession ID.")
    @LegacyBukkitOption(value = "ecoskills-tree-xp", scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TREE})
    public java.util.HashMap<String,Double> ecoskills_tree_xp;
    @ConfigGlobalDefaultNewInstance
    @ConfigComment("Per-block XP by skill/job/profession ID.")
    @LegacyBukkitOption(value = "ecojobs-trunk-xp", scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE})
    public java.util.HashMap<String,Double> ecojobs_trunk_xp;
    @ConfigGlobalDefaultNewInstance
    @ConfigComment("Per-block XP by skill/job/profession ID.")
    @LegacyBukkitOption(value = "ecojobs-leaf-xp", scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE})
    public java.util.HashMap<String,Double> ecojobs_leaf_xp;
    @ConfigGlobalDefaultNewInstance
    @ConfigComment("Per-block XP by skill/job/profession ID.")
    @LegacyBukkitOption(value = "auraskills-trunk-xp", scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TREE})
    public java.util.HashMap<String,Double> auraskills_trunk_xp;
    @ConfigGlobalDefaultNewInstance
    @ConfigComment("Per-block XP by skill/job/profession ID.")
    @LegacyBukkitOption(value = "auraskills-leaves-xp", scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TREE})
    public java.util.HashMap<String,Double> auraskills_leaves_xp;
    @ConfigGlobalDefaultNewInstance
    @ConfigComment("Per-block XP by skill/job/profession ID.")
    @LegacyBukkitOption(value = "aureliumskills-trunk-xp", scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TREE})
    public java.util.HashMap<String,Double> aureliumskills_trunk_xp;
    @ConfigGlobalDefaultNewInstance
    @ConfigComment("Per-block XP by skill/job/profession ID.")
    @LegacyBukkitOption(value = "aureliumskills-leaves-xp", scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TREE})
    public java.util.HashMap<String,Double> aureliumskills_leaves_xp;
    @ConfigGlobalDefaultNewInstance
    @ConfigComment("Mmocore required profession level.")
    @LegacyBukkitOption(value = "mmocore-required-profession-level", scopes = {LegacyBukkitOption.Scope.GLOBAL, LegacyBukkitOption.Scope.TOOL, LegacyBukkitOption.Scope.TREE})
    public java.util.HashMap<String,Integer> mmocore_required_profession_level;
}
