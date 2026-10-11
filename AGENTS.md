# Project instructions

- All logic, including MCAutoTester tests, belongs in Core. Platform modules are for integration hooks only.
- Only Bukkit is implemented currently. Leave other platforms unimplemented without explicit guards.
- Ask about unresolved design assumptions; reuse decisions the user has already made.

## Testing policy

There are two kinds of persistent tests allowed in this project:

- Traditional code tests, but only for high-risk core math systems, such as the existing block-position tester (`BlockPosLongTester`).
- Full end-to-end functionality tests, which must be implemented through MCAutoTester.

“High-risk” means failures are inherently silent and would cause major issues across the project, but their presence or cause may not be immediately clear.

Both kinds of tests must be explicitly approved by the user.

Ephemeral tests are allowed to verify implementation, but must be removed once that work is done. Do not retain their source files, build targets, or generated test artifacts.

## MCAutoTester test design

- Keep tests simple, short, and focused. Organize them by config section and option; an option may have multiple named tests.
- Use the bare minimum tests needed to cover each condition. Choose cases by the behavior they prove, without a fixed count or exhaustive combinations. For a condition that allows/refuses an action, test both outcomes; when testing a changed option value, verify both outcomes at that value too. Avoid cases that prove nothing new.
- Create all trees/fixtures together in one shared setup. Enlarge the arena when needed, then test them quickly in sequence.
- Spawn intact trees. Do not carve slices or access paths through their foliage during setup; let the client mine any obstructing leaves normally.
- Use only waits needed for reliable actions. Have one camera check at the end, without camera trips or viewing pauses between tests.
- Immediately before each test action, print one chat message identifying the option value and what is being tested (for cutting height, the position and height). Then perform the action.
- Leave debug output in chat unchanged. Do not repeat or pin test labels, redirect debug output, or add status-display machinery. Do not use the action bar for test identification; leave it available for future option tests.
- Place a standing block only when the player needs one and no standing surface is already available. Keep it outside the tree/fixture. Remove temporary blocks before the final camera view, and clean them up on failure.
- Assert the actual outcome, including what should remain after a refused action. Keep setup, actions, assertions, and cleanup in Core; platform code only translates integration hooks.

## Development testing

Run `./test/mcautotester/run.sh` from the repository root to build TreeFeller and run
the script's default tests through MCAutoTester with an automated client and recording.
To select tests, pass their sequence names: `./test/mcautotester/run.sh <sequence> [<sequence> ...]`.
Available sequences are defined in [TreeFellerTestSequences.java](Core/src/com/thizthizzydizzy/treefeller/core/test/TreeFellerTestSequences.java).

The script prints a temporary output directory containing `run.log` and videos in `recordings/`.
Check the log for test results and watch the recordings; the script passes through MCAutoTester's exit status.
New persistent tests require user approval under the testing policy above.
