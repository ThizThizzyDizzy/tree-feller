package com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_13_2;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.BukkitNbtConnector;
import java.io.*;
import net.minecraft.server.v1_13_R2.*;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
/** Direct native integration for 1_13_2; all matching and merging is in Core. */
public final class BukkitNbtConnectorV1_13_2 implements BukkitNbtConnector{
    private static byte[] bytes(NBTTagCompound tag){
        try{
            ByteArrayOutputStream bytes=new ByteArrayOutputStream();
            NBTCompressedStreamTools.a(tag,(DataOutput)new DataOutputStream(bytes));return bytes.toByteArray();
        }catch(Exception ex){throw new IllegalArgumentException("Cannot serialize NBT",ex);}
    }
    private static NBTTagCompound tag(byte[] bytes){
        try{return NBTCompressedStreamTools.a(new DataInputStream(new ByteArrayInputStream(bytes)));}
        catch(Exception ex){throw new IllegalArgumentException("Cannot deserialize NBT",ex);}
    }
    @Override public byte[] parse(String snbt){
        try{return bytes(MojangsonParser.parse(snbt));}
        catch(Exception ex){throw new IllegalArgumentException("Invalid SNBT compound: "+snbt,ex);}
    }
    @Override public byte[] block(Block block){
        net.minecraft.server.v1_13_R2.TileEntity tile=tile(block);NBTTagCompound data=new NBTTagCompound();
        if(tile!=null)tile.save(data);return bytes(data);
    }
    private static net.minecraft.server.v1_13_R2.TileEntity tile(Block block){
        return ((CraftWorld)block.getWorld()).getHandle().getTileEntity(new net.minecraft.server.v1_13_R2.BlockPosition(block.getX(),block.getY(),block.getZ()));
    }
    @Override public void block(Block block,byte[] data){
        net.minecraft.server.v1_13_R2.TileEntity tile=tile(block);
        if(tile==null)throw new IllegalArgumentException("Block has no block entity: "+block.getType());
        tile.load(tag(data));tile.update();block.getState().update(true,false);
    }
    @Override public String itemDataKey(){return "tag";}
    @Override public byte[] item(ItemStack item){
        return bytes(CraftItemStack.asNMSCopy(item).save(new NBTTagCompound()));
    }
    @Override public ItemStack item(ItemStack item,byte[] data){
        return CraftItemStack.asBukkitCopy(net.minecraft.server.v1_13_R2.ItemStack.a(tag(data)));
    }
}
