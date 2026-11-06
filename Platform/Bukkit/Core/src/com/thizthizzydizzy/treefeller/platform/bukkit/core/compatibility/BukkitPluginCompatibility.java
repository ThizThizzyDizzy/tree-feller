package com.thizthizzydizzy.treefeller.platform.bukkit.core.compatibility;
import com.thizthizzydizzy.treefeller.core.compatibility.*;
import com.thizthizzydizzy.treefeller.core.connector.world.*;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.connector.*;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.TreeFellerBukkit;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
/** Translates the native API boundary; Core controls selection and dispatch. */
public abstract class BukkitPluginCompatibility implements PluginCompatibility{
    protected final TreeFellerBukkit plugin;
    protected BukkitPluginCompatibility(TreeFellerBukkit plugin){this.plugin=plugin;}
    public abstract String getPluginName();
    public String getPluginPackageFilter(){return null;}
    public String getCompatibilityName(){return getPluginName();}
    public String getFriendlyName(){return getCompatibilityName();}
    public Plugin getPlugin(){return Bukkit.getPluginManager().getPlugin(getPluginName());}
    protected String requiredClass(){return null;}
    protected String excludedClass(){return null;}
    private boolean hasClass(Plugin target,String name){
        try{Class.forName(name,false,target.getClass().getClassLoader());return true;}
        catch(ClassNotFoundException|LinkageError ex){return false;}
    }
    @Override public String id(){return getCompatibilityName().toLowerCase(java.util.Locale.ROOT).replace(' ','-');}
    @Override public boolean installed(){
        Plugin target=getPlugin();
        return target!=null&&target.isEnabled()&&(requiredClass()==null||hasClass(target,requiredClass()))&&(excludedClass()==null||!hasClass(target,excludedClass()))&&(getPluginPackageFilter()==null||getPluginPackageFilter().equals(target.getClass().getPackage().getName()));
    }
    protected Block block(IWorldConnector world,long pos){return ((BukkitWorldConnector)world).getWorld().getBlockAt(BlockPos.getX(pos),BlockPos.getY(pos),BlockPos.getZ(pos));}
    protected Player player(CompatibilityContext context){return context.player==null?null:((BukkitPlayerConnector)context.player).getPlayer();}
    protected ItemStack tool(CompatibilityContext context){return ((BukkitItemConnector)context.item).getStack();}
    @Override public boolean canBreak(CompatibilityContext context,long pos){return test(player(context),block(context.world,pos));}
    protected boolean test(Player player,Block block){return true;}
    @Override public double breakBlock(CompatibilityContext context,long pos){return breakBlock(context,player(context),tool(context),block(context.world,pos));}
    protected double breakBlock(CompatibilityContext context,Player player,ItemStack axe,Block block){return 1;}
    @Override public void removeBlock(CompatibilityContext context,long pos){removeBlock(player(context),block(context.world,pos));}
    protected void removeBlock(Player player,Block block){}
    @Override public Object capture(IWorldConnector world,long pos){return block(world,pos).getState();}
    @Override public void addBlock(CompatibilityContext context,long pos,Object previous){addBlock(player(context),block(context.world,pos),(BlockState)previous);}
    protected void addBlock(Player player,Block block,BlockState previous){}
    @Override public void dropItem(IPlayerConnector player,Object item){dropItem(player==null?null:((BukkitPlayerConnector)player).getPlayer(),(Item)item);}
    protected void dropItem(Player player,Item item){}
}
