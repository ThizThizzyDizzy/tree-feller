package com.thizthizzydizzy.treefeller.test;
import com.thizthizzydizzy.treefeller.core.BlockPos;
import java.util.Random;
public class BlockPosLongTester{
    public static void main(String[] args){
        Random rand = new Random();
        for(int i = 0; i<4096; i++){
            int x = rand.nextInt(67108864)-33554432;
            int y = rand.nextInt(4096)-2048;
            int z = rand.nextInt(67108864)-33554432;
            BlockPos pos = new BlockPos(x, y, z);
            long l = pos.asLong();
            pos = BlockPos.fromLong(l);
            if(pos.x!=x)
                throw new AssertionError("X does not match! "+x+"!="+pos.x);
            if(pos.y!=y)
                throw new AssertionError("Y does not match! "+y+"!="+pos.y);
            if(pos.z!=z)
                throw new AssertionError("Z does not match! "+z+"!="+pos.z);
            System.out.println("Matches: "+x+" "+y+" "+z+" = "+l);
        }
    }
}
