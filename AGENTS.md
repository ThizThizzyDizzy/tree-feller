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

## Development testing

Run `./test/mcautotester/run.sh` from the repository root to build TreeFeller and run
the script's default tests through MCAutoTester with an automated client and recording.
To select tests, pass their sequence names: `./test/mcautotester/run.sh <sequence> [<sequence> ...]`.
Available sequences are defined in [TreeFellerTestSequences.java](Core/src/com/thizthizzydizzy/treefeller/core/test/TreeFellerTestSequences.java).

The script prints a temporary output directory containing `run.log` and videos in `recordings/`.
Check the log for test results and watch the recordings; the script passes through MCAutoTester's exit status.
New persistent tests require user approval under the testing policy above.
