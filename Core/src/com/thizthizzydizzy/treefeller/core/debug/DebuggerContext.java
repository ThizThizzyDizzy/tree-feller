package com.thizthizzydizzy.treefeller.core.debug;
import com.thizthizzydizzy.treefeller.core.config.structure.ToolConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeConfiguration;
import com.thizthizzydizzy.treefeller.core.connector.item.IItemConnector;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import com.thizthizzydizzy.treefeller.core.connector.player.PlayerGameMode;
import com.thizthizzydizzy.treefeller.core.connector.world.BlockPos;
import com.thizthizzydizzy.treefeller.core.connector.world.IWorldConnector;
import java.util.ArrayList;
import java.util.Objects;
public class DebuggerContext{
    private IPlayerConnector player;
    public void info(Object... objects){
        // collect all logs before printing, to allow the player (or log context) to not be the very first log item
        ArrayList<String> logs = new ArrayList();
        for(Object object : objects){
            if(object instanceof String){
                String str = (String)object;
                logs.add(str);
                continue;
            }
            
            if(object instanceof Long){
                long l = (long)object;
                int x = BlockPos.getX(l);
                int y = BlockPos.getY(l);
                int z = BlockPos.getZ(l);
                logs.add("Position: "+x+" "+y+" "+z);
                continue;
            }
            if(object instanceof PlayerGameMode){
                PlayerGameMode gameMode = (PlayerGameMode)object;
                logs.add("Game Mode: "+gameMode.toString());
                continue;
            }
            
            if(object instanceof IPlayerConnector){
                IPlayerConnector player = (IPlayerConnector)object;
                this.player = player;
                logs.add("Player: "+player.getPlayerName());
                continue;
            }
            if(object instanceof IWorldConnector){
                IWorldConnector world = (IWorldConnector)object;
                logs.add("World: "+world.getDimension());
                continue;
            }
            if(object instanceof IItemConnector){
                IItemConnector item = (IItemConnector)object;
                logs.add("Item: "+item.getItemId());
                continue;
            }
            
            if(object instanceof ToolConfiguration){
                ToolConfiguration tool = (ToolConfiguration)object;
                logs.add("Tool: "+Objects.toString(tool.item.asSimplified()));
                continue;
            }
            if(object instanceof TreeConfiguration){
                TreeConfiguration tree = (TreeConfiguration)object;
                String[] trunks = new String[tree.trunk.length];
                for(int i = 0; i<trunks.length; i++)trunks[i] = Objects.toString(tree.trunk[i].asSimplified());
                String[] leaves = new String[tree.leaves.length];
                for(int i = 0; i<leaves.length; i++)leaves[i] = Objects.toString(tree.leaves[i].asSimplified());
                logs.add("Tree: "+String.join(", ", trunks)+" | "+String.join(", ", leaves));
                continue;
            }
            
            String unknown = object.getClass().getName()+": "+object.toString();
            logs.add(unknown);
        }
        for(String line : logs){
            print("INFO", line);
        }
    }
    private void pass(String line){
        print("PASS", line);
    }
    private void fail(String line){
        print("FAIL", line);
    }
    private void print(String type, String line){
        line = "["+type+"] "+line;
        if(player==null){
            System.out.println(line);
            return;
        }
        player.sendMessage(line);
    }
    public boolean checkTrue(String label, boolean value){
        if(value){
            pass(label+" = "+value);
        }else{
            fail(label+" = "+value);
        }
        return value;
    }
    public boolean checkFalse(String label, boolean value){
        return !checkTrue(label, !value);
    }
}
