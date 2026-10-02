# Project instructions

## Testing policy

There are two kinds of persistent tests allowed in this project:

- Traditional code tests, but only for high-risk core math systems, such as the existing block-position tester (`BlockPosLongTester`).
- Full end-to-end functionality tests, which must be implemented through MCAutoTester.

“High-risk” means failures are inherently silent and would cause major issues across the project, but their presence or cause may not be immediately clear.

Both kinds of tests must be explicitly approved by the user.

Ephemeral tests are allowed to verify implementation, but must be removed once that work is done. Do not retain their source files, build targets, or generated test artifacts.
