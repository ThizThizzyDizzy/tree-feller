package com.thizthizzydizzy.treefeller.core.test;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockAxis;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
/** Fixed representative specimens, independent of Minecraft's random world generation. */
public final class TestTrees{
    private TestTrees(){}
    public enum Kind{
        OAK("oak", 5, true), BIRCH("birch", 6, true), SPRUCE("spruce", 8, true),
        JUNGLE("jungle", 7, true), GIANT_SPRUCE("spruce", 16, true), GIANT_JUNGLE("jungle", 16, true),
        ACACIA("acacia", 6, false), DARK_OAK("dark_oak", 7, false),
        MANGROVE("mangrove", 7, false), CHERRY("cherry", 6, false), PALE_OAK("pale_oak", 7, false),
        RED_POPLAR("poplar", 9, true), ORANGE_POPLAR("poplar", 9, true), YELLOW_POPLAR("poplar", 9, true),
        AZALEA("oak", 5, false), CRIMSON("crimson", 6, true), WARPED("warped", 6, true);
        public final String wood;
        public final int height;
        public final boolean variableHeight;
        Kind(String wood, int height, boolean variableHeight){
            this.wood = wood;
            this.height = height;
            this.variableHeight = variableHeight;
        }
    }
    public static final class Block{
        public final String material;
        public final BlockAxis axis;
        public final int leafDistance;
        private Block(String material, BlockAxis axis, int leafDistance){
            this.material = material;
            this.axis = axis;
            this.leafDistance = leafDistance;
        }
    }
    public static final class Tree{
        public final Kind kind;
        /** Coordinates relative to the bottom of the trunk. Logs override overlapping foliage. */
        public final Map<Long, Block> blocks;
        private Tree(Kind kind, Map<Long, Block> blocks){
            this.kind = kind;
            this.blocks = Collections.unmodifiableMap(blocks);
        }
    }
    public static Tree specimen(Kind kind){
        return specimen(kind, kind.height);
    }
    /** Height is the exact number of trunk blocks, before any branch/canopy decoration. */
    public static Tree specimen(Kind kind, int height){
        if(!kind.variableHeight&&height!=kind.height)
            throw new IllegalArgumentException(kind+" has a fixed specimen height of "+kind.height);
        if(height<4||height>32)throw new IllegalArgumentException("Trunk height must be 4..32");
        Builder b = new Builder(kind);
        boolean giant = kind==Kind.GIANT_SPRUCE||kind==Kind.GIANT_JUNGLE;
        boolean wide = giant||kind==Kind.DARK_OAK||kind==Kind.PALE_OAK;
        if(kind.wood.equals("poplar")){
            for(int y = 2; y<=height; y++)b.disk(0, y, 0, y==height?0:y>=height-2?1:2);
        }else if(kind==Kind.SPRUCE||kind==Kind.GIANT_SPRUCE){
            for(int y = 2; y<=height; y++){
                int radius = y==height?0:Math.min(giant?4:2, 1+(height-y)/3);
                b.disk(0, y, 0, radius);
            }
        }else if(kind==Kind.ACACIA){
            // Familiar bent trunk and two flat crowns, with no random branches.
            b.disk(2, height-1, 0, 3);
            b.disk(2, height, 0, 2);
            b.disk(-2, height-2, 0, 2);
            b.disk(-2, height-1, 0, 1);
            b.log(1, height-3, 0, BlockAxis.X);
            b.log(2, height-2, 0, BlockAxis.Y);
            b.log(2, height-1, 0, BlockAxis.Y);
            b.log(-1, height-3, 0, BlockAxis.X);
            b.log(-2, height-2, 0, BlockAxis.Y);
        }else if(kind==Kind.CHERRY){
            for(int x : new int[]{-2, 2}){
                b.disk(x, height-1, 0, 3);
                b.disk(x, height, 0, 2);
                b.log(x/2, height-3, 0, BlockAxis.X);
                b.log(x, height-2, 0, BlockAxis.Y);
                b.log(x, height-1, 0, BlockAxis.Y);
            }
        }else if(kind==Kind.CRIMSON||kind==Kind.WARPED){
            for(int y = height-2; y<=height; y++)b.disk(0, y, 0, y==height?2:3);
            b.put(2, height-2, 0, "shroomlight", null);
            b.put(-2, height-2, 0, "shroomlight", null);
        }else{
            int radius = giant?4:wide||kind==Kind.MANGROVE?3:2;
            b.disk(0, height-3, 0, radius);
            b.disk(0, height-2, 0, radius);
            b.disk(0, height-1, 0, radius-1);
            b.disk(0, height, 0, 1);
            if(giant){
                for(int y = 6; y<height-3; y+=4){
                    b.disk(3, y+1, 0, 2);
                    for(int x = 1; x<=3; x++)b.log(x, y, 0, BlockAxis.X);
                }
            }
        }
        int start = kind==Kind.MANGROVE?2:0;
        for(int y = start; y<height; y++){
            int trunkX = kind==Kind.ACACIA&&y>=height-2?2:0;
            b.log(trunkX, y, 0, BlockAxis.Y);
            if(wide){
                b.log(1, y, 0, BlockAxis.Y);
                b.log(0, y, 1, BlockAxis.Y);
                b.log(1, y, 1, BlockAxis.Y);
            }
        }
        if(kind==Kind.MANGROVE){
            for(int d = -2; d<=2; d++){
                if(d==0)continue;
                int y = 2-Math.abs(d);
                b.put(d, y, 0, "mangrove_roots", null);
                b.put(0, y, d, "mangrove_roots", null);
            }
            b.put(0, 1, 0, "mangrove_roots", null);
        }
        return b.finish();
    }
    private static final class Builder{
        final Kind kind;
        final Map<Long, Block> blocks = new LinkedHashMap<>();
        Builder(Kind kind){this.kind = kind;}
        void put(int x, int y, int z, String material, BlockAxis axis){
            blocks.put(BlockPos.toPos(x, y, z), new Block(material, axis, -1));
        }
        void log(int x, int y, int z, BlockAxis axis){
            put(x, y, z, kind.wood+(kind==Kind.CRIMSON||kind==Kind.WARPED?"_stem":"_log"), axis);
        }
        void disk(int cx, int y, int cz, int radius){
            String leaves = kind.wood.equals("poplar")?kind.name().toLowerCase(java.util.Locale.ROOT)+"_leaves":kind==Kind.AZALEA?"azalea_leaves":kind==Kind.CRIMSON?"nether_wart_block":
                    kind==Kind.WARPED?"warped_wart_block":kind.wood+"_leaves";
            for(int x = -radius; x<=radius; x++)for(int z = -radius; z<=radius; z++){
                if(radius>0&&Math.abs(x)==radius&&Math.abs(z)==radius)continue;
                long pos = BlockPos.toPos(cx+x, y, cz+z);
                // Branches already placed by another crown must survive overlapping foliage.
                if(!blocks.containsKey(pos))put(cx+x, y, cz+z, leaves, null);
            }
        }
        Tree finish(){
            // Vanilla leaf distance is six-neighbor distance, capped at seven.
            Map<Long, Integer> distances = new LinkedHashMap<>();
            for(Map.Entry<Long, Block> entry : blocks.entrySet())
                if(entry.getValue().axis!=null)distances.put(entry.getKey(), 0);
            int[][] neighbors = {{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}};
            for(int distance = 1; distance<=6; distance++){
                Map<Long, Integer> next = new LinkedHashMap<>();
                for(Map.Entry<Long, Integer> entry : distances.entrySet()){
                    if(entry.getValue()!=distance-1)continue;
                    for(int[] n : neighbors){
                        long pos = BlockPos.getRelative(entry.getKey(), n[0], n[1], n[2]);
                        Block block = blocks.get(pos);
                        if(block!=null&&block.material.endsWith("_leaves")&&!distances.containsKey(pos))next.put(pos, distance);
                    }
                }
                distances.putAll(next);
            }
            for(Map.Entry<Long, Block> entry : blocks.entrySet()){
                Block block = entry.getValue();
                if(block.material.endsWith("_leaves"))entry.setValue(new Block(block.material, null, distances.getOrDefault(entry.getKey(), 7)));
            }
            return new Tree(kind, new LinkedHashMap<>(blocks));
        }
    }
}
