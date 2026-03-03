package com.thizthizzydizzy.treefeller.test;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
import java.util.Random;
public class BlockPosLongTester{
    public static void main(String[] args){
        Random rand = new Random();
        for(int i = 0; i<4096; i++){
            int x = rand.nextInt(67108864)-33554432;
            int y = rand.nextInt(4096)-2048;
            int z = rand.nextInt(67108864)-33554432;
            long l = BlockPos.toPos(x, y, z);
            int x2 = BlockPos.getX(l);
            if(x2!=x)throw new AssertionError("X does not match! "+x+"!="+x2);
            int y2 = BlockPos.getY(l);
            if(y2!=y)throw new AssertionError("Y does not match! "+y+"!="+y2);
            int z2 = BlockPos.getZ(l);
            if(z2!=z)throw new AssertionError("Z does not match! "+z+"!="+z2);
            System.out.println("Matches: "+x+" "+y+" "+z+" = "+l);
        }
    }
}
