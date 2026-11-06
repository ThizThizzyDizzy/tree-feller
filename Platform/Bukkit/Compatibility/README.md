# Bukkit plugin compatibility

Each leaf directory is an independent Ant project with its own plugin API classpath, matching the Bukkit version projects. Builder compiles these projects after Bukkit Core and before its direct-construction bootstrap. Only TreeFeller connector classes enter the distribution; third-party plugin jars and compile-time API placeholders are never bundled.

Core owns configuration, enablement, protection/level checks, XP selection and aggregation, drop multipliers, lifecycle dispatch, and MCAutoTester scenarios. These projects translate plugin API calls. Bukkit Core translates native break events, world changes, and item entities. Other platforms remain unimplemented.

Version-dependent APIs live under `<plugin>/Version/<version>`, following the Bukkit version connector layout. Package and class suffixes identify the supported API version; `Classic` identifies the mcMMO API family. BlockRegen's `V3_25` connector targets the API introduced in 3.25 and compiles against 3.31.3. Helpers such as `WorldGuardRegionQuery` describe their API role and are not additional compatibility versions.

## Configuration

The top-level `bukkit` section contains `send_block_break_events` and `compatibility`. As with existing configuration fields, kebab-case input aliases are accepted. Compatibility toggles are global, matching v1. XP, modifier, double-drop and regeneration settings can be placed under `trees[].bukkit.compatibility` and `tools[].bukkit.compatibility`; `send_block_break_events` also accepts these overrides. The existing tree/tool combination rules apply. EcoJobs retains additive XP across scopes, and MMOCore profession requirements retain the highest scoped requirement.

```hocon
bukkit {
    send-block-break-events = false
    compatibility {
        worldguard = true
        coreprotect = true
        auraskills-trunk-xp = { "auraskills/foraging" = 1 }
        auraskills-apply-modifiers = true
        mmocore-required-profession-level = { woodcutting = 10 }
    }
}
```

Additional break events exclude the initiating block, which already has its native event. Cancelling an extra event preserves that block. TreeFeller tracks the exact events it emits to prevent recursion. Event drop/XP fields are notification fields, as in v1; Core still owns TreeFeller drops and tool costs. Using both native compatibility callbacks and extra events can cause the receiving plugin to process a block more than once, as in v1.

All v1 compatibility families are included:

| Projects | Integration |
| --- | --- |
| WorldGuard, GriefPrevention, Towny, Lands, SaberFactions | Protection checks |
| CoreProtect, LogBlock/Version/{V1_16_5_1,V1_19_0_0}, Prism/Version/{V2,V3,V4} | Removal, placement and supported item logging |
| McMMO/Version/{Classic,V2_2_007} | Native block XP; modern mcMMO double drops |
| MMOCore | Profession/global XP, level requirements, regeneration |
| AuraSkills, AureliumSkills, EcoSkills, EcoJobs | Configured XP and supported modifiers |
| JobsReborn, Quests | Native break/progress callbacks |
| BlockRegen/Version/{V3_10_1,V3_25}, OreRegenerator | Regeneration callbacks |
| Drop2Inventory, AuroraCollections | Native item/drop callbacks |
| PlaceholderAPI | `treefeller` debug, toggle and cooldown placeholders |

OreRegenerator, both BlockRegen integrations and legacy LogBlock remain opt-in, matching v1. Class/package probes distinguish conflicting APIs; modern mcMMO and its classic fallback never award XP simultaneously. Prism variants have separate package filters. An incompatible optional API does not prevent unrelated integrations from loading, and failures include a stack trace in the server log.

## Build dependencies

Place the corresponding v1 API jars in `Libraries/Bukkit/Plugins/`. Each project's `build.xml` lists its exact compile dependencies. These are the jars previously referenced by the v1 NetBeans project. EcoJobs, EcoSkills, Drop2Inventory and OreRegenerator use the same compile-only `BukkitPluginDependencyPlaceholder.jar` API declarations used by v1; their actual plugins must provide those APIs at runtime. No placeholder implementation is shipped.

## Live tests

Test fixtures and assertions are in `Core/.../test/options/bukkit/BukkitTests.java`. Real plugins are supplied to the normal runner with colon-separated jar paths:

```sh
MCAUTOTESTER_PLUGINS='/path/WorldEdit.jar:/path/WorldGuard.jar:/path/GriefPrevention.jar:/path/PlaceholderAPI.jar:/path/AuraSkills.jar:/path/Quests.jar' \
./test/mcautotester/run.sh \
  treefeller-option-bukkit-send-block-break-events \
  treefeller-option-bukkit-compatibility-worldguard \
  treefeller-option-bukkit-compatibility-griefprevention \
  treefeller-option-bukkit-compatibility-placeholderapi \
  treefeller-option-bukkit-compatibility-auraskills \
  treefeller-option-bukkit-compatibility-auraskills-trunk-xp \
  treefeller-option-bukkit-compatibility-auraskills-leaves-xp \
  treefeller-option-bukkit-compatibility-quests
```

The break-event sequence requires no other plugin. Protection tests use real claims/regions and a non-operator, checking owner/outsider behavior with compatibility enabled and disabled. Placeholder tests resolve the actual PlaceholderAPI expansion after a client fells a tree. AuraSkills tests observe exact mining XP to avoid interference from its ordinary foraging rewards, including zero values and tree/tool overrides. Quests tests register a temporary real blockbreak quest and inspect its progress. Native fixture adapters only translate plugin APIs; Core chooses and cleans up fixtures and asserts results.

The live subset is explicit; compilation alone is not an end-to-end claim for the other integrations. Commercial binaries, external databases, historical server versions, and configuration-specific external systems require separate runtime fixtures.

The live test environment uses Paper 1.21.5, WorldEdit 7.3.16, WorldGuard 7.0.14, GriefPrevention 16.18.5, PlaceholderAPI 2.11.6, AuraSkills 2.3.7, and Quests 3.16.1. Compile-time API versions are independently listed in each connector project.
