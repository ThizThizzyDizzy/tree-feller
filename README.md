# Thizzy'z Tree Feller
A Bukkit plugin for Minecraft 1.18+
(Older versions are available for 1.13-1.17)

## Full customization
- Configure custom trees with any combination of blocks for its leaves and logs
- Configure custom tools with custom name, durability, enchantments, and more
- Almost any configuration option can be customized for each tree or tool
- Tools or trees can be configured to only work in specific worlds, times of day, gamemodes, and more!

## Plug-and-play
The default configuration will reliably cut down all vanilla trees, while not disturbing player-made structures.

## Many user-requested features:
- Customizable permissions for each tree or tool
- Global and tool or tree-based cooldowns
- Tree felling animation
- Advanced falling trees
- Effects (particles, sounds, and explosions)
- /treefeller reload
- And more!
## Compatibility with the following plugins:
- McMMO
- MMOCore
- EcoSkills
- Zrip’s Jobs Reborn
- CoreProtect
- GriefPrevention
- WorldGuard
- Towny
- Ore Regenerator
- Drop2Inventory
- And more!

## FAQ
### Does it work with custom trees?
Yes


The tree feller is fully capable of detecting and cutting down trees of any shape and size, provided that the logs are connected.

The default config is configured for vanilla trees. If you have very large trees or trees with multiple or mismatched wood or leaf types, you may have to add your own trees to the config.


Many custom trees are generated improperly, containing diagonally connected or persistent leaves.

If this is the case, you may have to enable ignore-leaf-data, diagonal-leaves, and player-leaves.

### My Tool/Tree settings aren't working!
In version 1.11, the config was overhauled with more options.

As a result of this overhaul, tool and tree-specific options no longer override global values.

For example, if you want a single tree to require no leaves:
1. Set `required-leaves` for every other tree to some value (Default 10)
2. Set the global `required-leaves` to 0
3. Set `required-leaves` for the target tree to 0 (For readability)

### Troubleshooting
If you can't get something to work:
- Make sure you’re using the latest version
- Run /treefeller debug and try to cut down a tree.
- Check the startup logs in the server console

If you are unable to pinpoint the problem, I’m happy to help on discord or GitHub. Make sure to provide the config, startup logs, and the debug information from /treefeller debug

## Tips/Fun Facts
### You can use the tree feller for ores
Add trees using ore for the trunk and with leaves disabled:

[COAL_ORE, STONE, {required-leaves: 0, leaf-range: 0, max-height: 256}]
Add tools for each pickaxe with allowed-trees set to ORE

{type: IRON_PICKAXE, allowed-trees: ORE}
Set allowed-trees for the axes to LOG

{type: IRON_AXE, allowed-trees: LOG}
Fore more specific configuration, allowed-trees should be set to a list of tree indexes, such as [0, 1, 2, 3, 4, 5] for the first 6 trees defined

(you may need to set max-height higher as well)

### You can define more than one tool or tree of the same type
This is useful if you want OR behavior. In this example, a golden axe must have either unbreaking 1 or efficiency 2:

tools:
- {type: GOLDEN_AXE, required-enchantments: {unbreaking: 1}}
- {type: GOLDEN_AXE, required-enchantments: {efficiency: 2}}
### Tools and trees are checked in order
If you have an item that matches two tools, the first one will always be used.

For example:
- {type: IRON_AXE, required-enchantments: {unbreaking: 1}}
- {type: IRON_AXE, required-enchantments: {unbreaking: 2}, effects: [explosion]}

An explosion will never occur, as an iron axe with unbreaking 2 also matches the first tool.

## Commands
/treefeller reload<br>
Permission: treefeller.reload<br>
Reloads the tree feller configuration<br>

/treefeller help<br>
Permission: treefeller.help<br>
Displays help for tree feller commands<br>

/treefeller on<br>
Permission: treefeller.on<br>
Toggles on the tree feller for the player who ran the command<br>

/treefeller off<br>
Permission: treefeller.off<br>
Toggles off the tree feller for the player who ran the command<br>

/treefeller toggle<br>
Permission: treefeller.toggle<br>
Toggles the tree feller for the player who ran the command<br>

/treefeller debug [on|off]<br>
Permission: treefeller.debug<br>
Toggles debug mode on or off globally<br>

/treefeller config<br>
Permission: treefeller.config<br>
Opens the ingame configuration<br>

## Configuration
When you update the tree feller, make sure to add any missing configuration options, as they will not be automatically added

(See config.yml)

Discord
https://discord.gg/dhcPSMt
