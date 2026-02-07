package com.thizthizzydizzy.treefeller.sponge;
import com.google.inject.Inject;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import org.spongepowered.api.Server;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.plugin.builtin.jvm.Plugin;
@Plugin("treefeller")
public class TreeFellerSponge{
    public SpongeConnector connector;
    @Inject
    public TreeFellerSponge(){
    }
    @Listener
    public void onServerStart(final StartedEngineEvent<Server> event){
        TreeFellerCore.initialize(connector = new SpongeConnector());
    }
}
