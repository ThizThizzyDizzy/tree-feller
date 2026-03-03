package com.thizthizzydizzy.treefeller.core.event.player;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import com.thizthizzydizzy.treefeller.core.connector.world.IWorldConnector;
import com.thizthizzydizzy.treefeller.core.event.TreeFellerEvent;
public class PlayerBreakBlockEvent implements TreeFellerEvent{
    public final IPlayerConnector player;
    public final IWorldConnector world;
    public final long pos;
    public PlayerBreakBlockEvent(IPlayerConnector player, IWorldConnector world, long pos){
        this.player = player;
        this.world = world;
        this.pos = pos;
    }
}
