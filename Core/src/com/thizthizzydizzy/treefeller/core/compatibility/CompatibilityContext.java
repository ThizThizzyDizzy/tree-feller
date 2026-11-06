package com.thizthizzydizzy.treefeller.core.compatibility;
import com.thizthizzydizzy.treefeller.core.connector.world.IWorldConnector;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import com.thizthizzydizzy.treefeller.core.connector.item.IItemConnector;
import com.thizthizzydizzy.treefeller.core.detection.*;
import java.util.List;
public final class CompatibilityContext{
    public final IWorldConnector world;
    public final IPlayerConnector player;
    public final IItemConnector item;
    public final DetectionResult detected;
    public final List<TreeNode> nodes;
    public final com.thizthizzydizzy.treefeller.core.config.structure.BukkitConfiguration config;
    private final java.util.Map<Long,TreeNodeType> types=new java.util.HashMap<>();
    public CompatibilityContext(IWorldConnector world,IPlayerConnector player,IItemConnector item,DetectionResult detected,List<TreeNode> nodes){
        this.world=world;this.player=player;this.item=item;this.detected=detected;this.nodes=java.util.Collections.unmodifiableList(new java.util.ArrayList<>(nodes));
        for(TreeNode node:nodes)types.put(node.pos,node.type);
        config=com.thizthizzydizzy.treefeller.core.config.structure.TreeFellerConfiguration.getCombinedBukkitConfiguration(detected.tree,detected.tool);
        // V1 EcoJobs XP adds all scopes; profession requirements keep the highest requirement.
        config.compatibility.ecojobs_trunk_xp=sum("ecojobs_trunk_xp");
        config.compatibility.ecojobs_leaf_xp=sum("ecojobs_leaf_xp");
        config.compatibility.mmocore_required_profession_level=levels();
    }
    private java.util.List<com.thizthizzydizzy.treefeller.core.config.structure.BukkitCompatibilityConfiguration> scopes(){
        java.util.List<com.thizthizzydizzy.treefeller.core.config.structure.BukkitCompatibilityConfiguration> values=new java.util.ArrayList<>();
        for(com.thizthizzydizzy.treefeller.core.config.structure.BukkitConfiguration scope:new com.thizthizzydizzy.treefeller.core.config.structure.BukkitConfiguration[]{
            com.thizthizzydizzy.treefeller.core.TreeFellerCore.config.bukkit,detected.tree.bukkit,detected.tool.bukkit})if(scope!=null&&scope.compatibility!=null)values.add(scope.compatibility);
        return values;
    }
    private java.util.HashMap<String,Double> sum(String name){
        java.util.HashMap<String,Double> result=new java.util.HashMap<>();
        for(com.thizthizzydizzy.treefeller.core.config.structure.BukkitCompatibilityConfiguration scope:scopes())try{
            java.util.Map<String,Double> xp=(java.util.Map<String,Double>)scope.getClass().getField(name).get(scope);
            if(xp!=null)xp.forEach((key,value) -> result.merge(key,value,Double::sum));
        }catch(ReflectiveOperationException ex){throw new IllegalStateException(ex);}
        return result;
    }
    private java.util.HashMap<String,Integer> levels(){
        java.util.HashMap<String,Integer> result=new java.util.HashMap<>();
        for(com.thizthizzydizzy.treefeller.core.config.structure.BukkitCompatibilityConfiguration scope:scopes())
            if(scope.mmocore_required_profession_level!=null)scope.mmocore_required_profession_level.forEach((key,value) -> result.merge(key,value,Math::max));
        return result;
    }
    public TreeNodeType type(long pos){return types.get(pos);}
}
