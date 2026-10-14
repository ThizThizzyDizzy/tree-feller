# Effects

Configure `effects` inside a global, tree, or tool `result` section. Effects use
v2's usual inheritance: tree and tool lists accumulate; a supplied tree/tool list
replaces the global list. An empty list disables inherited global effects.

```hocon
global.result.effects = [
    { type = PARTICLE, location = TRUNK, particle = FLAME, count = 8,
      dx = 0.25, dy = 0.5, dz = 0.25, speed = 0.01, chance = 0.5 },
    { type = SOUND, location = TOOL, sound = "minecraft:block.wood.break",
      volume = 1, pitch = 0.8 },
    { type = MARKER, location = TOOL_BREAK, permanent = true,
      filter = [iron_axe, diamond_axe], tags = [broken_axe] }
]
```

Each effect requires a `type` and `location`. `TRUNK`, `LEAVES`, and `DECORATION`
play once at each selected block as it is removed, including each delayed cutting
layer. `TOOL` plays once at the initiating cut block. `TOOL_BREAK` plays there
only when the fell consumes a tool, including a tool from a stack. Creative or
unbreakable tools that do not take damage do not trigger tool-break effects.
Effects do not run for refused fells. Block conversions still produce effects at
the original block location and match the original material.

`chance` defaults to 1 and is rolled independently for each eligible location.
Values at or below zero never play; values at or above one always play.
`filter` defaults to an empty list, which accepts every material. At tree blocks
it matches the original block material; at `TOOL` and `TOOL_BREAK` it matches the
initiating held tool. Names ignore case and accept the `minecraft:` prefix.

All positions are block centers. Particle `x`, `y`, and `z` add offsets (default
0). `dx`, `dy`, and `dz` are the particle spread (default 0); `speed` defaults to
0 (negative speeds are forwarded as in v1), and `count` defaults to 1. A count of 0 retains Minecraft's special directional
particle behavior. `particle` names come from the server's Bukkit particle API;
v1 names such as `REDSTONE`, `ITEM_CRACK`, and `BLOCK_CRACK` also work on modern
servers. Dust uses `r`, `g`, `b` (0–255, default 0) and `size` (positive, default
1). Item particles require `item`, a material name. Block particles require
`block`, a material or block-data string such as `"oak_log[axis=x]"`. Particle
data types without a corresponding effect configuration field are rejected.

Sounds use `sound`, a Minecraft sound key, and `volume` and `pitch` (both default
1). They play in the blocks sound category; keys are sent as configured so custom
resource-pack sounds work. Explosions use `power` (default 0)
and `fire` (default false), with ordinary Minecraft explosion damage and fire
behavior. A zero-power explosion has no visible world effect on current Paper.

Markers have `permanent` (default false) and `tags` (default empty list). All
markers carry the `tree_feller` scoreboard tag in addition to configured tags.
Permanent markers are invisible, invulnerable, gravity-free marker armor stands;
temporary markers are zero-radius, zero-duration area effect clouds, as in v1.
Permanent markers remain until removed externally.

Bukkit resolves native effect data before removing blocks or charging durability.
Invalid particle names, particle payloads, or unsupported marker APIs refuse the
fell and appear in the breaking debugger.
The v1 importer expands named effects into v2's inline lists, maps `LOGS` to
`TRUNK`, expands `TREE` to the three block locations, and preserves material
filters.

# Tests

Every option has its own MCAutoTester sequence:
`treefeller-option-result-effects-<option>`. Options are `chance`, `location`,
`type`, `filter`, `particle`, `x`, `y`, `z`, `dx`, `dy`, `dz`, `speed`, `count`,
`r`, `g`, `b`, `size`, `item`, `block`, `sound`, `volume`, `pitch`, `power`,
`fire`, `permanent`, and `tags`. `treefeller-option-result-effects` covers the
list and inheritance. Named cases can also be selected individually.

The Core suites perform real client cuts on intact trees, inspect outgoing native
particle/sound packets, explosion events and world fire, and spawned marker
entities, then clean up captured markers and observation hooks. Run through
`test/mcautotester/run.sh` to record the automated client; all effect suites are
included in the default run.
