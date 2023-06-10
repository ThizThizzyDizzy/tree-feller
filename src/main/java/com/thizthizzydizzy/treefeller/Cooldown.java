package com.thizthizzydizzy.treefeller;
import java.util.ArrayList;
import java.util.HashMap;
public class Cooldown{
    public HashMap<Tree, Long> treeCooldowns = new HashMap<>();
    public HashMap<Tool, Long> toolCooldowns = new HashMap<>();
    public long globalCooldown;
    public long getCooldown(Tree tree){
        if(!treeCooldowns.containsKey(tree))return 0;
        return treeCooldowns.get(tree);
    }
    public long getCooldown(Tool tool){
        if(!toolCooldowns.containsKey(tool))return 0;
        return toolCooldowns.get(tool);
    }
    public long getGlobal(){
        long diff = System.currentTimeMillis()-globalCooldown;
        return Math.max(0,Option.COOLDOWN.getValue()-diff);
    }
    public long get(Tree tree){
        long diff = System.currentTimeMillis()-getCooldown(tree);
        return Math.max(0,Option.COOLDOWN.getValue(tree)-diff);
    }
    public long get(Tool tool){
        long diff = System.currentTimeMillis()-getCooldown(tool);
        return Math.max(0,Option.COOLDOWN.getValue(tool)-diff);
    }
    public ArrayList<Long> getCooldowns(){
        ArrayList<Long> cooldowns = new ArrayList<>();
        cooldowns.add(getGlobal());
        for(Tree tree : treeCooldowns.keySet())cooldowns.add(get(tree));
        for(Tool tool : toolCooldowns.keySet())cooldowns.add(get(tool));
        return cooldowns;
    }
}