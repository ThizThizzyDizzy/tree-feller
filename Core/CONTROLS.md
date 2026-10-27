# Controls and configuration editor

`TreeFellerControls` exposes actions independently of commands. Integrations call
`toggle`, `debug`, `reload`, and `save` on the server thread, passing a
`ControlAudience`. Actions enforce their own permissions. `ConfigurationEditor.open`
opens the shared editor. `TreeFellerCommands` is an optional text adapter used by
Bukkit's `/treefeller` integration.

A `ControlAudience` supplies permission checks, messages, an optional player,
semantic screen rendering, and text input. A `Screen` contains named actions and
descriptions, with no inventory slots, item types, Bukkit classes, or client GUI
assumptions. Future integrations can render the same actions as native screens,
custom client interfaces, or other widgets. Console audiences have no player;
help, reload, and debug toggling work without one. Player-specific actions explain
when a player is required.

## Bukkit commands

- `/treefeller help`: permission-filtered usage.
- `/treefeller toggle [on|off]`, `/treefeller on`, `/treefeller off`: personal felling state.
- `/treefeller reload`: reload the saved configuration. A parse failure retains the live configuration.
- `/treefeller debug [on|off]`: toggle chat diagnostics; console, overlay, verbosity,
  and stepping settings remain configured independently.
- `/treefeller debug next [event|layer|phase|all|count]`, `debug clear`, `debug status`:
  existing player trace controls.
- `/treefeller config`: open the configuration editor.

Permissions are `treefeller.help`, `treefeller.toggle`, `treefeller.reload`,
`treefeller.debug`, and `treefeller.config`. Help and personal toggling default to
everyone; administrative capabilities default to operators. Tab completion uses
the same permissions. Custom command feedback uses the existing messages section;
null chat/actionbar values disable those channels.

## Editor

Bukkit renders shared screens in paginated inventories and accepts value input in
chat (type `cancel` to return). All decisions, navigation, edits, and serialization
live in Core. Inventory click/drag protection and chat dispatch live in Bukkit.
Permissions are checked again when editing or saving, including pending inputs.

The editor follows v2's configuration structure. Tools and trees are lists;
effects live under the relevant global/tool/tree `result.effects`; messages and
debug settings have their own sections. Booleans toggle with one click. Other
values use the same syntax as `config.conf`: for example `7`, `"text"`,
`[oak_log, birch_log]`, `{item=diamond_axe}`, or `null`. A missing section can be
created by entering `{}`, and a missing list by entering `[]`, then navigating
into it. Complex values can also be replaced in one edit.

Changes apply immediately after the whole candidate configuration parses
successfully. `Reset / inherit` removes a field (restoring its default/inheritance)
or deletes the selected list entry. Object list entries have move earlier/later
actions; entire lists can also be edited, including primitive lists and maps.
`Save configuration` writes the live config to `config.conf` through a temporary
file and atomic replacement where supported. Unsaved changes can be discarded by
reloading. Reordering/removing trees uses v2's existing positional tree indices;
review any explicit index references when changing their order.

## MCAutoTester

Run:

```sh
./test/mcautotester/run.sh treefeller-option-controls-commands treefeller-option-controls-config
```

Scenarios can also be selected individually using the registered suffixes.
Command actions use the automated client, and toggle scenarios mine intact real
trees and assert the logs that remain. Editor scenarios use native Bukkit
inventory/chat event translation hooks because MCAutoTester's client API has no
inventory-click or free-text-chat request. These hooks exercise the actual editor
renderer, event handlers, and live configuration, rather than substituting a UI
implementation. Their navigation and assertions are entirely in Core. Recordings
include the client-visible screens and messages.
