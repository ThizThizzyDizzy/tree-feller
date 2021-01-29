package com.thizthizzydizzy.treefeller;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Material;
public class Tree{
    public final ArrayList<Material> trunk;
    public final ArrayList<Material> leaves;
    public Tree(ArrayList<Material> trunk, ArrayList<Material> leaves){
        this.trunk = trunk;
        this.leaves = leaves;
    }
    public void print(Logger logger){
        String trunks = "";//<editor-fold defaultstate="collapsed">
        for(Material m : trunk){
            trunks+=m+", ";
        }
        if(!trunks.isEmpty())trunks = trunks.substring(0, trunks.length()-2);
//</editor-fold>
        String leaveses = "";//<editor-fold defaultstate="collapsed">
        for(Material m : leaves){
            leaveses+=m+", ";
        }
        if(!leaveses.isEmpty())leaveses = leaveses.substring(0, leaveses.length()-2);
//</editor-fold>
        logger.log(Level.INFO, "Loaded Tree!");
        logger.log(Level.INFO, "- Trunk: {0}", trunks);
        logger.log(Level.INFO, "- Leaves: {0}", leaveses);
        for(Option option : Option.options){
            Object value = option.getValue(this);
            if(value!=null){
                logger.log(Level.INFO, "- {0}: {1}", new Object[]{option.name, option.makeReadable(value)});
            }
        }
    }
    public Material getDisplayMaterial(){
        if(trunk.isEmpty()){
            if(leaves.isEmpty()){
                return Material.BEDROCK;
            }
            return leaves.get(0);
        }
        return trunk.get(0);
    }
    public String writeToConfig(){
        String str = "["+trunk.toString()+", "+leaves.toString();
        String options = "";
        for(Option o : Option.options){
            if(o.getValue(this)!=null)options+=", "+o.getGlobalName()+": "+o.writeToConfig(this);
        }
        if(!options.isEmpty())str+=", {"+options.substring(2)+"}";
        return str+"]";
    }
}