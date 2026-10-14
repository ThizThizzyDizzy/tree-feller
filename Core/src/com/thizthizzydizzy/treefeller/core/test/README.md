Reusable MCAutoTester fixtures live here in Core. Platform code implements only
`ITestPlatform` and translates semantic block names/states to native APIs. Call
`TestArena.onServer(ctx, ...)` for additional native operations; it waits for
completion and sends failures back to the sequence thread. Do not call blocking
MCAutoTester methods from inside that callback.

Config option tests live under `options/<section>/<Option>Tests.java`. Each class
builds an `OptionTestSuite` for a config path with the bare minimum named scenarios
needed to cover each condition. There is no fixed test count: each case must prove
something distinct. Verify allowed/refused outcomes and verify them again when
testing a changed option value. Follow the test-design rules in
the repository-root `AGENTS.md`: shared setup, quick actions, one chat label before
each action, and one final camera check. Register the class in
`TreeFellerTestSequences.register`. Keep assertions and scenario logic in Core;
platform hooks only translate native operations.

```java
OptionTestSuite suite = new OptionTestSuite("criteria.max-height");
suite.add("max-0-base", "global.criteria.max-height = 0", (ctx, platform) -> {
    // Announce this case once in chat, cut through ctx.breakBlock, and assert the result.
});
suite.register(platform);
```

Each scenario starts with fresh platform defaults plus its HOCON config snippet,
using the real config reader. The prior active config is restored even on failure;
no config files are changed. Use `suite.lifecycle(prepare, finish, cleanup)` for
a shared arena: prepare places all fixtures once, scenarios perform quick actions
and assertions, cleanup removes temporary blocks (also on failure), and finish
provides one final camera check. Without lifecycle hooks, scenarios arrange their
own world/player state. Cleanup must be safe to call more than once.
The suite waits for the client, logs scenario names/passes, and reports success
only after every assertion passes. Scenarios must not call `ctx.success()`.

Run an entire option or a single scenario:

```sh
./test/mcautotester/run.sh treefeller-option-criteria-max-height
./test/mcautotester/run.sh treefeller-option-criteria-max-height-max-4-middle
```

`criteria.max-height` has four scenarios, using four eight-log spruce trees placed
up front in one 32-block arena. Maximum 0 is tested at the base (0, allowed) and
middle (4, refused). After changing the maximum to 4, the middle (4, allowed at the
inclusive boundary) and top (7, refused) are tested. These pairs establish that
the condition works and that it respects the changed setting. Refused cuts must
remove only the manually mined log;
allowed cuts must remove every log. Each scenario uses a separate intact tree.
Immediately before each cut, `platform.showTestStatus` announces the setting,
cut position/height, and expected outcome once in chat, then mines the log.
Debug output remains unchanged. The action bar is left available for future tests.

Cuts run consecutively without camera pauses. High cuts use temporary supports
four blocks from the trunk, outside the canopy, only where there is no existing
standing surface. All supports are removed and their removal checked before one
final aerial camera view. Trees are spawned intact; the client mines any obstructing
leaves normally while reaching the requested log in survival. The default runner includes this suite
and all criteria and trigger option suites;
new persistent tests still require user approval under `AGENTS.md`.

After `ctx.waitForJoin("testplayer")`:

```java
TestArena arena = TestArena.setup(ctx, platform, "testplayer");
TestTrees.Tree tree = TestTrees.specimen(TestTrees.Kind.BIRCH, 8);
arena.spawn(tree, 0, 2);
TestArena.onServer(ctx, () -> {
    platform.preparePlayer("testplayer", true);
    platform.showTestStatus("testplayer", "Birch fixture: cutting height 1");
    return null;
});
ctx.breakBlock(0, arena.floorY+2, 2);
arena.assertLogsRemoved(tree, 0, 2);
```

The default grass floor is at Y=99, from X/Z=-8 through 7. Setup loads the
intersecting chunks, clears every column above the floor to the exclusive build
ceiling (including mountains and overhangs), sets daytime/clear weather, equips
an iron axe, and teleports the creative player onto the platform. The overload
accepts custom coordinates, size, and floor height. No chunks are force-loaded;
the player keeps this small arena loaded normally. Use a larger arena for multiple
trees and place their full crowns inside its bounds. These helpers deliberately
replace arena blocks and are intended for disposable test worlds.

`TestTrees.Kind` includes all Overworld wood species, all three poplar colors,
azalea, crimson/warped fungi, and giant spruce/jungle variants. These are fixed,
representative block specimens, rather than reproductions of a particular
version's random feature generator. `specimen(kind)` chooses the canned height;
`specimen(kind, height)` selects an exact trunk height of 4..32 for kinds marked
`variableHeight`. Branched acacia/cherry and dark/pale oak, mangrove, and azalea
have fixed specimens. Leaf distance uses six-neighbor distance from logs. Leaves
are nonpersistent, like naturally generated leaves, and can decay when unsupported.

`arena.supports(kind)` queries the running version's material capabilities.
Species introduced in newer Minecraft versions cannot exist on older versions;
spawning one fails explicitly before any blocks are placed. Bukkit's 1.8 adapter
maps the original six species to legacy log/leaf metadata; the 1.13+ adapter uses
native block data, including axes and leaf distance. Other platforms can provide
an `ITestPlatform` and pass it to `TreeFellerTestSequences.register`.

`spawn` returns and logs a SHA-256 of actual block states throughout the arena,
including air and the floor. Equal hashes on the same server version demonstrate
identical inputs. `assertLogsRemoved` checks every trunk and branch block after
felling.

Run two consecutive sequences and record both with:

```sh
./test/mcautotester/run.sh treefeller-option-criteria-max-height treefeller-option-criteria-max-height
```

Criteria coverage includes every field in `CriteriaConfiguration`. Range bounds
(`required-trunk`, `required-leaves`, `height-ratio`, `trunk-vertical-ratio`) have
separate `-min` and `-max` sequences that isolate the bound, accept equality, and
refuse an outside value. Each trunk/leaf/decoration filter has separate
`-required` and `-blacklist` sequences for present and absent materials.
Decorations use two explicitly detectable materials next to intact trunks.
`require-cross-section` covers disabled felling, a single-log section, and
consecutive cuts through a 2x2 section, verifying all remaining logs after each
refusal and felling on the final cut. `trunk-max-horizontal-line` checks limits
one and two against narrow, 2x2, and branched trees. Each option and named case
is independently selectable using its `treefeller-option-criteria-...` name.

Sapling replant coverage has separate suites for `result.sapling-replant.enable`,
`use-tree-saplings`, `use-inventory-saplings`, `spawn-saplings`, `sapling-timeout`,
and `grasses`, plus `trees.saplings` and `trees.max-saplings`. Intact spruce
specimens share one setup per suite. Silk-touch leaf drops converted to saplings
provide deterministic tree sources; assertions count both planted blocks and
unconsumed items. Cases also cover animation, delayed falling-leaf drops,
blocked planting sites, retained stumps, default species, and block/item pairs.
Timeout cases observe server ticks and supply inventory items before and after
the deadline at two configured values. Each suite ends with one camera view.

Sapling priority cases use seven connected, intact spruce columns with heights
5, 7, 4, 10, 6, 9, and 8. With either `max-saplings = 2` or only two inventory
saplings available, exact-position assertions require saplings beneath the
10-log and 9-log columns and air at every shorter column's base. Both cases
also assert complete tree removal and the exact unconsumed inventory count.
