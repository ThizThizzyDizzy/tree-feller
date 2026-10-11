Reusable MCAutoTester fixtures live here in Core. Platform code implements only
`ITestPlatform` and translates semantic block names/states to native APIs. Call
`TestArena.onServer(ctx, ...)` for additional native operations; it waits for
completion and sends failures back to the sequence thread. Do not call blocking
MCAutoTester methods from inside that callback.

After `ctx.waitForJoin("testplayer")`:

```java
TestArena arena = TestArena.setup(ctx, platform, "testplayer");
TestTrees.Tree tree = TestTrees.specimen(TestTrees.Kind.BIRCH, 8);
arena.spawn(tree, 0, 2);
TestArena.onServer(ctx, () -> {
    platform.preparePlayer("testplayer", true);
    return null;
});
ctx.waitTicks(40);
ctx.breakBlock(0, arena.floorY+2, 2);
ctx.waitTicks(60);
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
felling. The existing acacia sequence uses both helpers.

Run two consecutive sequences and record both with:

```sh
./test/mcautotester/run.sh treefeller-acacia treefeller-acacia
```
