package com.thizthizzydizzy.treefeller;
import com.thizthizzydizzy.treefeller.compat.TreeFellerCompat;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Objects;
public class ConfigGenerator{
    public static void generateConfiguration(TreeFeller plugin){
        String version = plugin.getDescription().getVersion();
        add("# Tree Feller by ThizThizzyDizzy");
        add("# Version "+version);
        add();
        for(Option o : Option.options){
            if(!o.global)continue;
            add();
            for(Object s : o.getDescription(false)){
                add("# "+s);
            }
            add(o.getGlobalName()+": "+o.writeToConfig());
        }
        add();
        add("# What tools can be used to cut down trees?");
        add("# Format:");
        add("#    - {type: AXE_MATERIAL, <variables>}");
        add("# Any of the following variables may be used: (See above for descriptions)");
        int len = 0;
        for(Option o : Option.options){
            if(!o.tool)continue;
            len = Math.max(len, ("# "+o.getGlobalName()).length());
        }
        for(Option o : Option.options){
            if(!o.tool)continue;
            if(o.global)add("# "+o.getGlobalName());
            else add(normalize("# "+o.getGlobalName(), len)+" "+o.getDesc(false));
        }
        add("# AIR can be used instead of an item name if you want every item, including an empty hand, to fell a tree");
        add("# Examples of valid tools:");
        add("#    - {type: WOODEN_AXE, required-enchantments: {efficiency: 4, unbreaking: 1}, banned-enchantments: {unbreaking: 2}, min-durability: 4, max-durability-percent: 0.9}");
        add("#    - {type: DIAMOND_AXE, required-enchantments: {efficiency: 4}, banned-enchantments: {unbreaking: 2}, required-lore: [A line of lore, Another line of lore]}");
        add("#    - {type: GOLDEN_AXE, required-permissions: [treefeller.example,treefeller.anotherexample]}");
        add("#    - STONE_AXE");
        add("#    - {type: IRON_AXE, allowed-trees: [0,1,3]} <-this tool can only cut down the first, second, and fourth trees defined.");
        add("#    - {type: AIR, damage-mult: 0}");
        add("tools:");
        for(Tool t : TreeFeller.tools){
            add("    - "+t.writeToConfig());
        }
        add();
        add("# What materials count as trees?");
        add("# Format:");
        add("#    - [[TRUNK_MATERIALS], [LEAF_MATERIALS], {<options>}]");
        add("# Any of the following variables may be used: (See above for descriptions)");
        len = 0;
        for(Option o : Option.options){
            if(!o.tree)continue;
            len = Math.max(len, ("# "+o.getGlobalName()).length());
        }
        for(Option o : Option.options){
            if(!o.tree)continue;
            if(o.global)add("# "+o.getGlobalName());
            else add(normalize("# "+o.getGlobalName(), len)+" "+o.getDesc(false));
        }
        add("# Examples of valid trees:");
        add("#    - [[OAK_LOG, OAK_WOOD], OAK_LEAVES]");
        add("#    - [BIRCH_LOG, BIRCH_LEAVES]");
        add("#    - [STONE, NETHERRACK, {damage-mult: 50, allow-partial: false, sapling: OAK_SAPLING}]");
        add("#    - SPRUCE_LOG           <-This will attempt to automatically detect the leaf material.");
        add("trees:");
        for(Tree t : TreeFeller.trees){
            add("    - "+t.writeToConfig());
        }
        add();
        add("# Here, you can create custom effects for trees or tools");
        add("# Fields:");
        add("# name: <value>        This is the effect's name. It is used to assign the effect to a tree or tool");
        add("# chance: <value>      This is the chance of the effect happening (0-1, default 1)");
        add("# location: <value>    This is where the effect will occur. valid options:");
        add("#   logs       The effect will occur at every log in the tree");
        add("#   leaves     The effect will occur at every block of leaves in the tree");
        add("#   tree       The effect will occur at every block in the tree");
        add("#   tool       The effect will occur at the block that was cut down");
        add("#   tool_break The effect will occur at the block that was cut down, when the tool breaks");
        add("# type: <value>     This is what type of effect should occur. Valid options:");
        add("#   particle  A particle effect, such as flame, block, etc. particles");
        add("#   sound     Any sound");
        add("#   explosion An explosion that Will destroy blocks and items- This will occur after the block is destroyed");
        add("#   marker    A marker for use with datapacks (An armor stand or area effect cloud)");
        add("# Particle settings:");
        add("#   particle: <value> The particle to display");
        add("#   x: <value>        The X offset from the center of the block to display the particle (default 0)");
        add("#   y: <value>        The Y offset from the center of the block to display the particle (default 0)");
        add("#   z: <value>        The Z offset from the center of the block to display the particle (default 0)");
        add("#   dx: <value>       The delta X of the particle field (default 0)");
        add("#   dy: <value>       The delta Y of the particle field (default 0)");
        add("#   dz: <value>       The delta Z of the particle field (default 0)");
        add("#   speed: <value>    The speed of the particles (default 0)");
        add("#   count: <value>    The number of particles to display (default 1)");
        add("#   Extra information is required for some particles");
        add("#   For Dust particles:");
        add("#     r: <value>    The Red color channel for this particle, 0-255");
        add("#     g: <value>    The Green color channel for this particle, 0-255");
        add("#     b: <value>    The Blue color channel for this particle, 0-255");
        add("#     size: <value> The size of the particle");
        add("#   for Item particles:");
        add("#     item: <value> The item to be used for this particle");
        add("#   for Block or Falling Dust particles:");
        add("#     block: <value> The block to be used for this particle");
        add("# Sound settings:");
        add("#   sound: <value>     The sound to play");
        add("#   volume: <value>    The volume at which to play the sound (Default 1)");
        add("#   pitch: <value>     The pitch at which to play the sound (0.5-2, Default 1)");
        add("# Explosion settings:");
        add("#   power: <value>     The explosion power, where creepers are 3, tnt 4, charged creepers 5");
        add("#   fire: (true|false) Weather or not to light fires with the explosion (Default false)");
        add("# Marker settings:");
        add("#   permanent: (true|false) If true, an armor stand will be created. If false, an area effect cloud will be created. (Area effect clouds last exactly 1 tick)");
        add("#   tags: [<values>]   A list of tags to apply to the created entity. (Note that the \"tree_feller\" tag is always applied)");
        add("# Examples of valid effects:");
        add("#   - {name: smoke, chance: 1, location: logs, type: particle, particle: smoke, dx: 0.5, dy: 0.5, dz: 0.5, speed: .01, count: 10}");
        add("#   - {name: explosion, chance: .01, location: tool, type: explosion, power: 4}");
        add("#   - {name: ghost sound, chance: .1, location: tree, type: sound, sound: ambient.cave, volume: 10, pitch: 0.5}");
        add("effects:");
        for(Effect e : TreeFeller.effects){
            add("    - "+e.writeToConfig());
        }
        add();
        add("# Here, you can customize what messages are sent the tree feller is unable to cut down a tree. Most options are customizable; The debug messages are provided for reference");
        add("# Format:  prefix-<option name>-suffix: \"<text>\"");
        add("# Valid prefixes are:");
        add("# debug        This is the message used when debug mode is on");
        add("# actionbar    This will be sent to the player's actionbar");
        add("# chat         This will be sent to the player in the chat");
        add("# Valid suffixes are:");
        add("# <no suffix>  This defines what message is sent when this option's global requirement is not met");
        add("# -tool        This defines what message is sent when a tool requirement is not met");
        add("# -tree        This defines what message is sent when a tree requirement is not met");
        add("# -success     This defines what message is sent when global, tree, and tool requirements for are all met");
        add("# In addition to the options, there are a few additional messages that can be customized:");
        add("# toggle           This is sent when the player cuts down a tree when the tree feller is off");
        add("# checking         This is sent for each tree/tool pair the tree feller checks");
        add("# durability-low   This is sent when the tool's durability is too low to fell the tree");
        add("# partial          This is sent when a tree is being partially cut");
        add("# protected        This is sent when a tree cannot be felled due to a protection plugin (This may be on top of that plugin's protected message)");
        add("# success          This is sent when a tree is successfully felled");
        add("# For example, if you want a player to be sent a message in the chat if the tree is too small:");
        add("# chat-required-logs: The tree's too small!");
        add();
        for(Message m : Message.messages){
            if(m.debug!=null)add("debug-"+m.name+": \""+m.debug+"\"");
        }
        for(Message m : Message.messages){
            if(m.chat!=null)add("chat-"+m.name+": \""+m.chat+"\"");
        }
        for(Message m : Message.messages){
            if(m.actionbar!=null)add("actionbar-"+m.name+": \""+m.actionbar+"\"");
        }
        write(new File(plugin.getDataFolder(), "config.yml"));
    }
    public static void main(String[] args){
        if(args.length==1&&args[0].equals("genConfig")){
            TreeFellerCompat.init(null);
            String version = read(new File("src/plugin.yml"));//Not the best way to do it, but it works
            int i = version.indexOf("version:");
            if(i==-1){
                throw new IllegalArgumentException("Version is not specified in plugin.yml");
            }
            version = version.substring(i);
            i = version.indexOf("\n");
            if(i==-1){
                throw new IllegalArgumentException("Version is EOF");
            }
            version = version.substring(8, i).trim();
            add("# Tree Feller by ThizThizzyDizzy");
            add("# Version "+version);
            add();
            for(Option o : Option.options){
                if(!o.global)continue;
                add();
                for(Object s : o.getDescription(false)){
                    add("# "+s);
                }
                if(o.getDefaultConfigValue()==null)add(o.getGlobalName()+":");
                else add(o.getGlobalName()+": "+o.getDefaultConfigValue());
            }
            add();
            add("# What tools can be used to cut down trees?");
            add("# Format:");
            add("#    - {type: AXE_MATERIAL, <variables>}");
            add("# Any of the following variables may be used: (See above for descriptions)");
            int len = 0;
            for(Option o : Option.options){
                if(!o.tool)continue;
                len = Math.max(len, ("# "+o.getGlobalName()).length());
            }
            for(Option o : Option.options){
                if(!o.tool)continue;
                if(o.global)add("# "+o.getGlobalName());
                else add(normalize("# "+o.getGlobalName(), len)+" "+o.getDesc(false));
            }
            add("# AIR can be used instead of an item name if you want every item, including an empty hand, to fell a tree");
            add("# Examples of valid tools:");
            add("#    - {type: WOODEN_AXE, required-enchantments: {efficiency: 4, unbreaking: 1}, banned-enchantments: {unbreaking: 2}, min-durability: 4, max-durability-percent: 0.9}");
            add("#    - {type: DIAMOND_AXE, required-enchantments: {efficiency: 4}, banned-enchantments: {unbreaking: 2}, required-lore: [A line of lore, Another line of lore]}");
            add("#    - {type: GOLDEN_AXE, required-permissions: [treefeller.example,treefeller.anotherexample]}");
            add("#    - STONE_AXE");
            add("#    - {type: IRON_AXE, allowed-trees: [0,1,3]} <-this tool can only cut down the first, second, and fourth trees defined.");
            add("#    - {type: AIR, damage-mult: 0}");
            add("tools:");
            add("    - WOODEN_AXE");
            add("    - STONE_AXE");
            add("    - IRON_AXE");
            add("    - GOLDEN_AXE");
            add("    - DIAMOND_AXE");
            add("    - NETHERITE_AXE");
            add();
            add("# What materials count as trees?");
            add("# Format:");
            add("#    - [[TRUNK_MATERIALS], [LEAF_MATERIALS], {<options>}]");
            add("# Any of the following variables may be used: (See above for descriptions)");
            len = 0;
            for(Option o : Option.options){
                if(!o.tree)continue;
                len = Math.max(len, ("# "+o.getGlobalName()).length());
            }
            for(Option o : Option.options){
                if(!o.tree)continue;
                if(o.global)add("# "+o.getGlobalName());
                else add(normalize("# "+o.getGlobalName(), len)+" "+o.getDesc(false));
            }
            add("# Examples of valid trees:");
            add("#    - [[OAK_LOG, OAK_WOOD], OAK_LEAVES]");
            add("#    - [BIRCH_LOG, BIRCH_LEAVES]");
            add("#    - [STONE, NETHERRACK, {damage-mult: 50, allow-partial: false, sapling: OAK_SAPLING}]");
            add("#    - SPRUCE_LOG           <-This will attempt to automatically detect the leaf material.");
            add("trees:");
            add("    - [[OAK_LOG, OAK_WOOD], OAK_LEAVES, {sapling: OAK_SAPLING, max-saplings: 1}]");
            add("    - [[BIRCH_LOG, BIRCH_WOOD], BIRCH_LEAVES, {sapling: BIRCH_SAPLING, max-saplings: 1}]");
            add("    - [[SPRUCE_LOG, SPRUCE_WOOD], SPRUCE_LEAVES, {sapling: SPRUCE_SAPLING, max-saplings: 4}]");
            add("    - [[JUNGLE_LOG, JUNGLE_WOOD], JUNGLE_LEAVES, {sapling: JUNGLE_SAPLING, max-saplings: 4}]");
            add("    - [[DARK_OAK_LOG, DARK_OAK_WOOD], DARK_OAK_LEAVES, {sapling: DARK_OAK_SAPLING, max-saplings: 4}]");
            add("    - [[ACACIA_LOG, ACACIA_WOOD], ACACIA_LEAVES, {sapling: ACACIA_SAPLING, max-saplings: 1}]");
            add("    - [[OAK_LOG, OAK_WOOD], [AZALEA_LEAVES, FLOWERING_AZALEA_LEAVES], {sapling: [AZALEA, FLOWERING_AZALEA], max-saplings: 1, diagonal-leaves: true}]");
            add("    - [[MANGROVE_LOG, MANGROVE_WOOD], [MANGROVE_ROOTS, MANGROVE_LEAVES], {roots: [MANGROVE_ROOTS], sapling: [MANGROVE_PROPAGULE], max-saplings: 1, max-trunks: 16, max-horizontal-trunk-pillar-length: 16, leaf-detect-range: 16, leaf-break-range: 16, required-logs: 3, root-distance: 16, diagonal-leaves: true}]");
            add("    - [[CRIMSON_STEM, CRIMSON_HYPHAE], [NETHER_WART_BLOCK, SHROOMLIGHT], {sapling: CRIMSON_FUNGUS, max-saplings: 1, grass: [CRIMSON_NYLIUM], diagonal-leaves: true, leaf-detect-range: 8, leaf-break-range: 8}]");
            add("    - [[WARPED_STEM, WARPED_HYPHAE], [WARPED_WART_BLOCK, SHROOMLIGHT], {sapling: WARPED_FUNGUS, max-saplings: 1, grass: [WARPED_NYLIUM], diagonal-leaves: true, leaf-detect-range: 8, leaf-break-range: 8}]");
            add();
            add("# Here, you can create custom effects for trees or tools");
            add("# Fields:");
            add("# name: <value>        This is the effect's name. It is used to assign the effect to a tree or tool");
            add("# chance: <value>      This is the chance of the effect happening (0-1, default 1)");
            add("# location: <value>    This is where the effect will occur. valid options:");
            add("#   logs       The effect will occur at every log in the tree");
            add("#   leaves     The effect will occur at every block of leaves in the tree");
            add("#   tree       The effect will occur at every block in the tree");
            add("#   tool       The effect will occur at the block that was cut down");
            add("#   tool_break The effect will occur at the block that was cut down, when the tool breaks");
            add("# type: <value>     This is what type of effect should occur. Valid options:");
            add("#   particle  A particle effect, such as flame, block, etc. particles");
            add("#   sound     Any sound");
            add("#   explosion An explosion that Will destroy blocks and items- This will occur after the block is destroyed");
            add("#   marker    A marker for use with datapacks (An armor stand or area effect cloud)");
            add("# Particle settings:");
            add("#   particle: <value> The particle to display");
            add("#   x: <value>        The X offset from the center of the block to display the particle (default 0)");
            add("#   y: <value>        The Y offset from the center of the block to display the particle (default 0)");
            add("#   z: <value>        The Z offset from the center of the block to display the particle (default 0)");
            add("#   dx: <value>       The delta X of the particle field (default 0)");
            add("#   dy: <value>       The delta Y of the particle field (default 0)");
            add("#   dz: <value>       The delta Z of the particle field (default 0)");
            add("#   speed: <value>    The speed of the particles (default 0)");
            add("#   count: <value>    The number of particles to display (default 1)");
            add("#   Extra information is required for some particles");
            add("#   For Dust particles:");
            add("#     r: <value>    The Red color channel for this particle, 0-255");
            add("#     g: <value>    The Green color channel for this particle, 0-255");
            add("#     b: <value>    The Blue color channel for this particle, 0-255");
            add("#     size: <value> The size of the particle");
            add("#   for Item particles:");
            add("#     item: <value> The item to be used for this particle");
            add("#   for Block or Falling Dust particles:");
            add("#     block: <value> The block to be used for this particle");
            add("# Sound settings:");
            add("#   sound: <value>     The sound to play");
            add("#   volume: <value>    The volume at which to play the sound (Default 1)");
            add("#   pitch: <value>     The pitch at which to play the sound (0.5-2, Default 1)");
            add("# Explosion settings:");
            add("#   power: <value>     The explosion power, where creepers are 3, tnt 4, charged creepers 5");
            add("#   fire: (true|false) Weather or not to light fires with the explosion (Default false)");
            add("# Marker settings:");
            add("#   permanent: (true|false) If true, an armor stand will be created. If false, an area effect cloud will be created. (Area effect clouds last exactly 1 tick)");
            add("#   tags: [<values>]   A list of tags to apply to the created entity. (Note that the \"tree_feller\" tag is always applied)");
            add("# Examples of valid effects:");
            add("#   - {name: smoke, chance: 1, location: logs, type: particle, particle: smoke, dx: 0.5, dy: 0.5, dz: 0.5, speed: .01, count: 10}");
            add("#   - {name: explosion, chance: .01, location: tool, type: explosion, power: 4}");
            add("#   - {name: ghost sound, chance: .1, location: tree, type: sound, sound: ambient.cave, volume: 10, pitch: 0.5}");
            add("effects:");
            add();
            add("# Here, you can customize what messages are sent the tree feller is unable to cut down a tree. Most options are customizable; The debug messages are provided for reference");
            add("# Format:  prefix-<option name>-suffix: \"<text>\"");
            add("# Valid prefixes are:");
            add("# debug        This is the message used when debug mode is on");
            add("# actionbar    This will be sent to the player's actionbar");
            add("# chat         This will be sent to the player in the chat");
            add("# Valid suffixes are:");
            add("# <no suffix>  This defines what message is sent when this option's global requirement is not met");
            add("# -tool        This defines what message is sent when a tool requirement is not met");
            add("# -tree        This defines what message is sent when a tree requirement is not met");
            add("# -success     This defines what message is sent when global, tree, and tool requirements for are all met");
            add("# In addition to the options, there are a few additional messages that can be customized:");
            add("# toggle           This is sent when the player cuts down a tree when the tree feller is off");
            add("# checking         This is sent for each tree/tool pair the tree feller checks");
            add("# durability-low   This is sent when the tool's durability is too low to fell the tree");
            add("# partial          This is sent when a tree is being partially cut");
            add("# protected        This is sent when a tree cannot be felled due to a protection plugin (This may be on top of that plugin's protected message)");
            add("# success          This is sent when a tree is successfully felled");
            add("# For example, if you want a player to be sent a message in the chat if the tree is too small:");
            add("# chat-required-logs: The tree's too small!");
            add();
            for(Message m : Message.messages){
                add("debug-"+m.name+": \""+m.getDebugText()+"\"");
            }
            write(new File("src/config.yml"));
            return;
        }
        System.out.println("To install the Tree Feller, put this file in the plugins folder on your server");
    }
    private static String read(File file){
        String text = "";
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))){
            String line;
            while((line = reader.readLine())!=null){
                text+="\n"+line;
            }
        }catch(IOException ex){
            throw new RuntimeException(ex);
        }
        if(!text.isEmpty())text = text.substring(1);
        return text;
    }
    private static void write(File file){
        try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)))){
            for(String s : toWrite)writer.write(s+"\n");
        }catch(IOException ex){
            toWrite.clear();
            throw new RuntimeException(ex);
        }
        toWrite.clear();
    }
    private static final ArrayList<String> toWrite = new ArrayList<>();
    private static void add(String s){
        toWrite.add(s);
    }
    private static void add(){
        add("");
    }
    private static int findMaxWidth(Object[] objs){
        int len = 0;
        for(Object o : objs)len = Math.max(len, Objects.toString(o).length());
        return len;
    }
    private static String normalize(String name, int width){
        while(name.length()<width)name+=" ";
        return name;
    }
}