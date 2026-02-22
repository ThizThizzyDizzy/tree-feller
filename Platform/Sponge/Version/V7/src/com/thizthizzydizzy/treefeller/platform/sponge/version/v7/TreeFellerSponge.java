package com.thizthizzydizzy.treefeller.platform.sponge.version.v7;
import com.google.inject.Inject;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import java.nio.file.Path;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
@Plugin(id = TreeFellerSponge.PLUGIN_ID)
public class TreeFellerSponge{
    public static final String PLUGIN_ID = "treefeller";
    public SpongeConnector connector;
    @Inject
    @DefaultConfig(sharedRoot = true)
    public Path configPath;
    @Listener
    public void onServerStart(GameStartedServerEvent event){
        TreeFellerCore.initialize(connector = new SpongeConnector(this));
    }
}
