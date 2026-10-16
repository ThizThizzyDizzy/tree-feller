package com.thizthizzydizzy.treefeller.platform.bukkit.connector.version.v26_r1;
import com.thizthizzydizzy.treefeller.platform.bukkit.connector.BukkitNbtConnector;
import java.io.*;
import net.minecraft.nbt.*;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.CraftRegistry;
/** Direct native integration for 26_R1; all matching and merging is in Core. */
public final class BukkitNbtConnectorV26_R1 implements BukkitNbtConnector{
    private static byte[] bytes(CompoundTag tag){
        try{
            ByteArrayOutputStream bytes=new ByteArrayOutputStream();
            NbtIo.write(tag,(DataOutput)new DataOutputStream(bytes));return bytes.toByteArray();
        }catch(Exception ex){throw new IllegalArgumentException("Cannot serialize NBT",ex);}
    }
    private static CompoundTag tag(byte[] bytes){
        try{return NbtIo.read((DataInput)new DataInputStream(new ByteArrayInputStream(bytes)));}
        catch(Exception ex){throw new IllegalArgumentException("Cannot deserialize NBT",ex);}
    }
    @Override public byte[] parse(String snbt){
        try{return bytes(TagParser.parseCompoundFully(snbt));}
        catch(Exception ex){throw new IllegalArgumentException("Invalid SNBT compound: "+snbt,ex);}
    }
    @Override public byte[] block(Block block){
        org.bukkit.block.BlockState state=block.getState();
        return bytes(state instanceof CraftBlockEntityState?((CraftBlockEntityState<?>)state).getSnapshotNBT():new CompoundTag());
    }
    @Override public void block(Block block,byte[] data){
        org.bukkit.block.BlockState state=block.getState();
        if(!(state instanceof CraftBlockEntityState))throw new IllegalArgumentException("Block has no block entity: "+block.getType());
        ((CraftBlockEntityState<?>)state).loadData(tag(data));state.update(true,false);
    }
    @Override public String itemDataKey(){return "components";}
    @Override public byte[] item(ItemStack item){
        return bytes((CompoundTag)net.minecraft.world.item.ItemStack.CODEC.encodeStart(net.minecraft.resources.RegistryOps.create(net.minecraft.nbt.NbtOps.INSTANCE,CraftRegistry.getMinecraftRegistry()),CraftItemStack.asNMSCopy(item)).result().orElseThrow(() -> new IllegalArgumentException("Cannot serialize item NBT")));
    }
    @Override public ItemStack item(ItemStack item,byte[] data){
        return CraftItemStack.asBukkitCopy(net.minecraft.world.item.ItemStack.CODEC.parse(net.minecraft.resources.RegistryOps.create(net.minecraft.nbt.NbtOps.INSTANCE,CraftRegistry.getMinecraftRegistry()),tag(data)).result().orElseThrow(() -> new IllegalArgumentException("Invalid item NBT/components")));
    }
}
