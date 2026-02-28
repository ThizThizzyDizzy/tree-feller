package com.thizthizzydizzy.treefeller.core.player;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.config.structure.section.TriggerConfiguration;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import java.util.HashMap;
public class PlayerSettings{
    private static final HashMap<IPlayerConnector, PlayerSettings> playerSettings = new HashMap<>();
    public static PlayerSettings get(IPlayerConnector player){
        return playerSettings.computeIfAbsent(player, PlayerSettings::new);
    }
    private final IPlayerConnector player;
    private boolean toggleState;
    private HashMap<TriggerConfiguration, Long> cooldowns = new HashMap<>();
    private PlayerSettings(IPlayerConnector player){
        this.player = player;
        toggleState = TreeFellerCore.config.global.trigger.default_enabled;
    }
    public boolean isToggledOn(){
        return toggleState;
    }
    public boolean isOnCooldown(TriggerConfiguration config){
        Long cooldown = cooldowns.get(config);
        if(cooldown==null)return false;
        return System.currentTimeMillis()-cooldown>config.cooldown;
    }
}
