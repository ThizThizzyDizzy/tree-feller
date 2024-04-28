package com.thizthizzydizzy.treefeller;
import com.thizthizzydizzy.treefeller.compat.TreeFellerCompat;
import java.util.ArrayList;
import java.util.HashSet;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
public class Sapling{
    public final boolean spawn;
    private final TreeFeller treefeller;
    public final DetectedTree detectedTree;
    private final Player player;
    public final Block block;
    private final HashSet<Material> materials;
    private final int timeout;
    private int timer;
    private boolean placed = false;
    public Sapling(TreeFeller treefeller, DetectedTree detectedTree, Player player, Block block, HashSet<Material> materials, boolean spawn, int timeout){
        this.treefeller = treefeller;
        this.detectedTree = detectedTree;
        this.player = player;
        this.block = block;
        this.materials = materials;
        this.spawn = spawn;
        this.timeout = timeout;
    }
    public boolean isDead(){
        if(materials.contains(block.getType()))placed = true;
        return placed||timer>timeout;
    }
    public void tick(){
        timer++;
        if(player!=null){
            PlayerInventory inv = player.getInventory();
            ItemStack[] contents = inv.getContents();
            for(int i = 0; i<contents.length; i++){
                ItemStack content = contents[i];
                if(tryPlace(content))break;
            }
        }
        if(timer==timeout){
            if(spawn)place(null);
        }
    }
    public boolean canPlace(){
        if(isDead())return false;
        return block.getType()==Material.AIR;
    }
    public boolean place(Material material){
        if(!canPlace())return false;
        placed = true;
        if(material==null)material = new ArrayList<>(materials).get(0);
        BlockState was = block.getState();
        block.setType(material);
        TreeFellerCompat.addBlock(treefeller, player, block, was);
        return true;
    }
    public boolean tryPlace(ItemStack stack){
        if(stack==null)return false;
        if(materials.contains(stack.getType())){
            if(place(stack.getType())){
                stack.setAmount(stack.getAmount()-1);
                return true;
            }
        }
        return false;
    }
}