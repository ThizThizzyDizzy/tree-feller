package com.thizthizzydizzy.treefeller;
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
    public long getGlobalCooldown(){
        return 0;
    }
}