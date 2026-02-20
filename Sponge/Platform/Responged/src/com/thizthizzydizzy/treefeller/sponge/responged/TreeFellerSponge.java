package com.thizthizzydizzy.treefeller.sponge.responged;
import com.google.inject.Inject;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import java.nio.file.Path;
import org.spongepowered.api.Server;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.plugin.builtin.jvm.Plugin;
@Plugin(TreeFellerSponge.PLUGIN_ID)
public class TreeFellerSponge{
    public static final String PLUGIN_ID = "treefeller";
    public SpongeConnector connector;
    @Inject
    @DefaultConfig(sharedRoot = true)
    public Path configPath;
    @Listener
    public void onServerStart(final StartedEngineEvent<Server> event){
        TreeFellerCore.initialize(connector = new SpongeConnector(this));
    }
}
