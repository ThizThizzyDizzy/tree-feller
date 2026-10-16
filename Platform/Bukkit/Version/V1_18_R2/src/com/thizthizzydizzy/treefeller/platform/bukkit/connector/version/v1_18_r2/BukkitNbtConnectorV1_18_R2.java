package com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_18_r2;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.BukkitNbtConnector;
import java.io.*;
import net.minecraft.nbt.*;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
/** Direct native integration for 1_18_R2; all matching and merging is in Core. */
public final class BukkitNbtConnectorV1_18_R2 implements BukkitNbtConnector{
    private static byte[] bytes(NBTTagCompound tag){
        try{
            ByteArrayOutputStream bytes=new ByteArrayOutputStream();
            NBTCompressedStreamTools.a(tag,(DataOutput)new DataOutputStream(bytes));return bytes.toByteArray();
        }catch(Exception ex){throw new IllegalArgumentException("Cannot serialize NBT",ex);}
    }
    private static NBTTagCompound tag(byte[] bytes){
        try{return NBTCompressedStreamTools.a((DataInput)new DataInputStream(new ByteArrayInputStream(bytes)));}
        catch(Exception ex){throw new IllegalArgumentException("Cannot deserialize NBT",ex);}
    }
    @Override public byte[] parse(String snbt){
        try{return bytes(MojangsonParser.a(snbt));}
        catch(Exception ex){throw new IllegalArgumentException("Invalid SNBT compound: "+snbt,ex);}
    }
    @Override public byte[] block(Block block){
        net.minecraft.world.level.block.entity.TileEntity tile=tile(block);return bytes(tile==null?new NBTTagCompound():tile.m());
    }
    private static net.minecraft.world.level.block.entity.TileEntity tile(Block block){
        return ((CraftWorld)block.getWorld()).getHandle().c_(new net.minecraft.core.BlockPosition(block.getX(),block.getY(),block.getZ()));
    }
    @Override public void block(Block block,byte[] data){
        net.minecraft.world.level.block.entity.TileEntity tile=tile(block);
        if(tile==null)throw new IllegalArgumentException("Block has no block entity: "+block.getType());
        tile.a(tag(data));tile.e();block.getState().update(true,false);
    }
    @Override public String itemDataKey(){return "tag";}
    @Override public byte[] item(ItemStack item){
        return bytes((NBTTagCompound)net.minecraft.world.item.ItemStack.a.encodeStart(net.minecraft.nbt.DynamicOpsNBT.a,CraftItemStack.asNMSCopy(item)).result().orElseThrow(() -> new IllegalArgumentException("Cannot serialize item NBT")));
    }
    @Override public ItemStack item(ItemStack item,byte[] data){
        return CraftItemStack.asBukkitCopy(net.minecraft.world.item.ItemStack.a.parse(net.minecraft.nbt.DynamicOpsNBT.a,tag(data)).result().orElseThrow(() -> new IllegalArgumentException("Invalid item NBT/components")));
    }
}
