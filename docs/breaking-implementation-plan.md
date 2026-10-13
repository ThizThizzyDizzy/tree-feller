# Breaking implementation and verification

Basis: local v1 `master` at `508c6fb6897fdff4e24e1db6a7d884c2efceea5d`, especially FellBehavior, NaturalFall, FallingTreeBlock, and DirectionalFallBehavior. Implement the behavior in v2's Core pipeline, not the v1 Bukkit enum/scheduler design.

## Implementation order

1. Add per-behavior `landing = PLACE | BREAK | INVENTORY`. An unspecified landing action preserves in-place breaking for ordinary behavior and placement for natural/entity behavior. Explicit PLACE with both motion flags disabled restores the source in place; explicit INVENTORY supports immediate inventory delivery as well as delivery on impact. Natural/entity/damage options remain independent for trunk, leaves, and decorations.
2. Introduce opaque block-state snapshots, Core vectors and fall plans, and narrow world/entity hooks. Snapshot initiating configuration, original block state, tool, direction, origin, and lowest selected trunk before mutations. Core owns trajectories, placement, drop routing, damage calculation, delayed lifecycle, and completion; Bukkit only translates APIs/events. Other platforms remain unimplemented.
3. Implement one shared direction per fell, fixed north-based yaw, relative player-to-cut-block yaw, cardinal locking, standard velocity components, natural placement, and natural entity trajectories. Bound world-height searches and trajectory calculations and handle zero-height/zero-distance cases safely. Preserve natural mode's v1 distinction from ordinary velocity sums.
4. Implement impact placement/break/inventory, obstruction fallback, entity expiration, and once-only completion. Capture drops before removal; deliver deferred drops once on impact. Preserve creative suppression, overflow, conversions, enchantments, resource costs, and cooldowns. Honor the existing cutting layers and apply fall delay after each layer's removal.
5. Implement result.rotate-logs and result.overrideable-blocks as supporting options. Maintain original block properties and transform pillar axes in Core. Update legacy behavior/direction translation for the newly representable semantics. Existing block conversions take precedence over felling.

## Persistent MCAutoTester coverage

All fixtures, actions, assertions, cleanup, and option selection stay in Core. Each option has selectable named scenarios; shared setup creates intact trees together. Announce each cut once in chat, preserve debug output, use bounded completion waits, and one final camera view per sequence.

- Each of trunk/leaves/decoration behavior: ordinary BREAK and INVENTORY, instant NATURAL, and ordinary/natural entity fall with PLACE/BREAK/INVENTORY. Verify source removal, actual destination blocks/drops/inventory, no early deferred delivery, no duplicates, and no surviving tracked entities. Hurt/no-hurt modes exercise each landing action and each block group.
- natural and fall-as-entities: both flags' outcomes, with the combinations above proving distinct instantaneous and entity movement.
- fall-damage-amount: zero and changed positive amount, real living target hit and missed. fall-damage-maximum: zero cap and changed cap, below-cap and capped hits. Assert target health.
- fall-direction: FIXED, RELATIVE, RANDOM; fixed compass and relative player positions. Random assertions use finite/unit-length/coherent direction rather than expecting a particular random angle.
- fall-direction-yaw: cardinal and diagonal fixed yaw, relative yaw rotation, and wraparound.
- fall-cardinal: enabled/disabled at a non-cardinal angle, including random direction.
- directional/vertical/explosive/random-fall-velocity: zero and changed values, observed spawn velocity and landing. Explosive fixtures include an off-axis branch and the zero-distance center. Random tests check bounds/nonzero movement over multiple blocks, not a chosen direction. Verify component addition independently from natural targeting.
- fall-delay: zero and positive; removed sources and absence of early entities; eventual spawn/landing; cutting-animation interaction with unchanged initiating settings despite config restoration.
- block-conversions: matching and nonmatching blocks, conversion overriding falling, no conversion drops, and tool/tree override behavior.
- rotate-logs: disabled/enabled, vertical and horizontal original axes, natural and entity placement, non-pillar state preservation.
- overrideable-blocks: allowed and forbidden obstruction, support below passable blocks, occupied destination preserved with exactly-once fallback drops.
- Lifecycle/results: full inventory overflow, failed placement, landing/drop/expiration races, creative behavior, delayed config snapshots, drop conversions/enchantments/chances, one resource/cooldown charge per expected batch.
- Legacy mappings: all 15 v1 modes map to v2 fields and are exercised by real client cuts; no new persistent conventional tests.

## Completion

Build, run selected breaking/support sequences using test/mcautotester/run.sh, inspect logs and recordings, fix failures, then run the default regression suite. Review the recorded client actions and final camera views. Retain and link the final videos and test log for the user.

## V1 mode representation

The same fields apply independently to trunk, leaves, and decorations. Hurt variants additionally set `fall-damage-amount` and `fall-damage-maximum`.

| V1 mode | natural | fall-as-entities | landing |
| --- | --- | --- | --- |
| BREAK | false | false | BREAK |
| INVENTORY | false | false | INVENTORY |
| NATURAL | true | false | PLACE |
| FALL / FALL_HURT | false | true | PLACE |
| FALL_BREAK / FALL_HURT_BREAK | false | true | BREAK |
| FALL_INVENTORY / FALL_HURT_INVENTORY | false | true | INVENTORY |
| FALL_NATURAL / FALL_NATURAL_HURT | true | true | PLACE |
| FALL_NATURAL_BREAK / FALL_NATURAL_HURT_BREAK | true | true | BREAK |
| FALL_NATURAL_INVENTORY / FALL_NATURAL_HURT_INVENTORY | true | true | INVENTORY |

Conversion, behavior-mode, and placement scenarios cut an unconverted oak root beside the intact spruce trunk. MCAutoTester's client `breakBlock` holds attack until the target is air; cutting a converted/restored trunk directly would cause it to mine the replacement too.

## Final verification

`./test/mcautotester/run.sh` completed successfully on Paper 1.21.5: **85 option sequences, 277 scenarios passed, zero failures**. MCAutoTester validated and transferred the complete 1280×720 recording (15 minutes 2 seconds). The focused run of revised behavior, conversion, stationary landing, rotation, and obstacle fixtures also passed all 59 scenarios. Recorded client actions and final camera views were reviewed.

- [Full passing run log](/tmp/treefeller-mcautotester.BlDOtys0/run.log)
- [Full passing recording](/tmp/treefeller-mcautotester.BlDOtys0/recordings/paper-1.21.5.mp4)
- [Focused passing recording](/tmp/treefeller-mcautotester.dLTUvZxp/recordings/paper-1.21.5.mp4)

Core and Bukkit compile, and `git diff --check` passes. The builder still reports existing interface errors from unimplemented platform modules while producing the Bukkit artifact; those modules were left unimplemented as required. Runtime validation was performed on Paper 1.21.5.
