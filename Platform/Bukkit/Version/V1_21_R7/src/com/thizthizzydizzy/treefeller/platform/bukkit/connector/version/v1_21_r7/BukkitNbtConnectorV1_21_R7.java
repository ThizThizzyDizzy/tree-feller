package com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v1_21_r7;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.BukkitNbtConnector;
import java.io.*;
import net.minecraft.nbt.*;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.craftbukkit.v1_21_R7.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R7.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_21_R7.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_21_R7.CraftRegistry;
/** Direct native integration for 1_21_R7; all matching and merging is in Core. */
public final class BukkitNbtConnectorV1_21_R7 implements BukkitNbtConnector{
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
        org.bukkit.block.BlockState state=block.getState();
        return bytes(state instanceof CraftBlockEntityState?((CraftBlockEntityState<?>)state).getSnapshotNBT():new NBTTagCompound());
    }
    @Override public void block(Block block,byte[] data){
        org.bukkit.block.BlockState state=block.getState();
        if(!(state instanceof CraftBlockEntityState))throw new IllegalArgumentException("Block has no block entity: "+block.getType());
        ((CraftBlockEntityState<?>)state).loadData(tag(data));state.update(true,false);
    }
    @Override public String itemDataKey(){return "components";}
    @Override public byte[] item(ItemStack item){
        return bytes((NBTTagCompound)net.minecraft.world.item.ItemStack.b.encodeStart(net.minecraft.resources.RegistryOps.a(net.minecraft.nbt.DynamicOpsNBT.a,CraftRegistry.getMinecraftRegistry()),CraftItemStack.asNMSCopy(item)).result().orElseThrow(() -> new IllegalArgumentException("Cannot serialize item NBT")));
    }
    @Override public ItemStack item(ItemStack item,byte[] data){
        return CraftItemStack.asBukkitCopy(net.minecraft.world.item.ItemStack.b.parse(net.minecraft.resources.RegistryOps.a(net.minecraft.nbt.DynamicOpsNBT.a,CraftRegistry.getMinecraftRegistry()),tag(data)).result().orElseThrow(() -> new IllegalArgumentException("Invalid item NBT/components")));
    }
}
