# Configuration semantics

- Ranges accept an exact number (`4`), an inclusive interval (`"4-256"`), an open upper bound (`"10+"`), or an object with optional `min`/`max`. Reversed bounds wrap around, for example `"23000-1000"` for time of day. Negative and decimal bounds are supported where appropriate.
- Supplying part of a global section retains its initialized defaults. Missing or null tree/tool sections add no requirements.
- Trigger and criteria requirements are checked independently at global, tree, and tool scopes. All applicable requirements must pass. Failed criteria advance to the next eligible tree/tool combination. Tool tree lists contain zero-based tree indexes.
- Detection settings overlay tree values on globals, including nested block-data rules.
- Cutting, breaking, and result settings combine tree/tool values first, then overlay that combination on globals. Tree/tool booleans use OR; numbers use max; nested objects merge recursively; arrays concatenate; map and other scalar conflicts favor the tool. Explicit tree/tool values can override global defaults. The effective configuration is a copy, so changing it cannot change a source configuration.
- Conversion map keys are simplified block/item strings, including literal dots; values can use full definitions. Definitions that cannot simplify to strings cannot serve as map keys.
- Unknown fields and parsing errors fail with a configuration path rather than being silently ignored. `config.conf` tracks the current schema; it is not a legacy config migration fixture.

Cooldown durations use ticks (50 ms per tick). `PlayerSettings.startCooldown` is available for the eventual successful-felling path; prechecks alone do not start a cooldown. Breaking and result execution are still future stages.
