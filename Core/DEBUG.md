# Debugging TreeFeller

All settings live in `debug`. Existing `chat`, `block-changes`, and
`startup-logs` switches remain supported. Debug text and world overlays are
independent; enabling one does not enable the other. `console` independently
routes the same text to the server log. Debug output is disabled by default.

```hocon
debug {
  chat = true
  console = false
  block-changes = true
  text-level = USER
  overlay-level = USER
  phases {
    detection { text = TRACE, overlay = TRACE }
    breaking { text = DETAIL, overlay = DETAIL }
    effects { text = OFF, overlay = OFF }
  }
  overlay-duration = 100
  overlay-refresh = 10
  overlay-limit = 4096
  overlay-palette {
    REJECTED = redstone_block
    RETAINED = coal_block
    REPLANT = emerald_ore
  }
  step-through = false
  step-limit = 20000
  startup-logs = true
}
```

| Level | Text | World overlay |
| --- | --- | --- |
| `OFF` | Disabled | Disabled |
| `USER` | Readable outcomes, costs and refusal reasons | Detected tree, accepted/refused cut, selected/retained blocks and final outcomes |
| `SUMMARY` | Adds condition measurements/limits and phase or batch totals | Adds intermediate phase summaries where applicable |
| `DETAIL` | Adds effective settings, individual blocks, fall plans, drop and planting decisions | Adds per-block decisions, effect sites, cascade boundaries and obstacles |
| `TRACE` | Adds every scan candidate/rejection, filter checks, random rolls and falling positions | Adds scan/search/filter candidates and rejected positions, including air |

Levels include all lower levels. Both channel levels default to `DETAIL` for
compatibility with the previous enabled debug output. Use `USER` for the minimal
player-facing mode, or `TRACE` for maximum detail. The minimal mode follows v1's
readable reasons and outcomes, while suppressing internal scan steps and
unmatched tool candidates. Condition failures include the measured value and
required limit wherever applicable.

Every phase has optional `text` and `overlay` overrides: `trigger`, `detection`,
`criteria`, `cutting`, `breaking`, `result`, `replant`, `effects`, `cascade`, and
`events`. Unset or `null` overrides inherit the channel default; `OFF` disables
that phase. The channel's boolean switch still gates all phase overrides.

On Bukkit, overlays send fake blocks only to the initiating player. Core owns
the semantic marker types, lifetime, limits and playback; Bukkit translates them
to block changes. Other platforms can translate the same markers to their world
visualization. No real blocks are changed by the overlay.

Detection retains the original material legend: coal blocks for roots, gold for
trunk, iron for leaves, diamond for decorations, obsidian for extended leaves,
redstone/stone for secondary trunks/leaves, ores for disconnected sections, and
emerald for leaves assigned through extended scans. Later phases use emerald
for accepted/selected positions, coal for retained blocks, air for removed
blocks, lapis for conversions/drops, gold for landings, emerald ore for replanting,
glowstone for effects, and diamond for cascade candidates. TRACE adds glass
candidates and red stained glass rejections (plain glass on legacy Bukkit). Markers at a shared position are replaced by
the latest diagnostic event. `overlay-palette` can override any name from
`OverlayMark` with a Minecraft block material. Unsupported palette materials use
the default marker for the running Bukkit version.

`overlay-duration` is measured in server ticks from each update, defaulting to
100 (five seconds at 20 TPS). Zero retains markers until cleared. Expiry and clear
restore the **current** real block state, including its block data. Retained stump
and excluded branch markers replace their earlier detected markers. Quit, world
change, and plugin disable also discard traces and overlays. `overlay-limit`
bounds simultaneously displayed positions; existing positions can still update
when the limit is reached. `overlay-refresh` reapplies active markers every ten
server ticks by default, so later real block updates and chunk packets do not
permanently erase them. Zero disables refresh. Refresh preserves the last marker
and its original expiry; it does not add or advance recorded steps.

## Step-through playback

Enable `debug.step-through`, cut normally, then use:

```text
/treefeller debug status
/treefeller debug next
/treefeller debug next layer
/treefeller debug next phase
/treefeller debug next all
/treefeller debug clear
```

Bukkit requires `treefeller.debug` permission (operators by default). Other
platforms can expose Core's `next`, `pending`, and `clear` hooks through any
appropriate trigger. A bare `next` emits one recorded text line or world marker;
`next [count]` accepts 1 through 1000. The text and overlay events share their
original order. Console text associated with the player also waits for playback.

Replay tiers use recorded boundaries, never message wording: `next event` (or bare `next`) emits one text line or marker; `next layer` finishes the current scan pass, or consumes the phase preamble and its first scan pass. It always stops before another phase. With no scan layers it finishes the current phase. `next phase` finishes the current contiguous phase segment; revisiting a phase later is a new segment. `next all` explicitly drains the remaining trace. Numeric counts remain supported for compatibility. `status` reports the next phase and scan-layer label. Text and world markers share the same chronological boundaries.

Playback records immutable diagnostic values while real felling, animations,
landings, drops and planting run normally. It does not rewind the world or pause
gameplay. This avoids splitting the original block-break event or changing fall
physics and cooldown timing. Overlay lifetimes start when a marker is played.
`step-limit` bounds queued events per player, with a notice if output is truncated.
`clear` discards pending events and restores displayed positions. Playerless
console diagnostics are emitted immediately because they have no next trigger.

## Verification

```sh
./test/mcautotester/run.sh treefeller-option-debug-text-level \
  treefeller-option-debug-overlay-level treefeller-option-debug-step-through
```

Each option suite spawns its intact connected tree pairs together. Real client cuts exercise
all felling phases at USER and TRACE independently for text and overlays, verify
felling and planting outcomes, and assert emitted phase coverage, representative
decision details and overlay markers. It also verifies timed restoration and the
Bukkit next/clear commands. The final camera shows the maximum world overlay.

To inspect the air markers specifically:

```sh
./test/mcautotester/run.sh treefeller-option-debug-step-through-trace
```

This replay pauses at a clear-glass air candidate, its red-glass rejection, and
the completed first trunk-neighbor layer, using one final camera position. The
test checks that no fixture trunk is rejected and all rejection positions stay
inside that scan layer's 26-neighbor neighborhood. Leaves can be rejected during
the trunk scan and subsequently accepted during the leaf scan. Air markers at TRACE show
where the scanner looked and found no matching material for that scan; they are not
additional detected tree blocks. Lower overlay levels omit these air rejections.
